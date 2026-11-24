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

import com.android.systemui.shared.system.QuickStepContract.SYSUI_STATE_DIALOG_SHOWING
import com.android.systemui.shared.system.QuickStepContract.SYSUI_STATE_DIALOG_STASH_TASKBAR
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskbarDialogStashTest {

    @Test
    fun everyDialog_stashesOnDialogShowing() {
        assertEquals(
            SYSUI_STATE_DIALOG_SHOWING,
            TaskbarDialogStash.stashMask(stashesOnlyForDialogsThatAsk = false),
        )
    }

    @Test
    fun selective_stashesOnlyForDialogsThatAsk() {
        assertEquals(
            SYSUI_STATE_DIALOG_STASH_TASKBAR,
            TaskbarDialogStash.stashMask(stashesOnlyForDialogsThatAsk = true),
        )
    }
}
