package cc.polyfrost.lwjgl.bootstrap;

import cc.polyfrost.lwjgl.bootstrap.metadata.ArtifactMetadata;
import cc.polyfrost.lwjgl.bootstrap.metadata.PlatformMetadata;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testable
public class MetadataTests {
    @Test
    public void testPlatformMetadata() {
        assertThrows(IllegalArgumentException.class, () -> {
            PlatformMetadata.from(0);
        });
        assertDoesNotThrow(() -> {
            PlatformMetadata.from(10809);
        });

        PlatformMetadata platformMetadata =
                PlatformMetadata.from(10809);
        assertEquals("3.3.1", platformMetadata.getLwjglVersion());
        assertTrue(platformMetadata.isRequiresSystemPlatform());

        PlatformMetadata platformMetadata2 =
                PlatformMetadata.from(11302);
        assertEquals("3.1.6", platformMetadata2.getLwjglVersion());
        assertFalse(platformMetadata2.isRequiresSystemPlatform());
    }

    @Test
    public void artifacts_1_8_9() {
        PlatformMetadata platformMetadata =
                PlatformMetadata.from(10809);

        assertDoesNotThrow(() -> {
            List<ArtifactMetadata> artifacts = Lwjgl3Downloader.INSTANCE
                    .requiredFor(platformMetadata);

            // System + NVG, TFD & STB, * 2 for natives (so 8 in total uwu)
            assertEquals((1 + 3) * 2, artifacts.size());
        });
    }

    @Test
    public void artifacts_1_16_5() {
        PlatformMetadata platformMetadata =
                PlatformMetadata.from(11605);

        assertDoesNotThrow(() -> {
            List<ArtifactMetadata> artifacts = Lwjgl3Downloader.INSTANCE
                    .requiredFor(platformMetadata);
            artifacts.forEach(it -> System.out.println(it.getArtifactDeclaration()));

            // NVG, TFD & STB, * 2 for natives (so 6 in total uwu)
            assertEquals((3) * 2, artifacts.size());
        });
    }
}
