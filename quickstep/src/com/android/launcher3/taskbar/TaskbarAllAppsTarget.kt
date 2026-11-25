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

/**
 * Which All Apps the taskbar's All Apps button and the All Apps key toggle while the launcher is
 * in front: the home screen's, except on the desktop form factor, where All Apps is always the
 * taskbar's launcher panel, over the home screen as over apps.
 */
object TaskbarAllAppsTarget {

    @JvmStatic
    fun isHomeAllApps(isDesktopFormFactor: Boolean, isHomeInFront: Boolean): Boolean =
        isHomeInFront && !isDesktopFormFactor
}
