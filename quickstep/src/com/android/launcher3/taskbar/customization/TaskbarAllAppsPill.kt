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

package com.android.launcher3.taskbar.customization

import kotlin.math.max

/** Size of the pill shaped All Apps button of the desktop taskbar, with its icon centered. */
data class TaskbarAllAppsPill(val widthPx: Int, val heightPx: Int, val iconSizePx: Int) {

    val horizontalPaddingPx: Int
        get() = (widthPx - iconSizePx) / 2

    val verticalPaddingPx: Int
        get() = (heightPx - iconSizePx) / 2

    /** Width the pill takes beyond the touch area of an app icon. */
    fun extraWidthOver(touchSizePx: Int): Int = max(0, widthPx - touchSizePx)
}
