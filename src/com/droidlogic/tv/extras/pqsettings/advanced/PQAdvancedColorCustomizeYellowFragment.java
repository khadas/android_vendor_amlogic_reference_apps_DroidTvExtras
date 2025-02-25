/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless requiyellow by applicable law or agreed to in writing, software
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

public class PQAdvancedColorCustomizeYellowFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    public static final String TAG = "PQAdvancedColorCustomizeYellowFragment";

    private static final String PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_YELLOW_SATURATION = "pq_picture_advanced_color_customize_yellow_saturation";
    private static final String PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_YELLOW_LUMA = "pq_picture_advanced_color_customize_yellow_luma";
    private static final String PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_YELLOW_HUE = "pq_picture_advanced_color_customize_yellow_hue";
    private static final int PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_YELLOW_STEP = 1;

    private PQSettingsManager mPQSettingsManager;

    public static PQAdvancedColorCustomizeYellowFragment newInstance() {
        return new PQAdvancedColorCustomizeYellowFragment();
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
        setPreferencesFromResource(R.xml.pq_picture_advanced_color_customize_yellow, null);

        if (mPQSettingsManager == null) {
            mPQSettingsManager = new PQSettingsManager(getActivity());
        }

        final SeekBarPreference PQPictureAdvancedColorCustomizeYellowSaturationPref = (SeekBarPreference) findPreference(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_YELLOW_SATURATION);
        final SeekBarPreference PQPictureAdvancedColorCustomizeYellowLumaPref = (SeekBarPreference) findPreference(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_YELLOW_LUMA);
        final SeekBarPreference PQPictureAdvancedColorCustomizeYellowHuePref = (SeekBarPreference) findPreference(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_YELLOW_HUE);

        PQPictureAdvancedColorCustomizeYellowSaturationPref.setOnPreferenceChangeListener(this);
        PQPictureAdvancedColorCustomizeYellowSaturationPref.setSeekBarIncrement(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_YELLOW_STEP);
        PQPictureAdvancedColorCustomizeYellowSaturationPref.setMin(-50);
        PQPictureAdvancedColorCustomizeYellowSaturationPref.setMax(50);
        PQPictureAdvancedColorCustomizeYellowSaturationPref.setValue(mPQSettingsManager.getAdvancedColorCustomizeYellowSaturationStatus());
        PQPictureAdvancedColorCustomizeYellowSaturationPref.setVisible(true);

        PQPictureAdvancedColorCustomizeYellowLumaPref.setOnPreferenceChangeListener(this);
        PQPictureAdvancedColorCustomizeYellowLumaPref.setSeekBarIncrement(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_YELLOW_STEP);
        PQPictureAdvancedColorCustomizeYellowLumaPref.setMin(-15);
        PQPictureAdvancedColorCustomizeYellowLumaPref.setMax(15);
        PQPictureAdvancedColorCustomizeYellowLumaPref.setValue(mPQSettingsManager.getAdvancedColorCustomizeYellowLumaStatus());
        PQPictureAdvancedColorCustomizeYellowLumaPref.setVisible(true);

        PQPictureAdvancedColorCustomizeYellowHuePref.setOnPreferenceChangeListener(this);
        PQPictureAdvancedColorCustomizeYellowHuePref.setSeekBarIncrement(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_YELLOW_STEP);
        PQPictureAdvancedColorCustomizeYellowHuePref.setMin(-50);
        PQPictureAdvancedColorCustomizeYellowHuePref.setMax(50);
        PQPictureAdvancedColorCustomizeYellowHuePref.setValue(mPQSettingsManager.getAdvancedColorCustomizeYellowHueStatus());
        PQPictureAdvancedColorCustomizeYellowHuePref.setVisible(true);

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        logDebug(TAG, false, "[onPreferenceTreeClick] preference.getKey() = " + preference.getKey());
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_YELLOW_SATURATION:
                mPQSettingsManager.setAdvancedColorCustomizeYellowSaturationStatus((int)newValue);
                break;
            case PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_YELLOW_LUMA:
                mPQSettingsManager.setAdvancedColorCustomizeYellowLumaStatus((int)newValue);
                break;
            case PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_YELLOW_HUE:
                mPQSettingsManager.setAdvancedColorCustomizeYellowHueStatus((int)newValue);
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
