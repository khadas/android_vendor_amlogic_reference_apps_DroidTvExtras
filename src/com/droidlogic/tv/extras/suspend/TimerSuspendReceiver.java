/*
 * Copyright (c) 2014 Amlogic, Inc. All rights reserved.
 *
 * This source code is subject to the terms and conditions defined in the
 * file 'LICENSE' which is part of this source code package.
 *
 * Description:
 *     AMLOGIC TimerSuspendReceiver
 */

package com.droidlogic.tv.extras.suspend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.InputEvent;
import android.view.KeyEvent;
import com.droidlogic.app.DataProviderManager;
import com.droidlogic.app.DroidLogicKeyEvent;
import com.droidlogic.app.tv.DroidLogicTvUtils;
import static com.droidlogic.tv.extras.util.DroidUtils.logDebug;

import java.lang.reflect.Method;

public class TimerSuspendReceiver extends BroadcastReceiver {
    private static final String TAG = "TimerSuspendReceiver";
    private static final int INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH = 2;

    private Context mContext = null;

    @Override
    public void onReceive(Context context, Intent intent) {

        mContext = context;
        if (intent != null) {
            if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                // once new boot, clear sleep time
                logDebug(TAG, false, "Clear SleepTime");
                DataProviderManager.putIntValue(mContext, DroidLogicTvUtils.PROP_DROID_TV_SLEEP_TIME, 0);
                Intent intentService = new Intent(mContext, TimerSuspendService.class );
                mContext.startService(intentService);
                return;
            }
            //add for third party application that can't raise power key
            //action:droidlogic.intent.action.TIMER_SUSPEND key:command_suspend value:true
            if (intent.getBooleanExtra(DroidLogicTvUtils.KEY_COMMAND_REQUEST_SUSPEND, false)) {
                pressPowerKey();
            } else {
                if (intent.getBooleanExtra(DroidLogicTvUtils.KEY_ENABLE_SUSPEND_TIMEOUT, false)) {
                    DataProviderManager.putIntValue(mContext, DroidLogicTvUtils.PROP_DROID_TV_SLEEP_TIME, 0);//clear it as acted
                }
                startSleepTimer(intent);
            }
        }
    }

    public void startSleepTimer (Intent intent) {
        Intent intentService = new Intent(mContext, TimerSuspendService.class );
        intentService.putExtra(DroidLogicTvUtils.KEY_ENABLE_NOSIGNAL_TIMEOUT, intent.getBooleanExtra(DroidLogicTvUtils.KEY_ENABLE_NOSIGNAL_TIMEOUT, false));
        intentService.putExtra(DroidLogicTvUtils.KEY_ENABLE_SUSPEND_TIMEOUT, intent.getBooleanExtra(DroidLogicTvUtils.KEY_ENABLE_SUSPEND_TIMEOUT, false));
        mContext.startService (intentService);
    }

    private void pressPowerKey () {
        if (!isSystemScreenOn()) {
            logDebug(TAG, false, "pressPowerKey screen is off already");
            return;
        }
        long now = SystemClock.uptimeMillis();
        KeyEvent down = new KeyEvent(now, now, KeyEvent.ACTION_DOWN, DroidLogicKeyEvent.KEYCODE_POWER, 0);
        KeyEvent up = new KeyEvent(now, now, KeyEvent.ACTION_UP, DroidLogicKeyEvent.KEYCODE_POWER, 0);
        getInjectInputEvent(down, INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
        getInjectInputEvent(up, INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
    }

    private boolean isSystemScreenOn() {
        PowerManager powerManager = mContext.getSystemService(PowerManager.class);
        boolean isScreenOpen = powerManager.isScreenOn();
        logDebug(TAG, false, "isSystemScreenOn isScreenOpen = " + isScreenOpen);
        return isScreenOpen;
    }

    private void getInjectInputEvent(InputEvent keyevent, int mode) {
        try {
            Class<?> cls = Class.forName("android.hardware.input.InputManager");
            Method constructor = cls.getMethod("getInstance");
            Method method = cls.getMethod("injectInputEvent", InputEvent.class, int.class);
            method.invoke(constructor.invoke(null), keyevent, mode);
        } catch(Exception e) {
            logDebug(TAG, true, "getInjectInputEvent Exception = " + e.getMessage());
        }
    }
}
