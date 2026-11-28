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

import android.net.Uri
import androidx.annotation.WorkerThread
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * The trash of the shared storage in the freedesktop.org layout the Files app (PcFiles) uses:
 * ".Trash/files" at the volume's root holds the items, ".Trash/info" a ".trashinfo" for each, so
 * items trashed from the desktop show in the Files app's Trash and go back from there.
 */
class DesktopTrash(private val root: () -> File, private val now: () -> LocalDateTime) {
    private val files: File
        get() = File(File(root(), FOLDER), FILES)

    private val info: File
        get() = File(File(root(), FOLDER), INFO)

    /** Moves [source] to the trash; returns its path there. */
    @WorkerThread
    fun trash(source: File): String {
        if (!source.exists()) throw IOException("No file at ${source.path}")
        if (!(files.isDirectory || files.mkdirs()) || !(info.isDirectory || info.mkdirs())) {
            throw IOException("Cannot create the trash of ${root().path}")
        }
        val name =
            DesktopFileSources.buildFreeName(source.name) {
                File(files, it).exists() || infoFor(it).exists()
            }
        val infoFile = infoFor(name)
        infoFile.writeText(buildInfo(source.absolutePath))
        val target = File(files, name)
        if (!source.renameTo(target)) {
            infoFile.delete()
            throw IOException("Cannot move ${source.path} to the trash")
        }
        return target.path
    }

    /** Puts an item of the trash back where it was. */
    @WorkerThread
    fun restore(trashPath: String) {
        val item = File(trashPath)
        val infoFile = infoFor(item.name)
        val original =
            infoFile
                .takeIf { it.isFile }
                ?.readLines()
                ?.firstOrNull { it.startsWith(PATH_KEY) }
                ?.removePrefix(PATH_KEY)
                ?.let { File(decodePath(it)) } ?: throw IOException("No record of $trashPath")
        val parent = original.parentFile ?: throw IOException("No folder for ${original.path}")
        if (!parent.isDirectory && !parent.mkdirs()) throw IOException("Cannot create $parent")
        val target = File(parent, DesktopFileSources.buildFreeName(original.name) { File(parent, it).exists() })
        if (!item.renameTo(target)) throw IOException("Cannot restore $trashPath")
        infoFile.delete()
    }

    private fun infoFor(name: String) = File(info, name + INFO_SUFFIX)

    private fun buildInfo(path: String): String =
        "[Trash Info]\n$PATH_KEY${encodePath(path)}\nDeletionDate=${now().format(DATE_FORMAT)}\n"

    companion object {
        private const val FOLDER = ".Trash"
        private const val FILES = "files"
        private const val INFO = "info"
        private const val INFO_SUFFIX = ".trashinfo"
        private const val PATH_KEY = "Path="
        private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

        /** Percent-encodes the path, as the trash spec asks; "/" stays. */
        fun encodePath(path: String): String = Uri.encode(path, "/")

        fun decodePath(encoded: String): String = Uri.decode(encoded)
    }
}
