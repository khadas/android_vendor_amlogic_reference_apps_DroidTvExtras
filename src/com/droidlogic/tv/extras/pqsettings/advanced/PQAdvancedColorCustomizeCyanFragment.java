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


public class PQAdvancedColorCustomizeCyanFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    public static final String TAG = "PQAdvancedColorCustomizeCyanFragment";

    private static final String PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_CYAN_SATURATION = "pq_picture_advanced_color_customize_cyan_saturation";
    private static final String PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_CYAN_LUMA = "pq_picture_advanced_color_customize_cyan_luma";
    private static final String PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_CYAN_HUE = "pq_picture_advanced_color_customize_cyan_hue";
    private static final int PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_CYAN_STEP = 1;

    private PQSettingsManager mPQSettingsManager;

    public static PQAdvancedColorCustomizeCyanFragment newInstance() {
        return new PQAdvancedColorCustomizeCyanFragment();
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
        setPreferencesFromResource(R.xml.pq_picture_advanced_color_customize_cyan, null);

        if (mPQSettingsManager == null) {
            mPQSettingsManager = new PQSettingsManager(getActivity());
        }

        final SeekBarPreference PQPictureAdvancedColorCustomizeCyanSaturationPref = (SeekBarPreference) findPreference(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_CYAN_SATURATION);
        final SeekBarPreference PQPictureAdvancedColorCustomizeCyanLumaPref = (SeekBarPreference) findPreference(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_CYAN_LUMA);
        final SeekBarPreference PQPictureAdvancedColorCustomizeCyanHuePref = (SeekBarPreference) findPreference(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_CYAN_HUE);

        PQPictureAdvancedColorCustomizeCyanSaturationPref.setOnPreferenceChangeListener(this);
        PQPictureAdvancedColorCustomizeCyanSaturationPref.setSeekBarIncrement(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_CYAN_STEP);
        PQPictureAdvancedColorCustomizeCyanSaturationPref.setMin(-50);
        PQPictureAdvancedColorCustomizeCyanSaturationPref.setMax(50);
        PQPictureAdvancedColorCustomizeCyanSaturationPref.setValue(mPQSettingsManager.getAdvancedColorCustomizeCyanSaturationStatus());
        PQPictureAdvancedColorCustomizeCyanSaturationPref.setVisible(true);

        PQPictureAdvancedColorCustomizeCyanLumaPref.setOnPreferenceChangeListener(this);
        PQPictureAdvancedColorCustomizeCyanLumaPref.setSeekBarIncrement(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_CYAN_STEP);
        PQPictureAdvancedColorCustomizeCyanLumaPref.setMin(-15);
        PQPictureAdvancedColorCustomizeCyanLumaPref.setMax(15);
        PQPictureAdvancedColorCustomizeCyanLumaPref.setValue(mPQSettingsManager.getAdvancedColorCustomizeCyanLumaStatus());
        PQPictureAdvancedColorCustomizeCyanLumaPref.setVisible(true);

        PQPictureAdvancedColorCustomizeCyanHuePref.setOnPreferenceChangeListener(this);
        PQPictureAdvancedColorCustomizeCyanHuePref.setSeekBarIncrement(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_CYAN_STEP);
        PQPictureAdvancedColorCustomizeCyanHuePref.setMin(-50);
        PQPictureAdvancedColorCustomizeCyanHuePref.setMax(50);
        PQPictureAdvancedColorCustomizeCyanHuePref.setValue(mPQSettingsManager.getAdvancedColorCustomizeCyanHueStatus());
        PQPictureAdvancedColorCustomizeCyanHuePref.setVisible(true);

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        logDebug(TAG, false, "[onPreferenceTreeClick] preference.getKey() = " + preference.getKey());
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_CYAN_SATURATION:
                mPQSettingsManager.setAdvancedColorCustomizeCyanSaturationStatus((int)newValue);
                break;
            case PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_CYAN_LUMA:
                mPQSettingsManager.setAdvancedColorCustomizeCyanLumaStatus((int)newValue);
                break;
            case PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_CYAN_HUE:
                mPQSettingsManager.setAdvancedColorCustomizeCyanHueStatus((int)newValue);
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
