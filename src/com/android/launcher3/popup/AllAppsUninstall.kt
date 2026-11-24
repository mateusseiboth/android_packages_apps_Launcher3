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

/**
 * When an app's menu offers "Uninstall": in All Apps on the desktop form factor, where All Apps
 * opens from the taskbar and has no drop target to drag an app to.
 */
object AllAppsUninstall {

    private val ALL_APPS_CONTAINERS = setOf(CONTAINER_ALL_APPS, CONTAINER_ALL_APPS_PREDICTION)

    @JvmStatic
    fun isOffered(container: Int, isDesktopFormFactor: Boolean): Boolean =
        isDesktopFormFactor && container in ALL_APPS_CONTAINERS
}
