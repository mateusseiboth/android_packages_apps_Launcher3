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

package com.android.launcher3.taskbar

import com.android.launcher3.taskbar.TaskbarBackgroundRenderer.Companion.MAX_ROUNDNESS
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TaskbarCornerRoundnessTest {

    @Test
    fun desktopFormFactor_roundingRequested_isSquare() {
        assertThat(TaskbarCornerRoundness.resolve(isDesktopFormFactor = true, true)).isEqualTo(0f)
    }

    @Test
    fun desktopFormFactor_roundingNotRequested_isSquare() {
        assertThat(TaskbarCornerRoundness.resolve(isDesktopFormFactor = true, false)).isEqualTo(0f)
    }

    @Test
    fun otherFormFactor_roundingRequested_isRound() {
        assertThat(TaskbarCornerRoundness.resolve(isDesktopFormFactor = false, true))
            .isEqualTo(MAX_ROUNDNESS)
    }

    @Test
    fun otherFormFactor_roundingNotRequested_isSquare() {
        assertThat(TaskbarCornerRoundness.resolve(isDesktopFormFactor = false, false))
            .isEqualTo(0f)
    }
}
