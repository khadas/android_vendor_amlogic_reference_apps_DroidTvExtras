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

package com.droidlogic.tv.extras;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.net.ConnectivityManager;
import android.widget.Toast;
import android.net.NetworkInfo;

import androidx.preference.Preference;
import androidx.preference.TwoStatePreference;

import com.droidlogic.app.SystemControlManager;
import com.droidlogic.app.tv.TvControlDataManager;
import static com.droidlogic.tv.extras.util.DroidUtils.logDebug;


public class WolFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "WolFragment";
    private static int mNetworkType = ConnectivityManager.TYPE_NONE;

    private static final String KEY_ENABLE_WOL = "wake_on_lan_enable";
    Toast mDevHitToast;

    //private static final String PROP_AIPQ_ENABLE = "persist.vendor.sys.aipq.info";
    private static final String SAVE_WOL = "WOL";
    private static final int WOL_ENABLE = 1;
    private static final int WOL_DISABLE = 0;
    int WOL_MODE = 0;

    private Context mContext;
    private SystemControlManager mSystemControlManager;
    private TwoStatePreference enableWolPref;

    public static WolFragment newInstance() {
        return new WolFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.wake_on_lan, null);
        mSystemControlManager = SystemControlManager.getInstance();
        enableWolPref = (TwoStatePreference) findPreference(KEY_ENABLE_WOL);
        logDebug(TAG, false, "enableWolPref :" + enableWolPref);
        enableWolPref.setOnPreferenceChangeListener(this);
        WOL_MODE = TvControlDataManager.getInstance(mContext).getInt(mContext.getContentResolver(), SAVE_WOL, 0);
        if (WOL_MODE == 1) {
            enableWolPref.setChecked(true);
        }
        logDebug(TAG, false, "WOL_MODE :" + WOL_MODE);
        if (getWolEnabled()) {
            enableWolPref.setEnabled(true);
        }else{
            enableWolPref.setEnabled(false);
            enableWolPref.setChecked(false);
            setWolEnabled(false);
            WOL_MODE = 0;
            //Settings.System.putInt(getActivity().getContentResolver(), SAVE_WOL, WOL_DISABLE);
            mDevHitToast = Toast.makeText(getActivity(), R.string.show_ethernet_request, Toast.LENGTH_LONG);
            mDevHitToast.show();
            logDebug(TAG, false, "no ethernet");
        }
        //enableWolPref.setChecked(getWolEnabled());
        //mTvControlDataProvider.insert(SAVE_WOL , WOL_DISABLE);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean keyValue = (boolean) newValue;
        WOL_MODE = keyValue ? 1 : 0;
        setWolEnabled(keyValue);
        TvControlDataManager.getInstance(mContext).putInt(
                mContext.getContentResolver(), SAVE_WOL, keyValue ? WOL_ENABLE : WOL_DISABLE);
        logDebug(TAG, false, "WOL_MODE :" + WOL_MODE);
        return true;
    }

    private boolean getWolEnabled() {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
        boolean isConnected = false;
        if (networkInfo != null) {
            mNetworkType = networkInfo.getType();
            isConnected = isEthernetConnected();
        }
        return isConnected;
    }

    private void setWolEnabled(boolean enable) {
        mSystemControlManager.writeSysFs("/sys/class/ethernet/wol", enable ? "1" : "0");
    }
    private static boolean isEthernetConnected() {
        return mNetworkType == ConnectivityManager.TYPE_ETHERNET;
    }

}
