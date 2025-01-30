package org.polyfrost.lwjgl.isolatedloader.metadata;

import org.polyfrost.polyio.util.Architecture;
import org.polyfrost.polyio.util.OperatingSystem;
import org.jetbrains.annotations.NotNull;

/**
 * @author xtrm
 * @since 0.0.1
 */
public final class PlatformMetadata {

    private final @NotNull String lwjglVersion;
    private final @NotNull String lwjglNativeClassifier;

    public PlatformMetadata(@NotNull String lwjglVersion, @NotNull String lwjglNativeClassifier) {
        this.lwjglVersion = lwjglVersion;
        this.lwjglNativeClassifier = lwjglNativeClassifier;
    }

    @NotNull
    public String getLwjglVersion() {
        return lwjglVersion;
    }

    @NotNull
    public String getLwjglNativeClassifier() {
        return lwjglNativeClassifier;
    }

    @NotNull
    public static PlatformMetadata from() {
        OperatingSystem operatingSystem = OperatingSystem.find();
        Architecture architecture = Architecture.find();

        String classifier = "natives-" + operatingSystem.getName().toLowerCase();
        if (architecture != Architecture.X86_64) {
            classifier += "-" + architecture.getName().toLowerCase();
            if (classifier.contains("aarch")) {
                classifier = classifier.replace("aarch", "arm");
            }
        }

        return new PlatformMetadata(getPlatformLwjglVersion(), classifier);
    }

    private static String getPlatformLwjglVersion() {
        boolean debug = Boolean.getBoolean("isolatedlwjgl3loader.debug");
        String platformProvidedVersion = System.getProperty("isolatedlwjgl3loader.lwjglVersion");
        if (debug && platformProvidedVersion != null) {
            System.out.println("LWJGL version for this platform was explicitly set to " + platformProvidedVersion);
        }

        String version = platformProvidedVersion == null ? "3.3.3" : platformProvidedVersion;
        if (Boolean.getBoolean("isolatedlwjgl3loader.debug")) {
            System.out.println("PlatformMetadata will be using LWJGL version " + version);
        }

        return version;
    }

}
