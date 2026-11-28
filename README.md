# Launcher3 for LineageOS for PCs

LineageOS's Launcher3 with the desktop form factor changes of LineageOS for PCs (branch `pc`). The
commits on top of `lineage-24.0` describe each change; this file covers the ones that need a word on
their design.

## The desktop

On the desktop form factor the home screen is a computer's desktop: it holds the files of the user's
Desktop folder and the apps the user sends there, never apps by themselves.

- **Files**: AOSP's home screen files (`show_files_on_home_screen`) on the running user's
  `Desktop/` folder of the shared storage (user 10 under HSUM), turned on by asking for
  `MANAGE_EXTERNAL_STORAGE` (a preinstalled app gets it at install). The media store reports every change,
  so files saved, renamed or deleted by any app show at once. Clicks open with the default app; folders
  open in the Files app (PcFiles).
- **Menus**: a file has Open, Open with, Copy, Cut, Rename, Show in Files and Move to trash; the empty
  desktop has New folder, Paste, Arrange by name, Change wallpaper and Home settings; an app on the
  desktop has its usual menu with Remove from desktop.
- **Send to desktop**: in the menu of every app the taskbar lists (pinned, running, its app list and
  search) and of the launcher's All Apps. Dragging an app from the taskbar's app list onto the desktop
  also places it.
- **No apps by themselves**: installs add nothing (the "Add app icons to home screen" setting is gone),
  and the app icons earlier builds put there are taken off once, and after a restore.
- **Drag and drop and clipboard**: files dragged from the Files app move in (copied from other volumes),
  other apps' files are copied in, desktop files drag out to other apps. Copy and Cut use Android's
  clipboard with the mode in the clip's extras, as PcFiles does, so files go both ways.
- **Trash**: the freedesktop.org trash at the volume's root, the one PcFiles shows.
- **Selection**: a box drawn with the mouse, Ctrl+click, Shift+click, Ctrl+A; Copy, Cut, Move to trash
  and Delete act on the whole selection. Keys: Ctrl+C, Ctrl+X, Ctrl+V, Delete, F2, Esc.
- **Grid**: rows and columns by display size (`res/xml/grid_size_desktop.xml`), cells near 96dp, 48dp
  icons; a new page only when the first is full.

Known limits: a drop cannot tell Ctrl or Shift (within the shared storage files move, from elsewhere
they are copied), and only the icon under the pointer moves when a selection is dragged.

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
