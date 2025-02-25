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


public class PQAdvancedColorCustomizeMagentaFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    public static final String TAG = "PQAdvancedColorCustomizeMagentaFragment";

    private static final String PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_MAGENTA_SATURATION = "pq_picture_advanced_color_customize_magenta_saturation";
    private static final String PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_MAGENTA_LUMA = "pq_picture_advanced_color_customize_magenta_luma";
    private static final String PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_MAGENTA_HUE = "pq_picture_advanced_color_customize_magenta_hue";
    private static final int PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_MAGENTA_STEP = 1;

    private PQSettingsManager mPQSettingsManager;

    public static PQAdvancedColorCustomizeMagentaFragment newInstance() {
        return new PQAdvancedColorCustomizeMagentaFragment();
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
        setPreferencesFromResource(R.xml.pq_picture_advanced_color_customize_magenta, null);

        if (mPQSettingsManager == null) {
            mPQSettingsManager = new PQSettingsManager(getActivity());
        }

        final SeekBarPreference PQPictureAdvancedColorCustomizeMagentaSaturationPref = (SeekBarPreference) findPreference(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_MAGENTA_SATURATION);
        final SeekBarPreference PQPictureAdvancedColorCustomizeMagentaLumaPref = (SeekBarPreference) findPreference(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_MAGENTA_LUMA);
        final SeekBarPreference PQPictureAdvancedColorCustomizeMagentaHuePref = (SeekBarPreference) findPreference(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_MAGENTA_HUE);

        PQPictureAdvancedColorCustomizeMagentaSaturationPref.setOnPreferenceChangeListener(this);
        PQPictureAdvancedColorCustomizeMagentaSaturationPref.setSeekBarIncrement(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_MAGENTA_STEP);
        PQPictureAdvancedColorCustomizeMagentaSaturationPref.setMin(-50);
        PQPictureAdvancedColorCustomizeMagentaSaturationPref.setMax(50);
        PQPictureAdvancedColorCustomizeMagentaSaturationPref.setValue(mPQSettingsManager.getAdvancedColorCustomizeMagentaSaturationStatus());
        PQPictureAdvancedColorCustomizeMagentaSaturationPref.setVisible(true);

        PQPictureAdvancedColorCustomizeMagentaLumaPref.setOnPreferenceChangeListener(this);
        PQPictureAdvancedColorCustomizeMagentaLumaPref.setSeekBarIncrement(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_MAGENTA_STEP);
        PQPictureAdvancedColorCustomizeMagentaLumaPref.setMin(-15);
        PQPictureAdvancedColorCustomizeMagentaLumaPref.setMax(15);
        PQPictureAdvancedColorCustomizeMagentaLumaPref.setValue(mPQSettingsManager.getAdvancedColorCustomizeMagentaLumaStatus());
        PQPictureAdvancedColorCustomizeMagentaLumaPref.setVisible(true);

        PQPictureAdvancedColorCustomizeMagentaHuePref.setOnPreferenceChangeListener(this);
        PQPictureAdvancedColorCustomizeMagentaHuePref.setSeekBarIncrement(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_MAGENTA_STEP);
        PQPictureAdvancedColorCustomizeMagentaHuePref.setMin(-50);
        PQPictureAdvancedColorCustomizeMagentaHuePref.setMax(50);
        PQPictureAdvancedColorCustomizeMagentaHuePref.setValue(mPQSettingsManager.getAdvancedColorCustomizeMagentaHueStatus());
        PQPictureAdvancedColorCustomizeMagentaHuePref.setVisible(true);

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        logDebug(TAG, false, "[onPreferenceTreeClick] preference.getKey() = " + preference.getKey());
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_MAGENTA_SATURATION:
                mPQSettingsManager.setAdvancedColorCustomizeMagentaSaturationStatus((int)newValue);
                break;
            case PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_MAGENTA_LUMA:
                mPQSettingsManager.setAdvancedColorCustomizeMagentaLumaStatus((int)newValue);
                break;
            case PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE_MAGENTA_HUE:
                mPQSettingsManager.setAdvancedColorCustomizeMagentaHueStatus((int)newValue);
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
