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


public class PQAdvancedColorCustomizeBlueFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    public static final String TAG = "PQAdvancedColorCustomizeBlueFragment";

    private static final String PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_BLUE_SATURATION = "pq_picture_advanced_color_customize_blue_saturation";
    private static final String PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_BLUE_LUMA = "pq_picture_advanced_color_customize_blue_luma";
    private static final String PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_BLUE_HUE = "pq_picture_advanced_color_customize_blue_hue";
    private static final int PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_BLUE_STEP = 1;

    private PQSettingsManager mPQSettingsManager;

    public static PQAdvancedColorCustomizeBlueFragment newInstance() {
        return new PQAdvancedColorCustomizeBlueFragment();
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
        setPreferencesFromResource(R.xml.pq_picture_advanced_color_customize_blue, null);

        if (mPQSettingsManager == null) {
            mPQSettingsManager = new PQSettingsManager(getActivity());
        }

        final SeekBarPreference PQPictureAdvancedColorCustomizeBlueSaturationPref = (SeekBarPreference) findPreference(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_BLUE_SATURATION);
        final SeekBarPreference PQPictureAdvancedColorCustomizeBlueLumaPref = (SeekBarPreference) findPreference(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_BLUE_LUMA);
        final SeekBarPreference PQPictureAdvancedColorCustomizeBlueHuePref = (SeekBarPreference) findPreference(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_BLUE_HUE);

        PQPictureAdvancedColorCustomizeBlueSaturationPref.setOnPreferenceChangeListener(this);
        PQPictureAdvancedColorCustomizeBlueSaturationPref.setSeekBarIncrement(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_BLUE_STEP);
        PQPictureAdvancedColorCustomizeBlueSaturationPref.setMin(-50);
        PQPictureAdvancedColorCustomizeBlueSaturationPref.setMax(50);
        PQPictureAdvancedColorCustomizeBlueSaturationPref.setValue(mPQSettingsManager.getAdvancedColorCustomizeBlueSaturationStatus());
        PQPictureAdvancedColorCustomizeBlueSaturationPref.setVisible(true);

        PQPictureAdvancedColorCustomizeBlueLumaPref.setOnPreferenceChangeListener(this);
        PQPictureAdvancedColorCustomizeBlueLumaPref.setSeekBarIncrement(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_BLUE_STEP);
        PQPictureAdvancedColorCustomizeBlueLumaPref.setMin(-15);
        PQPictureAdvancedColorCustomizeBlueLumaPref.setMax(15);
        PQPictureAdvancedColorCustomizeBlueLumaPref.setValue(mPQSettingsManager.getAdvancedColorCustomizeBlueLumaStatus());
        PQPictureAdvancedColorCustomizeBlueLumaPref.setVisible(true);

        PQPictureAdvancedColorCustomizeBlueHuePref.setOnPreferenceChangeListener(this);
        PQPictureAdvancedColorCustomizeBlueHuePref.setSeekBarIncrement(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_BLUE_STEP);
        PQPictureAdvancedColorCustomizeBlueHuePref.setMin(-50);
        PQPictureAdvancedColorCustomizeBlueHuePref.setMax(50);
        PQPictureAdvancedColorCustomizeBlueHuePref.setValue(mPQSettingsManager.getAdvancedColorCustomizeBlueHueStatus());
        PQPictureAdvancedColorCustomizeBlueHuePref.setVisible(true);

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        logDebug(TAG, false, "[onPreferenceTreeClick] preference.getKey() = " + preference.getKey());
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_BLUE_SATURATION:
                mPQSettingsManager.setAdvancedColorCustomizeBlueSaturationStatus((int)newValue);
                break;
            case PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_BLUE_LUMA:
                mPQSettingsManager.setAdvancedColorCustomizeBlueLumaStatus((int)newValue);
                break;
            case PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_BLUE_HUE:
                mPQSettingsManager.setAdvancedColorCustomizeBlueHueStatus((int)newValue);
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
