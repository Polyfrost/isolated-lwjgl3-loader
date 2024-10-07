package org.polyfrost.lwjgl.isolatedloader.bootstrap;

import org.lwjgl.system.Configuration;

import java.nio.file.Path;

@SuppressWarnings({"unused", "DataFlowIssue"})
public class Lwjgl3Bootstrapper {

    private static boolean isSetup = false;

    public static void setup(Path nativesDir) {
        if (isSetup) {
            return;
        }

        // We also need to set the memory allocator to system, as the jemalloc allocator is not available in our isolated
        // environment. Usually, we wouldn't need to manually define the memory allocator, but because we may have multiple
        // instances of LWJGL *3* in the same JVM runtime, it may attempt to use the jemalloc allocator.
        Configuration.MEMORY_ALLOCATOR.set("system");

        // We need to tell LWJGL to extract all of its natives to a specified directory, because Java restricts
        // a single native to only be loaded once per runtime. This is a problem because we may be loading multiple
        // instances of LWJGL3 in the same JVM runtime.
        // To get around this, we extract all of our natives to a custom, isolated directory so that they don't
        // conflict with each other.
        Configuration.SHARED_LIBRARY_EXTRACT_DIRECTORY.set(nativesDir.toAbsolutePath().toString());

        // As of Minecraft 1.20+, the extract *path* is set, which takes precedence over the extract directory.
        // The extract path uses the same directory for all natives, regardless of LWJGL version. In our environment,
        // where we may have multiple versions of LWJGL loaded, this is a problem. We need to unset the extract path
        // so that LWJGL falls back to using the extract directory. Thankfully, this is as easy as setting the
        // extract path to null.
        Configuration.SHARED_LIBRARY_EXTRACT_PATH.set(null);

        isSetup = true;
    }

}
