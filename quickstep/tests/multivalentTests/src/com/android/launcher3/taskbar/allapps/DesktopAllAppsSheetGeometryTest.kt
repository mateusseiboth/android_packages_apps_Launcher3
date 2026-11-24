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

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DesktopAllAppsSheetGeometryTest {

    @Test
    fun height_tallScreen_isCappedAtTheMaximum() {
        assertThat(DesktopAllAppsSheetGeometry.height(4000, maxHeightPx = 1280, paddingPx = 40))
            .isEqualTo(1320)
    }

    @Test
    fun height_shortScreen_isTwoThirdsOfTheScreen() {
        assertThat(DesktopAllAppsSheetGeometry.height(900, maxHeightPx = 1280, paddingPx = 40))
            .isEqualTo(640)
    }

    @Test
    fun bottomMargin_sitsAboveTheTaskbar() {
        assertThat(
                DesktopAllAppsSheetGeometry.bottomMargin(
                    sheetMarginPx = 8,
                    insetBottomPx = 0,
                    taskbarHeightPx = 56,
                )
            )
            .isEqualTo(64)
    }

    @Test
    fun bottomMargin_largerInset_sitsAboveTheInset() {
        assertThat(
                DesktopAllAppsSheetGeometry.bottomMargin(
                    sheetMarginPx = 8,
                    insetBottomPx = 80,
                    taskbarHeightPx = 56,
                )
            )
            .isEqualTo(88)
    }
}
