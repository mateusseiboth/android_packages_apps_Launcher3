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

package com.android.launcher3.popup

import com.android.launcher3.LauncherSettings.Favorites.CONTAINER_ALL_APPS
import com.android.launcher3.LauncherSettings.Favorites.CONTAINER_ALL_APPS_PREDICTION
import com.android.launcher3.LauncherSettings.Favorites.CONTAINER_DESKTOP
import com.android.launcher3.LauncherSettings.Favorites.CONTAINER_HOTSEAT
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AllAppsUninstallTest {

    @Test
    fun offeredForAllAppsItemsOnTheDesktopFormFactor() {
        assertTrue(AllAppsUninstall.isOffered(CONTAINER_ALL_APPS, isDesktopFormFactor = true))
        assertTrue(
            AllAppsUninstall.isOffered(CONTAINER_ALL_APPS_PREDICTION, isDesktopFormFactor = true)
        )
    }

    @Test
    fun notOfferedOnOtherFormFactors() {
        assertFalse(AllAppsUninstall.isOffered(CONTAINER_ALL_APPS, isDesktopFormFactor = false))
    }

    @Test
    fun notOfferedOutsideAllApps() {
        assertFalse(AllAppsUninstall.isOffered(CONTAINER_HOTSEAT, isDesktopFormFactor = true))
        assertFalse(AllAppsUninstall.isOffered(CONTAINER_DESKTOP, isDesktopFormFactor = true))
    }
}
