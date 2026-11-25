/*
 * Copyright (C) 2026 The LineageOS Project
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

package com.android.launcher3.taskbar.allapps

import kotlin.math.max
import kotlin.math.min

/**
 * Geometry of All Apps on the desktop form factor: a floating panel above the taskbar, two thirds of the screen tall at most, instead of a full height bottom sheet.
 */
object DesktopAllAppsSheetGeometry {

    /** Height of the panel: two thirds of [availableHeightPx], capped at [maxHeightPx]. */
    @JvmStatic
    fun height(availableHeightPx: Int, maxHeightPx: Int, paddingPx: Int): Int =
        min(maxHeightPx, availableHeightPx * 2 / 3) + paddingPx

    /** Space below the panel: [sheetMarginPx] above the taskbar or the bottom inset. */
    @JvmStatic
    fun bottomMargin(sheetMarginPx: Int, insetBottomPx: Int, taskbarHeightPx: Int): Int =
        sheetMarginPx + max(insetBottomPx, taskbarHeightPx)

    /** Width of the panel: [maxWidthPx], or the screen less [screenMarginPx] on each side. */
    @JvmStatic
    fun width(availableWidthPx: Int, maxWidthPx: Int, screenMarginPx: Int): Int =
        min(maxWidthPx, availableWidthPx - 2 * screenMarginPx)

    /**
     * How far the background is drawn below the panel. A bottom sheet extends it by
     * [sheetOffsetPx] to fill the gap left while predictive back scales it down; a floating panel
     * keeps its bottom corners above the taskbar.
     */
    @JvmStatic
    fun backgroundBottomOffset(isFloating: Boolean, sheetOffsetPx: Int): Int =
        sheetOffsetPx.takeUnless { isFloating } ?: 0
}
