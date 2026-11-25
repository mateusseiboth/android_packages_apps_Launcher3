# Launcher3 for LineageOS for PCs

LineageOS's Launcher3 with the desktop form factor changes of LineageOS for PCs (branch `pc`). The
commits on top of `lineage-24.0` describe each change; this file covers the ones that need a word on
their design.

## Linux apps

The Linux apps of the containers (PcTerminal and PcWayland, `packages/apps/PcWayland`) show as apps of
their own: in All Apps, in the taskbar's launcher panel and in search, with their own label and icon;
a click opens them, and they pin to the taskbar and the desktop like any app.

How: Android lists only activities, and an app cannot add activities at run time, so PcWayland
publishes each Linux app as a dynamic shortcut with the category
`org.lineageos.pcwayland.category.LINUX_APP`. `model/LinuxApps.kt` turns each such shortcut into an
app entry (`LinuxAppInfo`):

- loaded with the other apps (`LoaderTask.loadAllApps`) and again when PcWayland's shortcuts change or
  its package updates (`ModelLauncherCallbacks`, `LinuxAppsChangedTask`);
- started through `LauncherApps.startShortcut` (`ActivityContext.startActivitySafely`, and the
  taskbar's own launch path, where looking up a recent task by package would pick any Linux window);
- pinned as the shortcut itself (a deep shortcut item, which the model pins in ShortcutManager).

PcWayland's launcher activities only hold the shortcuts (ShortcutManager takes a limited number per
activity), so `AppFilter` hides them. Shortcuts were chosen over a content provider or a service of
PcWayland's: the launcher already reads, caches, pins and launches shortcuts, and the platform checks
who may do so.

Known limits: the taskbar groups running windows by package, so every Linux window counts as one running
app there; an app pinned from All Apps is a shortcut, so the menu of its All Apps entry offers "Pin"
again.

## Tests

`tests/multivalentTests/src/com/android/launcher3/model/LinuxAppsTest.kt` (Launcher3's multivalent
tests). Without a build, the change was compiled against a previous build's classpath (the kotlinc and
javac inputs of `Launcher3QuickStepLib`) and `LinuxAppsTest` run on the host with JUnit.

## Credits

Built on [Launcher3](https://android.googlesource.com/platform/packages/apps/Launcher3/) of the
[Android Open Source Project](https://source.android.com/) and [LineageOS](https://lineageos.org/).
