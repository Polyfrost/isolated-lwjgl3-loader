package dev.deftu.lwjgl.isolatedloader.metadata;

import fr.stardustenterprises.plat4k.EnumArchitecture;
import fr.stardustenterprises.plat4k.EnumOperatingSystem;
import fr.stardustenterprises.plat4k.Platform;
import org.jetbrains.annotations.NotNull;

/**
 * @author xtrm
 * @since 0.0.1
 */
public final class PlatformMetadata {

    private static final String LWJGL_VERSION = "3.3.3";

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
        Platform platform = Platform.getCurrentPlatform();
        EnumOperatingSystem operatingSystem = platform.getOperatingSystem();
        EnumArchitecture architecture = platform.getArchitecture();

        String classifier = "natives-" + operatingSystem.getOsName().toLowerCase();
        if (architecture != EnumArchitecture.X86_64) {
            classifier += "-" + architecture.getIdentifier().toLowerCase();
            if (classifier.contains("aarch")) {
                classifier = classifier.replace("aarch", "arm");
            }
        }

        return new PlatformMetadata(LWJGL_VERSION, classifier);
    }

}
