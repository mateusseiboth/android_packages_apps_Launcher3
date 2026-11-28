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

package com.android.launcher3

import android.content.Context
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import com.android.launcher3.Flags.enableWorkspaceSelection
import com.android.launcher3.LauncherSettings.Favorites.CONTAINER_DESKTOP
import com.android.launcher3.WorkspaceSelectionManager.ItemSelectionType
import com.android.launcher3.folder.FolderIcon
import com.android.launcher3.model.data.ItemInfo

/**
 * Selection of the desktop's icons on a PC, on top of [WorkspaceSelectionManager]: a box drawn with
 * the mouse on empty space, Ctrl+click to add or take out one icon, Shift+click for the icons between
 * the last clicked one and this one, Ctrl+A for all. A plain click opens the item and clears the
 * selection. Menus and keys act on every selected icon when the one they start from is selected.
 */
object DesktopSelection {

    /** Modifier state of the latest touch down: a click carries none. */
    private var downMetaState = 0

    /** Which selection a click with each modifier makes. */
    private val CLICK_SELECTIONS: List<Pair<Int, ItemSelectionType>> =
        listOf(
            KeyEvent.META_CTRL_ON to ItemSelectionType.TOGGLE_SINGLE,
            KeyEvent.META_SHIFT_ON to ItemSelectionType.RANGE,
        )

    @JvmStatic
    fun isEnabled(context: Context): Boolean =
        enableWorkspaceSelection() || Utilities.isDesktopFormFactor(context)

    @JvmStatic
    fun onTouchDown(ev: MotionEvent) {
        downMetaState = ev.metaState
    }

    /** A click on a desktop icon with Ctrl or Shift only changes the selection: returns true. */
    @JvmStatic
    fun onItemClick(launcher: Launcher, view: View): Boolean {
        if (!isEnabled(launcher) || !isDesktopItem(view)) return false
        val selection =
            CLICK_SELECTIONS.firstOrNull { (mask, _) -> downMetaState and mask != 0 }?.second
        if (selection == null) {
            clear(launcher)
            return false
        }
        manager(launcher).updateItemSelection(view, selection)
        return true
    }

    /** The selected icons of the desktop, on every page. */
    @JvmStatic
    fun selectedViews(launcher: Launcher): List<View> {
        val views = ArrayList<View>()
        launcher.workspace.mapOverItems { _, view ->
            if (view.isSelected) views.add(view)
            false
        }
        return views
    }

    /** What a menu or key started from [view] acts on: the selection when [view] is in it. */
    @JvmStatic
    fun targetsFor(launcher: Launcher, view: View): List<View> =
        selectedViews(launcher).takeIf { view.isSelected && it.size > 1 } ?: listOf(view)

    @JvmStatic
    fun selectAll(launcher: Launcher) {
        launcher.workspace.mapOverVisibleItems { _, view ->
            if (isSelectable(view)) setSelected(view, true)
            false
        }
    }

    @JvmStatic
    fun clear(launcher: Launcher) {
        selectedViews(launcher).forEach { setSelected(it, false) }
    }

    private fun setSelected(view: View, isSelected: Boolean) {
        if (view.isSelected == isSelected) return
        view.isSelected = isSelected
        view.invalidate()
    }

    private fun isSelectable(view: View): Boolean =
        view is BubbleTextView || view is FolderIcon

    private fun isDesktopItem(view: View): Boolean =
        (view.tag as? ItemInfo)?.container == CONTAINER_DESKTOP

    private fun manager(launcher: Launcher): WorkspaceSelectionManager =
        launcher.activityComponent.workspaceSelectionManager
}
