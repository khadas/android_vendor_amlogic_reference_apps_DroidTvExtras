/*
 * Copyright (c) 2014 Amlogic, Inc. All rights reserved.
 *
 * This source code is subject to the terms and conditions defined in the
 * file 'LICENSE' which is part of this source code package.
 *
 * Description:
 *     AMLOGIC PQAdvancedMEMCFragment
 */

package com.droidlogic.tv.extras.pqsettings.advanced;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.Settings;
import androidx.preference.SwitchPreference;
import androidx.preference.SeekBarPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.TwoStatePreference;
import androidx.preference.PreferenceCategory;
import android.util.ArrayMap;
import android.text.TextUtils;

import com.droidlogic.tv.extras.SettingsPreferenceFragment;
import com.droidlogic.tv.extras.R;
import com.droidlogic.tv.extras.pqsettings.PQSettingsManager;

import static com.droidlogic.tv.extras.util.DroidUtils.logDebug;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import com.droidlogic.app.SystemControlManager;

public class PQAdvancedMEMCFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "PQAdvancedMEMCFragment";

    private static final String PQ_PICTURE_ADVANCED_MEMC_SWITCH              = "pq_picture_advanced_memc_switch";
    private static final String PQ_PICTURE_ADVANCED_MEMC_CUSTOMIZE_DEJUDDER  = "pq_picture_advanced_memc_customize_dejudder";
    private static final String PQ_PICTURE_ADVANCED_MEMC_CUSTOMIZE_DEBLUR    = "pq_picture_advanced_memc_customize_deblur";
    private static final String PQ_PICTURE_ADVANCED_MEMC_DEMO_SWITCH         = "pq_picture_advanced_memc_demo_switch";

    private SeekBarPreference PQPictureAdvancedMemcCustomizeDejudderPref;
    private SeekBarPreference PQPictureAdvancedMemcCustomizeDeblurPref;
    private TwoStatePreference mMemcDemoSwitchPref;

    private static final int PQ_PICTURE_ADVANCED_MEMC_CUSTOMIZE_STEP = 1;

    private PQSettingsManager mPQSettingsManager;

    public static PQAdvancedMEMCFragment newInstance() {
        return new PQAdvancedMEMCFragment();
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
        setPreferencesFromResource(R.xml.pq_picture_advanced_memc, null);
        if (mPQSettingsManager == null) {
            mPQSettingsManager = new PQSettingsManager(getActivity());
        }

        final ListPreference pictureAdvancedMemcSwitchPref = (ListPreference) findPreference(PQ_PICTURE_ADVANCED_MEMC_SWITCH);
        PQPictureAdvancedMemcCustomizeDejudderPref = (SeekBarPreference) findPreference(PQ_PICTURE_ADVANCED_MEMC_CUSTOMIZE_DEJUDDER);
        PQPictureAdvancedMemcCustomizeDeblurPref = (SeekBarPreference) findPreference(PQ_PICTURE_ADVANCED_MEMC_CUSTOMIZE_DEBLUR);

        pictureAdvancedMemcSwitchPref.setVisible(true);
        pictureAdvancedMemcSwitchPref.setValueIndex(mPQSettingsManager.getAdvancedMemcSwitchStatus());
        pictureAdvancedMemcSwitchPref.setOnPreferenceChangeListener(this);

        PQPictureAdvancedMemcCustomizeDejudderPref.setOnPreferenceChangeListener(this);
        PQPictureAdvancedMemcCustomizeDejudderPref.setSeekBarIncrement(PQ_PICTURE_ADVANCED_MEMC_CUSTOMIZE_STEP);
        PQPictureAdvancedMemcCustomizeDejudderPref.setMin(0);
        PQPictureAdvancedMemcCustomizeDejudderPref.setMax(10);
        PQPictureAdvancedMemcCustomizeDejudderPref.setValue(mPQSettingsManager.getAdvancedMemcCustomizeDejudderStatus());
        PQPictureAdvancedMemcCustomizeDejudderPref.setVisible(true);

        PQPictureAdvancedMemcCustomizeDeblurPref.setOnPreferenceChangeListener(this);
        PQPictureAdvancedMemcCustomizeDeblurPref.setSeekBarIncrement(PQ_PICTURE_ADVANCED_MEMC_CUSTOMIZE_STEP);
        PQPictureAdvancedMemcCustomizeDeblurPref.setMin(0);
        PQPictureAdvancedMemcCustomizeDeblurPref.setMax(10);
        PQPictureAdvancedMemcCustomizeDeblurPref.setValue(mPQSettingsManager.getAdvancedMemcCustomizeDeblurStatus());
        PQPictureAdvancedMemcCustomizeDeblurPref.setVisible(true);

        mMemcDemoSwitchPref = (TwoStatePreference) findPreference(PQ_PICTURE_ADVANCED_MEMC_DEMO_SWITCH);
        mMemcDemoSwitchPref.setOnPreferenceChangeListener(this);
        mMemcDemoSwitchPref.setChecked(mPQSettingsManager.getMemcDemoEnabled() >= 1 ? true : false);
        updateMemcCustomizeDisplay(mPQSettingsManager.getAdvancedMemcSwitchStatus());
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (TextUtils.equals(preference.getKey(), PQ_PICTURE_ADVANCED_MEMC_DEMO_SWITCH)) {
            mPQSettingsManager.setMemcDemoEnabled((boolean) newValue);
            return true;
        }

        logDebug(TAG, false, "[onPreferenceTreeClick] preference.getKey() = " + preference.getKey() + " newValue:" + newValue);
        switch (preference.getKey()) {
            case PQ_PICTURE_ADVANCED_MEMC_SWITCH:
                final int selection = Integer.parseInt((String) newValue);
                mPQSettingsManager.setAdvancedMemcSwitchStatus(selection);
                updateMemcCustomizeDisplay(selection);
                break;
            case PQ_PICTURE_ADVANCED_MEMC_CUSTOMIZE_DEJUDDER:
                mPQSettingsManager.setAdvancedMemcCustomizeDejudderStatus((int) newValue);
                break;
            case PQ_PICTURE_ADVANCED_MEMC_CUSTOMIZE_DEBLUR:
                mPQSettingsManager.setAdvancedMemcCustomizeDeblurStatus((int) newValue);
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

    private void updateMemcCustomizeDisplay(int value) {
        if (4 == value) {
            PQPictureAdvancedMemcCustomizeDeblurPref.setEnabled(true);
            PQPictureAdvancedMemcCustomizeDejudderPref.setEnabled(true);
        } else {
            PQPictureAdvancedMemcCustomizeDeblurPref.setEnabled(false);
            PQPictureAdvancedMemcCustomizeDejudderPref.setEnabled(false);
        }
    }

}
