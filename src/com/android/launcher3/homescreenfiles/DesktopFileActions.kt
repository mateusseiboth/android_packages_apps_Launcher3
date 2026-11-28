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

package com.android.launcher3.homescreenfiles

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Rect
import android.graphics.RectF
import android.os.Environment
import android.view.View
import android.widget.Toast
import com.android.launcher3.DesktopSelection
import com.android.launcher3.Launcher
import com.android.launcher3.R
import com.android.launcher3.Workspace
import com.android.launcher3.homescreenfiles.HomeScreenFilesProvider.Companion.HOME_SCREEN_FOLDER_RELATIVE_PATH
import com.android.launcher3.logging.StatsLogManager.LauncherEvent.IGNORE
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.model.data.WorkspaceItemCoordinates
import com.android.launcher3.util.Executors.MAIN_EXECUTOR
import com.android.launcher3.views.ActivityContext
import com.android.launcher3.views.OptionsPopupView
import com.android.launcher3.views.OptionsPopupView.OptionItem
import java.util.concurrent.CompletableFuture

/**
 * What the desktop of a PC does with its files besides the home screen's own actions: open with
 * another app, copy and cut for the Files app or the desktop, paste, move to the trash together, and
 * show the desktop folder in the Files app (PcFiles).
 */
object DesktopFileActions {

    /** The Files app (PcFiles) and the folder it opens at. */
    private val FILES_APP = ComponentName("org.lineageos.pcfiles", "org.lineageos.pcfiles.MainActivity")
    private const val FILES_EXTRA_PATH = "org.lineageos.pcfiles.extra.PATH"
    private const val FILES_EXTRA_SELECT = "org.lineageos.pcfiles.extra.SELECT"

    /** Where the latest menu of the empty desktop opened, in drag layer coordinates. */
    private var menuX = -1f
    private var menuY = -1f

    /** The files a menu started from [view] acts on: every selected file when [view] is selected. */
    @JvmStatic
    fun findTargets(activityContext: ActivityContext, view: View): List<ItemInfo> {
        val launcher = activityContext as? Launcher ?: return listOfNotNull(view.tag as? ItemInfo)
        return DesktopSelection.targetsFor(launcher, view)
            .mapNotNull { it.tag as? ItemInfo }
            .filter { it.isFileSystemItem() }
    }

    @JvmStatic
    fun copy(activityContext: ActivityContext, view: View, mode: String) {
        val files = findTargets(activityContext, view).mapNotNull { it.homeScreenFile }
        DesktopClipboard.put(activityContext.asContext(), files, mode)
    }

    @JvmStatic
    fun trash(activityContext: ActivityContext, view: View) {
        activityContext.dropTargetHandler.trashHomeScreenFiles(findTargets(activityContext, view))
    }

    /** "Open with": the apps that open the file, in a menu next to it. */
    @JvmStatic
    fun showOpenWith(activityContext: ActivityContext, item: ItemInfo, view: View) {
        val file = item.homeScreenFile ?: return
        val context = activityContext.asContext()
        val intent = HomeScreenFilesUtils.buildLaunchIntent(file.uri, file)
        val apps = findApps(context, intent)
        if (apps.isEmpty()) {
            Toast.makeText(context, R.string.desktop_files_no_app, Toast.LENGTH_SHORT).show()
            return
        }
        val packages = context.packageManager
        val items =
            apps.map { app ->
                OptionItem(app.loadLabel(packages), app.loadIcon(packages), IGNORE) { v ->
                    val component = ComponentName(app.activityInfo.packageName, app.activityInfo.name)
                    activityContext.startActivitySafely(v, Intent(intent).setComponent(component), item)
                    true
                }
            }
        OptionsPopupView.showNoReturn(activityContext, findBounds(activityContext, view), items, false)
    }

    private fun findApps(context: Context, intent: Intent): List<ResolveInfo> {
        val packages = context.packageManager
        return packages
            .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            .filter { it.activityInfo.packageName != context.packageName }
            .distinctBy { ComponentName(it.activityInfo.packageName, it.activityInfo.name) }
            .sortedBy { it.loadLabel(packages).toString().lowercase() }
    }

    private fun findBounds(activityContext: ActivityContext, view: View): RectF {
        val bounds = Rect()
        activityContext.dragLayer.getDescendantRectRelativeToSelf(view, bounds)
        return RectF(bounds)
    }

    @JvmStatic
    fun isFilesAppInstalled(context: Context): Boolean =
        context.packageManager.resolveActivity(Intent().setComponent(FILES_APP), 0) != null

    /** Opens the desktop folder in the Files app, with [file] selected when given. */
    @JvmStatic
    fun showInFiles(activityContext: ActivityContext, view: View, file: HomeScreenFile?) {
        val folder = Environment.getExternalStorageDirectory().resolve(HOME_SCREEN_FOLDER_RELATIVE_PATH)
        val intent =
            Intent(Intent.ACTION_MAIN)
                .setComponent(FILES_APP)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(FILES_EXTRA_PATH, folder.path)
        file?.let { intent.putExtra(FILES_EXTRA_SELECT, it.displayName) }
        activityContext.startActivitySafely(view, intent, null)
    }

    @JvmStatic
    fun canPaste(context: Context): Boolean =
        DesktopClipboard.hasFiles(context) &&
            HomeScreenFilesProvider.INSTANCE[context].canCreateNewFolder()

    @JvmStatic
    fun onMenuShown(x: Float, y: Float) {
        menuX = x
        menuY = y
    }

    /** Pastes from the cell the menu of the empty desktop opened at (the page's first one). */
    @JvmStatic
    fun paste(launcher: Launcher) {
        val extras =
            HomeScreenFilesUpdate.Extras.builder().findSpaceStartingFrom(findMenuCell(launcher)).build()
        val results = DesktopClipboard.paste(launcher, extras)
        CompletableFuture.allOf(*results.toTypedArray()).whenCompleteAsync(
            { _, _ ->
                if (!results.all { it.getNow(false) }) {
                    Toast.makeText(launcher, R.string.desktop_paste_failed, Toast.LENGTH_SHORT).show()
                }
            },
            MAIN_EXECUTOR,
        )
    }

    private fun findMenuCell(launcher: Launcher): WorkspaceItemCoordinates {
        val workspace = launcher.workspace
        val screenId = workspace.getScreenIdForPageIndex(workspace.currentPage)
        val layout = workspace.getScreenWithId(screenId)
        if (layout == null || menuX < 0 || menuY < 0) {
            return WorkspaceItemCoordinates(Workspace.FIRST_SCREEN_ID, 0, 0)
        }
        val point = floatArrayOf(menuX, menuY)
        launcher.dragLayer.mapCoordInSelfToDescendant(layout, point)
        val cell = IntArray(2)
        layout.pointToCellExact(point[0].toInt(), point[1].toInt(), cell)
        return WorkspaceItemCoordinates(
            screenId,
            cell[0].coerceIn(0, layout.countX - 1),
            cell[1].coerceIn(0, layout.countY - 1),
        )
    }

    @JvmStatic
    fun rename(activityContext: ActivityContext, item: ItemInfo) {
        val file = item.homeScreenFile ?: return
        HomeScreenFilesRenameDialogFactory().create(activityContext, file).show()
    }
}
