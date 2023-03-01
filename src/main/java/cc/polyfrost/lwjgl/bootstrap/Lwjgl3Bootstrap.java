package cc.polyfrost.lwjgl.bootstrap;

import cc.polyfrost.lwjgl.bootstrap.hook.LoaderHook;
import cc.polyfrost.polyio.api.Store;
import cc.polyfrost.polyio.store.PolyStore;
import fr.stardustenterprises.plat4k.Platform;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

/**
 * Main class for the LWJGL3 bootstrap library.
 *
 * @author xtrm
 * @since 0.0.1
 */
public enum Lwjgl3Bootstrap {
    /**
     * The singleton instance.
     */
    INSTANCE;

    @NotNull
    private final Platform platform;
    @NotNull
    private final Store store;

    Lwjgl3Bootstrap() {
        this.platform = Platform.getCurrentPlatform();
        this.store = PolyStore.GLOBAL_STORE.getSubStore("lwjgl3-bootstrap");
    }

    /**
     * Initializes lwjgl3-bootstrap for Minecraft usage.
     *
     * @param minecraftVersion The Minecraft version in context, in padded
     *                         integer form. (ex. 1.12.2 -> 11202)
     * @throws IOException If an I/O error occurs.
     */
    public void initialize(int minecraftVersion) throws IOException {
        // Download the LWJGL3 artifacts
        // Load the LWJGL3 artifacts

        List<Path> jars =
                Lwjgl3Downloader.INSTANCE.ensureDownloaded(minecraftVersion);
        URL[] urls = jars.stream().map(it -> {
            try {
                return it.toUri().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }).toArray(URL[]::new);

        // Add the jars to the classpath
        LoaderHook loaderHook = LoaderHook.All.findAppropriate();
        for (URL url : urls) {
            loaderHook.addURL(url);
        }
    }

    public @NotNull Store getStore() {
        return store;
    }
}
