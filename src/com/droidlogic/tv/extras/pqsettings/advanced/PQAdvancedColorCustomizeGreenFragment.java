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
import static com.droidlogic.tv.extras.util.DroidUtils.logDebug;
import com.droidlogic.tv.extras.R;

import com.droidlogic.tv.extras.pqsettings.PQSettingsManager;

public class PQAdvancedColorCustomizeGreenFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    public static final String TAG = "PQAdvancedColorCustomizeGreenFragment";

    private static final String PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_GREEN_SATURATION = "pq_picture_advanced_color_customize_green_saturation";
    private static final String PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_GREEN_LUMA = "pq_picture_advanced_color_customize_green_luma";
    private static final String PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_GREEN_HUE = "pq_picture_advanced_color_customize_green_hue";
    private static final int PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_GREEN_STEP = 1;

    private PQSettingsManager mPQSettingsManager;

    public static PQAdvancedColorCustomizeGreenFragment newInstance() {
        return new PQAdvancedColorCustomizeGreenFragment();
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
        setPreferencesFromResource(R.xml.pq_picture_advanced_color_customize_green, null);

        if (mPQSettingsManager == null) {
            mPQSettingsManager = new PQSettingsManager(getActivity());
        }

        final SeekBarPreference PQPictureAdvancedColorCustomizeGreenSaturationPref = (SeekBarPreference) findPreference(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_GREEN_SATURATION);
        final SeekBarPreference PQPictureAdvancedColorCustomizeGreenLumaPref = (SeekBarPreference) findPreference(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_GREEN_LUMA);
        final SeekBarPreference PQPictureAdvancedColorCustomizeGreenHuePref = (SeekBarPreference) findPreference(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_GREEN_HUE);

        PQPictureAdvancedColorCustomizeGreenSaturationPref.setOnPreferenceChangeListener(this);
        PQPictureAdvancedColorCustomizeGreenSaturationPref.setSeekBarIncrement(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_GREEN_STEP);
        PQPictureAdvancedColorCustomizeGreenSaturationPref.setMin(-50);
        PQPictureAdvancedColorCustomizeGreenSaturationPref.setMax(50);
        PQPictureAdvancedColorCustomizeGreenSaturationPref.setValue(mPQSettingsManager.getAdvancedColorCustomizeGreenSaturationStatus());
        PQPictureAdvancedColorCustomizeGreenSaturationPref.setVisible(true);

        PQPictureAdvancedColorCustomizeGreenLumaPref.setOnPreferenceChangeListener(this);
        PQPictureAdvancedColorCustomizeGreenLumaPref.setSeekBarIncrement(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_GREEN_STEP);
        PQPictureAdvancedColorCustomizeGreenLumaPref.setMin(-15);
        PQPictureAdvancedColorCustomizeGreenLumaPref.setMax(15);
        PQPictureAdvancedColorCustomizeGreenLumaPref.setValue(mPQSettingsManager.getAdvancedColorCustomizeGreenLumaStatus());
        PQPictureAdvancedColorCustomizeGreenLumaPref.setVisible(true);

        PQPictureAdvancedColorCustomizeGreenHuePref.setOnPreferenceChangeListener(this);
        PQPictureAdvancedColorCustomizeGreenHuePref.setSeekBarIncrement(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_GREEN_STEP);
        PQPictureAdvancedColorCustomizeGreenHuePref.setMin(-50);
        PQPictureAdvancedColorCustomizeGreenHuePref.setMax(50);
        PQPictureAdvancedColorCustomizeGreenHuePref.setValue(mPQSettingsManager.getAdvancedColorCustomizeGreenHueStatus());
        PQPictureAdvancedColorCustomizeGreenHuePref.setVisible(true);

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        logDebug(TAG, false, "[onPreferenceTreeClick] preference.getKey() = " + preference.getKey());
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_GREEN_SATURATION:
                mPQSettingsManager.setAdvancedColorCustomizeGreenSaturationStatus((int)newValue);
                break;
            case PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_GREEN_LUMA:
                mPQSettingsManager.setAdvancedColorCustomizeGreenLumaStatus((int)newValue);
                break;
            case PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_GREEN_HUE:
                mPQSettingsManager.setAdvancedColorCustomizeGreenHueStatus((int)newValue);
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
