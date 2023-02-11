package cc.polyfrost.lwjgl.bootstrap;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

@Testable
public class DownloaderTest {
    @Test
    public void testDownload() {
        Lwjgl3Downloader.INSTANCE.ensureDownloaded(11605);
    }
}
