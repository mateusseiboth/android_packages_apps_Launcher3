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

package com.android.launcher3.model

import android.content.ComponentName
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class LinuxAppsTest {

    @Test
    fun pcWaylandsLauncherActivities_areHolders() {
        assertThat(LinuxApps.isHolder(ComponentName(LinuxApps.PACKAGE, "org.lineageos.pcwayland.AppsActivity")))
            .isTrue()
        assertThat(LinuxApps.isHolder(ComponentName(LinuxApps.PACKAGE, "org.lineageos.pcwayland.LinuxApps3")))
            .isTrue()
    }

    @Test
    fun linuxApps_areNotHolders() {
        assertThat(LinuxApps.isHolder(LinuxApps.componentOf("debian/org.gnome.TextEditor"))).isFalse()
    }

    @Test
    fun otherApps_areNotHolders() {
        assertThat(LinuxApps.isHolder(ComponentName("org.lineageos.pcfiles", "org.lineageos.pcfiles.MainActivity")))
            .isFalse()
        assertThat(LinuxApps.isHolder(null)).isFalse()
    }

    @Test
    fun eachLinuxApp_hasItsOwnComponent() {
        assertThat(LinuxApps.componentOf("debian/a")).isNotEqualTo(LinuxApps.componentOf("debian/b"))
        assertThat(LinuxApps.componentOf("debian/a").packageName).isEqualTo(LinuxApps.PACKAGE)
    }
}
