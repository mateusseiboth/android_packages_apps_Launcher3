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

import android.view.KeyEvent
import android.view.View
import com.android.launcher3.LauncherSettings.Favorites.CONTAINER_DESKTOP
import com.android.launcher3.homescreenfiles.DesktopClipboard
import com.android.launcher3.homescreenfiles.DesktopFileActions
import com.android.launcher3.homescreenfiles.homeScreenFile
import com.android.launcher3.homescreenfiles.isFileSystemItem
import com.android.launcher3.model.data.ItemInfo

/**
 * Keys of the desktop of a PC, on its selected icons (or the focused one): Ctrl+A selects all,
 * Ctrl+C and Ctrl+X put the files aside, Ctrl+V pastes them, Delete moves files to the trash and takes
 * apps off the desktop, F2 renames a file, Esc clears the selection.
 */
object DesktopKeys {

    private val CTRL_KEYS: Map<Int, (Launcher) -> Boolean> =
        mapOf(
            KeyEvent.KEYCODE_A to ::selectAll,
            KeyEvent.KEYCODE_C to { launcher -> putAside(launcher, DesktopClipboard.MODE_COPY) },
            KeyEvent.KEYCODE_X to { launcher -> putAside(launcher, DesktopClipboard.MODE_CUT) },
            KeyEvent.KEYCODE_V to ::paste,
        )

    private val PLAIN_KEYS: Map<Int, (Launcher) -> Boolean> =
        mapOf(
            KeyEvent.KEYCODE_FORWARD_DEL to ::delete,
            KeyEvent.KEYCODE_F2 to ::rename,
            KeyEvent.KEYCODE_ESCAPE to ::clearSelection,
        )

    /** Ctrl+key; null when the key is not the desktop's. */
    @JvmStatic
    fun onKeyShortcut(launcher: Launcher, keyCode: Int, event: KeyEvent): Boolean? =
        handle(launcher, CTRL_KEYS[keyCode].takeIf { event.hasModifiers(KeyEvent.META_CTRL_ON) })

    /** A key without modifiers; null when the key is not the desktop's. */
    @JvmStatic
    fun onKeyDown(launcher: Launcher, keyCode: Int, event: KeyEvent): Boolean? =
        handle(launcher, PLAIN_KEYS[keyCode].takeIf { event.hasNoModifiers() })

    private fun handle(launcher: Launcher, action: ((Launcher) -> Boolean)?): Boolean? {
        if (action == null || !isOnDesktop(launcher)) return null
        return action(launcher).takeIf { it }
    }

    private fun isOnDesktop(launcher: Launcher): Boolean =
        Utilities.isDesktopFormFactor(launcher) &&
            launcher.isInState(LauncherState.NORMAL) &&
            AbstractFloatingView.getTopOpenView(launcher) == null

    /** The selected icons, or the focused one when nothing is selected. */
    private fun findTargets(launcher: Launcher): List<View> =
        DesktopSelection.selectedViews(launcher).ifEmpty {
            listOfNotNull(launcher.currentFocus?.takeIf { isDesktopItem(it) })
        }

    private fun isDesktopItem(view: View): Boolean =
        (view.tag as? ItemInfo)?.container == CONTAINER_DESKTOP

    private fun selectAll(launcher: Launcher): Boolean {
        DesktopSelection.selectAll(launcher)
        return true
    }

    private fun clearSelection(launcher: Launcher): Boolean {
        val hasSelection = DesktopSelection.selectedViews(launcher).isNotEmpty()
        DesktopSelection.clear(launcher)
        return hasSelection
    }

    private fun putAside(launcher: Launcher, mode: String): Boolean {
        val files = findTargets(launcher).mapNotNull { (it.tag as? ItemInfo)?.homeScreenFile }
        DesktopClipboard.put(launcher, files, mode)
        return files.isNotEmpty()
    }

    private fun paste(launcher: Launcher): Boolean {
        if (!DesktopFileActions.canPaste(launcher)) return false
        DesktopFileActions.onMenuShown(-1f, -1f)
        DesktopFileActions.paste(launcher)
        return true
    }

    /** Files go to the trash together; apps, folders and widgets leave the desktop. */
    private fun delete(launcher: Launcher): Boolean {
        val targets = findTargets(launcher).filter { it.tag is ItemInfo }
        val (files, others) = targets.partition { (it.tag as ItemInfo).isFileSystemItem() }
        files.takeIf { it.isNotEmpty() }?.let { views ->
            launcher.dropTargetHandler.trashHomeScreenFiles(views.map { it.tag as ItemInfo })
        }
        others.forEach { launcher.removeItem(it, it.tag as ItemInfo, true, "Removed from the desktop") }
        return targets.isNotEmpty()
    }

    private fun rename(launcher: Launcher): Boolean {
        val file = findTargets(launcher).singleOrNull()?.tag as? ItemInfo ?: return false
        if (!file.isFileSystemItem()) return false
        DesktopFileActions.rename(launcher, file)
        return true
    }
}
