/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless requiyellow_green by applicable law or agreed to in writing, software
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

public class PQAdvancedColorCustomizeYellowGreenFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    public static final String TAG = "PQAdvancedColorCustomizeYellowGreenFragment";

    private static final String PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_YELLOWGREEN_SATURATION = "pq_picture_advanced_color_customize_yellow_green_saturation";
    private static final String PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_YELLOWGREEN_LUMA = "pq_picture_advanced_color_customize_yellow_green_luma";
    private static final String PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_YELLOWGREEN_HUE = "pq_picture_advanced_color_customize_yellow_green_hue";
    private static final int PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_YELLOWGREEN_STEP = 1;

    private PQSettingsManager mPQSettingsManager;

    public static PQAdvancedColorCustomizeYellowGreenFragment newInstance() {
        return new PQAdvancedColorCustomizeYellowGreenFragment();
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
        setPreferencesFromResource(R.xml.pq_picture_advanced_color_customize_yellow_green, null);

        if (mPQSettingsManager == null) {
            mPQSettingsManager = new PQSettingsManager(getActivity());
        }

        final SeekBarPreference PQPictureAdvancedColorCustomizeYellowGreenSaturationPref = (SeekBarPreference) findPreference(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_YELLOWGREEN_SATURATION);
        final SeekBarPreference PQPictureAdvancedColorCustomizeYellowGreenLumaPref = (SeekBarPreference) findPreference(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_YELLOWGREEN_LUMA);
        final SeekBarPreference PQPictureAdvancedColorCustomizeYellowGreenHuePref = (SeekBarPreference) findPreference(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_YELLOWGREEN_HUE);

        PQPictureAdvancedColorCustomizeYellowGreenSaturationPref.setOnPreferenceChangeListener(this);
        PQPictureAdvancedColorCustomizeYellowGreenSaturationPref.setSeekBarIncrement(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_YELLOWGREEN_STEP);
        PQPictureAdvancedColorCustomizeYellowGreenSaturationPref.setMin(-50);
        PQPictureAdvancedColorCustomizeYellowGreenSaturationPref.setMax(50);
        PQPictureAdvancedColorCustomizeYellowGreenSaturationPref.setValue(mPQSettingsManager.getAdvancedColorCustomizeYellowGreenSaturationStatus());
        PQPictureAdvancedColorCustomizeYellowGreenSaturationPref.setVisible(true);

        PQPictureAdvancedColorCustomizeYellowGreenLumaPref.setOnPreferenceChangeListener(this);
        PQPictureAdvancedColorCustomizeYellowGreenLumaPref.setSeekBarIncrement(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_YELLOWGREEN_STEP);
        PQPictureAdvancedColorCustomizeYellowGreenLumaPref.setMin(-15);
        PQPictureAdvancedColorCustomizeYellowGreenLumaPref.setMax(15);
        PQPictureAdvancedColorCustomizeYellowGreenLumaPref.setValue(mPQSettingsManager.getAdvancedColorCustomizeYellowGreenLumaStatus());
        PQPictureAdvancedColorCustomizeYellowGreenLumaPref.setVisible(true);

        PQPictureAdvancedColorCustomizeYellowGreenHuePref.setOnPreferenceChangeListener(this);
        PQPictureAdvancedColorCustomizeYellowGreenHuePref.setSeekBarIncrement(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_YELLOWGREEN_STEP);
        PQPictureAdvancedColorCustomizeYellowGreenHuePref.setMin(-50);
        PQPictureAdvancedColorCustomizeYellowGreenHuePref.setMax(50);
        PQPictureAdvancedColorCustomizeYellowGreenHuePref.setValue(mPQSettingsManager.getAdvancedColorCustomizeYellowGreenHueStatus());
        PQPictureAdvancedColorCustomizeYellowGreenHuePref.setVisible(true);

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        logDebug(TAG, false, "[onPreferenceTreeClick] preference.getKey() = " + preference.getKey());
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_YELLOWGREEN_SATURATION:
                mPQSettingsManager.setAdvancedColorCustomizeYellowGreenSaturationStatus((int) newValue);
                break;
            case PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_YELLOWGREEN_LUMA:
                mPQSettingsManager.setAdvancedColorCustomizeYellowGreenLumaStatus((int) newValue);
                break;
            case PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_YELLOWGREEN_HUE:
                mPQSettingsManager.setAdvancedColorCustomizeYellowGreenHueStatus((int) newValue);
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
