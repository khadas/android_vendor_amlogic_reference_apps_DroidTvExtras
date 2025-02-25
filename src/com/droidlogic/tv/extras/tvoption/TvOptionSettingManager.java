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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.TextUtils;
import android.os.SystemProperties;
import android.os.SystemClock;
import android.media.AudioManager;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.media.tv.TvInputManager;
import android.media.tv.TvInputInfo;
import android.media.tv.TvContract;
import android.media.tv.TvContract.Channels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.lang.reflect.Method;

import com.droidlogic.tv.extras.R;
import com.droidlogic.app.tv.ChannelInfo;
import com.droidlogic.app.tv.TvDataBaseManager;
import com.droidlogic.app.tv.TvInSignalInfo;
import com.droidlogic.app.tv.TvControlManager;
import com.droidlogic.app.tv.DroidLogicTvUtils;
import com.droidlogic.app.DataProviderManager;
import com.droidlogic.app.SystemControlManager;
import com.droidlogic.app.tv.TvChannelParams;
import com.droidlogic.app.DaylightSavingTime;
import com.droidlogic.tv.extras.TvSettingsActivity;
import static com.droidlogic.tv.extras.util.DroidUtils.logDebug;

public class TvOptionSettingManager {
    public static final int SET_DTMB = 0;
    public static final int SET_DVB_C = 1;
    public static final int SET_DVB_T = 2;
    public static final int SET_DVB_T2 = 3;
    public static final int SET_ATSC_T = 4;
    public static final int SET_ATSC_C = 5;
    public static final int SET_ISDB_T = 6;

    public static final String KEY_MENU_TIME = DroidLogicTvUtils.KEY_MENU_TIME;
    public static final int DEFAULT_MENU_TIME = DroidLogicTvUtils.DEFAULT_MENU_TIME;
    public static final String KEY_NO_SIGNAL_TIMEOUT = DroidLogicTvUtils.KEY_NO_SIGNAL_TIMEOUT;
    public static final String AUDIO_LATENCY = "vendor.media.dtv.passthrough.latencyms";

    public static final String STRING_NAME = "name";
    public static final String STRING_STATUS = "status";
    public static final String DTV_AUTOSYNC_TVTIME = "autosync_tvtime";

    public static final String TAG = "TvOptionSettingManager";

    private Resources mResources;
    private Context mContext;
    private ChannelInfo mCurrentChannel;
    private int mDeviceId;
    private String mInputInfoId = null;
    private TvDataBaseManager mTvDataBaseManager;
    private TvControlManager mTvControlManager;
    private TvControlManager.SourceInput mTvSourceInput;
    private SystemControlManager mSystemControlManager;
    private DaylightSavingTime mDaylightSavingTime = null;
    private AudioManager mAudioManager;

    public TvOptionSettingManager(Context context, boolean isOtherContext) {
        mContext = context;
        mResources = mContext.getResources();
        mSystemControlManager = SystemControlManager.getInstance();
        mAudioManager = (AudioManager) context.getSystemService(context.AUDIO_SERVICE);

        if (SystemProperties.getBoolean("persist.sys.daylight.control", false)) {
            mDaylightSavingTime = DaylightSavingTime.getInstance();
        }
        if (isOtherContext) {
            return;
        }
        mTvDataBaseManager = new TvDataBaseManager(mContext);
        mTvControlManager = TvControlManager.getInstance();
        mDeviceId = ((TvSettingsActivity) context).getIntent().getIntExtra("tv_current_device_id", -1);
        mTvSourceInput = DroidLogicTvUtils.parseTvSourceInputFromDeviceId(mDeviceId);
        mInputInfoId = ((TvSettingsActivity) context).getIntent().getStringExtra("current_tvinputinfo_id");
        mCurrentChannel = mTvDataBaseManager.getChannelInfo(TvContract.buildChannelUri(((TvSettingsActivity) context).getIntent().getLongExtra("current_channel_id", -1)));
    }

    public int getDtvTypeStatus() {
        String type = getDtvType();
        int ret = SET_ATSC_T;
        if (type != null) {
            logDebug(TAG, false, "getDtvTypeStatus = " + type);
            if (TextUtils.equals(type, TvContract.Channels.TYPE_DTMB)) {
                ret = SET_DTMB;
            } else if (TextUtils.equals(type, TvContract.Channels.TYPE_DVB_C)) {
                ret = SET_DVB_C;
            } else if (TextUtils.equals(type, TvContract.Channels.TYPE_DVB_T)) {
                ret = SET_DVB_T;
            } else if (TextUtils.equals(type, TvContract.Channels.TYPE_DVB_T2)) {
                ret = SET_DVB_T2;
            } else if (TextUtils.equals(type, TvContract.Channels.TYPE_ATSC_T)) {
                ret = SET_ATSC_T;
            } else if (TextUtils.equals(type, TvContract.Channels.TYPE_ATSC_C)) {
                ret = SET_ATSC_C;
            } else if (TextUtils.equals(type, TvContract.Channels.TYPE_ISDB_T)) {
                ret = SET_ISDB_T;
            }
            return ret;
        } else {
            ret = -1;
            return ret;
        }
    }

    public String getDtvType() {
        String type = DataProviderManager.getStringValue(mContext,
                DroidLogicTvUtils.TV_KEY_DTV_TYPE, null);
        return type;
    }

    public int getSoundChannelStatus() {
        int type = 0;
        if (mCurrentChannel != null) {
            type = mCurrentChannel.getAudioChannel();
        }
        if (type < 0 || type > 2) {
            type = 0;
        }
        logDebug(TAG, false, "getSoundChannelStatus = " + type);
        return type;
    }

    public ArrayList<HashMap<String, String>> getChannelInfoStatus() {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        TvControlManager.SourceInput_Type tvSource = DroidLogicTvUtils.parseTvSourceTypeFromDeviceId(mDeviceId);
        TvControlManager.SourceInput_Type virtualTvSource = tvSource;
        if (tvSource == TvControlManager.SourceInput_Type.SOURCE_TYPE_ADTV) {
            if (mCurrentChannel != null) {
                tvSource = DroidLogicTvUtils.parseTvSourceTypeFromSigType(DroidLogicTvUtils.getSigType(mCurrentChannel));
            }
            if (virtualTvSource == tvSource) {//no channels in adtv input, DTV for default.
                tvSource = TvControlManager.SourceInput_Type.SOURCE_TYPE_DTV;
            }
        }
        if (mCurrentChannel != null) {
            HashMap<String, String> item = new HashMap<String, String>();
            item.put(STRING_NAME, mResources.getString(R.string.channel_info_channel));
            item.put(STRING_STATUS, mCurrentChannel.getDisplayNameLocal());
            list.add(item);

            item = new HashMap<String, String>();
            item.put(STRING_NAME, mResources.getString(R.string.channel_info_frequency));
            item.put(STRING_STATUS, Integer.toString(mCurrentChannel.getFrequency() + mCurrentChannel.getFineTune()));
            list.add(item);

            if (tvSource == TvControlManager.SourceInput_Type.SOURCE_TYPE_DTV) {
                item = new HashMap<String, String>();
                item.put(STRING_NAME, mResources.getString(R.string.channel_info_type));
                item.put(STRING_STATUS, mCurrentChannel.getType());
                list.add(item);

                item = new HashMap<String, String>();
                item.put(STRING_NAME, mResources.getString(R.string.channel_info_service_id));
                item.put(STRING_STATUS, Integer.toString(mCurrentChannel.getServiceId()));
                list.add(item);

                item = new HashMap<String, String>();
                item.put(STRING_NAME, mResources.getString(R.string.channel_info_pcr_id));
                item.put(STRING_STATUS, Integer.toString(mCurrentChannel.getPcrPid()));
                list.add(item);
            }
        }
        return list;
    }

    public int getMenuTimeStatus() {
        int type = DataProviderManager.getIntValue(mContext, KEY_MENU_TIME, DEFAULT_MENU_TIME);
        logDebug(TAG, false, "getMenuTimeStatus = " + type);
        return type;
    }

    public int getSleepTimerStatus() {
        int time = DataProviderManager.getIntValue(mContext, DroidLogicTvUtils.PROP_DROID_TV_SLEEP_TIME, 0);
        logDebug(TAG, false, "getSleepTimerStatus:" + time);
        return time;
    }

    public int getNoSignalSleepTimeStatus() {
        int mode = DataProviderManager.getIntValue(mContext, KEY_NO_SIGNAL_TIMEOUT, DEFAULT_MENU_TIME);
        logDebug(TAG, false, "getNoSignalSleepTimeStatus = " + mode);
        return mode;
    }

    //0 1 ~ launcher livetv
    public int getStartupSettingStatus() {
        int type = DataProviderManager.getIntValue(mContext, DroidLogicTvUtils.TV_START_UP_ENTER_APP, 0);
        logDebug(TAG, false, "getStartupSettingStatus = " + type);
        if (type != 0) {
            type = 1;
        }
        return type;
    }

    // 0 1 ~ off on
    public int getDynamicBacklightStatus() {
        int switchVal = mSystemControlManager.GetDynamicBacklight();
        logDebug(TAG, false, "getDynamicBacklightStatus = " + switchVal);

        if (switchVal != 0) {
            switchVal = 1;
        }

        return switchVal;
    }

    // 0 1 ~ off on others on
    public int getADSwitchStatus() {
        int switchVal = DataProviderManager.getIntValue(mContext, DroidLogicTvUtils.TV_KEY_AD_SWITCH, 0);
        logDebug(TAG, false, "getADSwitchStatus = " + switchVal);
        if (switchVal != 0) {
            switchVal = 1;
        }
        return switchVal;
    }

    public int getVolumeCompensateStatus() {
        int value = 0;
        if (mCurrentChannel != null)
            value = mCurrentChannel.getAudioCompensation();
        else
            value = 0;
        return value;
    }

    public int getStaticFrameStatus() {
        //0:disable, 1:enable
        return mSystemControlManager.getStaticFrameStatus();
    }

    public int getScreenColorForSignalChange() {
        //0:black screen, 1:blue screen
        return mSystemControlManager.getScreenColorForSignalChange();
    }

    public int getADMixStatus() {
        int val = DataProviderManager.getIntValue(mContext, DroidLogicTvUtils.TV_KEY_AD_MIX, 50);
        logDebug(TAG, false, "getADMixStatus = " + val);
        return val;
    }

    public int[] getFourHdmi20Status() {
        int[] fourHdmiStatus = new int[4];
        TvControlManager.HdmiPortID[] allport = {TvControlManager.HdmiPortID.HDMI_PORT_1, TvControlManager.HdmiPortID.HDMI_PORT_2,
                TvControlManager.HdmiPortID.HDMI_PORT_3, TvControlManager.HdmiPortID.HDMI_PORT_4};
        for (int i = 0; i < allport.length; i++) {
            if (mTvControlManager.GetHdmiEdidVersion(allport[i]) == TvControlManager.HdmiEdidVer.HDMI_EDID_VER_20.toInt()) {
                fourHdmiStatus[i] = 1;
            } else {
                fourHdmiStatus[i] = 0;
            }
        }
        logDebug(TAG, false, "getFourHdmi20Status 1 to 4 " + fourHdmiStatus[0]
                + ", " + fourHdmiStatus[1] + ", " + fourHdmiStatus[2] + ", " + fourHdmiStatus[3]);
        return fourHdmiStatus;
    }

    public int getNumOfHdmi() {
        TvInputManager inputManager = (TvInputManager) mContext.getSystemService(Context.TV_INPUT_SERVICE);
        List<TvInputInfo> inputs = inputManager.getTvInputList();
        int num_hdmi = 0;
        for (TvInputInfo input : inputs) {
            logDebug(TAG, false, "input:" + input.toString());
            if (input.getId().contains("Hdmi") && input.getParentId() == null) {
                num_hdmi++;
            }
        }
        logDebug(TAG, false, "num_hdmi:" + num_hdmi);
        return num_hdmi;
    }

    public int GetRelativeSourceInput() {
        int result = -1;
        //hdmi1~hdmi4 5~8 TvControlManager.SourceInput.HDMI1~TvControlManager.SourceInput.HDMI4
        result = mTvControlManager.GetCurrentSourceInput() - TvControlManager.SourceInput.HDMI1.toInt();
        logDebug(TAG, false, "GetRelativeSourceInput = " + result);
        return result;
    }

    public void setDtvType(int value) {
        logDebug(TAG, false, "setDtvType = " + value);
        String type = null;
        switch (value) {
            case SET_DTMB:
                type = TvContract.Channels.TYPE_DTMB;
                break;
            case SET_DVB_C:
                type = TvContract.Channels.TYPE_DVB_C;
                break;
            case SET_DVB_T:
                type = TvContract.Channels.TYPE_DVB_T;
                break;
            case SET_DVB_T2:
                type = TvContract.Channels.TYPE_DVB_T2;
                break;
            case SET_ATSC_T:
                type = TvContract.Channels.TYPE_ATSC_T;
                break;
            case SET_ATSC_C:
                type = TvContract.Channels.TYPE_ATSC_C;
                break;
            case SET_ISDB_T:
                type = TvContract.Channels.TYPE_ISDB_T;
                break;
        }
        if (type != null) {
            DataProviderManager.putStringValue(mContext, DroidLogicTvUtils.TV_KEY_DTV_TYPE, type);
        }
    }

    public void setSoundChannel(int type) {
        logDebug(TAG, false, "setSoundChannel = " + type);
        if (mCurrentChannel != null) {
            mCurrentChannel.setAudioChannel(type);
            mTvDataBaseManager.updateChannelInfo(mCurrentChannel);
            mTvControlManager.DtvSetAudioChannleMod(mCurrentChannel.getAudioChannel());
        }
    }

    public void setStartupSetting(int type) {
        logDebug(TAG, false, "setStartupSetting = " + type);
        DataProviderManager.putIntValue(mContext, DroidLogicTvUtils.TV_START_UP_ENTER_APP, type);
    }

    public void setMenuTime(int type) {
        logDebug(TAG, false, "setMenuTime = " + type);
        DataProviderManager.putIntValue(mContext, KEY_MENU_TIME, type);
    }

    public void setSleepTimer(int mode) {
        DataProviderManager.putIntValue(mContext, DroidLogicTvUtils.PROP_DROID_TV_SLEEP_TIME, mode);
        String sleepTimerService = "com.droidlogic.tv.extras.suspend.TimerSuspendService";
        String targetPackage = "com.droidlogic.tv.extras";
        Intent intent = new Intent();
        intent.putExtra(DroidLogicTvUtils.KEY_ENABLE_SUSPEND_TIMEOUT, true);
        intent.putExtra("mode", mode);
        intent.setComponent(ComponentName.unflattenFromString(targetPackage + "/" + sleepTimerService));
        mContext.startService(intent);
    }

    public void setNoSignalSleepTime(int mode) {
        logDebug(TAG, false, "setNoSignalSleepTime = " + mode);
        DataProviderManager.putIntValue(mContext, KEY_NO_SIGNAL_TIMEOUT, mode);
    }

    public void setAutoBacklightStatus(int value) {
        logDebug(TAG, false, "setAutoBacklightStatus = " + value);
        SystemControlManager.Dynamic_Backlight_Mode mode;
        if (value != 0) {
            mode = SystemControlManager.Dynamic_Backlight_Mode.DYNAMIC_BACKLIGHT_HIGH;
        } else {
            mode = SystemControlManager.Dynamic_Backlight_Mode.DYNAMIC_BACKLIGHT_OFF;
        }
        mSystemControlManager.SetDynamicBacklight(mode, 1);
    }

    public void setAudioADSwitch(int switchVal) {
        logDebug(TAG, false, "setAudioADSwitch = " + switchVal);
        DataProviderManager.putIntValue(mContext, DroidLogicTvUtils.TV_KEY_AD_SWITCH, switchVal);
        Intent intent = new Intent(DroidLogicTvUtils.ACTION_AD_SWITCH);
        intent.putExtra(DroidLogicTvUtils.EXTRA_SWITCH_VALUE, switchVal);
        mContext.sendBroadcast(intent);
    }

    public void setVolumeCompensate(int value) {
        if (mCurrentChannel != null) {
            int current = mCurrentChannel.getAudioCompensation();
            int offset = 0;
            if (value > current) {
                offset = 1;
            } else if (value < current) {
                offset = -1;
            }
            if ((current < 20 && offset > 0)
                    || (current > -20 && offset < 0)) {
                mCurrentChannel.setAudioCompensation(current + offset);
                mTvDataBaseManager.updateChannelInfo(mCurrentChannel);
                mTvControlManager.SetCurProgVolumeCompesition(mCurrentChannel.getAudioCompensation());
            }
        }
    }

    public void setStaticFrameStatus(int status, int isSave) {
        boolean value = false;
        if (status == 1)
            value = true;
        DataProviderManager.putBooleanValue(mContext, DroidLogicTvUtils.TV_STATIC_FRAME_SETTING, value);
        mSystemControlManager.setStaticFrameStatus(status, isSave);
    }

    public void setScreenColorForSignalChange(int screenColor, int isSave) {
        //0:black screen, 1:blue screen
        DataProviderManager.putIntValue(mContext, DroidLogicTvUtils.TV_BLUE_SCREEN_SETTING, screenColor);
        mSystemControlManager.setScreenColorForSignalChange(screenColor, isSave);

        Intent intent = new Intent();
        intent.setAction("android.intent.action.blue_screen_setting");
        mContext.sendBroadcast(intent);
    }

    public void setADMix(int step) {
        logDebug(TAG, false, "setADMix = " + step);
        int level = getADMixStatus() + step;
        if (level <= 100 && level >= 0) {
            DataProviderManager.putIntValue(mContext, DroidLogicTvUtils.TV_KEY_AD_MIX, level);
            Intent intent = new Intent(DroidLogicTvUtils.ACTION_AD_MIXING_LEVEL);
            intent.putExtra(DroidLogicTvUtils.PARA_VALUE1, level);
            mContext.sendBroadcast(intent);
        }
    }

    public boolean isAnalogChannel() {
        if (mCurrentChannel != null) {
            return mCurrentChannel.isAnalogChannel();
        } else {
            return false;
        }
    }

    public boolean isChannelSource() {
        if ((mDeviceId == DroidLogicTvUtils.DEVICE_ID_ATV)
                || (mDeviceId == DroidLogicTvUtils.DEVICE_ID_DTV)
                || (mDeviceId == DroidLogicTvUtils.DEVICE_ID_ADTV)
                || (mDeviceId == DroidLogicTvUtils.DEVICE_ID_HDMIEXTEND)) {
            return true;
        } else {
            return false;
        }
    }

    public void doFactoryReset() {
        logDebug(TAG, false, "doFactoryReset");
        mTvControlManager.StopTv();
        setStartupSetting(0);
        setAudioADSwitch(0);
        setAutoBacklightStatus(0);
        setMenuTime(0);
        setSleepTimer(0);
        setDefAudioStreamVolume();
        clearHdmi20Mode();
        mTvDataBaseManager.deleteChannels("com.droidlogic.tvinput/.services.ADTVInputService/HW16", null);
        mTvControlManager.SSMInitDevice();
        mTvControlManager.FactoryCleanAllTableForProgram();
    }

    private void setDefAudioStreamVolume() {
        int maxVolume = SystemProperties.getInt("ro.config.media_vol_steps", 100);
        int streamMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int defaultVolume = maxVolume == streamMaxVolume ? (maxVolume * 3) / 10 : (streamMaxVolume * 3) / 4;
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, defaultVolume, 0);
    }

    private void sendResetSoundEffectBroadcast() {
        Intent intent = new Intent();
        intent.setAction("droid.action.resetsoundeffect");
        mContext.sendBroadcast(intent);
    }

    private void ClearPackageData(String packageName) {
        logDebug(TAG, false, "ClearPackageData:" + packageName);
        //clear data
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        //ClearUserDataObserver mClearDataObserver = new ClearUserDataObserver();
        boolean res = false;
        try {
            Class IPackageManagerclz = Class.forName("android.content.pm.IPackageDataObserver");
            Method clearApplicationUserDataMethod = ActivityManager.class.getMethod("clearApplicationUserData", String.class, IPackageManagerclz);
            res = (boolean) clearApplicationUserDataMethod.invoke(am, packageName, null);
            //boolean res = am.clearApplicationUserData(packageName, mClearDataObserver);
            if (!res) {
                logDebug(TAG, false, " clear " + packageName + " data failed");
            } else {
                logDebug(TAG, false, " clear " + packageName + " data succeed");
            }

            //clear cache
            PackageManager packageManager = mContext.getPackageManager();
            Method delApplicationCacheFilesMethod = ActivityManager.class.getMethod("deleteApplicationCacheFiles", String.class, IPackageManagerclz);
            delApplicationCacheFilesMethod.invoke(packageManager, packageName, null);
            //clear default
            packageManager.clearPackagePreferredActivities(packageName);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void setHdmi20Mode(int order, int mode) {
        logDebug(TAG, false, "setHdmi20Mode order = " + order + ", mode = " + mode);
        TvControlManager.HdmiPortID[] allport = {TvControlManager.HdmiPortID.HDMI_PORT_1, TvControlManager.HdmiPortID.HDMI_PORT_2,
                TvControlManager.HdmiPortID.HDMI_PORT_3, TvControlManager.HdmiPortID.HDMI_PORT_4};
        if (order < 0 || order > 3) {
            logDebug(TAG, true, "setHdmi20Mode device id erro");
            return;
        }
        if (mode == 1) {
            // set HDMI mode sequence: save than set
            mTvControlManager.SetHdmiEdidVersion(allport[order],
                    TvControlManager.HdmiEdidVer.HDMI_EDID_VER_20);
            mTvControlManager.SaveHdmiEdidVersion(allport[order],
                    TvControlManager.HdmiEdidVer.HDMI_EDID_VER_20);
        } else {
            mTvControlManager.SetHdmiEdidVersion(allport[order],
                    TvControlManager.HdmiEdidVer.HDMI_EDID_VER_14);
            mTvControlManager.SaveHdmiEdidVersion(allport[order],
                    TvControlManager.HdmiEdidVer.HDMI_EDID_VER_14);
        }
    }

    public void doFbcUpgrade() {
        logDebug(TAG, false, "doFactoryReset need add");
    }

    public void clearHdmi20Mode() {
        logDebug(TAG, false, "reset Hdmi20Mode status");
        TvControlManager.HdmiPortID[] allport = {TvControlManager.HdmiPortID.HDMI_PORT_1, TvControlManager.HdmiPortID.HDMI_PORT_2,
                TvControlManager.HdmiPortID.HDMI_PORT_3, TvControlManager.HdmiPortID.HDMI_PORT_4};

        for (int i = 0; i < allport.length; i++) {
            mTvControlManager.SaveHdmiEdidVersion(allport[i],
                    TvControlManager.HdmiEdidVer.HDMI_EDID_VER_14);
        }
    }

    public void setDaylightSavingTime(int value) {
        mDaylightSavingTime.setDaylightSavingTime(value);
    }

    public int getDaylightSavingTime() {
        return mDaylightSavingTime.getDaylightSavingTime();
    }

    public void setHdmiAudioLatency(String value) {
        logDebug(TAG, false, "setHdmiAudioLatency = " + value);
        mSystemControlManager.setProperty(AUDIO_LATENCY, value);
    }

    public int getHdmiAudioLatency() {
        int result = mSystemControlManager.getPropertyInt(AUDIO_LATENCY, 0);
        logDebug(TAG, false, "getHdmiAudioLatency = " + result);
        return result;
    }
}
