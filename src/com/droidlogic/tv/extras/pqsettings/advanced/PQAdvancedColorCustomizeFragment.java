/*
 * Copyright (c) 2014 Amlogic, Inc. All rights reserved.
 *
 * This source code is subject to the terms and conditions defined in the
 * file 'LICENSE' which is part of this source code package.
 *
 * Description:
 *     AMLOGIC PQAdvancedColorCustomizeFragment
 */



package com.droidlogic.tv.extras.pqsettings.advanced;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.Settings;
import androidx.preference.SwitchPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.TwoStatePreference;
import androidx.preference.PreferenceCategory;
import android.util.ArrayMap;
import android.text.TextUtils;

import com.droidlogic.app.DisplayPositionManager;
import com.droidlogic.tv.extras.SettingsPreferenceFragment;
import com.droidlogic.tv.extras.R;
import com.droidlogic.tv.extras.SettingsConstant;
import com.droidlogic.tv.extras.pqsettings.PQSettingsManager;
import static com.droidlogic.tv.extras.util.DroidUtils.logDebug;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class PQAdvancedColorCustomizeFragment extends SettingsPreferenceFragment {
    private static final String TAG = "PQAdvancedColorCustomizeFragment";
    private static final String PQ_ADVANCED_COLOR_CUSTOMIZE_ALLRESET = "pq_picture_advanced_color_customize_reset";

    public static PQAdvancedColorCustomizeFragment newInstance() {
        return new PQAdvancedColorCustomizeFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        boolean isTv = SettingsConstant.needDroidlogicTvFeature(getActivity());
        setPreferencesFromResource(R.xml.pq_picture_advanced_color_customize, null);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        logDebug(TAG, false, "[onPreferenceTreeClick] preference.getKey() = " + preference.getKey());
        switch (preference.getKey()) {
            case PQ_ADVANCED_COLOR_CUSTOMIZE_ALLRESET:
                Intent PQAdvancedColorCustomizeResetAllResetIntent = new Intent();
                PQAdvancedColorCustomizeResetAllResetIntent.setClassName(
                        "com.droidlogic.tv.settings",
                        "com.droidlogic.tv.settings.pqsettings.advanced.PQAdvancedColorCustomizeResetAllActivity");
                startActivity(PQAdvancedColorCustomizeResetAllResetIntent);
                break;
            default:
                break;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

}
