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

package com.android.launcher3.allapps

/** Corner radii of the All Apps sheet background. */
object AllAppsSheetCorners {

    /**
     * Radii (x, y per corner, clockwise from top left): a floating panel rounds every corner, a
     * bottom sheet only its top corners.
     */
    @JvmStatic
    fun radii(radius: Float, isFloating: Boolean): FloatArray {
        val bottomRadius = radius.takeIf { isFloating } ?: 0f
        return floatArrayOf(
            radius,
            radius,
            radius,
            radius,
            bottomRadius,
            bottomRadius,
            bottomRadius,
            bottomRadius,
        )
    }
}
