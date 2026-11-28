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

package com.android.launcher3.dragndrop

import android.content.ClipData
import android.content.ClipDescription
import com.android.launcher3.dragndrop.BaseItemDragListener.EXTRA_WRAPPED_ITEM_INFO
import com.android.launcher3.dragndrop.BaseItemDragListener.MIME_TYPE_INTERNAL_APP_ACTIVITY
import com.android.launcher3.dragndrop.SystemDragController.Companion.DOCS_UI_EXTRA_PREFIX
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.util.ObjectWrapper

/**
 * Which system drags the home screen takes: apps dragged from the taskbar's app list (another window
 * of this app), and files from file managers and other apps, which are moved or copied into the
 * desktop folder.
 */
object SystemDropRules {

    /** Label of the Files app's (PcFiles) drags. */
    private const val PC_FILES_DRAG_LABEL = "pcfiles"

    /** What other apps' drags carry that the desktop cannot take as a file. */
    private val NOT_FILE_MIME_TYPES =
        arrayOf(
            ClipDescription.MIMETYPE_APPLICATION_ACTIVITY,
            ClipDescription.MIMETYPE_APPLICATION_SHORTCUT,
            ClipDescription.MIMETYPE_APPLICATION_TASK,
            ClipDescription.MIMETYPE_TEXT_INTENT,
        )

    private val TEXT_MIME_TYPES =
        setOf(
            ClipDescription.MIMETYPE_TEXT_PLAIN,
            ClipDescription.MIMETYPE_TEXT_HTML,
            ClipDescription.MIMETYPE_TEXT_URILIST,
        )

    /** Drags known to carry file URIs; a text-only drag of another app is a text selection. */
    private val FILE_DRAG_SIGNS: List<(ClipDescription) -> Boolean> =
        listOf(
            { it.extras?.keySet()?.any { key -> key.startsWith(DOCS_UI_EXTRA_PREFIX) } == true },
            { it.label?.toString() == PC_FILES_DRAG_LABEL },
            { (0 until it.mimeTypeCount).any { i -> it.getMimeType(i) !in TEXT_MIME_TYPES } },
        )

    @JvmStatic
    fun isAcceptable(description: ClipDescription?, isFilesEnabled: Boolean): Boolean {
        if (description == null) return false
        if (isLauncherItem(description)) return true
        return isFilesEnabled && isFileDrag(description)
    }

    @JvmStatic
    fun isLauncherItem(description: ClipDescription?): Boolean =
        description?.hasMimeType(MIME_TYPE_INTERNAL_APP_ACTIVITY) == true

    private fun isFileDrag(description: ClipDescription): Boolean =
        !description.hasMimeType(NOT_FILE_MIME_TYPES) && FILE_DRAG_SIGNS.any { it(description) }

    /** The app a drag from the taskbar's app list carries (same process, so the object itself). */
    @JvmStatic
    fun readLauncherItem(clipData: ClipData?): ItemInfo? {
        if (clipData == null) return null
        return (0 until clipData.itemCount)
            .mapNotNull { clipData.getItemAt(it)?.intent?.getBundleExtra(EXTRA_WRAPPED_ITEM_INFO) }
            .firstNotNullOfOrNull { ObjectWrapper.unwrap<ItemInfo>(it.getBinder(EXTRA_WRAPPED_ITEM_INFO)) }
    }
}
