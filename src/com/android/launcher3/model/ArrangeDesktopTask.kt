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
import com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_FILE_SYSTEM_FILE
import com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_FILE_SYSTEM_FOLDER
import com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_FOLDER
import com.android.launcher3.Workspace
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.util.GridOccupancy
import java.text.Collator

/**
 * "Arrange by name" on the desktop of a PC: the icons line up from the first cell, row by row,
 * folders first, then files, then apps, each by name. Widgets stay where they are.
 */
class ArrangeDesktopTask : ModelUpdateTask {

    override fun execute(taskController: ModelTaskController, dataModel: BgDataModel, apps: AllAppsList) {
        val idp = InvariantDeviceProfile.INSTANCE.get(taskController.context)
        val collator = Collator.getInstance()
        synchronized(dataModel) {
            val desktopItems = dataModel.itemsIdMap.filter { it.container == CONTAINER_DESKTOP }
            val (icons, fixed) = desktopItems.partition { it.spanX == 1 && it.spanY == 1 }
            if (icons.isEmpty()) return
            val sorted =
                icons.sortedWith(
                    compareBy<ItemInfo> { KIND_ORDER[it.itemType] ?: KIND_ORDER.size }
                        .thenComparator { a, b ->
                            collator.compare(a.title?.toString().orEmpty(), b.title?.toString().orEmpty())
                        }
                )
            val cells = findFreeCells(fixed, idp, taskController, sorted.size)
            val writer = taskController.getModelWriter()
            sorted.zip(cells).forEach { (item, cell) ->
                writer.moveItemInDatabase(item, CONTAINER_DESKTOP, cell.screenId, cell.cellX, cell.cellY)
            }
        }
        taskController.model.forceReload("ArrangeDesktopTask")
    }

    private data class Cell(val screenId: Int, val cellX: Int, val cellY: Int)

    /** [count] free cells in reading order, page by page, around the widgets ([fixed]). */
    private fun findFreeCells(
        fixed: List<ItemInfo>,
        idp: InvariantDeviceProfile,
        taskController: ModelTaskController,
        count: Int,
    ): List<Cell> {
        val existingScreens = (fixed.map { it.screenId } + Workspace.FIRST_SCREEN_ID).distinct().sorted()
        val screens = existingScreens.iterator()
        var nextNewScreenId =
            maxOf(taskController.model.modelDbController.newScreenId, existingScreens.last() + 1)
        val cells = ArrayList<Cell>(count)
        while (cells.size < count) {
            val screenId = screens.takeIf { it.hasNext() }?.next() ?: nextNewScreenId++
            val occupancy = GridOccupancy(idp.numColumns, idp.numRows)
            fixed.filter { it.screenId == screenId }.forEach { occupancy.markCells(it, true) }
            for (y in 0 until idp.numRows) {
                for (x in 0 until idp.numColumns) {
                    if (cells.size < count && !occupancy.cells[x][y]) cells.add(Cell(screenId, x, y))
                }
            }
        }
        return cells
    }

    private companion object {
        /** Order of the kinds of icons; anything else (apps, shortcuts) comes last. */
        val KIND_ORDER =
            mapOf(
                ITEM_TYPE_FILE_SYSTEM_FOLDER to 0,
                ITEM_TYPE_FOLDER to 0,
                ITEM_TYPE_FILE_SYSTEM_FILE to 1,
            )
    }
}
