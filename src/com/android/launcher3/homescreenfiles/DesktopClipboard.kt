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

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.PersistableBundle
import java.util.concurrent.CompletableFuture

/** Files put aside with Copy or Cut: their URIs, and whether Paste moves them. */
data class DesktopClip(val uris: List<Uri>, val mode: String)

/**
 * Copy, Cut and Paste of the desktop's files through the system clipboard, which the Files app
 * (PcFiles) reads and writes the same way: file URIs, and the mode in the clip's extras. A clip of
 * URIs without the mode (another app's) pastes as a copy.
 */
object DesktopClipboard {
    /** Keep in sync with PcFiles' SystemClipboard. */
    const val EXTRA_CLIP_MODE = "org.lineageos.pcfiles.extra.CLIP_MODE"
    const val MODE_COPY = "copy"
    const val MODE_CUT = "cut"
    private const val CLIP_LABEL = "pcfiles"
    private const val ANY_FILE_MIME_TYPE = "application/octet-stream"

    private val TEXT_MIME_TYPES =
        setOf(
            ClipDescription.MIMETYPE_TEXT_PLAIN,
            ClipDescription.MIMETYPE_TEXT_HTML,
            ClipDescription.MIMETYPE_TEXT_URILIST,
            ClipDescription.MIMETYPE_TEXT_INTENT,
        )

    /** How Paste brings each mode's files: Cut moves them, Copy copies them. */
    private val PASTES:
        Map<
            String,
            (HomeScreenFilesProvider, List<Uri>, HomeScreenFilesUpdate.Extras) -> List<
                    CompletableFuture<Boolean>
                >,
        > =
        mapOf(
            MODE_COPY to { provider, uris, extras -> provider.copyToHomeScreen(uris, extras) },
            MODE_CUT to { provider, uris, extras -> provider.moveToHomeScreen(uris, extras, null) },
        )

    @JvmStatic
    fun put(context: Context, files: List<HomeScreenFile>, mode: String) {
        if (files.isEmpty()) return
        val mimeTypes = files.map { it.mimeType ?: ANY_FILE_MIME_TYPE }.distinct().toTypedArray()
        val description =
            ClipDescription(CLIP_LABEL, mimeTypes).apply {
                extras = PersistableBundle().apply { putString(EXTRA_CLIP_MODE, mode) }
            }
        val clip = ClipData(description, ClipData.Item(files.first().uri))
        files.drop(1).forEach { clip.addItem(ClipData.Item(it.uri)) }
        clipboard(context)?.setPrimaryClip(clip)
    }

    /** Whether the clipboard holds files (not text) that Paste can bring to the desktop. */
    @JvmStatic
    fun hasFiles(context: Context): Boolean {
        val description = clipboard(context)?.primaryClipDescription ?: return false
        return description.extras?.containsKey(EXTRA_CLIP_MODE) == true ||
            (0 until description.mimeTypeCount).any { description.getMimeType(it) !in TEXT_MIME_TYPES }
    }

    @JvmStatic
    fun read(context: Context): DesktopClip? {
        val clip = clipboard(context)?.primaryClip ?: return null
        val uris = (0 until clip.itemCount).mapNotNull { clip.getItemAt(it).uri }
        if (uris.isEmpty()) return null
        val mode = clip.description.extras?.getString(EXTRA_CLIP_MODE) ?: MODE_COPY
        return DesktopClip(uris, mode.takeIf { it in PASTES } ?: MODE_COPY)
    }

    /** Brings the clipboard's files to the desktop; a cut is taken off the clipboard once moved. */
    @JvmStatic
    fun paste(context: Context, extras: HomeScreenFilesUpdate.Extras): List<CompletableFuture<Boolean>> {
        val clip = read(context) ?: return emptyList()
        val provider = HomeScreenFilesProvider.INSTANCE[context]
        val results = PASTES.getValue(clip.mode)(provider, clip.uris, extras)
        if (clip.mode == MODE_CUT) {
            CompletableFuture.allOf(*results.toTypedArray()).thenRun {
                runCatching { clipboard(context)?.clearPrimaryClip() }
            }
        }
        return results
    }

    private fun clipboard(context: Context): ClipboardManager? =
        context.getSystemService(ClipboardManager::class.java)
}
