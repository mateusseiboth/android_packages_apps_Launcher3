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

import android.content.Context
import com.android.launcher3.LauncherAppState
import com.android.launcher3.LauncherSettings.Favorites.CONTAINER_DESKTOP
import com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
import com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_DEEP_SHORTCUT
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.model.AddToDesktopTask
import com.android.launcher3.model.data.ItemInfo

/**
 * "Send to desktop" in the menu of an app wherever apps are listed on a PC (taskbar, its app list
 * and search, the home screen's All Apps): the desktop holds only what the user puts there.
 */
object SendToDesktop {

    private val SENDABLE_TYPES = setOf(ITEM_TYPE_APPLICATION, ITEM_TYPE_DEEP_SHORTCUT)

    @JvmStatic
    fun isOffered(context: Context, item: ItemInfo): Boolean =
        Utilities.isDesktopFormFactor(context) &&
            Utilities.isWorkspaceEditAllowed(context) &&
            item.container != CONTAINER_DESKTOP &&
            item.itemType in SENDABLE_TYPES &&
            item.targetComponent != null

    @JvmStatic
    fun send(context: Context, item: ItemInfo) {
        LauncherAppState.getInstance(context).model.enqueueModelUpdateTask(AddToDesktopTask(item))
    }

    /** "Remove from desktop" for the desktop's items on a PC, "Remove" elsewhere. */
    @JvmStatic
    fun buildRemoveLabel(context: Context, item: ItemInfo): Int =
        R.string.remove_from_desktop_label.takeIf {
            Utilities.isDesktopFormFactor(context) && item.container == CONTAINER_DESKTOP
        } ?: R.string.remove_system_shortcut_label
}
