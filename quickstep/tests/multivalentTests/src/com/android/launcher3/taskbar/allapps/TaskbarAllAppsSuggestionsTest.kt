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

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TaskbarAllAppsSuggestionsTest {

    private val apps = mapOf("files" to "Files", "browser" to "Browser", "music" to "Music")

    @Test
    fun recentApps_newestFirst() {
        assertThat(
                TaskbarAllAppsSuggestions.recentApps(
                    recentKeysOldestFirst = listOf("files", "browser", "music"),
                    appOf = apps::get,
                    max = 6,
                )
            )
            .containsExactly("Music", "Browser", "Files")
            .inOrder()
    }

    @Test
    fun recentApps_appWithManyTasks_showsOnce() {
        assertThat(
                TaskbarAllAppsSuggestions.recentApps(
                    recentKeysOldestFirst = listOf("files", "browser", "files"),
                    appOf = apps::get,
                    max = 6,
                )
            )
            .containsExactly("Files", "Browser")
            .inOrder()
    }

    @Test
    fun recentApps_tasksOfUnknownApps_areLeftOut() {
        assertThat(
                TaskbarAllAppsSuggestions.recentApps(
                    recentKeysOldestFirst = listOf("settings-activity", "music"),
                    appOf = apps::get,
                    max = 6,
                )
            )
            .containsExactly("Music")
    }

    @Test
    fun recentApps_keepsAtMostOneRow() {
        assertThat(
                TaskbarAllAppsSuggestions.recentApps(
                    recentKeysOldestFirst = listOf("files", "browser", "music"),
                    appOf = apps::get,
                    max = 2,
                )
            )
            .containsExactly("Music", "Browser")
            .inOrder()
    }

    @Test
    fun choose_withPredictions_usesThem() {
        assertThat(TaskbarAllAppsSuggestions.choose(listOf("Mail"), listOf("Music")))
            .containsExactly("Mail")
    }

    @Test
    fun choose_withoutPredictions_usesRecentApps() {
        assertThat(TaskbarAllAppsSuggestions.choose(emptyList(), listOf("Music")))
            .containsExactly("Music")
    }
}
