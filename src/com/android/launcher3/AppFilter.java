package com.android.launcher3;

import android.content.ComponentName;
import android.content.Context;

import com.android.launcher3.dagger.ApplicationContext;
import com.android.launcher3.model.LinuxApps;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * Utility class to filter out components from various lists
 */
public class AppFilter {

    private final Set<ComponentName> mFilteredComponents;

    @Inject
    public AppFilter(@ApplicationContext Context context) {
        mFilteredComponents = Arrays.stream(
                context.getResources().getStringArray(R.array.filtered_components))
                .map(ComponentName::unflattenFromString)
                .collect(Collectors.toSet());
    }

    public boolean shouldShowApp(ComponentName app) {
        // PcWayland's activities only hold the Linux apps' shortcuts, listed as apps of their own.
        return !mFilteredComponents.contains(app) && !LinuxApps.isHolder(app);
    }
}
