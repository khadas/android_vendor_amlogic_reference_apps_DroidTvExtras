/*
 * Copyright (C) 2015 The Android Open Source Project
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

import androidx.preference.Preference;
import androidx.preference.TwoStatePreference;

import android.content.Intent;
import android.text.TextUtils;
import android.os.Bundle;

import com.droidlogic.app.tv.TvControlManager;
import com.droidlogic.tv.extras.SettingsPreferenceFragment;
import com.droidlogic.tv.extras.SettingsConstant;
import static com.droidlogic.tv.extras.util.DroidUtils.logDebug;
import com.droidlogic.tv.extras.R;

public class MainFragment extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener{
    private static final String TAG = "MainFragment";

    private static final String KEY_DLG = "device_dlg";
    private static final String KEY_PICTURE = "picture_mode";
    private static final String KEY_TV_CHANNEL = "channel";
    private static final String KEY_POWER_AND_ENERGY = "power_and_energy_settings";

    private TvControlManager mTvControlManager;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.main_prefs, null);
        mTvControlManager = TvControlManager.getInstance();

        final Preference channelPref = findPreference(KEY_TV_CHANNEL);
        final Preference powerAndEnergyPref = findPreference(KEY_POWER_AND_ENERGY);

        channelPref.setVisible(false);
        if (SettingsConstant.isTvFeature()) {
            final TwoStatePreference deviceDlgPref = (TwoStatePreference) findPreference(KEY_DLG);
            deviceDlgPref.setOnPreferenceChangeListener(this);
            deviceDlgPref.setVisible(mTvControlManager.IsSupportDLG());
            int dlgState = mTvControlManager.GetDLGEnable();
            logDebug(TAG, false, "GetDLGEnable: " + dlgState);

            deviceDlgPref.setChecked(dlgState == 1 ? true : false);
        }
        if (!SettingsConstant.needGTVFeature(getContext())) {
            powerAndEnergyPref.setVisible(false);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        super.onPreferenceTreeClick(preference);
        if (TextUtils.equals(preference.getKey(), KEY_TV_CHANNEL)) {
            startUiInLiveTv(KEY_TV_CHANNEL);
        } else if (TextUtils.equals(preference.getKey(), KEY_POWER_AND_ENERGY)) {
            startPowerEnergyActivityAsSlices();
        }
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        // SetDLGEnable param:1 enable; 0 disable.
        if (TextUtils.equals(preference.getKey(), KEY_DLG)) {
            mTvControlManager.SetDLGEnable((boolean) newValue ? 1 : 0);
        }
        return true;
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

    private void startUiInLiveTv(String value) {
        Intent intent = new Intent();
        intent.setAction("action.startlivetv.settingui");
        intent.putExtra(value, true);
        getActivity().sendBroadcast(intent);
        getActivity().finish();
    }

    private void startPowerEnergyActivityAsSlices() {
        Intent intent = new Intent("android.settings.SLICE_SETTINGS");
        String sliceUri = "content://com.google.android.apps.tv.launcherx.sliceprovider/power_boot_resume";
        intent.putExtra("slice_uri", sliceUri);
        startActivity(intent);
    }
}