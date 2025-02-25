/*
 * Copyright (C) 2014 The Android Open Source Project
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
 * limitations under the License.
 */

package com.droidlogic.tv.extras.pqsettings.advanced;

import androidx.fragment.app.Fragment;

import com.droidlogic.tv.extras.TvSettingsActivity;
import com.droidlogic.tv.extras.overlay.FlavorUtils;

/**
 * Activity to adjust multi picture option.
 */
public class PQAdvancedColorCustomizeYellowGreenActivity extends TvSettingsActivity {

    @Override
    protected Fragment createSettingsFragment() {
        return FlavorUtils.getFeatureFactory(this).getSettingsFragmentProvider()
            .newSettingsFragment(PQAdvancedColorCustomizeYellowGreenFragment.class.getName(), null);
    }

}
