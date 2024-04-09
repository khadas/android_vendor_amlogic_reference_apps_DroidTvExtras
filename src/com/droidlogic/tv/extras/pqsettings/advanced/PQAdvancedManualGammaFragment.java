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

package com.droidlogic.tv.extras.pqsettings.advanced;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.preference.Preference;
import androidx.preference.SeekBarPreference;
import androidx.preference.PreferenceCategory;
import androidx.preference.ListPreference;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.provider.Settings;

import com.droidlogic.tv.extras.SettingsPreferenceFragment;
import com.droidlogic.tv.extras.R;

import com.droidlogic.tv.extras.pqsettings.PQSettingsManager;
import static com.droidlogic.tv.extras.util.DroidUtils.logDebug;

public class PQAdvancedManualGammaFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    public static final String TAG = "PQAdvancedManualGammaFragment";

    private static final String PQ_PICTURE_ADVANCED_MANUAL_GAMMA_LEVEL = "pq_picture_advanced_manual_gamma_level";
    private static final String PQ_PICTURE_ADVANCED_MANUAL_GAMMA_RGAIN = "pq_picture_advanced_manual_gamma_rgain";
    private static final String PQ_PICTURE_ADVANCED_MANUAL_GAMMA_GGAIN = "pq_picture_advanced_manual_gamma_ggain";
    private static final String PQ_PICTURE_ADVANCED_MANUAL_GAMMA_BGAIN = "pq_picture_advanced_manual_gamma_bgain";
    private static final String PQ_PICTURE_ADVANCED_MANUAL_GAMMA_RESET = "pq_picture_advanced_manual_gamma_reset";
    private static SeekBarPreference PQPictureAdvancedManualGammaLevelPref;
    private static SeekBarPreference PQPictureAdvancedManualGammaRGainPref;
    private static SeekBarPreference PQPictureAdvancedManualGammaGGainPref;
    private static SeekBarPreference PQPictureAdvancedManualGammaBGainPref;

    private static final int PQ_PICTURE_ADVANCED_MANUAL_STEP = 1;
    private static int PQ_PICTURE_ManualGamma_LEVEL = 0;

    private PQSettingsManager mPQSettingsManager;

    public static PQAdvancedManualGammaFragment newInstance() {
        return new PQAdvancedManualGammaFragment();
    }

    private String[] getArrayString(int resid) {
        return getActivity().getResources().getStringArray(resid);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mPQSettingsManager == null) {
            mPQSettingsManager = new PQSettingsManager(getActivity());
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pq_picture_advanced_manual_gamma, null);

        if (mPQSettingsManager == null) {
            mPQSettingsManager = new PQSettingsManager(getActivity());
        }

        PQPictureAdvancedManualGammaLevelPref = (SeekBarPreference) findPreference(PQ_PICTURE_ADVANCED_MANUAL_GAMMA_LEVEL);
        PQPictureAdvancedManualGammaRGainPref = (SeekBarPreference) findPreference(PQ_PICTURE_ADVANCED_MANUAL_GAMMA_RGAIN);
        PQPictureAdvancedManualGammaGGainPref = (SeekBarPreference) findPreference(PQ_PICTURE_ADVANCED_MANUAL_GAMMA_GGAIN);
        PQPictureAdvancedManualGammaBGainPref = (SeekBarPreference) findPreference(PQ_PICTURE_ADVANCED_MANUAL_GAMMA_BGAIN);
        final Preference PQPictureAdvancedManualGammaResetPref = (Preference) findPreference(PQ_PICTURE_ADVANCED_MANUAL_GAMMA_RESET);

        PQPictureAdvancedManualGammaLevelPref.setOnPreferenceChangeListener(this);
        PQPictureAdvancedManualGammaLevelPref.setSeekBarIncrement(PQ_PICTURE_ADVANCED_MANUAL_STEP);
        //The display value of level starts from 0.
        PQPictureAdvancedManualGammaLevelPref.setMin(0);
        PQPictureAdvancedManualGammaLevelPref.setMax(10);
        PQPictureAdvancedManualGammaLevelPref.setValue(0);
        PQPictureAdvancedManualGammaLevelPref.setVisible(true);

        PQPictureAdvancedManualGammaRGainPref.setOnPreferenceChangeListener(this);
        PQPictureAdvancedManualGammaRGainPref.setSeekBarIncrement(PQ_PICTURE_ADVANCED_MANUAL_STEP);
        PQPictureAdvancedManualGammaRGainPref.setMin(-50);
        PQPictureAdvancedManualGammaRGainPref.setMax(50);

        PQPictureAdvancedManualGammaGGainPref.setOnPreferenceChangeListener(this);
        PQPictureAdvancedManualGammaGGainPref.setSeekBarIncrement(PQ_PICTURE_ADVANCED_MANUAL_STEP);
        PQPictureAdvancedManualGammaGGainPref.setMin(-50);
        PQPictureAdvancedManualGammaGGainPref.setMax(50);

        PQPictureAdvancedManualGammaBGainPref.setOnPreferenceChangeListener(this);
        PQPictureAdvancedManualGammaBGainPref.setSeekBarIncrement(PQ_PICTURE_ADVANCED_MANUAL_STEP);
        PQPictureAdvancedManualGammaBGainPref.setMin(-50);
        PQPictureAdvancedManualGammaBGainPref.setMax(50);

        updateManualGamma(0);
        PQPictureAdvancedManualGammaRGainPref.setVisible(true);
        PQPictureAdvancedManualGammaGGainPref.setVisible(true);
        PQPictureAdvancedManualGammaBGainPref.setVisible(true);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        logDebug(TAG, false, "[onPreferenceTreeClick] preference.getKey() = " + preference.getKey());
        switch (preference.getKey()) {
            case PQ_PICTURE_ADVANCED_MANUAL_GAMMA_RESET:
                Intent PQPictureAdvancedManualGammaResetAllIntent = new Intent();
                PQPictureAdvancedManualGammaResetAllIntent.setClassName(
                        "com.droidlogic.tv.extras",
                        "com.droidlogic.tv.extras.pqsettings.advanced.PQAdvancedManualGammaResetAllActivity");
                startActivity(PQPictureAdvancedManualGammaResetAllIntent);
                break;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case PQ_PICTURE_ADVANCED_MANUAL_GAMMA_LEVEL:
                PQ_PICTURE_ManualGamma_LEVEL = (int)newValue;
                updateManualGamma(PQ_PICTURE_ManualGamma_LEVEL);
                break;
            case PQ_PICTURE_ADVANCED_MANUAL_GAMMA_RGAIN:
                mPQSettingsManager.setWhiteBalanceGamma(
                        PQSettingsManager.RGB_CHANNEL_TYPE.RED_CH.getVal(),
                        PQ_PICTURE_ManualGamma_LEVEL,
                        (int)newValue
                        );
                break;
            case PQ_PICTURE_ADVANCED_MANUAL_GAMMA_GGAIN:
                mPQSettingsManager.setWhiteBalanceGamma(
                        PQSettingsManager.RGB_CHANNEL_TYPE.GREEN_CH.getVal(),
                        PQ_PICTURE_ManualGamma_LEVEL,
                        (int)newValue
                );
                break;
            case PQ_PICTURE_ADVANCED_MANUAL_GAMMA_BGAIN:
                mPQSettingsManager.setWhiteBalanceGamma(
                        PQSettingsManager.RGB_CHANNEL_TYPE.BLUE_CH.getVal(),
                        PQ_PICTURE_ManualGamma_LEVEL,
                        (int)newValue
                );
                break;
            default:
                break;
        }
        return true;
    }

    private void updateManualGamma(int pq_picture_manualGamma_level) {
        PQPictureAdvancedManualGammaRGainPref.setValue(
                mPQSettingsManager.getWhiteBalanceGamma(
                        PQSettingsManager.RGB_CHANNEL_TYPE.RED_CH.getVal(), pq_picture_manualGamma_level));
        PQPictureAdvancedManualGammaGGainPref.setValue(
                mPQSettingsManager.getWhiteBalanceGamma(
                        PQSettingsManager.RGB_CHANNEL_TYPE.GREEN_CH.getVal(), pq_picture_manualGamma_level));
        PQPictureAdvancedManualGammaBGainPref.setValue(
                mPQSettingsManager.getWhiteBalanceGamma(
                        PQSettingsManager.RGB_CHANNEL_TYPE.BLUE_CH.getVal(), pq_picture_manualGamma_level));
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

}
