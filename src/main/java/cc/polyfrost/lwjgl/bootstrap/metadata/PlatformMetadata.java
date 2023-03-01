package cc.polyfrost.lwjgl.bootstrap.metadata;

import fr.stardustenterprises.plat4k.EnumArchitecture;
import fr.stardustenterprises.plat4k.EnumOperatingSystem;
import fr.stardustenterprises.plat4k.Platform;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xtrm
 * @since 0.0.1
 */
public final @Data class PlatformMetadata {
    private static final Map<Integer, @Nullable String> LWJGL_VERSION_MAP = new HashMap<>();
    private static final int LEGACY_MINECRAFT_VERSION_MAX = 11202;

    private final @NotNull String lwjglVersion;
    private final @NotNull String lwjglNativeClassifier;
    private final boolean requiresSystemPlatform;

    @NotNull
    public static PlatformMetadata from(int minecraftVersion) {
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
                getLwjglVersion(minecraftVersion),
                classifier,
                minecraftVersion <= LEGACY_MINECRAFT_VERSION_MAX
        );
    }

    private static @NotNull String getLwjglVersion(int minecraftVersion) {
        while (minecraftVersion > 0) {
            if (LWJGL_VERSION_MAP.containsKey(minecraftVersion))
                break;
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
        LWJGL_VERSION_MAP.put(1, "3.3.1");
        LWJGL_VERSION_MAP.put(11302, "3.1.6");
        LWJGL_VERSION_MAP.put(11400, "3.2.1");
        LWJGL_VERSION_MAP.put(11404, "3.2.2");
        LWJGL_VERSION_MAP.put(11900, "3.3.1");
    }
}
