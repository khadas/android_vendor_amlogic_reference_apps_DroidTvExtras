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

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;

import com.droidlogic.tv.extras.R;
import com.droidlogic.tv.extras.SettingsConstant;
import static com.droidlogic.tv.extras.util.DroidUtils.logDebug;

import com.droidlogic.app.SystemControlManager;
import com.droidlogic.app.tv.TvControlManager;
import com.droidlogic.app.tv.TvInSignalInfo;
import com.droidlogic.app.tv.DroidLogicTvUtils;
import com.droidlogic.app.tv.ChannelInfo;
import com.droidlogic.app.tv.TvDataBaseManager;
import com.droidlogic.app.DataProviderManager;
import android.media.tv.TvContract;
import android.media.AudioManager;

import vendor.amlogic.hardware.systemcontrol.V1_0.SourceInputParam;

public class PQSettingsManager {
    public static final String TAG                                  = "PQSettingsManager";
    public static final String CURRENT_DEVICE_ID                    = "current_device_id";
    public static final String CURRENT_CHANNEL_ID                   = "current_channel_id";
    public static final String TV_CURRENT_DEVICE_ID                 = "tv_current_device_id";

    public static final String KEY_PICTURE                          = "picture";
    public static final String KEY_PICTURE_MODE                     = "picture_mode";
    public static final String KEY_BRIGHTNESS                       = "brightness";
    public static final String KEY_CONTRAST                         = "contrast";
    public static final String KEY_COLOR                            = "color";
    public static final String KEY_SHARPNESS                        = "sharpness";
    public static final String KEY_BACKLIGHT                        = "backlight";
    public static final String KEY_TONE                             = "tone";
    public static final String KEY_COLOR_TEMPERATURE                = "color_temperature";
    public static final String KEY_ASPECT_RATIO                     = "aspect_ratio";
    public static final String KEY_DNR                              = "dnr";
    public static final String KEY_3D_SETTINGS                      = "settings_3d";

    public static final String STATUS_STANDARD                      = "standard";
    public static final String STATUS_VIVID                         = "vivid";
    public static final String STATUS_SOFT                          = "soft";
    public static final String STATUS_SPORT                         = "sport";
    public static final String STATUS_MONITOR                       = "monitor";
    public static final String STATUS_BRIGHT                        = "bright";
    public static final String STATUS_DARK                          = "dark";
    public static final String STATUS_HR                            = "hr";
    public static final String STATUS_GAME                          = "game";
    public static final String STATUS_USER                          = "user";
    public static final String STATUS_WARM                          = "warm";
    public static final String STATUS_MUSIC                         = "music";
    public static final String STATUS_NEWS                          = "news";
    public static final String STATUS_MOVIE                         = "movie";
    public static final String STATUS_CINEMA                        = "cinema";
    public static final String STATUS_COOL                          = "cool";
    public static final String STATUS_ON                            = "on";
    public static final String STATUS_OFF                           = "off";
    public static final String STATUS_AUTO                          = "auto";
    public static final String STATUS_4_TO_3                        = "4:3";
    public static final String STATUS_PANORAMA                      = "panorama";
    public static final String STATUS_FULL_SCREEN                   = "full_screen";
    public static final String STATUS_MEDIUM                        = "medium";
    public static final String STATUS_HIGH                          = "high";
    public static final String STATUS_LOW                           = "low";
    public static final String STATUS_3D_LR_MODE                    = "left right mode";
    public static final String STATUS_3D_RL_MODE                    = "right left mode";
    public static final String STATUS_3D_UD_MODE                    = "up down mode";
    public static final String STATUS_3D_DU_MODE                    = "down up mode";
    public static final String STATUS_3D_TO_2D                      = "3D to 2D";
    public static final String STATUS_PCM                           = "pcm";
    public static final String STATUS_STEREO                        = "stereo";
    public static final String STATUS_LEFT_CHANNEL                  = "left channel";
    public static final String STATUS_RIGHT_CHANNEL                 = "right channel";
    public static final String STATUS_RAW                           = "raw";

    public static final int PERCENT_INCREASE                        = 1;
    public static final int PERCENT_DECREASE                        = -1;
    public static final int ADVANCED_GAMMA_FIXED_DIFFERENCE         = -6;

    public static final int HDR_TYPE_DOVI =3;

    public static String currentTag = null;
    private final int memcSave = 1;
    private final int mSave = 1;
    private int memcStatus = 0;

    public static final String PARAM_HAL_PICTRUE_MODE_STANDARD = "picture_mode=PQ_MODE_STANDARD";
    public static final String PARAM_HAL_PICTRUE_MODE_GAME = "picture_mode=PQ_MODE_GAME";
    private AudioManager mAudioManager;

    private Resources mResources;
    private Context mContext;
    private SystemControlManager mSystemControlManager;
    private TvControlManager mTvControlManager;
    private TvDataBaseManager mTvDataBaseManager;
    private TvControlManager.SourceInput mTvSourceInput;
    private TvControlManager.SourceInput_Type mVirtualTvSource;
    private TvControlManager.SourceInput_Type mTvSource;
    private int mDeviceId;
    private long mChannelId;
    private int mVideoStd;
    private Activity mActivity;

    public PQSettingsManager (Context context) {
        mContext = context;
        mActivity = (Activity)context;
        mAudioManager = context.getSystemService(AudioManager.class);
        mDeviceId = mActivity.getIntent().getIntExtra(TV_CURRENT_DEVICE_ID, -1);
        mChannelId = mActivity.getIntent().getLongExtra(CURRENT_CHANNEL_ID, -1);
        mResources = mContext.getResources();
        mSystemControlManager = SystemControlManager.getInstance();
        if (SettingsConstant.needDroidlogicTvFeature(mContext)) {
            ChannelInfo currentChannel;
            if (mTvControlManager == null) {
                mTvControlManager = TvControlManager.getInstance();
            }
            mTvDataBaseManager = new TvDataBaseManager(context);
            mTvSource = DroidLogicTvUtils.parseTvSourceTypeFromDeviceId(mDeviceId);
            mTvSourceInput = DroidLogicTvUtils.parseTvSourceInputFromDeviceId(mDeviceId);
            mVirtualTvSource = mTvSource;

            if (mTvSource == TvControlManager.SourceInput_Type.SOURCE_TYPE_ADTV) {
                logDebug(TAG, false, "channelId: " + mChannelId);
                currentChannel = mTvDataBaseManager.getChannelInfo(TvContract.buildChannelUri(mChannelId));
                if (currentChannel != null) {
                    mVideoStd = currentChannel.getVideoStd();
                    mTvSource = DroidLogicTvUtils.parseTvSourceTypeFromSigType(DroidLogicTvUtils.getSigType(currentChannel));
                    mTvSourceInput = DroidLogicTvUtils.parseTvSourceInputFromSigType(DroidLogicTvUtils.getSigType(currentChannel));
                    logDebug(TAG, false, "currentChannel != null");
                } else {
                    mVideoStd = -1;
                    logDebug(TAG, false, "currentChannel == null");
                }
                if (mVirtualTvSource == mTvSource) {//no channels in adtv input, DTV for default.
                    mTvSource = TvControlManager.SourceInput_Type.SOURCE_TYPE_DTV;
                    mTvSourceInput = TvControlManager.SourceInput.DTV;
                    logDebug(TAG, false, "no channels in adtv input, DTV for default.");
                }
            }
        }
        logDebug(TAG, true, "mDeviceId: " + mDeviceId);
    }

    public static final int PIC_STANDARD = 0;
    public static final int PIC_VIVID = 1;
    public static final int PIC_SOFT = 2;
    public static final int PIC_USER = 3;
    public static final int PIC_CINEMA = 4;
    public static final int PIC_MOVIE = 5;
    public static final int PIC_MONITOR = 6;
    public static final int PIC_GAME = 7;
    public static final int PIC_SPORT = 8;

    public static final int PIC_DV_DARK = 12;
    public static final int PIC_DV_BRIGHT = 13;


    public static final int VRR_BASIC = 1;
    public static final int FreeSync = 2;
    public static final int FreeSync_Premium = 3;
    public static final int FreeSync_Premium_Pro = 4;
    public static final int FreeSync_Premium_G_SYNC = 5;

    /*todo:PQ hasn't defined this value yet,
     so it's just drawing the interface for now
     */
    public static final int PIC_HR = 20;

    public String getPictureModeStatus () {
        int pictureModeIndex = mSystemControlManager.GetPQMode();
        logDebug(TAG, false, "getPictureModeStatus : " + pictureModeIndex);
        switch (pictureModeIndex) {
            case PIC_STANDARD:
                return STATUS_STANDARD;
            case PIC_VIVID:
                return STATUS_VIVID;
            case PIC_SOFT:
                return STATUS_SOFT;
            case PIC_USER:
                return STATUS_USER;
            case PIC_MONITOR:
                return STATUS_MONITOR;
            case PIC_SPORT:
                return STATUS_SPORT;
            case PIC_MOVIE:
                return STATUS_MOVIE;
            case PIC_CINEMA:
                return STATUS_CINEMA;
            case PIC_GAME:
                return STATUS_GAME;
            case PIC_DV_BRIGHT:
                return STATUS_BRIGHT;
            case PIC_DV_DARK:
                return STATUS_DARK;
            case PIC_HR:
                return STATUS_HR;
            default:
                return STATUS_STANDARD;
        }
    }

    /**
     * When Picture Mode is FS2 signal, the display needs to obtain vrrmode.
     * @return VRRModeName
     */
    String getVRRModeName() {
        int vrrModeId = mTvControlManager.GetVRRMode();
        String vrrModeName = null;
        if (vrrModeId == VRR_BASIC
                || vrrModeId == FreeSync
                || vrrModeId == FreeSync_Premium
                || vrrModeId == FreeSync_Premium_Pro
                || vrrModeId == FreeSync_Premium_G_SYNC) {
            vrrModeName = "(" + mTvControlManager.GetVRRModeString() + ")";
        }
        logDebug(TAG, false, "vrrModeId:" + vrrModeId + " vrrModeName:" + vrrModeName);
        return vrrModeName;
    }

    /**
     * setDolbyDarkDetail
     * 1:no
     * 0:off
     */
    public void setDolbyDarkDetail(boolean enableDarkDetail) {
        logDebug(TAG, false, "enableDarkDetail: " + enableDarkDetail);
        mSystemControlManager.SetDolbyDarkDetail(enableDarkDetail ? 1 : 0, mSave);
    }

    public boolean getDolbyDarkDetail(){
        int GetDolbyDarkDetailId = mSystemControlManager.GetDolbyDarkDetail();
        logDebug(TAG, false, "GetDolbyDarkDetail: "+ GetDolbyDarkDetailId);

        return GetDolbyDarkDetailId == 1 ? true : false; //1 is on ,0 is off
    }

    public boolean getVrr() {
        int vrrEnabled = mTvControlManager.GetVRREnable();
        logDebug(TAG, false, "vrrEnabled: " + vrrEnabled);
        return vrrEnabled == 1; //1 is on ,0 is off
    }

    /**
     * setVrr
     * 1:enable; 0:disable
     */
    public void setVrr(boolean enableVrr) {
        logDebug(TAG, false, "enableVrr: " + enableVrr);
        mTvControlManager.SetVRREnable(enableVrr ? 1 : 0);
    }

    public boolean isSupportQMS() {
        return mTvControlManager.IsSupportQMS();
    }

    public boolean getQMSEnable() {
        return mTvControlManager.GetQMSEnable();
    }

    public void setQMSEnable(boolean enableQms) {
        mTvControlManager.SetQMSEnable(enableQms ? 1 : 0);
    }

    private TvControlManager.HdmiPortID getCurrentHdmiPortId(final int currentSourceInput){
        TvControlManager.HdmiPortID hdmiPortId = null;
        switch (currentSourceInput) {
            case TvControlManager.AM_AUDIO_HDMI1:
                hdmiPortId = TvControlManager.HdmiPortID.HDMI_PORT_1;
                break;
            case TvControlManager.AM_AUDIO_HDMI2:
                hdmiPortId = TvControlManager.HdmiPortID.HDMI_PORT_2;
                break;
            case TvControlManager.AM_AUDIO_HDMI3:
                hdmiPortId = TvControlManager.HdmiPortID.HDMI_PORT_3;
                break;
            case TvControlManager.AM_AUDIO_HDMI4:
                hdmiPortId = TvControlManager.HdmiPortID.HDMI_PORT_4;
                break;
            default:
                break;
        }
        logDebug(TAG, false, "currentSourceInput:" + currentSourceInput
                + " hdmiPortId: " + hdmiPortId);
        return hdmiPortId;
    }

    public boolean isHdmi20Status() {
        TvControlManager.HdmiPortID hdmiPortIdEnum =
            getCurrentHdmiPortId(mTvControlManager.GetCurrentSourceInput());

        if (hdmiPortIdEnum == null) {
            return false;
        }
      // edidversion: Hdmi2.0 state: 0 off, 1 on
        int hdmiEdidVersion = mTvControlManager.GetHdmiEdidVersion(hdmiPortIdEnum);
        logDebug(TAG, false, "hdmiEdidVersion: " + hdmiEdidVersion);

        return hdmiEdidVersion != 0 ? true : false;
    }

    public String getChipVersionInfo () {
        String chipVersion = mSystemControlManager.getChipVersionInfo();
        logDebug(TAG, false, "getChipVersionInfo: "+chipVersion);
        return chipVersion;
    }

    public int getBrightnessStatus () {
        int value = mSystemControlManager.GetBrightness();
        logDebug(TAG, false, "getBrightnessStatus : " + value);
        return value;
    }

    public int getContrastStatus () {
        int value = mSystemControlManager.GetContrast();
        logDebug(TAG, false, "getContrastStatus : " + value);
        return value;
    }

    public int getColorStatus () {
        int value = mSystemControlManager.GetSaturation();
        logDebug(TAG, false, "getColorStatus : " + value);
        return value;
    }

    public int getSharpnessStatus () {
        int value = mSystemControlManager.GetSharpness();
        logDebug(TAG, false, "getSharpnessStatus : " + value);
        return value;
    }

    public int getToneStatus () {
        int value = mSystemControlManager.GetHue();
        logDebug(TAG, false, "getTintStatus : " + value);
        return value;
    }

    public int getPictureModeSource () {
        logDebug(TAG, false, "getPictureModeSource");
        return mSystemControlManager.GetSourceHdrType();
    }

    public enum Aspect_Ratio_Mode {
        ASPEC_RATIO_AUTO(0),
        ASPEC_RATIO_43(1),
        ASPEC_RATIO_PANORAMA(2),
        ASPEC_RATIO_FULL_SCREEN(3),
        ASPEC_RATIO_DOT_BY_NOT(4);

        private int val;

        Aspect_Ratio_Mode(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    public enum RGB_CHANNEL_TYPE {
        RED_CH(0),
        GREEN_CH(1),
        BLUE_CH(2),
        MAX_CH(3);
        private int val;

        RGB_CHANNEL_TYPE(int val) {
            this.val = val;
        }

        public int getVal() {
            return val;
        }
    }

    public int getAspectRatioStatus () {
        int itemPosition = mSystemControlManager.GetDisplayMode(TvControlManager.SourceInput.XXXX.toInt());
        logDebug(TAG, false, "getAspectRatioStatus:" + itemPosition);
        if (itemPosition == SystemControlManager.Display_Mode.DISPLAY_MODE_MODE43.toInt()) {
            return Aspect_Ratio_Mode.ASPEC_RATIO_43.toInt();
        } else if (itemPosition == SystemControlManager.Display_Mode.DISPLAY_MODE_FULL.toInt()) {
            return Aspect_Ratio_Mode.ASPEC_RATIO_PANORAMA.toInt();
        } else if (itemPosition == SystemControlManager.Display_Mode.DISPLAY_MODE_169.toInt()) {
            return Aspect_Ratio_Mode.ASPEC_RATIO_FULL_SCREEN.toInt();
        } else if (itemPosition == SystemControlManager.Display_Mode. DISPLAY_MODE_NOSCALEUP.toInt()) {
            return Aspect_Ratio_Mode.ASPEC_RATIO_DOT_BY_NOT.toInt();
        } else {
            return Aspect_Ratio_Mode.ASPEC_RATIO_AUTO.toInt();
        }
    }

    public int GetSourceHdrType() {
        int sourceHdrType = mSystemControlManager.GetSourceHdrType();
        logDebug(TAG, false, "GetSourceHdrType: " + sourceHdrType);
        return sourceHdrType;
    }

    public int getAdvancedDynamicToneMappingStatus () {
        logDebug(TAG, false, "getAdvancedDynamicToneMappingStatus");
        int dynamicToneMappingStatus = mSystemControlManager.GetHDRTMOMode();
        return dynamicToneMappingStatus != -1 ? dynamicToneMappingStatus: 0;//0 is on ,1 is off
    }

    public int getAdvancedColorManagementStatus () {
        logDebug(TAG, false, "getAdvancedColorManagementStatus");
        int colorManagementStatus = mSystemControlManager.GetColorBaseMode();
        return colorManagementStatus != -1 ? colorManagementStatus : 0;
    }

    public int getAdvancedColorSpaceStatus () {
        // Leave blank first, add later
        logDebug(TAG, false, "getAdvancedColorSpaceStatus");
        return 0;
    }

    public int getAdvancedGlobalDimmingStatus () {
        logDebug(TAG, false, "getAdvancedGlobalDimmingStatus");
        return mSystemControlManager.GetDynamicBacklight();
    }

    public int getAdvancedLocalDimmingStatus () {
        int dimmingStatus = mSystemControlManager.GetLocalDimming();
        logDebug(TAG, false, "getAdvancedLocalDimmingStatus:" + dimmingStatus);
        return dimmingStatus;
    }

    public int getAdvancedBlackStretchStatus () {
        logDebug(TAG, false, "getAdvancedBlackStretchStatus");
        return mSystemControlManager.GetBlackExtensionMode();
    }

    public int getAdvancedDNLPStatus () {
        logDebug(TAG, false, "getAdvancedDNLPStatus");
        int CurrentSourceInfo[] = mSystemControlManager.GetCurrentSourceInfo();
        int DNLPStatus = mSystemControlManager.getDNLPCurveParams(SystemControlManager.SourceInput.valueOf(CurrentSourceInfo[0]), SystemControlManager.SignalFmt.valueOf(CurrentSourceInfo[1]), SystemControlManager.TransFmt.valueOf(CurrentSourceInfo[2]));
        return DNLPStatus != -1? DNLPStatus :0;
    }

    public int getAdvancedLocalContrastStatus () {
        logDebug(TAG, false, "getAdvancedLocalContrastStatus");
        return mSystemControlManager.GetLocalContrastMode();
    }

    public int getAdvancedSRStatus () {
        // Leave blank first, add later
        logDebug(TAG, false, "getAdvancedSRStatus");
        return 0;
    }

    public int getAdvancedDeBlockStatus () {
        logDebug(TAG, false, "getAdvancedDeBlockStatus");
        int deBlockStatus = mSystemControlManager.GetDeblockMode();
        return deBlockStatus != -1? deBlockStatus :0;
    }

    public int getAdvancedDeMosquitoStatus () {
        logDebug(TAG, false, "getAdvancedDeMosquitoStatus");
        int deMosquitoStatus = mSystemControlManager.GetDemoSquitoMode();
        return deMosquitoStatus != -1? deMosquitoStatus :0;
    }

    public int getAdvancedDecontourStatus () {
        logDebug(TAG, false, "getAdvancedDecontourStatus");
        return mSystemControlManager.GetSmoothPlusMode();
    }

    public int getAdvancedMemcSwitchStatus () {
        memcStatus = mSystemControlManager.GetMemcMode();
        logDebug(TAG, false, "getAdvancedGlobalDimmingStatus value: " + memcStatus);
        return memcStatus;
    }

    public int getAdvancedMemcCustomizeDejudderStatus () {
        int DeJudderStatus = mSystemControlManager.GetMemcDeJudderLevel();
        logDebug(TAG, false, "deBlurStatus: " + DeJudderStatus);
        return DeJudderStatus;
    }

    public int getAdvancedMemcCustomizeDeblurStatus () {
        int deBlurStatus = mSystemControlManager.GetMemcDeBlurLevel();
        logDebug(TAG, false, "deBlurStatus: " + deBlurStatus);
        return deBlurStatus;
    }

    public int getAdvancedGammaStatus () {
        return mSystemControlManager.GetGammaValue() + ADVANCED_GAMMA_FIXED_DIFFERENCE;
    }

    public int getWhiteBalanceGamma(int channel, int point) {
        return mSystemControlManager.GetWhitebalanceGamma(channel, point);
    }

    public void setWhiteBalanceGamma(int channel, int point, int offset) {
        mSystemControlManager.SetWhitebalanceGamma(channel, point, offset);
    }

    public int getColorTemperatureStatus () {
        int itemPosition = mSystemControlManager.GetColorTemperature();
        logDebug(TAG, false, "getColorTemperatureStatus : " + itemPosition);
        return itemPosition;
    }

    public int getAdvancedColorTemperatureRGainStatus () {
        return mSystemControlManager.GetColorTemperatureUserParam().r_gain;
    }

    public int getAdvancedColorTemperatureGGainStatus () {
        return mSystemControlManager.GetColorTemperatureUserParam().g_gain;
    }

    public int getAdvancedColorTemperatureBGainStatus () {
        return mSystemControlManager.GetColorTemperatureUserParam().b_gain;
    }

    public int getAdvancedColorTemperatureROffsetStatus () {
        return mSystemControlManager.GetColorTemperatureUserParam().r_offset;
    }

    public int getAdvancedColorTemperatureGOffsetStatus () {
        return mSystemControlManager.GetColorTemperatureUserParam().g_offset;
    }

    public int getAdvancedColorTemperatureBOffsetStatus () {
        return mSystemControlManager.GetColorTemperatureUserParam().b_offset;
    }

    public int getDnrStatus () {
        int itemPosition = mSystemControlManager.GetNoiseReductionMode();
        logDebug(TAG, false, "getDnrStatus : " + itemPosition);
        return itemPosition;
    }

    public int getAdvancedColorCustomizeCyanSaturationStatus() {
        // Leave blank first, add later
        return 0;
    }

    public int getAdvancedColorCustomizeCyanLumaStatus() {
        // Leave blank first, add later
        return 0;
    }

    public int getAdvancedColorCustomizeCyanHueStatus() {
        // Leave blank first, add later
        return 0;
    }

    public int getAdvancedColorCustomizeBlueSaturationStatus() {
        // Leave blank first, add later
        return 0;
    }

    public int getAdvancedColorCustomizeBlueLumaStatus() {
        // Leave blank first, add later
        return 0;
    }

    public int getAdvancedColorCustomizeBlueHueStatus() {
        // Leave blank first, add later
        return 0;
    }

    public int getAdvancedColorCustomizeBlueGreenSaturationStatus() {
        // Leave blank first, add later
        return 0;
    }

    public int getAdvancedColorCustomizeBlueGreenLumaStatus() {
        // Leave blank first, add later
        return 0;
    }

    public int getAdvancedColorCustomizeBlueGreenHueStatus() {
        // Leave blank first, add later
        return 0;
    }

    public int getAdvancedColorCustomizeGreenSaturationStatus() {
        // Leave blank first, add later
        return 0;
    }

    public int getAdvancedColorCustomizeGreenLumaStatus() {
        // Leave blank first, add later
        return 0;
    }

    public int getAdvancedColorCustomizeGreenHueStatus() {
        // Leave blank first, add later
        return 0;
    }

    public int getAdvancedColorCustomizeMagentaSaturationStatus() {
        // Leave blank first, add later
        return 0;
    }

    public int getAdvancedColorCustomizeMagentaLumaStatus() {
        // Leave blank first, add later
        return 0;
    }

    public int getAdvancedColorCustomizeMagentaHueStatus() {
        // Leave blank first, add later
        return 0;
    }

    public int getAdvancedColorCustomizeRedSaturationStatus() {
        // Leave blank first, add later
        return 0;
    }

    public int getAdvancedColorCustomizeRedLumaStatus() {
        // Leave blank first, add later
        return 0;
    }

    public int getAdvancedColorCustomizeRedHueStatus() {
        // Leave blank first, add later
        return 0;
    }

    public int getAdvancedColorCustomizeSkinSaturationStatus() {
        // Leave blank first, add later
        return 0;
    }

    public int getAdvancedColorCustomizeSkinLumaStatus() {
        // Leave blank first, add later
        return 0;
    }

    public int getAdvancedColorCustomizeSkinHueStatus() {
        // Leave blank first, add later
        return 0;
    }

    public int getAdvancedColorCustomizeYellowSaturationStatus() {
        // Leave blank first, add later
        return 0;
    }

    public int getAdvancedColorCustomizeYellowLumaStatus() {
        // Leave blank first, add later
        return 0;
    }

    public int getAdvancedColorCustomizeYellowHueStatus() {
        // Leave blank first, add later
        return 0;
    }

    public int getAdvancedColorCustomizeYellowGreenSaturationStatus() {
        // Leave blank first, add later
        return 0;
    }

    public int getAdvancedColorCustomizeYellowGreenLumaStatus() {
        // Leave blank first, add later
        return 0;
    }

    public int getAdvancedColorCustomizeYellowGreenHueStatus() {
        // Leave blank first, add later
        return 0;
    }

    public void setPictureMode (String mode) {
        logDebug(TAG, false, "setPictureMode : " + mode);
        if (mode.equals(STATUS_STANDARD)) {
            mSystemControlManager.SetPQMode(PIC_STANDARD, mSave, 0);
        } else if (mode.equals(STATUS_VIVID)) {
            mSystemControlManager.SetPQMode(PIC_VIVID, mSave, 0);
        } else if (mode.equals(STATUS_SOFT)) {
            mSystemControlManager.SetPQMode(PIC_SOFT, mSave, 0);
        } else if (mode.equals(STATUS_USER)) {
            mSystemControlManager.SetPQMode(PIC_USER, mSave, 0);
        } else if (mode.equals(STATUS_MONITOR)) {
            mSystemControlManager.SetPQMode(PIC_MONITOR, mSave, 0);
        } else if (mode.equals(STATUS_SPORT)) {
            mSystemControlManager.SetPQMode(PIC_SPORT, mSave, 0);
        } else if (mode.equals(STATUS_MOVIE)) {
            mSystemControlManager.SetPQMode(PIC_MOVIE, mSave, 0);
        } else if (mode.equals(STATUS_CINEMA)) {
            mSystemControlManager.SetPQMode(PIC_CINEMA, mSave, 0);
        } else if (mode.equals(STATUS_GAME)) {
            mSystemControlManager.SetPQMode(PIC_GAME, mSave, 0);
        } else if (mode.equals(STATUS_BRIGHT)) {
            mSystemControlManager.SetPQMode(PIC_DV_BRIGHT, mSave, 0);
        } else if (mode.equals(STATUS_DARK)) {
            mSystemControlManager.SetPQMode(PIC_DV_DARK, mSave, 0);
        } else if (mode.equals(STATUS_HR)) {
            // mSystemControlManager.SetPQMode(PIC_HR, mSave, 0);
        }
    }

    public void setPictureModeSDR (String mode) {
        logDebug(TAG, false, "setPictureModeSDR : " + mode);
        if (mode.equals(STATUS_GAME)) {
            mAudioManager.setParameters(PARAM_HAL_PICTRUE_MODE_GAME);
        } else {
            mAudioManager.setParameters(PARAM_HAL_PICTRUE_MODE_STANDARD);
        }
    }

    public void setBrightness(int step) {
        logDebug(TAG, false, "setBrightness step : " + step);
        int brightness = mSystemControlManager.GetBrightness();
        mSystemControlManager.SetBrightness(brightness + step, 1);
    }

    public void setContrast(int step) {
        logDebug(TAG, false, "setContrast step : " + step);
        int contrast = mSystemControlManager.GetContrast();
        mSystemControlManager.SetContrast(contrast + step, 1);
    }

    public void setColor(int step) {
        logDebug(TAG, false, "setColor step : " + step);
        int saturation = mSystemControlManager.GetSaturation();
        mSystemControlManager.SetSaturation(saturation + step, 1);
    }

    public void setSharpness(int step) {
        logDebug(TAG, false, "setSharpness step : " + step);
        int sharpness = mSystemControlManager.GetSharpness();
        mSystemControlManager.SetSharpness(sharpness + step, 1, 1);
    }

    public void setTone(int step) {
        logDebug(TAG, false, "setTint step : " + step);
        int hue = mSystemControlManager.GetHue();
        mSystemControlManager.SetHue(hue + step, 1);
    }

    public String getVideoStd () {
        if (mVideoStd != -1) {
            switch (mVideoStd) {
                case 1:
                    return mResources.getString(R.string.pal);
                case 2:
                    return mResources.getString(R.string.ntsc);
                default:
                    return mResources.getString(R.string.pal);
            }
        }
        return null;
    }

    public boolean isNtscSignalOrNot() {
        if (!SettingsConstant.needDroidlogicTvFeature(mContext)) {
            return false;
        }
        TvInSignalInfo info;
        String videoStd = getVideoStd();
        if (mTvSource == TvControlManager.SourceInput_Type.SOURCE_TYPE_TV
            && videoStd != null && videoStd.equals(mResources.getString(R.string.ntsc))) {
            if (mTvControlManager == null) {
                mTvControlManager = TvControlManager.getInstance();
            }
            info = mTvControlManager.GetCurrentSignalInfo();
            if (info.sigStatus == TvInSignalInfo.SignalStatus.TVIN_SIG_STATUS_STABLE) {
                logDebug(TAG, false, "ATV NTSC mode signal is stable, show Tint");
                return true;
            }
        } else if (mTvSource == TvControlManager.SourceInput_Type.SOURCE_TYPE_AV) {
            if (mTvControlManager == null) {
                mTvControlManager = TvControlManager.getInstance();
            }
            info = mTvControlManager.GetCurrentSignalInfo();
            if (info.sigStatus == TvInSignalInfo.SignalStatus.TVIN_SIG_STATUS_STABLE) {
                String[] strings = info.sigFmt.toString().split("_");
                if (strings[4].contains("NTSC")) {
                    logDebug(TAG, false, "AV NTSC mode signal is stable, show Tint");
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isNtscSignal() {
        final int DEFAULT_VALUE = -1;
        final int NTSC = 2;
        return DataProviderManager.getIntValue(mContext, "current_video_std", DEFAULT_VALUE) == NTSC;
    }

    public void setAspectRatio(int mode) {
        logDebug(TAG, false, "setAspectRatio:" + mode);
        int source = TvControlManager.SourceInput.XXXX.toInt();
        if (mode == 0) {
            mSystemControlManager.SetDisplayMode(source, SystemControlManager.Display_Mode.DISPLAY_MODE_NORMAL, 1);
        } else if (mode == 1) {
            mSystemControlManager.SetDisplayMode(source, SystemControlManager.Display_Mode.DISPLAY_MODE_MODE43, 1);
        } else if (mode == 2) {
            mSystemControlManager.SetDisplayMode(source, SystemControlManager.Display_Mode.DISPLAY_MODE_FULL, 1);
        } else if (mode == 3) {
            mSystemControlManager.SetDisplayMode(source, SystemControlManager.Display_Mode.DISPLAY_MODE_169, 1);
        } else if (mode == 4) {
            mSystemControlManager.SetDisplayMode(source, SystemControlManager.Display_Mode.DISPLAY_MODE_NOSCALEUP, 1);
        }
    }

    public boolean hasAipqFunc() {
        return mSystemControlManager.hasAipqFunc();
    }

    public int getAipqModeLevel() {
        return mSystemControlManager.GetAipqMode();
    }

    public void setAipqModeLevel(int selection, int save) {
        mSystemControlManager.SetAipqMode(selection, save);
    }

    public boolean getAipqInfo(String aipqInfoEnable) {
        return mSystemControlManager.getPropertyBoolean(aipqInfoEnable, false);
    }

    public boolean hasAisrFunc() {
        return mSystemControlManager.hasAisrFunc();
    }

    public int getAisrModeLevel() {
        return mSystemControlManager.GetAisrMode();
    }

    public void setAisrModeLevel(int selection, int save) {
        mSystemControlManager.SetAisrMode(selection, save);
    }

    public void setAdvancedDynamicToneMappingStatus (int value) {
        // Leave blank first, add later
        logDebug(TAG, false, "setAdvancedDynamicToneMappingStatus value:"+value);
        mSystemControlManager.SetHDRTMOMode(value, 1);
    }

    public void setAdvancedColorManagementStatus (int value) {
        // Leave blank first, add later
        switch (value) {
                case 0:
                    mSystemControlManager.SetColorBaseMode( SystemControlManager.ColorBaseMode.COLOR_BASE_MODE_OFF, 1);// off
                    break;
                case 1:
                    mSystemControlManager.SetColorBaseMode( SystemControlManager.ColorBaseMode.COLOR_BASE_MODE_OPTIMIZE, 1);// low
                    break;
                case 2:
                    mSystemControlManager.SetColorBaseMode( SystemControlManager.ColorBaseMode.COLOR_BASE_MODE_ENHANCE, 1);// middle
                    break;
                case 3:
                    mSystemControlManager.SetColorBaseMode( SystemControlManager.ColorBaseMode.COLOR_BASE_MODE_DEMO, 1); // high
                    break;
                default:
                    mSystemControlManager.SetColorBaseMode( SystemControlManager.ColorBaseMode.COLOR_BASE_MODE_OFF, 1);// off
                    break;
        }
    }

    public void setAdvancedColorSpaceStatus (int value) {
        // Leave blank first, add later
        logDebug(TAG, false, "setAdvancedColorSpaceStatus value:"+value);
        switch (value) {
                case 0:
                    // auto
                    break;
                case 1:
                     // srgb/rec.709
                    break;
                case 2:
                     // dci-p3
                    break;
                case 3:
                     // adobe rgb
                    break;
                case 4:
                     // bt.2020
                    break;
                default:
                    // auto
                    break;
        }
    }

    public void setAdvancedGlobalDimmingStatus (int value) {
        logDebug(TAG, false, "setAdvancedGlobalDimmingStatus value:"+value);
        mSystemControlManager.SetDynamicBacklight(SystemControlManager.Dynamic_Backlight_Mode.valueOf(value), 1);
    }

    public void setAdvancedLocalDimmingStatus (int value) {
        logDebug(TAG, false, "setAdvancedLocalDimmingStatus value:"+value);
        mSystemControlManager.SetLocalDimming(value, mSave);
    }

    public void setAdvancedBlackStretchStatus (int value) {
        logDebug(TAG, false, "setAdvancedBlackStretchStatus value:"+value);
        mSystemControlManager.SetBlackExtensionMode(SystemControlManager.Black_Extension_Mode.valueOf(value), 1);
    }

    public void setAdvancedDNLPStatus (int value) {
        logDebug(TAG, false, "setAdvancedDNLPStatus value:"+value);
        int CurrentSourceInfo[] = mSystemControlManager.GetCurrentSourceInfo();
        mSystemControlManager.setDNLPCurveParams(SystemControlManager.SourceInput.valueOf(CurrentSourceInfo[0]), SystemControlManager.SignalFmt.valueOf(CurrentSourceInfo[1]), SystemControlManager.TransFmt.valueOf(CurrentSourceInfo[2]), value);
    }

    public void setAdvancedLocalContrastStatus (int value) {
        logDebug(TAG, false, "setAdvancedLocalContrastStatus value:"+value);
        switch (value) {
                case 0:
                    mSystemControlManager.SetLocalContrastMode(SystemControlManager.Local_Contrast_Mode.LOCAL_CONTRAST_MODE_OFF,1);// off
                    break;
                case 1:
                     mSystemControlManager.SetLocalContrastMode(SystemControlManager.Local_Contrast_Mode.LOCAL_CONTRAST_MODE_LOW,1); // low
                    break;
                case 2:
                     mSystemControlManager.SetLocalContrastMode(SystemControlManager.Local_Contrast_Mode.LOCAL_CONTRAST_MODE_MID,1); // middle
                    break;
                case 3:
                     mSystemControlManager.SetLocalContrastMode(SystemControlManager.Local_Contrast_Mode.LOCAL_CONTRAST_MODE_HIGH,1); // high
                    break;
                default:
                     mSystemControlManager.SetLocalContrastMode(SystemControlManager.Local_Contrast_Mode.LOCAL_CONTRAST_MODE_OFF,1); // off
                    break;
        }
    }

    public void setAdvancedSRStatus (int value) {
        // Leave blank first, add later
        logDebug(TAG, false, "setAdvancedSRStatus value:"+value);
        switch (value) {
                case 0:
                    // off
                    break;
                case 1:
                     // standard
                    break;
                case 2:
                     // enhance
                    break;
                default:
                    // off
                    break;
        }
    }

    public void setAdvancedDeBlockStatus (int value) {
        logDebug(TAG, false, "setAdvancedDeBlockStatus value:"+value);
        mSystemControlManager.SetDeblockMode(SystemControlManager.Deblock_Mode.valueOf(value), 1);
    }

    public void setAdvancedDeMosquitoStatus (int value) {
        logDebug(TAG, false, "setAdvancedDeMosquitoStatus value:"+value);
        mSystemControlManager.SetDemoSquitoMode(SystemControlManager.DemoSquito_Mode.valueOf(value), 1);
    }

    public void setAdvancedDecontourStatus (int value) {
        logDebug(TAG, false, "setAdvancedDecontourStatus value:"+value);
        mSystemControlManager.SetSmoothPlusMode(value, 1);
    }

    public void setAdvancedMemcSwitchStatus (int value) {
        mSystemControlManager.SetMemcMode(value, memcSave);
    }

    public void setAdvancedGammaStatus (int value) {
        mSystemControlManager.SetGammaValue(value - ADVANCED_GAMMA_FIXED_DIFFERENCE, 1);
    }

    public void setAdvancedMemcCustomizeDejudderStatus (int value) {
        mSystemControlManager.SetMemcDeJudderLevel(value, mSave);
    }

    public void setAdvancedMemcCustomizeDeblurStatus (int value) {
        mSystemControlManager.SetMemcDeBlurLevel(value, mSave);
    }

    // 0 1 2 3 ~ standard warm1 cool warm2
    public void setColorTemperature(int mode) {
        logDebug(TAG, false, "setColorTemperature : " + mode);
        mSystemControlManager.SetColorTemperature(mode, 1);
    }

    public void setAdvancedColorTemperatureRGainStatus (int value) {
        int currentColorTemperatureType = mSystemControlManager.GetColorTemperature();
        switch (currentColorTemperatureType) {
                case 0:
                    mSystemControlManager.SetColorTemperatureUserParam(SystemControlManager.color_temperature.COLOR_TEMP_STANDARD, 1, SystemControlManager.rgb_type.R_GAIN, value);
                    break;
                case 1:
                    mSystemControlManager.SetColorTemperatureUserParam(SystemControlManager.color_temperature.COLOR_TEMP_WARM, 1, SystemControlManager.rgb_type.R_GAIN, value);
                    break;
                case 2:
                    mSystemControlManager.SetColorTemperatureUserParam(SystemControlManager.color_temperature. COLOR_TEMP_COLD, 1, SystemControlManager.rgb_type.R_GAIN, value);
                    break;
                case 3:
                    mSystemControlManager.SetColorTemperatureUserParam(SystemControlManager.color_temperature.COLOR_TEMP_USER, 1, SystemControlManager.rgb_type.R_GAIN, value);
                    break;
                default:
                    mSystemControlManager.SetColorTemperatureUserParam(SystemControlManager.color_temperature.COLOR_TEMP_STANDARD, 1, SystemControlManager.rgb_type.R_GAIN, value);
                    break;
        }
    }

    public void setAdvancedColorTemperatureGGainStatus (int value) {
        int currentColorTemperatureType = mSystemControlManager.GetColorTemperature();
        switch (currentColorTemperatureType) {
                case 0:
                    mSystemControlManager.SetColorTemperatureUserParam(SystemControlManager.color_temperature.COLOR_TEMP_STANDARD, 1, SystemControlManager.rgb_type.G_GAIN, value);
                    break;
                case 1:
                    mSystemControlManager.SetColorTemperatureUserParam(SystemControlManager.color_temperature.COLOR_TEMP_WARM, 1, SystemControlManager.rgb_type.G_GAIN, value);
                    break;
                case 2:
                    mSystemControlManager.SetColorTemperatureUserParam(SystemControlManager.color_temperature. COLOR_TEMP_COLD, 1, SystemControlManager.rgb_type.G_GAIN, value);
                    break;
                case 3:
                    mSystemControlManager.SetColorTemperatureUserParam(SystemControlManager.color_temperature.COLOR_TEMP_USER, 1, SystemControlManager.rgb_type.G_GAIN, value);
                    break;
                default:
                    mSystemControlManager.SetColorTemperatureUserParam(SystemControlManager.color_temperature.COLOR_TEMP_STANDARD, 1, SystemControlManager.rgb_type.G_GAIN, value);
                    break;
        }
    }

    public void setAdvancedColorTemperatureBGainStatus (int value) {
        int currentColorTemperatureType = mSystemControlManager.GetColorTemperature();
        switch (currentColorTemperatureType) {
                case 0:
                    mSystemControlManager.SetColorTemperatureUserParam(SystemControlManager.color_temperature.COLOR_TEMP_STANDARD, 1, SystemControlManager.rgb_type.B_GAIN, value);
                    break;
                case 1:
                    mSystemControlManager.SetColorTemperatureUserParam(SystemControlManager.color_temperature.COLOR_TEMP_WARM, 1, SystemControlManager.rgb_type.B_GAIN, value);
                    break;
                case 2:
                    mSystemControlManager.SetColorTemperatureUserParam(SystemControlManager.color_temperature. COLOR_TEMP_COLD, 1, SystemControlManager.rgb_type.B_GAIN, value);
                    break;
                case 3:
                    mSystemControlManager.SetColorTemperatureUserParam(SystemControlManager.color_temperature.COLOR_TEMP_USER, 1, SystemControlManager.rgb_type.B_GAIN, value);
                    break;
                default:
                    mSystemControlManager.SetColorTemperatureUserParam(SystemControlManager.color_temperature.COLOR_TEMP_STANDARD, 1, SystemControlManager.rgb_type.B_GAIN, value);
                    break;
        }
    }

    public void setAdvancedColorTemperatureROffsetStatus (int value) {
        int currentColorTemperatureType = mSystemControlManager.GetColorTemperature();
        switch (currentColorTemperatureType) {
                case 0:
                    mSystemControlManager.SetColorTemperatureUserParam(SystemControlManager.color_temperature.COLOR_TEMP_STANDARD, 1, SystemControlManager.rgb_type.R_POST_OFFSET, value);
                    break;
                case 1:
                    mSystemControlManager.SetColorTemperatureUserParam(SystemControlManager.color_temperature.COLOR_TEMP_WARM, 1, SystemControlManager.rgb_type.R_POST_OFFSET, value);
                    break;
                case 2:
                    mSystemControlManager.SetColorTemperatureUserParam(SystemControlManager.color_temperature. COLOR_TEMP_COLD, 1, SystemControlManager.rgb_type.R_POST_OFFSET, value);
                    break;
                case 3:
                    mSystemControlManager.SetColorTemperatureUserParam(SystemControlManager.color_temperature.COLOR_TEMP_USER, 1, SystemControlManager.rgb_type.R_POST_OFFSET, value);
                    break;
                default:
                    mSystemControlManager.SetColorTemperatureUserParam(SystemControlManager.color_temperature.COLOR_TEMP_STANDARD, 1, SystemControlManager.rgb_type.R_POST_OFFSET, value);
                    break;
        }
    }

    public void setAdvancedColorTemperatureGOffsetStatus (int value) {
        int currentColorTemperatureType = mSystemControlManager.GetColorTemperature();
        switch (currentColorTemperatureType) {
                case 0:
                    mSystemControlManager.SetColorTemperatureUserParam(SystemControlManager.color_temperature.COLOR_TEMP_STANDARD, 1, SystemControlManager.rgb_type.G_POST_OFFSET, value);
                    break;
                case 1:
                    mSystemControlManager.SetColorTemperatureUserParam(SystemControlManager.color_temperature.COLOR_TEMP_WARM, 1, SystemControlManager.rgb_type.G_POST_OFFSET, value);
                    break;
                case 2:
                    mSystemControlManager.SetColorTemperatureUserParam(SystemControlManager.color_temperature. COLOR_TEMP_COLD, 1, SystemControlManager.rgb_type.G_POST_OFFSET, value);
                    break;
                case 3:
                    mSystemControlManager.SetColorTemperatureUserParam(SystemControlManager.color_temperature.COLOR_TEMP_USER, 1, SystemControlManager.rgb_type.G_POST_OFFSET, value);
                    break;
                default:
                    mSystemControlManager.SetColorTemperatureUserParam(SystemControlManager.color_temperature.COLOR_TEMP_STANDARD, 1, SystemControlManager.rgb_type.G_POST_OFFSET, value);
                    break;
        }
    }

    public void setAdvancedColorTemperatureBOffsetStatus (int value) {
        int currentColorTemperatureType = mSystemControlManager.GetColorTemperature();
        switch (currentColorTemperatureType) {
                case 0:
                    mSystemControlManager.SetColorTemperatureUserParam(SystemControlManager.color_temperature.COLOR_TEMP_STANDARD, 1, SystemControlManager.rgb_type.B_POST_OFFSET, value);
                    break;
                case 1:
                    mSystemControlManager.SetColorTemperatureUserParam(SystemControlManager.color_temperature.COLOR_TEMP_WARM, 1, SystemControlManager.rgb_type.B_POST_OFFSET, value);
                    break;
                case 2:
                    mSystemControlManager.SetColorTemperatureUserParam(SystemControlManager.color_temperature. COLOR_TEMP_COLD, 1, SystemControlManager.rgb_type.B_POST_OFFSET, value);
                    break;
                case 3:
                    mSystemControlManager.SetColorTemperatureUserParam(SystemControlManager.color_temperature.COLOR_TEMP_USER, 1, SystemControlManager.rgb_type.B_POST_OFFSET, value);
                    break;
                default:
                    mSystemControlManager.SetColorTemperatureUserParam(SystemControlManager.color_temperature.COLOR_TEMP_STANDARD, 1, SystemControlManager.rgb_type.B_POST_OFFSET, value);
                    break;
        }
    }

    //0 1 2 3 4 ~ off low medium high auto
    public void setDnr (int mode) {
        logDebug(TAG, false, "setDnr : "+ mode);
        mSystemControlManager.SetNoiseReductionMode(mode, 1);
    }

    public void setAdvancedColorCustomizeBlueSaturationStatus(int step) {
        // Leave blank first, add later
    }

    public void setAdvancedColorCustomizeBlueLumaStatus(int step) {
        // Leave blank first, add later
    }

    public void setAdvancedColorCustomizeBlueHueStatus(int step) {
        // Leave blank first, add later
    }

    public void setAdvancedColorCustomizeBlueGreenSaturationStatus(int step) {
        // Leave blank first, add later
    }

    public void setAdvancedColorCustomizeBlueGreenLumaStatus(int step) {
        // Leave blank first, add later
    }

    public void setAdvancedColorCustomizeBlueGreenHueStatus(int step) {
        // Leave blank first, add later
    }

    public void setAdvancedColorCustomizeCyanSaturationStatus(int step) {
        // Leave blank first, add later
    }

    public void setAdvancedColorCustomizeCyanLumaStatus(int step) {
        // Leave blank first, add later
    }

    public void setAdvancedColorCustomizeCyanHueStatus(int step) {
        // Leave blank first, add later
    }

    public void setAdvancedColorCustomizeGreenSaturationStatus(int step) {
        // Leave blank first, add later
    }

    public void setAdvancedColorCustomizeGreenLumaStatus(int step) {
        // Leave blank first, add later
    }

    public void setAdvancedColorCustomizeGreenHueStatus(int step) {
        // Leave blank first, add later
    }

    public void setAdvancedColorCustomizeMagentaSaturationStatus(int step) {
        // Leave blank first, add later
    }

    public void setAdvancedColorCustomizeMagentaLumaStatus(int step) {
        // Leave blank first, add later
    }

    public void setAdvancedColorCustomizeMagentaHueStatus(int step) {
        // Leave blank first, add later
    }

    public void setAdvancedColorCustomizeRedSaturationStatus(int step) {
        // Leave blank first, add later
    }

    public void setAdvancedColorCustomizeRedLumaStatus(int step) {
        // Leave blank first, add later
    }

    public void setAdvancedColorCustomizeRedHueStatus(int step) {
        // Leave blank first, add later
    }

    public void setAdvancedColorCustomizeSkinSaturationStatus(int step) {
        // Leave blank first, add later
    }

    public void setAdvancedColorCustomizeSkinLumaStatus(int step) {
        // Leave blank first, add later
    }

    public void setAdvancedColorCustomizeSkinHueStatus(int step) {
        // Leave blank first, add later
    }

    public void setAdvancedColorCustomizeYellowSaturationStatus(int step) {
        // Leave blank first, add later
    }

    public void setAdvancedColorCustomizeYellowLumaStatus(int step) {
        // Leave blank first, add later
    }

    public void setAdvancedColorCustomizeYellowHueStatus(int step) {
        // Leave blank first, add later
    }

    public void setAdvancedColorCustomizeYellowGreenSaturationStatus(int step) {
        // Leave blank first, add later
    }

    public void setAdvancedColorCustomizeYellowGreenLumaStatus(int step) {
        // Leave blank first, add later
    }

    public void setAdvancedColorCustomizeYellowGreenHueStatus(int step) {
        // Leave blank first, add later
    }

    private int setPictureUserMode(String key) {
        logDebug(TAG, false, "setPictureUserMode : "+ key);
        int brightness = mSystemControlManager.GetBrightness();
        int contrast = mSystemControlManager.GetContrast();
        int color = mSystemControlManager.GetSaturation();
        int sharpness = mSystemControlManager.GetSharpness();
        int tint = -1;
        tint = mSystemControlManager.GetHue();
        int ret = -1;

        switch (mSystemControlManager.GetPQMode()) {
            case PIC_STANDARD:
            case PIC_VIVID:
            case PIC_SOFT:
            case PIC_MONITOR:
            case PIC_SPORT:
            case PIC_MOVIE:
            case PIC_GAME:
                setPictureMode(STATUS_USER);
                break;
            default:
                break;
        }

        logDebug(TAG, false, " brightness=" + brightness + " contrast=" + contrast + " color=" + color + " sharp=" + sharpness);
        if (!key.equals(KEY_BRIGHTNESS))
            mSystemControlManager.SetBrightness(brightness, 1);
        else
            ret = brightness;

        if (!key.equals(KEY_CONTRAST))
            mSystemControlManager.SetContrast(contrast, 1);
        else
            ret = contrast;

        if (!key.equals(KEY_COLOR))
            mSystemControlManager.SetSaturation(color, 1);
        else
            ret = color;

        if (!key.equals(KEY_SHARPNESS))
            mSystemControlManager.SetSharpness(sharpness, 1 , 1);
        else
            ret = sharpness;

        if (!key.equals(KEY_TONE))
            mSystemControlManager.SetHue(tint, 1);
        else
            ret = tint;
        return ret;
    }

    public void setBacklightValue (int value) {
        logDebug(TAG, false, "setBacklightValue : "+ value);
        mSystemControlManager.SetBacklight(getBacklightStatus() + value, 1);
    }

    public int getBacklightStatus () {
        int value = mSystemControlManager.GetBacklight();
        logDebug(TAG, false, "getBacklightStatus : " + value);
        return value;
    }

    public int SSMRecovery() {
        int value = mSystemControlManager.SSMRecovery();
        logDebug(TAG, false, "SSMRecovery : " + value);
        if (value == 1) {
            //srcInputParam not used
            SourceInputParam srcInputParam= new SourceInputParam();
            value = mSystemControlManager.LoadPQSettings(srcInputParam);
        }
        return value;
    }

    private static final int AUTO_RANGE = 0;
    private static final int FULL_RANGE = 1;
    private static final int LIMIT_RANGE = 2;

    public void setHdmiColorRangeValue (int value) {
        logDebug(TAG, false, "setHdmiColorRangeValue : "+ value);
        TvControlManager.HdmiColorRangeMode hdmicolor = TvControlManager.HdmiColorRangeMode.AUTO_RANGE;
        switch (value) {
            case AUTO_RANGE :
                hdmicolor = TvControlManager.HdmiColorRangeMode.AUTO_RANGE;
                break;
            case FULL_RANGE :
                hdmicolor = TvControlManager.HdmiColorRangeMode.FULL_RANGE;
                break;
            case LIMIT_RANGE :
                hdmicolor = TvControlManager.HdmiColorRangeMode.LIMIT_RANGE;
                break;
            default:
                hdmicolor = TvControlManager.HdmiColorRangeMode.AUTO_RANGE;
                break;
        }
        if (mTvControlManager == null) {
            mTvControlManager = TvControlManager.getInstance();
        }
        if (mTvControlManager != null) {
            mTvControlManager.SetHdmiColorRangeMode(hdmicolor);
        }
    }

    public int getHdmiColorRangeStatus () {
        int value = 0;
        if (mTvControlManager == null) {
            mTvControlManager = TvControlManager.getInstance();
        }
        if (mTvControlManager != null) {
            value = mTvControlManager.GetHdmiColorRangeMode();
        }
        logDebug(TAG, false, "getHdmiColorRangeStatus : " + value);
        return value;
    }

    public int getAiColor() {
        return mSystemControlManager.GetAiColor();
    }

    public int setAiColor(int value, int isSave) {
        logDebug(TAG, false, "setAiColor value: " + value + ", isSave: " + isSave);
        return mSystemControlManager.SetAiColor(value, isSave);
    }

    public int getMemcDemoEnabled() {
        final int MEMC_DEMO = 0;
        return mSystemControlManager.GetPQModuleDemoState(MEMC_DEMO);
    }

    public int setMemcDemoEnabled(boolean enable) {
        final int MEMC_DEMO = 0;
        int stateValue = 0;
        if (enable) {
            stateValue = 1;
        }
        return mSystemControlManager.SetPQModuleDemoState(MEMC_DEMO, stateValue);
    }

    public int getAisreDemoEnabled() {
        final int AISR_DEMO = 1;
        return mSystemControlManager.GetPQModuleDemoState(AISR_DEMO);
    }

    public int setAisreDemoEnabled(boolean enable) {
        final int AISR_DEMO = 1;
        int stateValue = 0;
        if (enable) {
            stateValue = 1;
        }
        return mSystemControlManager.SetPQModuleDemoState(AISR_DEMO, stateValue);
    }

    public boolean isHdmiSource() {
        if (mTvSourceInput != null) {
            return mTvSource == TvControlManager.SourceInput_Type.SOURCE_TYPE_HDMI;
        }
        return false;
    }

    public boolean isAvSource() {
        if (mTvSourceInput != null) {
            return mTvSource == TvControlManager.SourceInput_Type.SOURCE_TYPE_AV;
        }
        return false;
    }

    public boolean isAtvSource() {
        if (mTvSourceInput != null) {
            return mTvSource == TvControlManager.SourceInput_Type.SOURCE_TYPE_TV;
        }
        return false;
    }

    public boolean isDtvSource() {
        if (mTvSourceInput != null) {
            return mTvSource == TvControlManager.SourceInput_Type.SOURCE_TYPE_DTV
                || mTvSource == TvControlManager.SourceInput_Type.SOURCE_TYPE_ADTV;
        }
        return false;
    }

    public boolean isMpegSource() {
        if (mTvSourceInput != null) {
            return mTvSource == TvControlManager.SourceInput_Type.SOURCE_TYPE_MPEG;
        }
        return false;
    }
    public boolean isNullSource() {
        if (mTvSourceInput == null) {
            return true;
        }
        return false;
    }

    public boolean hasSmoothPlusFunc() {
        return mSystemControlManager.hasSmoothPlusFunc();
    }
}
