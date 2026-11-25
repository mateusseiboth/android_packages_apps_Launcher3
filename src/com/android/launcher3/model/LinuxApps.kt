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

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.os.Bundle
import android.os.UserHandle
import com.android.launcher3.LauncherModel.ModelUpdateTask
import com.android.launcher3.LauncherSettings.Favorites.CONTAINER_ALL_APPS
import com.android.launcher3.icons.IconCache
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.model.data.WorkspaceItemInfo
import com.android.launcher3.shortcuts.ShortcutKey
import com.android.launcher3.shortcuts.ShortcutRequest

/**
 * Linux apps of LineageOS for PCs. PcWayland (packages/apps/PcWayland) publishes each graphical app of
 * the Linux containers as a dynamic shortcut with [CATEGORY]; here each one is an app of its own in All
 * Apps and the taskbar's launcher panel, with its own label and icon. It starts through
 * LauncherApps.startShortcut, and pinning it places the shortcut itself (a deep shortcut item, pinned
 * by the model). PcWayland's launcher activities only hold the shortcuts and are hidden.
 */
object LinuxApps {
    const val PACKAGE = "org.lineageos.pcwayland"
    const val CATEGORY = "org.lineageos.pcwayland.category.LINUX_APP"
    private const val CLASS_PREFIX = "linux:"

    /** PcWayland's launcher activities: they hold the shortcuts, the person sees the apps instead. */
    @JvmStatic
    fun isHolder(component: ComponentName?): Boolean =
        component?.packageName == PACKAGE && !component.className.startsWith(CLASS_PREFIX)

    /** Replaces the Linux apps of [user] in [apps] with what PcWayland publishes now. */
    @JvmStatic
    fun reload(context: Context, iconCache: IconCache, apps: AllAppsList, user: UserHandle) {
        apps.removePackage(PACKAGE, user)
        ShortcutRequest(context, user)
            .forPackage(PACKAGE)
            .query(ShortcutRequest.PUBLISHED)
            .filter { it.isEnabled && it.categories?.contains(CATEGORY) == true }
            .forEach { shortcut ->
                val info = LinuxAppInfo(shortcut)
                apps.add(info, null, false)
                info.title = shortcut.shortLabel ?: shortcut.id
                iconCache.getShortcutIcon(info, shortcut)
                apps.updateSectionName(info)
            }
    }

    /** Starts the Linux app of [info]; true when the shortcut service took it. */
    @JvmStatic
    fun start(context: Context, info: LinuxAppInfo, options: Bundle?): Boolean {
        context.getSystemService(LauncherApps::class.java)
            ?.startShortcut(PACKAGE, info.shortcutId, null, options, info.user) ?: return false
        return true
    }

    /** The component that keys a Linux app in All Apps (it names no activity). */
    fun componentOf(shortcutId: String) = ComponentName(PACKAGE, CLASS_PREFIX + shortcutId)
}

/** A Linux app in All Apps: an app entry backed by PcWayland's shortcut. */
class LinuxAppInfo(val shortcut: ShortcutInfo) :
    AppInfo(
        LinuxApps.componentOf(shortcut.id),
        shortcut.shortLabel ?: shortcut.id,
        shortcut.userHandle,
        ShortcutKey.makeIntent(shortcut),
    ) {

    init {
        container = CONTAINER_ALL_APPS
    }

    val shortcutId: String
        get() = shortcut.id

    /** Pinned or dropped on the desktop, it becomes the deep shortcut itself. */
    override fun makeWorkspaceItem(context: Context): WorkspaceItemInfo =
        WorkspaceItemInfo(shortcut, context).also { it.bitmap = bitmap }

    override fun clone(): AppInfo = LinuxAppInfo(shortcut).also {
        it.title = title
        it.bitmap = bitmap
        it.sectionName = sectionName
    }
}

/** PcWayland changed its shortcuts or was updated: list its Linux apps again. */
class LinuxAppsChangedTask(private val user: UserHandle) : ModelUpdateTask {
    override fun execute(taskController: ModelTaskController, dataModel: BgDataModel, apps: AllAppsList) {
        LinuxApps.reload(taskController.context, taskController.iconCache, apps, user)
        taskController.bindApplicationsIfNeeded()
    }
}
