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

package com.android.launcher3.taskbar

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SysUiPanelsCollapseTest {

    @Test
    fun nothingOpen_doesNotCollapse() {
        assertFalse(
            SysUiPanelsCollapse.shouldCollapse(
                isShadeExpanded = false,
                isQuickActionsShowing = false,
                closesQuickActions = true,
            )
        )
    }

    @Test
    fun shadeExpanded_collapses() {
        assertTrue(
            SysUiPanelsCollapse.shouldCollapse(
                isShadeExpanded = true,
                isQuickActionsShowing = false,
                closesQuickActions = false,
            )
        )
    }

    @Test
    fun quickActionsShowing_collapsesWhenEnabled() {
        assertTrue(
            SysUiPanelsCollapse.shouldCollapse(
                isShadeExpanded = false,
                isQuickActionsShowing = true,
                closesQuickActions = true,
            )
        )
    }

    @Test
    fun quickActionsShowing_staysWhenDisabled() {
        assertFalse(
            SysUiPanelsCollapse.shouldCollapse(
                isShadeExpanded = false,
                isQuickActionsShowing = true,
                closesQuickActions = false,
            )
        )
    }
}
