/*
 * Copyright (c) 2014 Amlogic, Inc. All rights reserved.
 *
 * This source code is subject to the terms and conditions defined in the
 * file 'LICENSE' which is part of this source code package.
 *
 * Description:
 *     AMLOGIC TimerSuspendService
 */

package com.droidlogic.tv.extras.suspend;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import com.droidlogic.app.DataProviderManager;
import com.droidlogic.app.DroidLogicKeyEvent;
import com.droidlogic.app.tv.DroidLogicTvUtils;
import com.droidlogic.tv.extras.R;
import static com.droidlogic.tv.extras.util.DroidUtils.logDebug;

import java.lang.reflect.Method;

public class TimerSuspendService extends Service {
    private final String TAG = "TimerSuspendService";

    /* time suspend dialog */
    private AlertDialog mDialog;//use to dismiss
    private TextView mCountDownText;
    private int mSuspendCount = 0;
    private boolean mEnableNoSignalTimeout = false;
    private boolean mEnableSuspendTimeout = false;

    private Context mContext = null;
    public static final int INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH = 2;
    private static final int TIMEOUT_10MIN = 10 * 60;//10min
    private static final int TIMEOUT_1MIN = 1 * 60;//1min
    private static TimerSuspendService sInstance;
    private static final String SUSPEND_NOTIFICATION_CHANNEL_ID = "suspend_notification_channel";
    private static final String SUSPEND_PROMPT = "Standby countdown";

    protected final BroadcastReceiver mScreenBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    logDebug(TAG, false, "clear sleepTime because poweroff, screen_off");
                    cancelSleepTimer();
                    DataProviderManager.putIntValue(mContext, DroidLogicTvUtils.PROP_DROID_TV_SLEEP_TIME, 0);
                    hideDialog();
                    remove_shutdown_time();
                }
            }
        }
    };

    @Override
    public void onCreate() {
        logDebug(TAG, false, "onCreate");
        super.onCreate();
        this.mContext = this;
        initTimeSuspend();
        IntentFilter boot = new IntentFilter();
        boot.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenBroadcastReceiver, boot);
        createNotificationChannel();
    }

    @Override
    public IBinder onBind ( Intent intent ) {
        return null;
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        logDebug(TAG, true, "onStartCommand");
        if (intent != null)  {
            logDebug(TAG, false, "intent=" + intent);
            mEnableSuspendTimeout = intent.getBooleanExtra(DroidLogicTvUtils.KEY_ENABLE_SUSPEND_TIMEOUT, false);
            mEnableNoSignalTimeout = intent.getBooleanExtra(DroidLogicTvUtils.KEY_ENABLE_NOSIGNAL_TIMEOUT, false);
            int mode = intent.getIntExtra("mode", -1);
            if (mode >= 0) {
                setSleepTimer(mode);
            } else {
                startForeground();
            }
        }

        return super.onStartCommand ( intent, flags, startId );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mScreenBroadcastReceiver);
        remove_shutdown_time();
    }

    private void initTimeSuspend() {
        AlertDialog.Builder suspendDialog = new AlertDialog.Builder(this);
        View suspendDialogView = View.inflate(this, R.layout.time_suspend_dialog, null);
        Button mBn = (Button) suspendDialogView.findViewById(R.id.btn_cancel_suspend);
        mBn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //checkTimeoutStatus();
                hideDialog();
                logDebug(TAG, false, "onClick");
            }
        });
        mCountDownText = (TextView) suspendDialogView.findViewById(R.id.tv_dialog);
        suspendDialog.setView(suspendDialogView);
        mDialog = suspendDialog.create();
        mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                logDebug(TAG, false, "onDismiss mSuspendCount = " + mSuspendCount);
                if (mSuspendCount > 0) {
                    checkTimeoutStatus();
                }
            }
        });
    }

    private void checkTimeoutStatus() {
        if (!mEnableSuspendTimeout) {
            if (mEnableNoSignalTimeout) {
                reset_shutdown_time(TIMEOUT_10MIN);//10min
            }
        } else {
            DataProviderManager.putIntValue(mContext, DroidLogicTvUtils.PROP_DROID_TV_SLEEP_TIME, 0);
            stopSelf();
        }
    }

    private void getInjectInputEvent(InputEvent keyevent, int mode) {
        try {
            Class<?> cls = Class.forName("android.hardware.input.InputManager");
            Method constructor = cls.getMethod("getInstance");
            Method method = cls.getMethod("injectInputEvent", InputEvent.class, int.class);
            method.invoke(constructor.invoke(null), keyevent, mode);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private Handler timeSuspend_handler = new Handler();
    private Runnable timeSuspend_runnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (mSuspendCount == 0) {
                    pressPowerKey();
                    hideDialog();
                    //stopSelf();
                } else {
                    if (mSuspendCount == 60) {
                        String str = mSuspendCount + " " + getResources().getString(R.string.countdown_tips);
                        //initTimeSuspend();
                        mCountDownText.setText(str);
                        mDialog.show();
                    } else if (mSuspendCount < 60) {
                        String str = mSuspendCount + " " + getResources().getString(R.string.countdown_tips);
                        mCountDownText.setText(str);
                    }
                    logDebug(TAG, false, "mSuspendCount=" + mSuspendCount);
                    timeSuspend_handler.postDelayed(timeSuspend_runnable, 1000);
                }
                mSuspendCount--;
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    };

    private void reset_shutdown_time(int time) {
        logDebug(TAG, false, "reset_shutdown_time = " + time);
        mSuspendCount =  time;
        remove_shutdown_time();
        timeSuspend_handler.post(timeSuspend_runnable);
        hideDialog();
    }

    private void setSleepTimer (int mode) {
        AlarmManager alarm = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent("droidlogic.intent.action.TIMER_SUSPEND");
        intent.addFlags(0x01000000/*Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND*/);
        intent.putExtra(DroidLogicTvUtils.KEY_ENABLE_SUSPEND_TIMEOUT, true);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        alarm.cancel(pendingIntent);

        DataProviderManager.putIntValue(mContext, DroidLogicTvUtils.PROP_DROID_TV_SLEEP_TIME, mode);

        long timeout = 0;
        if (mode == 0) {
            return;
        } else if (mode < 5) {
            timeout = (mode * 15  - 1) * 60 * 1000;
        } else {
            timeout = ((mode - 4) * 30 + 4 * 15  - 1) * 60 * 1000;
        }

        alarm.setExact(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + timeout, pendingIntent);
        logDebug(TAG, false, "start time count down after " + timeout + " ms");
    }

    private void cancelSleepTimer() {
        logDebug(TAG, false, "canel TIMER_SUSPEND alarm");
        AlarmManager alarm = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent("droidlogic.intent.action.TIMER_SUSPEND");
        intent.addFlags(0x01000000/*Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND*/);
        intent.putExtra(DroidLogicTvUtils.KEY_ENABLE_SUSPEND_TIMEOUT, true);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        alarm.cancel(pendingIntent);
    }

    private void remove_shutdown_time() {
        timeSuspend_handler.removeCallbacksAndMessages(null);
    }

    private void hideDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    private void pressPowerKey () {
        if (!isSystemScreenOn()) {
            logDebug(TAG, true, "pressPowerKey screen is off already");
            return;
        }
        long now = SystemClock.uptimeMillis();
        KeyEvent down = new KeyEvent(now, now, KeyEvent.ACTION_DOWN, DroidLogicKeyEvent.KEYCODE_POWER, 0);
        KeyEvent up = new KeyEvent(now, now, KeyEvent.ACTION_UP, DroidLogicKeyEvent.KEYCODE_POWER, 0);
        getInjectInputEvent(down, INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
        getInjectInputEvent(up, INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
    }

    private boolean isSystemScreenOn() {
        PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOpen = powerManager.isScreenOn();
        logDebug(TAG, false, "isSystemScreenOn isScreenOpen = " + isScreenOpen);
        return isScreenOpen;
    }

    static void startForegroundService(Context context,Intent intent) {
        if (sInstance == null) {
            Intent intentService = new Intent(context, TimerSuspendService.class);
            intentService.putExtra(DroidLogicTvUtils.KEY_ENABLE_NOSIGNAL_TIMEOUT, intent.getBooleanExtra(DroidLogicTvUtils.KEY_ENABLE_NOSIGNAL_TIMEOUT, false));
            intentService.putExtra(DroidLogicTvUtils.KEY_ENABLE_SUSPEND_TIMEOUT, intent.getBooleanExtra(DroidLogicTvUtils.KEY_ENABLE_SUSPEND_TIMEOUT, false));
            context.startForegroundService(intentService);
        } else {
            sInstance.mEnableNoSignalTimeout = intent.getBooleanExtra(DroidLogicTvUtils.KEY_ENABLE_NOSIGNAL_TIMEOUT, false);
            sInstance.mEnableSuspendTimeout = intent.getBooleanExtra(DroidLogicTvUtils.KEY_ENABLE_SUSPEND_TIMEOUT, false);
            sInstance.startForeground();
        }
    }

    private void startForeground() {
        logDebug(TAG, true, "startForeground");
        Intent notificationIntent = new Intent(this, TimerSuspendService.class);
        Notification.Builder builder  = new Notification.Builder(this)
                .setContentTitle(SUSPEND_PROMPT)
                .setContentText(SUSPEND_PROMPT)
                .setSmallIcon(R.drawable.ic_settings_more);
        Notification notification = builder.setChannelId(SUSPEND_NOTIFICATION_CHANNEL_ID).build();

        startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED);

        //stop it if need cancel
        if (!mEnableSuspendTimeout) {
            if (!mEnableNoSignalTimeout) {
                //DataProviderManager.putIntValue(mContext, DroidLogicTvUtils.PROP_DROID_TV_SLEEP_TIME, 0);
                //stopSelf();
            } else {
                reset_shutdown_time(TIMEOUT_10MIN);//10min
            }
        } else {
            if (DataProviderManager.getBooleanValue(mContext, "is_channel_searching", false)) {
                logDebug(TAG, true, "Station search in progress, stop standby");
                stopSelf();
            } else {
                reset_shutdown_time(TIMEOUT_1MIN);//one min
            }
        }
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                SUSPEND_NOTIFICATION_CHANNEL_ID,
                SUSPEND_PROMPT,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }
}
