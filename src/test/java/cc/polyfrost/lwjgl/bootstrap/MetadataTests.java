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
        assertEquals("3.3.1", platformMetadata.lwjglVersion);
        assertTrue(platformMetadata.requiresSystemPlatform);

        PlatformMetadata platformMetadata2 =
                PlatformMetadata.from(11302);
        assertEquals("3.1.6", platformMetadata2.lwjglVersion);
        assertFalse(platformMetadata2.requiresSystemPlatform);
    }

    @Test
    public void test() {
        PlatformMetadata platformMetadata =
                PlatformMetadata.from(10809);

        assertDoesNotThrow(() -> {
            List<ArtifactMetadata> artifacts = Lwjgl3Downloader.INSTANCE
                    .fetchArtifactsInfo(platformMetadata);

            // System + NVG, TFD & STB, * 2 for natives (so 8 in total uwu)
            assertEquals((1 + 3) * 2, artifacts.size());
        });
    }
}
