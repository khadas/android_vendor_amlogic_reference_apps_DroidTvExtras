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

package com.droidlogic.tv.extras.tvsource;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.hardware.hdmi.HdmiDeviceInfo;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.TwoStatePreference;
import android.text.TextUtils;
import android.widget.ListAdapter;

import com.droidlogic.app.DataProviderManager;
import com.droidlogic.tv.extras.SettingsConstant;
import com.droidlogic.tv.extras.SettingsPreferenceFragment;
import com.droidlogic.tv.extras.overlay.FlavorUtils;
import com.droidlogic.tv.extras.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.droidlogic.app.tv.TvControlManager;
import com.droidlogic.app.tv.DroidLogicTvUtils;
import com.droidlogic.app.tv.TvScanConfig;
import com.droidlogic.app.tv.ChannelInfo;

import android.hardware.hdmi.HdmiControlManager;
import android.hardware.hdmi.HdmiTvClient;
import android.media.tv.TvInputHardwareInfo;

import java.util.ArrayList;

import com.droidlogic.app.SystemControlManager;
import static com.droidlogic.tv.extras.util.DroidUtils.logDebug;

public class TvSourceFragment extends SettingsPreferenceFragment {

    private static final String TAG = "TvSourceFragment";
    private static final boolean DEBUG = true;

    private static final int MODE_GLOBAL = 0;
    private static final int MODE_LIVE_TV = 1;

    private static final String COMMANDACTION = "action.startlivetv.settingui";
    private static final String PACKAGE_DROIDLOGIC_TVINPUT = "com.droidlogic.tvinput";
    private static final String PACKAGE_DROIDLOGIC_DTVKIT = "com.droidlogic.dtvkit.inputsource";
    private static final String PACKAGE_GOOGLE_VIDEOS = "com.google.android.videos";
    private static final String AMATI_FEATURE = "com.google.android.feature.AMATI_EXPERIENCE";
    private static final String DATA_FROM_TV_APP = "from_live_tv";
    private static final String DATA_REQUEST_PACKAGE = "requestpackage";

    private static final String INPUT_SOURCE_GOOGLE_HOME_KEY = "home";
    private static final String INPUT_ADTV = "ADTV";
    private static final String INPUT_ATV = "ATV";
    private static final String INPUT_DTV = "DTV";
    private static final String INPUT_AV = "AV";
    private static final String INPUT_HDMI = "HDMI";
    private static final String INPUT_HDMI_LOWER = "Hdmi";

    private final String DTVKITSOURCE = "com.droidlogic.dtvkit.inputsource/.DtvkitTvInput/HW19";

    private static final int RESULT_OK = -1;
    private static final int LOGICAL_ADDRESS_AUDIO_SYSTEM = 5;

    private static TvInputManager mTvInputManager;
    private TvControlManager mTvControlManager;
    private HdmiControlManager mHdmiControlManager;

    private final InputsComparator mComparator = new InputsComparator();
    private Context mContext;
    private String mPreSource;
    private String mCurSource;

    private boolean mFromTvApp;
    private String mStartPackage;

    private HdmiTvClient mTvClient;

    // if Fragment has no nullary constructor, it might throw InstantiationException, so add this constructor.
    // For more details, you can visit http://blog.csdn.net/xplee0576/article/details/43057633 .
    public TvSourceFragment() {
    }

    public TvSourceFragment(Context context) {
        mContext = context;
        if (mTvInputManager == null) {
            mTvInputManager = context.getSystemService(TvInputManager.class);
        }
        mTvControlManager = TvControlManager.getInstance();
        mHdmiControlManager = (HdmiControlManager) context.getSystemService(Context.HDMI_CONTROL_SERVICE);
        if (mHdmiControlManager != null) {
            mTvClient = mHdmiControlManager.getTvClient();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Intent intent = null;

        if (mContext != null) {
            intent = getActivity().getIntent();
        }

        logDebug(TAG, false, "onCreatePreferences  intent= " + intent);
        if (intent != null) {
            mFromTvApp = intent.getIntExtra(DATA_FROM_TV_APP, MODE_GLOBAL) == MODE_LIVE_TV;
            mStartPackage = intent.getStringExtra(DATA_REQUEST_PACKAGE);
        }

        updatePreferenceFragment();
    }

    public void calculatePreSrcToCurSrc(Preference preference) {
        String currentInputId = DroidLogicTvUtils.getCurrentInputId(mContext);
        if (currentInputId != null) {
            if (currentInputId.contains(INPUT_ADTV)) {
                if (DroidLogicTvUtils.isATV(mContext)) {
                    mPreSource = INPUT_ATV;
                } else {
                    mPreSource = INPUT_DTV;
                }
            } else if (currentInputId.contains(INPUT_AV)) {
                mPreSource = INPUT_AV;
            } else if (currentInputId.contains(INPUT_HDMI_LOWER)) {
                mPreSource = INPUT_HDMI;
            }
        }

        String inputId = preference.getKey();
        if (!TextUtils.isEmpty(inputId) && inputId.contains(INPUT_HDMI_LOWER)) {
            mCurSource = INPUT_HDMI;
        } else if (TextUtils.regionMatches(preference.getTitle(), 0, INPUT_AV, 0, 2)) {
            mCurSource = INPUT_AV;
        } else if (TextUtils.regionMatches(preference.getTitle(), 0, INPUT_ATV, 0, 3)) {
            mCurSource = INPUT_ATV;
        } else if (TextUtils.regionMatches(preference.getTitle(), 0, INPUT_DTV, 0, 3)) {
            mCurSource = INPUT_DTV;
        }
        logDebug(TAG, true, "onPreferenceTreeClick SwitchSourceTime PreSource - CurSource " + mPreSource + "-" + mCurSource);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        calculatePreSrcToCurSrc(preference);
        float Time = (float) SystemClock.uptimeMillis() / 1000;
        logDebug(TAG, true, "onPreferenceTreeClick SwitchSourceTime = " + Time);
        final Preference sourcePreference = preference;
        if (sourcePreference.getKey().equals(INPUT_SOURCE_GOOGLE_HOME_KEY)) {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            getPreferenceManager().getContext().startActivity(homeIntent);
            ((Activity) mContext).finish();
            return true;
        }
        List<TvInputInfo> inputList = mTvInputManager.getTvInputList();
        for (TvInputInfo input : inputList) {
            if (sourcePreference.getKey().equals(input.getId())) {
                logDebug(TAG, false, "onPreferenceTreeClick:  info=" + input);
                DroidLogicTvUtils.setCurrentInputId(mContext, input.getId());
                if (!input.isPassthroughInput()) {
                    DroidLogicTvUtils.setSearchInputId(mContext, input.getId(), false);
                    if (TextUtils.equals(sourcePreference.getTitle(), mContext.getResources().getString(R.string.input_atv))) {
                        DroidLogicTvUtils.setSearchType(mContext, TvScanConfig.TV_SEARCH_TYPE.get(TvScanConfig.TV_SEARCH_TYPE_ATV_INDEX));
                    } else if (TextUtils.equals(sourcePreference.getTitle(), mContext.getResources().getString(R.string.input_dtv))) {
                        String country = DroidLogicTvUtils.getCountry(mContext);
                        ArrayList<String> dtvList = TvScanConfig.GetTvDtvSystemList(country);
                        DroidLogicTvUtils.setSearchType(mContext, dtvList.get(0));
                    }
                }

                Settings.System.putInt(mContext.getContentResolver(), DroidLogicTvUtils.TV_CURRENT_DEVICE_ID,
                        DroidLogicTvUtils.getHardwareDeviceId(input));

                SystemControlManager mSystemControlManager = SystemControlManager.getInstance();
                if (DTVKITSOURCE.equals(input.getId())) {//DTVKIT SOURCE
                    logDebug(TAG, false, "DtvKit source");
                    mSystemControlManager.SetDtvKitSourceEnable(1);
                } else {
                    logDebug(TAG, false, "Not DtvKit source");
                    mSystemControlManager.SetDtvKitSourceEnable(0);
                }

                if (mFromTvApp) {
                    Intent intent = new Intent();
                    intent.setAction(COMMANDACTION);
                    intent.putExtra("from_tv_source", true);
                    intent.putExtra(TvInputInfo.EXTRA_INPUT_ID, input.getId());
                    getActivity().sendBroadcast(intent);
                } else {
                    Intent intent = new Intent(TvInputManager.ACTION_SETUP_INPUTS);
                    intent.putExtra("from_tv_source", true);
                    intent.putExtra(TvInputInfo.EXTRA_INPUT_ID, input.getId());
                    if (mStartPackage != null && mStartPackage.equals("com.droidlogic.mboxlauncher")) {
                        ((Activity) mContext).setResult(RESULT_OK, intent);
                    } else {
                        getPreferenceManager().getContext().startActivity(intent);
                    }
                }
                ((Activity) mContext).finish();
                break;
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void updatePreferenceFragment() {
        final Context themedContext = getPreferenceManager().getContext();
        final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(
                themedContext);
        screen.setTitle(R.string.tv_source);
        setPreferenceScreen(screen);

        try {
            List<TvInputInfo> inputList = mTvInputManager.getTvInputList();
            Collections.sort(inputList, mComparator);
            List<Preference> preferenceList = new ArrayList<Preference>();
            List<HdmiDeviceInfo> hdmiList = getHdmiList();
            HdmiDeviceInfo audioSystem = getOrigHdmiDevice(LOGICAL_ADDRESS_AUDIO_SYSTEM, hdmiList);

            boolean isGTV = themedContext.getPackageManager().hasSystemFeature(AMATI_FEATURE);
            boolean calledByIntent = false;
            if (mContext != null) {
                Intent intent = ((Activity) mContext).getIntent();
                logDebug(TAG, false, "updatePreferenceFragment " + intent);
                if (intent != null) {
                    if (TextUtils.equals(intent.getAction(), "com.android.tv.action.VIEW_INPUTS")) {
                        calledByIntent = true;
                    }
                }
            }
            if (isGTV && calledByIntent) {
                Preference sourcePreference = new Preference(themedContext);
                sourcePreference.setKey(INPUT_SOURCE_GOOGLE_HOME_KEY);
                sourcePreference.setPersistent(false);
                sourcePreference.setIcon(R.drawable.ic_home);
                if (isBasicMode(themedContext)) {
                    sourcePreference.setTitle(R.string.channels_and_inputs_home_title);
                } else {
                    sourcePreference.setTitle(R.string.channels_and_inputs_home_google_title);
                }
                preferenceList.add(sourcePreference);
            }
            for (TvInputInfo input : inputList) {
                logDebug(TAG, false, "updatePreferenceFragment input " + input + "-->" + input.getType());
                if (input.isHidden(themedContext)) {
                    logDebug(TAG, false, "updatePreferenceFragment this input hidden");
                    continue;
                }
                if (input.isPassthroughInput() && input.getParentId() != null) {
                    // DroidSettings always show the fixed hdmi port related sources, even though
                    // there are no devices connected, so we should only care about the parent
                    // sources.
                    continue;
                }
                if (isGTV && calledByIntent && !input.isHardwareInput()) {
                    logDebug(TAG, false, "updatePreferenceFragment, Input switcher don't show " + input);
                    continue;
                }
                Preference sourcePreference = new Preference(themedContext);
                sourcePreference.setKey(input.getId());
                sourcePreference.setPersistent(false);
                sourcePreference.setIcon(getIcon(input, isInputEnabled(input)));
                sourcePreference.setTitle(getTitle(themedContext, input, audioSystem, hdmiList));
                preferenceList.add(sourcePreference);
                addSpecificDtv(themedContext, input, preferenceList);
            }
            for (Preference sourcePreference : preferenceList) {
                screen.addPreference(sourcePreference);
            }
        } catch (Exception e) {
            logDebug(TAG, true, "inputList is " + e.getMessage());
        }

    }

    public boolean isBasicMode(Context context) {
        final String SETTINGS_PACKAGE_NAME = "com.android.tv.settings";
        String providerUriString = "";
        try {
            Resources resources = context.getPackageManager()
                    .getResourcesForApplication(SETTINGS_PACKAGE_NAME);
            int id = resources.getIdentifier("basic_mode_provider_uri", "string", SETTINGS_PACKAGE_NAME);
            if (id != 0) {
                providerUriString = resources.getString(id);
            }
        } catch (Exception e) {
            return false;
        }
        if (TextUtils.isEmpty(providerUriString)) {
            logDebug(TAG, false, "ContentProvider for basic mode is undefined.");
            return false;
        }
        // The string "offline_mode" is a static protocol and should not be changed in general.
        final String KEY_BASIC_MODE = "offline_mode";
        try {
            Uri contentUri = Uri.parse(providerUriString);
            Cursor cursor = context.getContentResolver().query(contentUri, null, null, null);
            if (cursor != null && cursor.getCount() != 0) {
                cursor.moveToFirst();
                String basicMode = cursor.getString(cursor.getColumnIndex(KEY_BASIC_MODE));
                return "1".equals(basicMode);
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            logDebug(TAG, true, "Unable to query the ContentProvider for basic mode.");
            return false;
        }
        return false;
    }

    private void addSpecificDtv(Context themedContext, TvInputInfo input, List<Preference> preferenceList) {
        if (DroidLogicTvUtils.isChina(themedContext)
                && input.getType() == TvInputInfo.TYPE_TUNER
                && PACKAGE_DROIDLOGIC_TVINPUT.equals(input.getServiceInfo().packageName)) {
            Preference sourcePreferenceDtv = new Preference(themedContext);
            sourcePreferenceDtv.setKey(input.getId());
            sourcePreferenceDtv.setPersistent(false);
            sourcePreferenceDtv.setIcon(R.drawable.ic_dtv_connected);
            sourcePreferenceDtv.setTitle(R.string.input_dtv);
            if (mTvControlManager.GetHotPlugDetectEnableStatus()) {
                sourcePreferenceDtv.setEnabled(isInputEnabled(input));
            }
            preferenceList.add(sourcePreferenceDtv);
        }
    }

    private CharSequence getTitle(Context themedContext, TvInputInfo input, HdmiDeviceInfo audioSystem, List<HdmiDeviceInfo> hdmiList) {
        CharSequence title = "";
        CharSequence label = input.loadLabel(themedContext);
        CharSequence customLabel = input.loadCustomLabel(themedContext);
        if (TextUtils.isEmpty(customLabel) || customLabel.equals(label)) {
            title = label;
        } else {
            title = customLabel;
        }
        logDebug(TAG, true, "getTitle default " + title
                + ", label = " + label + ", customLabel = " + customLabel);
        if (input.isPassthroughInput()) {
            int portId = DroidLogicTvUtils.getPortId(input);
            if (audioSystem != null && audioSystem.getPortId() == portId) {
                // there is an audiosystem connected.
                title = audioSystem.getDisplayName();
            } else {
                HdmiDeviceInfo hdmiDevice = getOrigHdmiDeviceByPort(portId, hdmiList);
                if (hdmiDevice != null) {
                    // there is a playback connected.
                    title = hdmiDevice.getDisplayName();
                }
            }
        } else if (input.getType() == TvInputInfo.TYPE_TUNER) {
            title = getTitleForTuner(themedContext, input.getServiceInfo().packageName, title, input);
        } else if (TextUtils.isEmpty(title)) {
            title = input.getServiceInfo().name;
        }
        logDebug(TAG, true, "getTitle " + title);
        return title;
    }

    private CharSequence getTitleForTuner(Context themedContext, String packageName, CharSequence label, TvInputInfo input) {
        CharSequence title = label;
        if (PACKAGE_DROIDLOGIC_TVINPUT.equals(packageName)) {
            title = themedContext.getString(DroidLogicTvUtils.isChina(themedContext) ? R.string.input_atv : R.string.input_long_label_for_tuner);
        } else if (TextUtils.isEmpty(label)) {
            if (PACKAGE_DROIDLOGIC_DTVKIT.equals(packageName)) {
                title = themedContext.getString(R.string.input_dtv_kit);
            } else if (PACKAGE_GOOGLE_VIDEOS.equals(packageName)) {
                title = themedContext.getString(R.string.input_google_channel);
            } else {
                title = input.getServiceInfo().name;
            }
        }

        logDebug(TAG, true, "getTitleForTuner title " + title + " for package " + packageName);
        return title;
    }

    private List<HdmiDeviceInfo> getHdmiList() {
        if (mTvClient == null) {
            logDebug(TAG, true, "mTvClient null!");
            return null;
        }
        return mTvClient.getDeviceList();
    }

    /**
     * The update of hdmi device info will not notify TvInputManagerService now.
     */
    private HdmiDeviceInfo getOrigHdmiDeviceByPort(int portId, List<HdmiDeviceInfo> hdmiList) {
        if (hdmiList == null) {
            logDebug(TAG, true, "mTvInputManager or mTvClient maybe null");
            return null;
        }
        for (HdmiDeviceInfo info : hdmiList) {
            if (info.getPortId() == portId) {
                return info;
            }
        }
        return null;
    }

    private HdmiDeviceInfo getOrigHdmiDevice(int logicalAddress, List<HdmiDeviceInfo> hdmiList) {
        if (hdmiList == null) {
            logDebug(TAG, true, "mTvInputManager or mTvClient maybe null");
            return null;
        }
        for (HdmiDeviceInfo info : hdmiList) {
            if ((info.getLogicalAddress() == logicalAddress)) {
                return info;
            }
        }
        return null;
    }

    private boolean isInputEnabled(TvInputInfo input) {
        HdmiDeviceInfo hdmiInfo = input.getHdmiDeviceInfo();
        if (hdmiInfo != null && !TextUtils.isEmpty(input.getParentId())) {
            logDebug(TAG, false, "isInputEnabled:  hdmiInfo=" + hdmiInfo);
            return true;
        }

        int deviceId = DroidLogicTvUtils.getHardwareDeviceId(input);
        logDebug(TAG, false, "===== getHardwareDeviceId:tvInputId = " + input.getId());
        logDebug(TAG, false, "===== deviceId : " + deviceId);
        TvControlManager.SourceInput tvSourceInput = DroidLogicTvUtils.parseTvSourceInputFromDeviceId(deviceId);
        int connectStatus = -1;
        if (tvSourceInput != null) {
            connectStatus = mTvControlManager.GetSourceConnectStatus(tvSourceInput);
        } else {
            logDebug(TAG, false, "===== cannot find tvSourceInput");
        }

        return !input.isPassthroughInput() || 1 == connectStatus || deviceId == DroidLogicTvUtils.DEVICE_ID_SPDIF;
    }

    private class InputsComparator implements Comparator<TvInputInfo> {
        @Override
        public int compare(TvInputInfo lhs, TvInputInfo rhs) {
            if (lhs == null) {
                return (rhs == null) ? 0 : 1;
            }
            if (rhs == null) {
                return -1;
            }

           /* boolean enabledL = isInputEnabled(lhs);
            boolean enabledR = isInputEnabled(rhs);
            if (enabledL != enabledR) {
                return enabledL ? -1 : 1;
            }*/

            int priorityL = getPriority(lhs);
            int priorityR = getPriority(rhs);
            if (priorityL != priorityR) {
                return priorityR - priorityL;
            }

            String customLabelL = (String) lhs.loadCustomLabel(getContext());
            String customLabelR = (String) rhs.loadCustomLabel(getContext());
            if (!TextUtils.equals(customLabelL, customLabelR)) {
                customLabelL = customLabelL == null ? "" : customLabelL;
                customLabelR = customLabelR == null ? "" : customLabelR;
                return customLabelL.compareToIgnoreCase(customLabelR);
            }

            String labelL = (String) lhs.loadLabel(getContext());
            String labelR = (String) rhs.loadLabel(getContext());
            labelL = labelL == null ? "" : labelL;
            labelR = labelR == null ? "" : labelR;
            return labelL.compareToIgnoreCase(labelR);
        }

        private int getPriority(TvInputInfo info) {
            switch (info.getType()) {
                case TvInputInfo.TYPE_TUNER:
                    return 9;
                case TvInputInfo.TYPE_HDMI:
                    HdmiDeviceInfo hdmiInfo = info.getHdmiDeviceInfo();
                    if (hdmiInfo != null && hdmiInfo.isCecDevice()) {
                        return 8;
                    }
                    return 7;
                case TvInputInfo.TYPE_DVI:
                    return 6;
                case TvInputInfo.TYPE_COMPONENT:
                    return 5;
                case TvInputInfo.TYPE_SVIDEO:
                    return 4;
                case TvInputInfo.TYPE_COMPOSITE:
                    return 3;
                case TvInputInfo.TYPE_DISPLAY_PORT:
                    return 2;
                case TvInputInfo.TYPE_VGA:
                    return 1;
                case TvInputInfo.TYPE_SCART:
                default:
                    return 0;
            }
        }
    }

    private int getIcon(TvInputInfo info, boolean isConnected) {
        int icon = R.drawable.ic_dtv_connected;
        if (info.isPassthroughInput()) {
            icon = getIconForPassthrough(info, isConnected);
        }
        return icon;
    }

    private int getIconForPassthrough(TvInputInfo info, boolean isConnected) {
        switch (info.getType()) {
            case TvInputInfo.TYPE_TUNER:
                if (isConnected) {
                    return DroidLogicTvUtils.isChina(mContext) ? R.drawable.ic_atv_connected : R.drawable.ic_atsc_connected;
                } else {
                    return DroidLogicTvUtils.isChina(mContext) ? R.drawable.ic_atv_disconnected : R.drawable.ic_atsc_disconnected;
                }
            case TvInputInfo.TYPE_HDMI:
                if (isConnected) {
                    return R.drawable.ic_hdmi_connected;
                } else {
                    return R.drawable.ic_hdmi_disconnected;
                }
            case TvInputInfo.TYPE_COMPOSITE:
                if (isConnected) {
                    return R.drawable.ic_av_connected;
                } else {
                    return R.drawable.ic_av_disconnected;
                }
            default:
                if (isConnected) {
                    return R.drawable.ic_spdif_connected;
                } else {
                    return R.drawable.ic_spdif_disconnected;
                }
        }
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

}