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

/** Which SystemUI dialog state stashes the taskbar. */
object TaskbarDialogStash {

    /**
     * The system UI state flag that stashes the taskbar while a SystemUI dialog shows: every
     * dialog, or only the dialogs that ask the taskbar to stash.
     */
    @JvmStatic
    fun stashMask(stashesOnlyForDialogsThatAsk: Boolean): Long {
        if (stashesOnlyForDialogsThatAsk) return SYSUI_STATE_DIALOG_STASH_TASKBAR
        return SYSUI_STATE_DIALOG_SHOWING
    }
}
