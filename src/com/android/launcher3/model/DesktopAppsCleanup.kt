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

package com.android.launcher3.model

import android.content.Context
import com.android.launcher3.EncryptionType
import com.android.launcher3.LauncherPrefs
import com.android.launcher3.LauncherPrefs.Companion.nonRestorableItem
import com.android.launcher3.LauncherSettings.Favorites.CONTAINER_DESKTOP
import com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
import com.android.launcher3.Utilities
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.util.IntSparseArrayMap

/**
 * On a PC the desktop holds files and the apps the user sends there. Earlier builds (and restores
 * from a phone) put every new app on it, so the app icons found there are taken off once, and again
 * after each restore. Files, folders, shortcuts and widgets stay.
 */
object DesktopAppsCleanup {

    @JvmField
    val PREF_DESKTOP_APPS_CLEARED =
        nonRestorableItem("pc_desktop_apps_cleared", false, EncryptionType.DEVICE_PROTECTED)

    @JvmStatic
    fun isDue(context: Context, prefs: LauncherPrefs, isRestoreFromBackup: Boolean): Boolean =
        Utilities.isDesktopFormFactor(context) &&
            (isRestoreFromBackup || !prefs.get(PREF_DESKTOP_APPS_CLEARED))

    /** Takes the desktop's app icons out of [loadedItems] and of the database. */
    @JvmStatic
    fun apply(loadedItems: IntSparseArrayMap<ItemInfo>, writer: IModelWriter, prefs: LauncherPrefs) {
        val apps = loadedItems.filter(::isDesktopApp)
        apps.forEach { loadedItems.remove(it.id) }
        if (apps.isNotEmpty()) {
            writer.deleteItemsFromDatabase(apps, "Apps are not added to a PC's desktop by themselves")
        }
        prefs.put(PREF_DESKTOP_APPS_CLEARED, true)
    }

    private fun isDesktopApp(item: ItemInfo): Boolean =
        item.container == CONTAINER_DESKTOP && item.itemType == ITEM_TYPE_APPLICATION
}
