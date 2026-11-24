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

package com.android.quickstep.taskbar.customization

import com.android.launcher3.taskbar.customization.TaskbarAllAppsPill
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TaskbarAllAppsPillTest {

    private val pill = TaskbarAllAppsPill(widthPx = 68, heightPx = 48, iconSizePx = 24)

    @Test
    fun padding_centersTheIconInThePill() {
        assertThat(pill.horizontalPaddingPx).isEqualTo(22)
        assertThat(pill.verticalPaddingPx).isEqualTo(12)
    }

    @Test
    fun extraWidth_isTheWidthBeyondAnIconTouchArea() {
        assertThat(pill.extraWidthOver(touchSizePx = 48)).isEqualTo(20)
    }

    @Test
    fun extraWidth_narrowerThanTheTouchArea_isZero() {
        assertThat(TaskbarAllAppsPill(40, 48, 24).extraWidthOver(touchSizePx = 48)).isEqualTo(0)
    }
}
