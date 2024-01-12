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

import static com.droidlogic.tv.extras.util.DroidUtils.logDebug;
import com.droidlogic.tv.extras.SettingsPreferenceFragment;
import com.droidlogic.tv.extras.R;

import com.droidlogic.tv.extras.pqsettings.PQSettingsManager;


public class PQAdvancedColorCustomizeRedFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    public static final String TAG = "PQAdvancedColorCustomizeRedFragment";

    private static final String PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_RED_SATURATION = "pq_picture_advanced_color_customize_red_saturation";
    private static final String PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_RED_LUMA = "pq_picture_advanced_color_customize_red_luma";
    private static final String PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_RED_HUE = "pq_picture_advanced_color_customize_red_hue";
    private static final int PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_RED_STEP = 1;

    private PQSettingsManager mPQSettingsManager;

    public static PQAdvancedColorCustomizeRedFragment newInstance() {
        return new PQAdvancedColorCustomizeRedFragment();
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
        setPreferencesFromResource(R.xml.pq_picture_advanced_color_customize_red, null);

        if (mPQSettingsManager == null) {
            mPQSettingsManager = new PQSettingsManager(getActivity());
        }

        final SeekBarPreference PQPictureAdvancedColorCustomizeRedSaturationPref = (SeekBarPreference) findPreference(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_RED_SATURATION);
        final SeekBarPreference PQPictureAdvancedColorCustomizeRedLumaPref = (SeekBarPreference) findPreference(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_RED_LUMA);
        final SeekBarPreference PQPictureAdvancedColorCustomizeRedHuePref = (SeekBarPreference) findPreference(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_RED_HUE);

        PQPictureAdvancedColorCustomizeRedSaturationPref.setOnPreferenceChangeListener(this);
        PQPictureAdvancedColorCustomizeRedSaturationPref.setSeekBarIncrement(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_RED_STEP);
        PQPictureAdvancedColorCustomizeRedSaturationPref.setMin(-50);
        PQPictureAdvancedColorCustomizeRedSaturationPref.setMax(50);
        PQPictureAdvancedColorCustomizeRedSaturationPref.setValue(mPQSettingsManager.getAdvancedColorCustomizeRedSaturationStatus());
        PQPictureAdvancedColorCustomizeRedSaturationPref.setVisible(true);

        PQPictureAdvancedColorCustomizeRedLumaPref.setOnPreferenceChangeListener(this);
        PQPictureAdvancedColorCustomizeRedLumaPref.setSeekBarIncrement(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_RED_STEP);
        PQPictureAdvancedColorCustomizeRedLumaPref.setMin(-15);
        PQPictureAdvancedColorCustomizeRedLumaPref.setMax(15);
        PQPictureAdvancedColorCustomizeRedLumaPref.setValue(mPQSettingsManager.getAdvancedColorCustomizeRedLumaStatus());
        PQPictureAdvancedColorCustomizeRedLumaPref.setVisible(true);

        PQPictureAdvancedColorCustomizeRedHuePref.setOnPreferenceChangeListener(this);
        PQPictureAdvancedColorCustomizeRedHuePref.setSeekBarIncrement(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_RED_STEP);
        PQPictureAdvancedColorCustomizeRedHuePref.setMin(-50);
        PQPictureAdvancedColorCustomizeRedHuePref.setMax(50);
        PQPictureAdvancedColorCustomizeRedHuePref.setValue(mPQSettingsManager.getAdvancedColorCustomizeRedHueStatus());
        PQPictureAdvancedColorCustomizeRedHuePref.setVisible(true);

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        logDebug(TAG, false, "[onPreferenceTreeClick] preference.getKey() = " + preference.getKey());
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_RED_SATURATION:
                mPQSettingsManager.setAdvancedColorCustomizeRedSaturationStatus((int)newValue);
                break;
            case PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_RED_LUMA:
                mPQSettingsManager.setAdvancedColorCustomizeRedLumaStatus((int)newValue);
                break;
            case PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_RED_HUE:
                mPQSettingsManager.setAdvancedColorCustomizeRedHueStatus((int)newValue);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

}
