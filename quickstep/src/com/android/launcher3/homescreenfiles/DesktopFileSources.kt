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

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.annotation.WorkerThread
import java.io.File

/**
 * Where a URI dropped on or pasted to the desktop comes from, which decides how it gets into the
 * desktop folder: items of the shared storage move (or copy) through the media store, files of other
 * volumes and other apps' content are copied in.
 */
sealed interface DesktopFileSource {
    /** An item of the primary shared storage, known to the media store. */
    data class Media(val mediaUri: Uri) : DesktopFileSource

    /** A file or folder of another volume (a USB drive, a disk the user mounted). */
    data class LocalFile(val file: File) : DesktopFileSource

    /** Content another app hands over, readable as a stream only. */
    data class Content(val uri: Uri, val name: String, val mimeType: String?) : DesktopFileSource
}

/**
 * Resolves dropped or pasted URIs to a [DesktopFileSource]: media store URIs, the external storage
 * provider's documents, URIs whose provider tells the file's path (the Files app's, and most file
 * managers'), then any readable content.
 */
class DesktopFileSources(private val context: Context, private val primaryRoot: () -> File) {
    private val resolver: ContentResolver
        get() = context.contentResolver

    private val resolvers: List<(Uri) -> DesktopFileSource?> =
        listOf(::fromMediaStore, ::fromExternalStorageDocument, ::fromFilePath, ::fromContent)

    @WorkerThread
    fun resolve(uri: Uri): DesktopFileSource? =
        if (uri.scheme != ContentResolver.SCHEME_CONTENT) null
        else resolvers.firstNotNullOfOrNull { runCatching { it(uri) }.getOrNull() }

    /** Media of other volumes go on to [fromFilePath] and are copied. */
    private fun fromMediaStore(uri: Uri): DesktopFileSource? =
        uri.takeIf(::isPrimaryMedia)?.let(DesktopFileSource::Media)

    private fun fromExternalStorageDocument(uri: Uri): DesktopFileSource? {
        if (uri.authority != DocumentsContract.EXTERNAL_STORAGE_PROVIDER_AUTHORITY) return null
        return MediaStore.getMediaUri(context, uri)?.takeIf(::isPrimaryMedia)?.let(DesktopFileSource::Media)
    }

    private fun isPrimaryMedia(uri: Uri): Boolean =
        uri.authority == MediaStore.AUTHORITY &&
            MediaStore.getVolumeName(uri) == MediaStore.VOLUME_EXTERNAL_PRIMARY

    private fun fromFilePath(uri: Uri): DesktopFileSource? {
        val file = readDataPath(uri)?.let(::File)?.takeIf { it.exists() } ?: return null
        if (!isInside(file, primaryRoot())) return DesktopFileSource.LocalFile(file)
        return DesktopFileSource.Media(MediaStore.scanFile(resolver, file))
    }

    private fun fromContent(uri: Uri): DesktopFileSource =
        DesktopFileSource.Content(uri, readDisplayName(uri), resolver.getType(uri))

    private fun readDataPath(uri: Uri): String? =
        resolver.query(uri, arrayOf(MediaStore.MediaColumns.DATA), null, null, null)?.use {
            if (it.moveToFirst()) it.getString(0) else null
        }

    private fun readDisplayName(uri: Uri): String {
        val name =
            runCatching {
                    resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                        ?.use { if (it.moveToFirst()) it.getString(0) else null }
                }
                .getOrNull()
        return name?.substringAfterLast('/')?.takeIf { it.isNotBlank() }
            ?: uri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() }
            ?: FALLBACK_NAME
    }

    companion object {
        private const val TAG = "DesktopFileSources"
        private const val FALLBACK_NAME = "file"

        fun isInside(file: File, root: File): Boolean {
            val path = file.absoluteFile.normalize().path
            val rootPath = root.absoluteFile.normalize().path
            return path == rootPath || path.startsWith("$rootPath/")
        }

        /** "name", then "name (1).ext", "name (2).ext"... until [isTaken] says no. */
        fun buildFreeName(name: String, isTaken: (String) -> Boolean): String {
            if (!isTaken(name)) return name
            val dot = name.lastIndexOf('.').takeIf { it > 0 } ?: name.length
            val base = name.substring(0, dot)
            val extension = name.substring(dot)
            return generateSequence(1) { it + 1 }
                .map { "$base ($it)$extension" }
                .first { !isTaken(it) }
        }

        /** Copies a file or a folder tree; false when something could not be copied. */
        @WorkerThread
        fun copyTree(source: File, target: File): Boolean =
            try {
                source.copyRecursively(target, overwrite = false)
            } catch (e: Exception) {
                Log.e(TAG, "Unable to copy ${source.path}", e)
                false
            }
    }
}
