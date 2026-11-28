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

import com.android.launcher3.InvariantDeviceProfile
import com.android.launcher3.LauncherModel.ModelUpdateTask
import com.android.launcher3.LauncherSettings.Favorites.CONTAINER_DESKTOP
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.model.data.WorkspaceItemFactory
import com.android.launcher3.model.data.WorkspaceItemInfo
import com.android.launcher3.util.IntSet

/**
 * "Send to desktop": puts an app (or a Linux app's shortcut) at the desktop's first free cell, from
 * the first page on. An app that is already on the desktop stays where it is.
 */
class AddToDesktopTask(private val item: ItemInfo) : ModelUpdateTask {

    override fun execute(taskController: ModelTaskController, dataModel: BgDataModel, apps: AllAppsList) {
        val context = taskController.context
        val desktopItem = buildDesktopItem(item, taskController) ?: return
        synchronized(dataModel) {
            if (dataModel.itemsIdMap.any { isSameTarget(it, desktopItem) }) return
            val finder =
                WorkspaceItemSpaceFinder(
                    dataModel,
                    InvariantDeviceProfile.INSTANCE.get(context),
                    taskController.model,
                )
            finder.findSpaceForItem(ArrayList(), 1, 1, IntSet()).applyTo(desktopItem)
            taskController.getModelWriter().addItemsToDatabase(listOf(desktopItem))
        }
        taskController.scheduleCallbackTask { it.bindItemsAdded(listOf(desktopItem)) }
    }

    private fun buildDesktopItem(item: ItemInfo, taskController: ModelTaskController): WorkspaceItemInfo? {
        val desktopItem =
            (item as? WorkspaceItemFactory)?.makeWorkspaceItem(taskController.context)
                ?: (item as? WorkspaceItemInfo)?.clone()
                ?: return null
        desktopItem.id = ItemInfo.NO_ID
        desktopItem.container = CONTAINER_DESKTOP
        desktopItem.spanX = 1
        desktopItem.spanY = 1
        return desktopItem
    }

    private fun isSameTarget(existing: ItemInfo, item: WorkspaceItemInfo): Boolean =
        existing.container == CONTAINER_DESKTOP &&
            existing.itemType == item.itemType &&
            existing.user == item.user &&
            existing.getIntent()?.component == item.getIntent()?.component &&
            (existing as? WorkspaceItemInfo)?.deepShortcutId == item.deepShortcutId
}
