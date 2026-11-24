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

package com.android.launcher3.settings

/** Whether Home settings shows the up arrow in its action bar. */
object SettingsUpArrow {

    /**
     * @param opensSubScreen the screen was opened on a sub screen or a highlighted preference.
     * @param isTaskRoot the screen is the root of its task, so there is nothing to go up to.
     * @param hidesOnTaskRoot the device hides the arrow when there is nothing to go up to.
     */
    @JvmStatic
    fun isShown(opensSubScreen: Boolean, isTaskRoot: Boolean, hidesOnTaskRoot: Boolean): Boolean {
        if (!opensSubScreen) return false
        return !(hidesOnTaskRoot && isTaskRoot)
    }
}
