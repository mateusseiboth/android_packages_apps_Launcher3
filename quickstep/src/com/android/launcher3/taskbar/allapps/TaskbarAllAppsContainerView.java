/*
 * Copyright (C) 2022 The Android Open Source Project
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
package com.android.launcher3.taskbar.allapps;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.android.launcher3.R;
import com.android.launcher3.allapps.ActivityAllAppsContainerView;
import com.android.launcher3.taskbar.overlay.TaskbarOverlayContext;

import java.util.Optional;

/** All apps container accessible from taskbar. */
public class TaskbarAllAppsContainerView extends
        ActivityAllAppsContainerView<TaskbarOverlayContext> {

    private @Nullable OnInvalidateHeaderListener mOnInvalidateHeaderListener;

    public TaskbarAllAppsContainerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TaskbarAllAppsContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    void setOnInvalidateHeaderListener(OnInvalidateHeaderListener onInvalidateHeaderListener) {
        mOnInvalidateHeaderListener = onInvalidateHeaderListener;
    }

    @Override
    public void invalidateHeader() {
        super.invalidateHeader();
        Optional.ofNullable(mOnInvalidateHeaderListener).ifPresent(
                OnInvalidateHeaderListener::onInvalidateHeader);
    }

    @Override
    protected boolean isFloatingSheet() {
        return getResources().getBoolean(R.bool.desktop_form_factor);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (!isFloatingSheet()) {
            return;
        }
        // A floating panel opens from the taskbar button and the keyboard; it has no handle.
        findViewById(R.id.bottom_sheet_handle).setVisibility(GONE);
        findViewById(R.id.bottom_sheet_handle_area).setVisibility(GONE);
    }

    @Override
    public void setInsets(Rect insets) {
        super.setInsets(insets);
        if (!isFloatingSheet()) {
            return;
        }
        // The floating panel is exactly as wide as this view (TaskbarAllAppsController sizes it),
        // so the panel needs no side margins; the search field keeps the inner padding.
        setPadding(0, getPaddingTop(), 0, getPaddingBottom());
        int innerPadding =
                getResources().getDimensionPixelSize(R.dimen.taskbar_all_apps_sheet_inner_padding);
        MarginLayoutParams searchLp = (MarginLayoutParams) getSearchView().getLayoutParams();
        searchLp.topMargin = innerPadding;
        searchLp.setMarginStart(innerPadding);
        searchLp.setMarginEnd(innerPadding);
        getSearchView().setLayoutParams(searchLp);
    }

    @Override
    public boolean isInAllApps() {
        // All apps is always open
        return true;
    }

    interface OnInvalidateHeaderListener {
        void onInvalidateHeader();
    }
}
