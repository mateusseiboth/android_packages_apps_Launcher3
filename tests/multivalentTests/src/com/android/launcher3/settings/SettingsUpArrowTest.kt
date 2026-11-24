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

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsUpArrowTest {

    @Test
    fun mainScreen_hasNoUpArrow() {
        assertFalse(
            SettingsUpArrow.isShown(opensSubScreen = false, isTaskRoot = false, hidesOnTaskRoot = true)
        )
    }

    @Test
    fun subScreenOpenedFromSettings_showsUpArrow() {
        assertTrue(
            SettingsUpArrow.isShown(opensSubScreen = true, isTaskRoot = false, hidesOnTaskRoot = true)
        )
    }

    @Test
    fun subScreenAsTaskRoot_hidesUpArrow() {
        assertFalse(
            SettingsUpArrow.isShown(opensSubScreen = true, isTaskRoot = true, hidesOnTaskRoot = true)
        )
    }

    @Test
    fun subScreenAsTaskRoot_keepsUpArrowWhenNotHiding() {
        assertTrue(
            SettingsUpArrow.isShown(opensSubScreen = true, isTaskRoot = true, hidesOnTaskRoot = false)
        )
    }
}
