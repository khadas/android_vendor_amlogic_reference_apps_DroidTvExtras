/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.droidlogic.tv.extras;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.transition.Scene;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.droidlogic.tv.extras.overlay.FlavorUtils;
import com.droidlogic.tv.extras.tvoption.TvOptionSettingManager;
import com.droidlogic.app.DataProviderManager;

import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.settingslib.core.instrumentation.SharedPreferencesLogger;

public abstract class TvSettingsActivity extends FragmentActivity {
    private static final String TAG = "TvSettingsActivity";

    private static final String FRAGMENT_TAG =
            "com.droidlogic.tv.extras.MainActivity.SETTINGS_FRAGMENT";

    private static final int REQUEST_CODE_STARTUP_VERIFICATION = 1;

    public static final String INTENT_ACTION_FINISH_FRAGMENT = "action.finish.droidsettingsmodefragment";
    public static final int MODE_LAUNCHER = 0;
    public static final int MODE_LIVE_TV = 1;
    private int mStartMode = MODE_LAUNCHER;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((FlavorUtils.getFlavor(this) & getAvailableFlavors()) == 0) {
            Log.w(TAG, "Activity is not supported in current flavor");
            finish();
        }
        if (savedInstanceState == null) {

            final Fragment fragment = createSettingsFragment();
            if (fragment == null) {
                return;
            }
            if (isStartupVerificationRequired()) {
                return;
            }
            if (FlavorUtils.isTwoPanel(this)) {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.animator.fade_in,
                                android.R.animator.fade_out)
                        .add(android.R.id.content, fragment, FRAGMENT_TAG)
                        .commitNow();
                return;
            }

            final ViewGroup root = findViewById(android.R.id.content);
            root.getViewTreeObserver().addOnPreDrawListener(
                    new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            root.getViewTreeObserver().removeOnPreDrawListener(this);
                            final Scene scene = new Scene(root);
                            scene.setEnterAction(() -> {
                                if (getFragmentManager().isStateSaved()
                                        || getFragmentManager().isDestroyed()) {
                                    Log.d(TAG, "Got torn down before adding fragment");
                                    return;
                                }
                                getSupportFragmentManager().beginTransaction()
                                        .add(android.R.id.content, fragment,
                                                FRAGMENT_TAG)
                                        .commitNow();
                            });

                            final Slide slide = new Slide(Gravity.END);
                            slide.setSlideFraction(
                                    getResources().getDimension(R.dimen.lb_settings_pane_width)
                                            / root.getWidth());
                            TransitionManager.go(scene, slide);

                            // Skip the current draw, there's nothing in it
                            return false;
                        }
                    });
        }

        mStartMode = getIntent().getIntExtra("from_live_tv", MODE_LAUNCHER);
        Log.d(TAG, "mStartMode : " + mStartMode);
        if (SettingsConstant.needDroidlogicCustomization(this)) {
            if (mStartMode == MODE_LIVE_TV) {
                startShowActivityTimer();
            }
        }

    }

    public BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "intent = " + intent);
            switch (intent.getAction()) {
                case INTENT_ACTION_FINISH_FRAGMENT:
                case Intent.ACTION_CLOSE_SYSTEM_DIALOGS:
                    finish();
                    break;
            }
        }
    };
    public void registerSomeReceivers() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_ACTION_FINISH_FRAGMENT);
        intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
    }

    public void unregisterSomeReceivers() {
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onResume() {
        registerSomeReceivers();
        if (SettingsConstant.needDroidlogicCustomization(this)) {
            if (mStartMode == MODE_LIVE_TV) {
                startShowActivityTimer();
            }
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeMessages(0);
        Log.d(TAG, "onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterSomeReceivers();
        Log.d(TAG, "onDestroy");
    }

    public void finish() {
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (FlavorUtils.isTwoPanel(this)) {
            super.finish();
            return;
        }

        if (isResumed() && fragment != null) {
            final ViewGroup root = findViewById(android.R.id.content);
            final Scene scene = new Scene(root);
            scene.setEnterAction(() -> getSupportFragmentManager().beginTransaction()
                    .remove(fragment)
                    .commitNow());
            final Slide slide = new Slide(Gravity.END);
            slide.setSlideFraction(
                    getResources().getDimension(R.dimen.lb_settings_pane_width) / root.getWidth());
            slide.addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {
                    getWindow().setDimAmount(0);
                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    transition.removeListener(this);
                    TvSettingsActivity.super.finish();
                }

                @Override
                public void onTransitionCancel(Transition transition) {
                }

                @Override
                public void onTransitionPause(Transition transition) {
                }

                @Override
                public void onTransitionResume(Transition transition) {
                }
            });
            TransitionManager.go(scene, slide);
        } else {
            super.finish();
        }
    }

    protected abstract Fragment createSettingsFragment();

    /**
     * Subclass may override this to return true to indicate that the Activity may only be started
     * after some verification. Example: in special mode, we need to challenge the user with re-auth
     * before launching account settings.
     *
     * This only works in certain flavors as we do not have features requiring the startup
     * verification in classic flavor or ordinary two panel flavor.
     */
    protected boolean isStartupVerificationRequired() {
        return false;
    }

    /** Subclass may override this to specify the flavor, in which the activity is available. */
    protected int getAvailableFlavors() {
        return FlavorUtils.ALL_FLAVORS_MASK;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_STARTUP_VERIFICATION) {
            if (resultCode == RESULT_OK) {
                Log.v(TAG, "Startup verification succeeded.");
                if (FlavorUtils.getFlavor(this) == FlavorUtils.FLAVOR_X
                        || FlavorUtils.getFlavor(this) == FlavorUtils.FLAVOR_VENDOR) {
                    if (createSettingsFragment() == null) {
                        Log.e(TAG, "Fragment is null.");
                        finish();
                        return;
                    }
                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(
                                    android.R.animator.fade_in, android.R.animator.fade_out)
                            .add(
                                    android.R.id.content,
                                    createSettingsFragment(),
                                    FRAGMENT_TAG)
                            .commitNow();
                }
            } else {
                Log.v(TAG, "Startup verification cancelled or failed.");
                finish();
            }
        }
    }

    private String getMetricsTag() {
        String tag = getClass().getName();
        if (tag.startsWith("com.droidlogic.tv.extras.")) {
            tag = tag.replace("com.droidlogic.tv.extras.", "");
        }
        return tag;
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        if (name.equals(getPackageName() + "_preferences")) {
            return new SharedPreferencesLogger(this, getMetricsTag(),
                    new MetricsFeatureProvider());
        }
        return super.getSharedPreferences(name, mode);
    }

    @Override
    public boolean dispatchKeyEvent (KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_DOWN:
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_BACK:
                    if (mStartMode == MODE_LIVE_TV) {
                        Log.d(TAG, "dispatchKeyEvent");
                        startShowActivityTimer();
                    }
                    break;
                default:
                    break;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    public void startShowActivityTimer() {
        handler.removeMessages(0);

        int seconds = DataProviderManager.getIntValue(this, TvOptionSettingManager.KEY_MENU_TIME,
                TvOptionSettingManager.DEFAULT_MENU_TIME);
        if (seconds == 1) {
            seconds = 15;
        } else if (seconds == 2) {
            seconds = 30;
        } else if (seconds == 3) {
            seconds = 60;
        } else if (seconds == 4) {
            seconds = 120;
        } else if (seconds == 5) {
            seconds = 240;
        } else {
            seconds = 0;
        }
        if (seconds > 0) {
            handler.sendEmptyMessageDelayed(0, seconds * 1000);
        } else {
            handler.removeMessages(0);
        }
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Intent intent = new Intent();
            intent.setAction(INTENT_ACTION_FINISH_FRAGMENT);
            sendBroadcast(intent);
        }
    };
}
