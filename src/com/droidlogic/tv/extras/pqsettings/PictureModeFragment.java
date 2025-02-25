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

package com.droidlogic.tv.extras.pqsettings;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import androidx.preference.SwitchPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.TwoStatePreference;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.media.tv.TvInputInfo;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.droidlogic.tv.extras.SettingsConstant;
import com.droidlogic.tv.extras.SettingsPreferenceFragment;
import com.droidlogic.tv.extras.MainFragment;
import com.droidlogic.tv.extras.R;
import static com.droidlogic.tv.extras.util.DroidUtils.logDebug;

import java.util.ArrayList;
import java.util.List;

public class PictureModeFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "PictureModeFragment";
    private static final String PQ_PICTURE_MODE = "pq_picture_mode";
    private static final String PQ_PICTURE_MODE_SDR = "pq_picture_mode_sdr";
    private static final String PQ_PICTURE_MODE_HDR10= "pq_picture_mode_hdr10";
    private static final String PQ_PICTURE_MODE_HDR10PLUS= "pq_picture_mode_hdr10plus";
    private static final String PQ_PICTURE_MODE_HLG = "pq_picture_mode_hlg";
    private static final String PQ_PICTURE_MODE_DOLBYVISION = "pq_picture_mode_dolbyvision";
    private static final String PQ_PICTURE_MODE_CVUA = "pq_picture_mode_cvua";

    private static String FLAG_CURRENT_SOURCE = "SDR";
    private boolean FLAG_PQ_PICTURE_MODE = false;
    private boolean FLAG_PQ_PICTURE_MODE_SDR = false;
    private boolean FLAG_PQ_PICTURE_MODE_HDR10= false;
    private boolean FLAG_PQ_PICTURE_MODE_HDR10PLUS= false;
    private boolean FLAG_PQ_PICTURE_MODE_HLG = false;
    private boolean FLAG_PQ_PICTURE_MODE_DOLBYVISION = false;
    private boolean FLAG_PQ_PICTURE_MODE_CVUA = false;

    private static final String PQ_CUSTOM = "pq_custom";
    private static final String PQ_AI_PQ = "ai_pq";
    private static final String PQ_ASPECT_RATIO = "pq_aspect_ratio";
    private static final String PQ_BACKLIGHT = "pq_backlight";
    private static final String PQ_ADVANCED = "pq_advanced";
    private static final String PQ_ALLRESET = "pq_allreset";

    private static final String CURRENT_DEVICE_ID = "current_device_id";
    private static final String TV_CURRENT_DEVICE_ID = "tv_current_device_id";
    private static final String DTVKIT_PACKAGE = "org.dtvkit.inputsource";

    private ListPreference mPicturemodesdrPref;
    private ListPreference mPicturemodehdr10Pref;
    private ListPreference mPicturemodePref;
    private ListPreference mPicturemodehdr10plusPref;
    private ListPreference mPicturemodehlgPref;
    private ListPreference mPicturemodedolbyvisionPref;
    private ListPreference mPicturemodecvuaPref;

    private PQSettingsManager mPQSettingsManager;

    public static PictureModeFragment newInstance() {
        return new PictureModeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mPQSettingsManager == null) {
            mPQSettingsManager = new PQSettingsManager(getActivity());
        }
        getCurrentSource();
        //Use the interface to get the current source
    }

    @Override
    public void onResume() {
        super.onResume();
        //Use the interface to get the current source
        mPicturemodePref = (ListPreference) findPreference(PQ_PICTURE_MODE);
        if (mPQSettingsManager == null) {
            mPQSettingsManager = new PQSettingsManager(getActivity());
        }
        getCurrentSource();

        if (mPQSettingsManager.isHdmiSource()) {
            mPicturemodePref.setEntries(setHdmiPicEntries());
            mPicturemodePref.setEntryValues(setHdmiPicEntryValues());
        }

        int currentPictureModeSource = mPQSettingsManager.getPictureModeSource();
        if (currentPictureModeSource == Current_Source_Type.PQ_PICTURE_MODE_SDR.toInt()) {
            if (mPQSettingsManager.isAtvSource()
                || mPQSettingsManager.isDtvSource()
                || mPQSettingsManager.isAvSource()
                || mPQSettingsManager.isMpegSource()
                || mPQSettingsManager.isNullSource()) {
                mPicturemodesdrPref.setEntries(R.array.pq_picture_mode_sdr_no_game_monitor_entries);
                mPicturemodesdrPref.setEntryValues(R.array.pq_picture_mode_sdr_no_game_monitor_entry_values);
            }
        } else if (currentPictureModeSource == Current_Source_Type.PQ_PICTURE_MODE_HDR10.toInt()) {
            if (mPQSettingsManager.isDtvSource()
                || mPQSettingsManager.isMpegSource()) {
                mPicturemodehdr10Pref.setEntries(R.array.pq_picture_mode_hdr10_no_game_entries);
                mPicturemodehdr10Pref.setEntryValues(R.array.pq_picture_mode_hdr10_no_game_entry_values);
            }
        }

        mPicturemodePref.setValue(mPQSettingsManager.getPictureModeStatus());

        int is_from_live_tv = getActivity().getIntent().getIntExtra("from_live_tv", 0);
        String currentInputInfoId = getActivity().getIntent().getStringExtra("current_tvinputinfo_id");
        boolean isTv = SettingsConstant.needDroidlogicTvFeature(getActivity());
        boolean hasMboxFeature = SettingsConstant.hasMboxFeature(getActivity());
        String curPictureMode = mPQSettingsManager.getPictureModeStatus();

        setPicturMode(isTv, curPictureMode);

        final Preference backlightPref = (Preference) findPreference(PQ_BACKLIGHT);
        if ((isTv && getActivity().getResources().getBoolean(R.bool.tv_pq_need_backlight)) ||
                (!isTv && getActivity().getResources().getBoolean(R.bool.box_pq_need_backlight)) ||
                (isTv && isDtvKitInput(currentInputInfoId))) {
            backlightPref.setSummary(mPQSettingsManager.getBacklightStatus() + "%");
        } else {
            backlightPref.setVisible(false);
        }

        final Preference pictureCustomerPref = (Preference) findPreference(PQ_CUSTOM);
        if (curPictureMode.equals(PQSettingsManager.STATUS_MONITOR) ||
            curPictureMode.equals(PQSettingsManager.STATUS_GAME)) {
            pictureCustomerPref.setVisible(false);
        } else {
            pictureCustomerPref.setVisible(true);
        }

        final ListPreference aspectratioPref = (ListPreference) findPreference(PQ_ASPECT_RATIO);
        if (is_from_live_tv == 1 || (isTv && getActivity().getResources().getBoolean(R.bool.tv_pq_need_aspect_ratio)) ||
                    (!isTv && getActivity().getResources().getBoolean(R.bool.box_pq_need_aspect_ratio))) {
            aspectratioPref.setValueIndex(mPQSettingsManager.getAspectRatioStatus());
        } else {
            aspectratioPref.setVisible(false);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View innerView = super.onCreateView(inflater, container, savedInstanceState);
        if (getActivity().getIntent().getIntExtra("from_live_tv", 0) == 1) {
            //MainFragment.changeToLiveTvStyle(innerView, getActivity());
        }
        return innerView;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pq_picture_mode, null);
        if (mPQSettingsManager == null) {
            mPQSettingsManager = new PQSettingsManager(getActivity());
        }

        getCurrentSource();
        int is_from_live_tv = getActivity().getIntent().getIntExtra("from_live_tv", 0);
        boolean isTv = SettingsConstant.needDroidlogicTvFeature(getActivity());
        boolean hasMboxFeature = SettingsConstant.hasMboxFeature(getActivity());
        String curPictureMode = mPQSettingsManager.getPictureModeStatus();
        mPicturemodePref = (ListPreference) findPreference(PQ_PICTURE_MODE);
        mPicturemodesdrPref = (ListPreference) findPreference(PQ_PICTURE_MODE_SDR);
        mPicturemodehdr10Pref = (ListPreference) findPreference(PQ_PICTURE_MODE_HDR10);
        mPicturemodehdr10plusPref = (ListPreference) findPreference(PQ_PICTURE_MODE_HDR10PLUS);
        mPicturemodehlgPref = (ListPreference) findPreference(PQ_PICTURE_MODE_HLG);
        mPicturemodedolbyvisionPref = (ListPreference) findPreference(PQ_PICTURE_MODE_DOLBYVISION);
        mPicturemodecvuaPref = (ListPreference) findPreference(PQ_PICTURE_MODE_CVUA);

        if (mPQSettingsManager.isHdmiSource()) {
            mPicturemodePref.setEntries(setHdmiPicEntries());
            mPicturemodePref.setEntryValues(setHdmiPicEntryValues());
        }

        logDebug(TAG, true, "curPictureMode:" + curPictureMode
                + " isTv:" + isTv + " isLiveTv:" + is_from_live_tv);
        setPicturMode(isTv, curPictureMode);

        final Preference pictureCustomerPref = (Preference) findPreference(PQ_CUSTOM);
        if (curPictureMode.equals(PQSettingsManager.STATUS_MONITOR) ||
            curPictureMode.equals(PQSettingsManager.STATUS_GAME)) {
            pictureCustomerPref.setVisible(false);
        } else {
            pictureCustomerPref.setVisible(true);
        }

        final ListPreference aspectratioPref = (ListPreference) findPreference(PQ_ASPECT_RATIO);
        if (is_from_live_tv == 1 || (isTv && getActivity().getResources().getBoolean(R.bool.tv_pq_need_aspect_ratio)) ||
                    (!isTv && getActivity().getResources().getBoolean(R.bool.box_pq_need_aspect_ratio))) {
            aspectratioPref.setValueIndex(mPQSettingsManager.getAspectRatioStatus());
            aspectratioPref.setOnPreferenceChangeListener(this);
        } else {
            aspectratioPref.setVisible(false);
        }

        final Preference aipqPref = (Preference) findPreference(PQ_AI_PQ);
        if (!mPQSettingsManager.hasAipqFunc() && !mPQSettingsManager.hasAisrFunc()) {
            aipqPref.setVisible(false);
        }

        final Preference backlightPref = (Preference) findPreference(PQ_BACKLIGHT);
        if ((isTv && getActivity().getResources().getBoolean(R.bool.tv_pq_need_backlight)) ||
                (!isTv && getActivity().getResources().getBoolean(R.bool.box_pq_need_backlight))) {
            backlightPref.setSummary(mPQSettingsManager.getBacklightStatus() + "%");
        } else {
            backlightPref.setVisible(false);
        }

        final Preference pictureAllResetPref = (Preference) findPreference(PQ_ALLRESET);
        if (curPictureMode.equals(PQSettingsManager.STATUS_MONITOR) ||
            curPictureMode.equals(PQSettingsManager.STATUS_GAME)) {
            pictureAllResetPref.setVisible(false);
        } else {
            pictureAllResetPref.setVisible(true);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        logDebug(TAG, false, "[onPreferenceTreeClick] preference.getKey() = " + preference.getKey());
        switch (preference.getKey()) {
            case PQ_PICTURE_MODE_SDR:
                if (mPQSettingsManager.STATUS_GAME.equals( mPQSettingsManager.getPictureModeStatus())
                        && null != mPQSettingsManager.getVRRModeName()) {
                    mPicturemodesdrPref.setTitle(getActivity().getResources().getString(R.string.pq_picture_mode_sdr)
                            + mPQSettingsManager.getVRRModeName());
                }
                break;
            case  PQ_PICTURE_MODE_HDR10:
                if (mPQSettingsManager.STATUS_GAME.equals( mPQSettingsManager.getPictureModeStatus())
                        && null != mPQSettingsManager.getVRRModeName()) {
                    mPicturemodehdr10Pref.setTitle(getActivity().getResources().getString(R.string.pq_picture_mode_hdr10)
                            + mPQSettingsManager.getVRRModeName());
                }
                break;
            case PQ_ALLRESET:
                Intent PQAllResetIntent = new Intent();
                PQAllResetIntent.setClassName(
                        "com.droidlogic.tv.extras",
                        "com.droidlogic.tv.extras.pqsettings.PQResetAllActivity");
                startActivity(PQAllResetIntent);
                break;
            default:
                break;

        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        logDebug(TAG, false, "[onPreferenceChange] preference.getKey() = " + preference.getKey()
                + ", newValue = " + newValue);
        getCurrentSource();
        if (FLAG_PQ_PICTURE_MODE &&
            TextUtils.equals(preference.getKey(), PQ_PICTURE_MODE)) {
            mPQSettingsManager.setPictureMode((String)newValue);
        } else if (FLAG_PQ_PICTURE_MODE_SDR
            && TextUtils.equals(preference.getKey(), PQ_PICTURE_MODE_SDR)) {
            mPQSettingsManager.setPictureMode((String)newValue);
            mPQSettingsManager.setPictureModeSDR((String)newValue);
        } else if (FLAG_PQ_PICTURE_MODE_HDR10
            && TextUtils.equals(preference.getKey(), PQ_PICTURE_MODE_HDR10)) {
            mPQSettingsManager.setPictureMode((String)newValue);
        } else if (FLAG_PQ_PICTURE_MODE_HDR10PLUS
            && TextUtils.equals(preference.getKey(), PQ_PICTURE_MODE_HDR10PLUS)) {
            mPQSettingsManager.setPictureMode((String)newValue);
        } else if (FLAG_PQ_PICTURE_MODE_HLG
            && TextUtils.equals(preference.getKey(), PQ_PICTURE_MODE_HLG)) {
            mPQSettingsManager.setPictureMode((String)newValue);
        } else if (FLAG_PQ_PICTURE_MODE_DOLBYVISION
            && TextUtils.equals(preference.getKey(), PQ_PICTURE_MODE_DOLBYVISION)) {
            mPQSettingsManager.setPictureMode((String)newValue);
        } else if (FLAG_PQ_PICTURE_MODE_CVUA
            && TextUtils.equals(preference.getKey(), PQ_PICTURE_MODE_CVUA)) {
            mPQSettingsManager.setPictureMode((String)newValue);
        } else if (TextUtils.equals(preference.getKey(), PQ_ASPECT_RATIO)) {
            final int selection = Integer.parseInt((String)newValue);
            mPQSettingsManager.setAspectRatio(selection);
        }
        return true;
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

    private final int[] HDMI_PIC_RES = {
            R.string.pq_standard,
            R.string.pq_vivid,
            R.string.pq_soft,
            R.string.pq_sport,
            R.string.pq_movie,
            R.string.pq_monitor,
            R.string.pq_game,
            R.string.pq_user};
    private final String[] HDMI_PIC_MODE = {
            PQSettingsManager.STATUS_STANDARD,
            PQSettingsManager.STATUS_VIVID,
            PQSettingsManager.STATUS_SOFT,
            PQSettingsManager.STATUS_SPORT,
            PQSettingsManager.STATUS_MOVIE,
            PQSettingsManager.STATUS_MONITOR,
            PQSettingsManager.STATUS_GAME,
            PQSettingsManager.STATUS_USER};

    private String[] setHdmiPicEntries() {
        String[] temp = null;//new String[HDMI_PIC_RES.length];
        List<String> list = new ArrayList<String>();
        if (mPQSettingsManager == null) {
            mPQSettingsManager = new PQSettingsManager(getActivity());
        }
        if (mPQSettingsManager.isHdmiSource()) {
            for (int i = 0; i < HDMI_PIC_RES.length; i++) {
                list.add(getString(HDMI_PIC_RES[i]));
            }
        }
        temp = (String[])list.toArray(new String[list.size()]);

        return temp;
    }

    private String[] setHdmiPicEntryValues() {
        String[] temp = null;//new String[HDMI_PIC_MODE.length];
        List<String> list = new ArrayList<String>();
        if (mPQSettingsManager == null) {
            mPQSettingsManager = new PQSettingsManager(getActivity());
        }
        if (mPQSettingsManager.isHdmiSource()) {
            for (int i = 0; i < HDMI_PIC_MODE.length; i++) {
                list.add(HDMI_PIC_MODE[i]);
            }
        }
        temp = (String[])list.toArray(new String[list.size()]);

        return temp;
    }

    private static boolean isDtvKitInput(String inputId) {
        boolean result = false;
        if (inputId != null && inputId.startsWith(DTVKIT_PACKAGE)) {
            result = true;
        }
        logDebug(TAG, false, "isDtvKitInput result = " + result);
        return result;
    }

    public enum Current_Source_Type {
        PQ_PICTURE_MODE(0),
        PQ_PICTURE_MODE_HDR10(1),
        PQ_PICTURE_MODE_HDR10PLUS(2),
        PQ_PICTURE_MODE_DOLBYVISION(3),
        PQ_PICTURE_MODE_HLG(5),
        PQ_PICTURE_MODE_SDR(6),
        PQ_PICTURE_MODE_CVUA(10);

        private int val;

        Current_Source_Type(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    private void setPicturMode(boolean isTv, String curPictureMode){
        if ((FLAG_PQ_PICTURE_MODE && isTv && getActivity().getResources().getBoolean(R.bool.tv_pq_need_picture_mode)) ||
                (FLAG_PQ_PICTURE_MODE && !isTv && getActivity().getResources().getBoolean(R.bool.box_pq_need_picture_mode))) {
            mPicturemodePref.setValue(curPictureMode);
            mPicturemodePref.setVisible(true);
            mPicturemodePref.setOnPreferenceChangeListener(this);
        } else {
            mPicturemodePref.setVisible(false);
        }

        if ((FLAG_PQ_PICTURE_MODE_SDR && isTv && getActivity().getResources().getBoolean(R.bool.tv_pq_need_picture_mode)) ||
                (FLAG_PQ_PICTURE_MODE_SDR && !isTv && getActivity().getResources().getBoolean(R.bool.box_pq_need_picture_mode))) {
            if (mPQSettingsManager.STATUS_GAME.equals(curPictureMode) && null != mPQSettingsManager.getVRRModeName()) {
                mPicturemodesdrPref.setTitle(getActivity().getResources().getString(R.string.pq_picture_mode_sdr)
                        + mPQSettingsManager.getVRRModeName());
            }
            mPicturemodesdrPref.setValue(curPictureMode);
            mPicturemodesdrPref.setVisible(true);
            mPicturemodesdrPref.setOnPreferenceChangeListener(this);
        } else {
            mPicturemodesdrPref.setVisible(false);
        }

        if ((FLAG_PQ_PICTURE_MODE_HDR10 && isTv && getActivity().getResources().getBoolean(R.bool.tv_pq_need_picture_mode)) ||
                (FLAG_PQ_PICTURE_MODE_HDR10 && !isTv && getActivity().getResources().getBoolean(R.bool.box_pq_need_picture_mode))) {
            if (mPQSettingsManager.STATUS_GAME.equals(curPictureMode) && null != mPQSettingsManager.getVRRModeName()) {
                mPicturemodehdr10Pref.setTitle(getActivity().getResources().getString(R.string.pq_picture_mode_hdr10)
                        + mPQSettingsManager.getVRRModeName());
            }
            mPicturemodehdr10Pref.setValue(curPictureMode);
            mPicturemodehdr10Pref.setVisible(true);
            mPicturemodehdr10Pref.setOnPreferenceChangeListener(this);
        } else {
            mPicturemodehdr10Pref.setVisible(false);
        }

        if ((FLAG_PQ_PICTURE_MODE_HDR10PLUS && isTv && getActivity().getResources().getBoolean(R.bool.tv_pq_need_picture_mode)) ||
                (FLAG_PQ_PICTURE_MODE_HDR10PLUS && !isTv && getActivity().getResources().getBoolean(R.bool.box_pq_need_picture_mode))) {
            mPicturemodehdr10plusPref.setValue(curPictureMode);
            mPicturemodehdr10plusPref.setVisible(true);
            mPicturemodehdr10plusPref.setOnPreferenceChangeListener(this);
        } else {
            mPicturemodehdr10plusPref.setVisible(false);
        }

        if ((FLAG_PQ_PICTURE_MODE_HLG && isTv && getActivity().getResources().getBoolean(R.bool.tv_pq_need_picture_mode)) ||
                (FLAG_PQ_PICTURE_MODE_HLG && !isTv && getActivity().getResources().getBoolean(R.bool.box_pq_need_picture_mode))) {
            mPicturemodehlgPref.setValue(curPictureMode);
            mPicturemodehlgPref.setVisible(true);
            mPicturemodehlgPref.setOnPreferenceChangeListener(this);
        } else {
            mPicturemodehlgPref.setVisible(false);
        }

        if ((FLAG_PQ_PICTURE_MODE_DOLBYVISION && isTv && getActivity().getResources().getBoolean(R.bool.tv_pq_need_picture_mode)) ||
                (FLAG_PQ_PICTURE_MODE_DOLBYVISION && !isTv && getActivity().getResources().getBoolean(R.bool.box_pq_need_picture_mode))) {
            mPicturemodedolbyvisionPref.setValue(curPictureMode);
            mPicturemodedolbyvisionPref.setVisible(true);
            mPicturemodedolbyvisionPref.setOnPreferenceChangeListener(this);
        } else {
            mPicturemodedolbyvisionPref.setVisible(false);
        }

        if ((FLAG_PQ_PICTURE_MODE_CVUA && isTv && getActivity().getResources().getBoolean(R.bool.tv_pq_need_picture_mode)) ||
                (FLAG_PQ_PICTURE_MODE_CVUA && !isTv && getActivity().getResources().getBoolean(R.bool.box_pq_need_picture_mode))) {
            mPicturemodecvuaPref.setValue(curPictureMode);
            mPicturemodecvuaPref.setVisible(true);
            mPicturemodecvuaPref.setOnPreferenceChangeListener(this);
        } else {
            mPicturemodecvuaPref.setVisible(false);
        }
    }

    private void getCurrentSource() {
        ////Use the interface to get the current source
        int currentPictureModeSource = mPQSettingsManager.getPictureModeSource();
        logDebug(TAG, false, "currentPictureModeSource: " + currentPictureModeSource);
        if (currentPictureModeSource == Current_Source_Type.PQ_PICTURE_MODE_SDR.toInt()) {
            FLAG_PQ_PICTURE_MODE_SDR = true;
            FLAG_PQ_PICTURE_MODE_HDR10= false;
            FLAG_PQ_PICTURE_MODE_HDR10PLUS= false;
            FLAG_PQ_PICTURE_MODE_HLG = false;
            FLAG_PQ_PICTURE_MODE_DOLBYVISION = false;
            FLAG_PQ_PICTURE_MODE_CVUA = false;
        } else if (currentPictureModeSource == Current_Source_Type.PQ_PICTURE_MODE_HDR10.toInt()) {
            FLAG_PQ_PICTURE_MODE_SDR = false;
            FLAG_PQ_PICTURE_MODE_HDR10= true;
            FLAG_PQ_PICTURE_MODE_HDR10PLUS= false;
            FLAG_PQ_PICTURE_MODE_HLG = false;
            FLAG_PQ_PICTURE_MODE_DOLBYVISION = false;
            FLAG_PQ_PICTURE_MODE_CVUA = false;
        } else if (currentPictureModeSource == Current_Source_Type.PQ_PICTURE_MODE_HDR10PLUS.toInt()) {
            FLAG_PQ_PICTURE_MODE_SDR = false;
            FLAG_PQ_PICTURE_MODE_HDR10= false;
            FLAG_PQ_PICTURE_MODE_HDR10PLUS= true;
            FLAG_PQ_PICTURE_MODE_HLG = false;
            FLAG_PQ_PICTURE_MODE_DOLBYVISION = false;
            FLAG_PQ_PICTURE_MODE_CVUA = false;
        } else if (currentPictureModeSource == Current_Source_Type.PQ_PICTURE_MODE_HLG.toInt()) {
            FLAG_PQ_PICTURE_MODE_SDR = false;
            FLAG_PQ_PICTURE_MODE_HDR10= false;
            FLAG_PQ_PICTURE_MODE_HDR10PLUS= false;
            FLAG_PQ_PICTURE_MODE_HLG = true;
            FLAG_PQ_PICTURE_MODE_DOLBYVISION = false;
            FLAG_PQ_PICTURE_MODE_CVUA = false;
        } else if (currentPictureModeSource == Current_Source_Type.PQ_PICTURE_MODE_DOLBYVISION.toInt()) {
            FLAG_PQ_PICTURE_MODE_SDR = false;
            FLAG_PQ_PICTURE_MODE_HDR10= false;
            FLAG_PQ_PICTURE_MODE_HDR10PLUS= false;
            FLAG_PQ_PICTURE_MODE_HLG = false;
            FLAG_PQ_PICTURE_MODE_DOLBYVISION = true;
            FLAG_PQ_PICTURE_MODE_CVUA = false;
        } else if (currentPictureModeSource == Current_Source_Type.PQ_PICTURE_MODE_CVUA.toInt()) {
            FLAG_PQ_PICTURE_MODE_SDR = false;
            FLAG_PQ_PICTURE_MODE_HDR10= false;
            FLAG_PQ_PICTURE_MODE_HDR10PLUS= false;
            FLAG_PQ_PICTURE_MODE_HLG = false;
            FLAG_PQ_PICTURE_MODE_DOLBYVISION = false;
            FLAG_PQ_PICTURE_MODE_CVUA = true;
        } else {
            FLAG_PQ_PICTURE_MODE = true;
            FLAG_PQ_PICTURE_MODE_SDR = false;
            FLAG_PQ_PICTURE_MODE_HDR10= false;
            FLAG_PQ_PICTURE_MODE_HDR10PLUS= false;
            FLAG_PQ_PICTURE_MODE_HLG = false;
            FLAG_PQ_PICTURE_MODE_DOLBYVISION = false;
            FLAG_PQ_PICTURE_MODE_CVUA = false;
        }
    }

}
