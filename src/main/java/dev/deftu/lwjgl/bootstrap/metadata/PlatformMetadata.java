package dev.deftu.lwjgl.bootstrap.metadata;

import fr.stardustenterprises.plat4k.EnumArchitecture;
import fr.stardustenterprises.plat4k.EnumOperatingSystem;
import fr.stardustenterprises.plat4k.Platform;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xtrm
 * @since 0.0.1
 */
public final class PlatformMetadata {

    private static final Map<Integer, String> LWJGL_VERSION_MAP = new HashMap<>();
    private static final int LEGACY_MINECRAFT_VERSION_MAX = 1_12_02;

    private final @NotNull String lwjglVersion;
    private final @NotNull String lwjglNativeClassifier;
    private final boolean requiresSystemPlatform;

    public PlatformMetadata(@NotNull String lwjglVersion, @NotNull String lwjglNativeClassifier, boolean requiresSystemPlatform) {
        this.lwjglVersion = lwjglVersion;
        this.lwjglNativeClassifier = lwjglNativeClassifier;
        this.requiresSystemPlatform = requiresSystemPlatform;
    }

    @NotNull
    public String getLwjglVersion() {
        return lwjglVersion;
    }

    @NotNull
    public String getLwjglNativeClassifier() {
        return lwjglNativeClassifier;
    }

    public boolean isRequiresSystemPlatform() {
        return requiresSystemPlatform;
    }

    @NotNull
    public static PlatformMetadata from(int paddedMinecraftVersion) {
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

        return new PlatformMetadata(
                getLwjglVersion(paddedMinecraftVersion),
                classifier,
                paddedMinecraftVersion <= LEGACY_MINECRAFT_VERSION_MAX
        );
    }

    private static @NotNull String getLwjglVersion(int minecraftVersion) {
        while (minecraftVersion > 0) {
            if (LWJGL_VERSION_MAP.containsKey(minecraftVersion)) {
                break;
            }

            minecraftVersion--;
        }

        String version = LWJGL_VERSION_MAP.get(minecraftVersion);
        if (version == null) {
            throw new IllegalArgumentException("Version " + minecraftVersion + " is not supported!");
        }

        return version;
    }

    static {
        LWJGL_VERSION_MAP.put(0, null);
        LWJGL_VERSION_MAP.put(1, "3.3.3");
        LWJGL_VERSION_MAP.put(1_16_05, "3.2.2");
        LWJGL_VERSION_MAP.put(1_19_02, "3.3.1");
        LWJGL_VERSION_MAP.put(1_20_02, "3.3.2");
        LWJGL_VERSION_MAP.put(1_20_06, "3.3.3");
    }

}
