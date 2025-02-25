/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.droidlogic.tv.extras;

import static androidx.lifecycle.Lifecycle.Event.ON_CREATE;
import static androidx.lifecycle.Lifecycle.Event.ON_DESTROY;
import static androidx.lifecycle.Lifecycle.Event.ON_PAUSE;
import static androidx.lifecycle.Lifecycle.Event.ON_RESUME;
import static androidx.lifecycle.Lifecycle.Event.ON_START;
import static androidx.lifecycle.Lifecycle.Event.ON_STOP;


import android.animation.AnimatorInflater;
import android.annotation.CallSuper;
import android.app.tvsettings.TvSettingsEnums;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceGroupAdapter;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settingslib.core.instrumentation.Instrumentable;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.settingslib.core.instrumentation.VisibilityLoggerMixin;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.droidlogic.tv.extras.overlay.FlavorUtils;
import com.droidlogic.tv.extras.util.SettingsPreferenceUtil;
import com.droidlogic.tv.extras.widget.SettingsViewModel;
import com.droidlogic.tv.extras.widget.TsPreference;
import static com.droidlogic.tv.extras.util.InstrumentationUtils.logPageFocused;
import com.android.tv.twopanelsettings.TwoPanelSettingsFragment;

/**
 * A {@link LeanbackPreferenceFragmentCompat} that has hooks to observe fragment lifecycle events
 * and allow for instrumentation.
 */
public abstract class SettingsPreferenceFragment extends LeanbackPreferenceFragmentCompat
        implements LifecycleOwner, Instrumentable,
        TwoPanelSettingsFragment.PreviewableComponentCallback {
    private final Lifecycle mLifecycle = new Lifecycle(this);
    private final VisibilityLoggerMixin mVisibilityLoggerMixin;
    protected MetricsFeatureProvider mMetricsFeatureProvider;

    // Rename getLifecycle() to getSettingsLifecycle() as androidx Fragment has already implemented
    // getLifecycle(), overriding here would cause unexpected crash in framework.
    @NonNull
    public Lifecycle getSettingsLifecycle() {
        return mLifecycle;
    }

    public SettingsPreferenceFragment() {
        mMetricsFeatureProvider = new MetricsFeatureProvider();
        // Mixin that logs visibility change for activity.
        mVisibilityLoggerMixin = new VisibilityLoggerMixin(getMetricsCategory(),
                mMetricsFeatureProvider);
        getSettingsLifecycle().addObserver(mVisibilityLoggerMixin);
    }

    @CallSuper
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mLifecycle.onAttach(context);
    }

    @CallSuper
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mLifecycle.onCreate(savedInstanceState);
        mLifecycle.handleLifecycleEvent(ON_CREATE);
        super.onCreate(savedInstanceState);
        if (getCallbackFragment() != null
                && !(getCallbackFragment() instanceof TwoPanelSettingsFragment)) {
            logPageFocused(getPageId(), true);
        }
    }

    // While the default of relying on text language to determine gravity works well in general,
    // some page titles (e.g., SSID as Wifi details page title) are dynamic and can be in different
    // languages. This can cause some complex gravity issues. For example, Wifi details page in RTL
    // showing an English SSID title would by default align the title to the left, which is
    // incorrectly considered as START in RTL.
    // We explicitly set the title gravity to RIGHT in RTL cases to remedy this issue.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (view != null) {
            TextView titleView = view.findViewById(R.id.decor_title);
            // We rely on getResources().getConfiguration().getLayoutDirection() instead of
            // view.isLayoutRtl() as the latter could return false in some complex scenarios even if
            // it is RTL.
            if (titleView != null
                    && getResources().getConfiguration().getLayoutDirection()
                        == View.LAYOUT_DIRECTION_RTL) {
                titleView.setGravity(Gravity.RIGHT);
            }
            if (FlavorUtils.isTwoPanel(getContext())) {
                ViewGroup decor = view.findViewById(R.id.decor_title_container);
                if (decor != null) {
                    decor.setOutlineProvider(null);
                    decor.setBackgroundResource(R.color.tp_preference_panel_background_color);
                }
            }
            removeAnimationClipping(view);
        }
        SettingsViewModel settingsViewModel = new ViewModelProvider(this.getActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(
                        this.getActivity().getApplication())).get(SettingsViewModel.class);
        iteratePreferenceAndSetObserver(settingsViewModel, getPreferenceScreen());
    }

    private void iteratePreferenceAndSetObserver(SettingsViewModel viewModel,
            PreferenceGroup preferenceGroup) {
        for (int i = 0; i < preferenceGroup.getPreferenceCount(); i++) {
            Preference pref = preferenceGroup.getPreference(i);
            if (pref instanceof TsPreference
                    && ((TsPreference) pref).updatableFromGoogleSettings()) {
                viewModel.getVisibilityLiveData(
                        SettingsPreferenceUtil.getCompoundKey(this, pref))
                        .observe(getViewLifecycleOwner(), (Boolean b) -> pref.setVisible(b));
            }
            if (pref instanceof PreferenceGroup) {
                iteratePreferenceAndSetObserver(viewModel, (PreferenceGroup) pref);
            }
        }
    }

    protected void removeAnimationClipping(View v) {
        if (v instanceof ViewGroup) {
            ((ViewGroup) v).setClipChildren(false);
            ((ViewGroup) v).setClipToPadding(false);
            for (int index = 0; index < ((ViewGroup) v).getChildCount(); index++) {
                View child = ((ViewGroup) v).getChildAt(index);
                removeAnimationClipping(child);
            }
        }
    }

    @Override
    protected RecyclerView.Adapter onCreateAdapter(PreferenceScreen preferenceScreen) {
        if (FlavorUtils.isTwoPanel(getContext())) {
            return new PreferenceGroupAdapter(preferenceScreen) {
                @Override
                @NonNull
                public PreferenceViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                        int viewType) {
                    PreferenceViewHolder vh = super.onCreateViewHolder(parent, viewType);
                    vh.itemView.setStateListAnimator(AnimatorInflater.loadStateListAnimator(
                            getContext(), R.animator.preference));
                    return vh;
                }
            };
        }
        return new PreferenceGroupAdapter(preferenceScreen);
    }

    @Override
    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        mLifecycle.setPreferenceScreen(preferenceScreen);
        super.setPreferenceScreen(preferenceScreen);
    }

    @CallSuper
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mLifecycle.onSaveInstanceState(outState);
    }

    @CallSuper
    @Override
    public void onStart() {
        mLifecycle.handleLifecycleEvent(ON_START);
        super.onStart();
    }

    @CallSuper
    @Override
    public void onResume() {
        mVisibilityLoggerMixin.setSourceMetricsCategory(getActivity());
        super.onResume();
        mLifecycle.handleLifecycleEvent(ON_RESUME);
        if (getCallbackFragment() instanceof TwoPanelSettingsFragment) {
            TwoPanelSettingsFragment parentFragment =
                    (TwoPanelSettingsFragment) getCallbackFragment();
            parentFragment.addListenerForFragment(this);
        }
    }

    // This should only be invoked if the parent Fragment is TwoPanelSettingsFragment.
    @CallSuper
    @Override
    public void onArriveAtMainPanel(boolean forward) {
        logPageFocused(getPageId(), forward);
    }

    @CallSuper
    @Override
    public void onPause() {
        mLifecycle.handleLifecycleEvent(ON_PAUSE);
        super.onPause();
        if (getCallbackFragment() instanceof TwoPanelSettingsFragment) {
            TwoPanelSettingsFragment parentFragment =
                    (TwoPanelSettingsFragment) getCallbackFragment();
            parentFragment.removeListenerForFragment(this);
        }
    }

    @CallSuper
    @Override
    public void onStop() {
        mLifecycle.handleLifecycleEvent(ON_STOP);
        super.onStop();
    }

    @CallSuper
    @Override
    public void onDestroy() {
        mLifecycle.handleLifecycleEvent(ON_DESTROY);
        super.onDestroy();
    }

    @CallSuper
    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        mLifecycle.onCreateOptionsMenu(menu, inflater);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @CallSuper
    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        mLifecycle.onPrepareOptionsMenu(menu);
        super.onPrepareOptionsMenu(menu);
    }

    @CallSuper
    @Override
    public boolean onOptionsItemSelected(final MenuItem menuItem) {
        boolean lifecycleHandled = mLifecycle.onOptionsItemSelected(menuItem);
        if (!lifecycleHandled) {
            return super.onOptionsItemSelected(menuItem);
        }
        return lifecycleHandled;
    }

    /** Subclasses should override this to use their own PageId for statsd logging. */
    protected int getPageId() {
        return TvSettingsEnums.PAGE_CLASSIC_DEFAULT;
    }
}
