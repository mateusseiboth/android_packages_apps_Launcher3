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

package com.android.launcher3.taskbar.allapps

/**
 * The row of suggested apps above the app list of the taskbar's All Apps: the predictions of the
 * app prediction service when there is one, otherwise the apps used last, from the recent tasks.
 */
object TaskbarAllAppsSuggestions {

    /** [predicted] when it has apps, otherwise [recent]. */
    @JvmStatic fun <T> choose(predicted: List<T>, recent: List<T>): List<T> =
        predicted.ifEmpty { recent }

    /**
     * The apps of the recent tasks, newest first and once each, at most [max]. The keys come in
     * the order of the recents list, the oldest first; [appOf] gives the app of a key, or null for
     * a task of an app that All Apps does not list.
     */
    @JvmStatic
    fun <K, T> recentApps(recentKeysOldestFirst: List<K>, appOf: (K) -> T?, max: Int): List<T> =
        recentKeysOldestFirst.asReversed().distinct().mapNotNull(appOf).take(max)
}
