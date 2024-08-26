package dev.deftu.lwjgl.isolatedloader.metadata;

import dev.deftu.filestream.util.Architecture;
import dev.deftu.filestream.util.OperatingSystem;
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
        OperatingSystem operatingSystem = OperatingSystem.find();
        Architecture architecture = Architecture.find();

        String classifier = "natives-" + operatingSystem.getName().toLowerCase();
        if (architecture != Architecture.X86_64) {
            classifier += "-" + architecture.getName().toLowerCase();
            if (classifier.contains("aarch")) {
                classifier = classifier.replace("aarch", "arm");
            }
        }

        return new PlatformMetadata(LWJGL_VERSION, classifier);
    }

}
