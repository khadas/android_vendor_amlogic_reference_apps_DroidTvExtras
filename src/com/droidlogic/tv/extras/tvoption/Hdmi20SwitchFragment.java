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

package com.droidlogic.tv.extras.tvoption;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import android.os.Bundle;
import android.text.TextUtils;

import java.util.List;
import java.util.ArrayList;

import com.droidlogic.tv.extras.SettingsPreferenceFragment;
import com.droidlogic.tv.extras.R;
import static com.droidlogic.tv.extras.util.DroidUtils.logDebug;

public class Hdmi20SwitchFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener{

    private static final String TAG = "Hdmi20SwitchFragment";

    private static final String HDMI1_SWITCH = "tv_hdmi1_switch";
    private static final String HDMI2_SWITCH = "tv_hdmi2_switch";
    private static final String HDMI3_SWITCH = "tv_hdmi3_switch";
    private static final String HDMI4_SWITCH = "tv_hdmi4_switch";
    private static final int HDMI1= 0;
    private static final int HDMI2 = 1;
    private static final int HDMI3 = 2;
    private static final int HDMI4 = 3;

    private TvOptionSettingManager mTvOptionSettingManager;

    public static Hdmi20SwitchFragment newInstance() {
        return new Hdmi20SwitchFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        boolean is_from_live_tv = getActivity().getIntent().getIntExtra("from_live_tv", 0) == 1;
        final ListPreference hdmi1 = (ListPreference) findPreference(HDMI1_SWITCH);
        final ListPreference hdmi2 = (ListPreference) findPreference(HDMI2_SWITCH);
        final ListPreference hdmi3 = (ListPreference) findPreference(HDMI3_SWITCH);
        final ListPreference hdmi4 = (ListPreference) findPreference(HDMI4_SWITCH);
        List<ListPreference> all = new ArrayList<>();
        all.add(hdmi1);
        all.add(hdmi2);
        all.add(hdmi3);
        all.add(hdmi4);
        int source = mTvOptionSettingManager.GetRelativeSourceInput();
        for (int i = 0; i < 4; i++) {
            if (!is_from_live_tv || all.get(i) == null) {
                break;
            }
            if (source != i) {
                all.get(i).setEnabled(false);
            } else {
                all.get(i).setEnabled(true);
            }
        }
        int no = mTvOptionSettingManager.getNumOfHdmi();
        logDebug(TAG, false, "refresh:" + no
                + ",total size:" + getPreferenceScreen().getPreferenceCount());
        int preferenceCount = getPreferenceScreen().getPreferenceCount();
        for (int i = no; i < preferenceCount; i++) {
            getPreferenceScreen().removePreference(all.get(i));
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.tv_hdmi_switch, null);
        if (mTvOptionSettingManager == null) {
            mTvOptionSettingManager = new TvOptionSettingManager(getActivity(), false);
        }
        int[] fourhdmi = mTvOptionSettingManager.getFourHdmi20Status();
        final ListPreference hdmi1 = (ListPreference) findPreference(HDMI1_SWITCH);
        hdmi1.setEntries(initswitchEntries());
        hdmi1.setEntryValues(initSwitchEntryValue());
        hdmi1.setValueIndex(fourhdmi[HDMI1]);
        hdmi1.setOnPreferenceChangeListener(this);
        final ListPreference hdmi2 = (ListPreference) findPreference(HDMI2_SWITCH);
        hdmi2.setEntries(initswitchEntries());
        hdmi2.setEntryValues(initSwitchEntryValue());
        hdmi2.setValueIndex(fourhdmi[HDMI2]);
        hdmi2.setOnPreferenceChangeListener(this);
        final ListPreference hdmi3 = (ListPreference) findPreference(HDMI3_SWITCH);
        hdmi3.setEntries(initswitchEntries());
        hdmi3.setEntryValues(initSwitchEntryValue());
        hdmi3.setValueIndex(fourhdmi[HDMI3]);
        hdmi3.setOnPreferenceChangeListener(this);
        final ListPreference hdmi4 = (ListPreference) findPreference(HDMI4_SWITCH);
        hdmi4.setEntries(initswitchEntries());
        hdmi4.setEntryValues(initSwitchEntryValue());
        hdmi4.setValueIndex(fourhdmi[HDMI4]);
        hdmi4.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        logDebug(TAG, false, "[onPreferenceChange] preference.getKey() = " + preference.getKey()
                + ", newValue = " + newValue);
        final int selection = Integer.parseInt((String)newValue);
        if (TextUtils.equals(preference.getKey(), HDMI1_SWITCH)) {
            mTvOptionSettingManager.setHdmi20Mode(HDMI1, selection);
        } else if (TextUtils.equals(preference.getKey(), HDMI2_SWITCH)) {
            mTvOptionSettingManager.setHdmi20Mode(HDMI2, selection);
        } else if (TextUtils.equals(preference.getKey(), HDMI3_SWITCH)) {
            mTvOptionSettingManager.setHdmi20Mode(HDMI3, selection);
        } else if (TextUtils.equals(preference.getKey(), HDMI4_SWITCH)) {
            mTvOptionSettingManager.setHdmi20Mode(HDMI4, selection);
        }
        return true;
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

    private String[] initswitchEntries() {
        String[] temp = new String[2];
        temp[0] = getActivity().getResources().getString(R.string.tv_settings_off);
        temp[1] = getActivity().getResources().getString(R.string.tv_settings_on);
        return temp;
    }

    private String[] initSwitchEntryValue() {
        String[] temp = new String[2];
        for (int i = 0; i < 2; i++) {
            temp[i] = String.valueOf(i);
        }
        return temp;
    }
}
