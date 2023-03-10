package cc.polyfrost.lwjgl.bootstrap;

import cc.polyfrost.lwjgl.bootstrap.hook.LoaderHook;
import cc.polyfrost.polyio.api.Store;
import cc.polyfrost.polyio.store.PolyStore;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Method;
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
@Log4j2
public enum Lwjgl3Bootstrap {
    /**
     * The singleton instance.
     */
    INSTANCE;

    @NotNull
    private final Store store;

    Lwjgl3Bootstrap() {
        this.store = PolyStore.GLOBAL_STORE.getSubStore("lwjgl3-bootstrap");
    }

    /**
     * Initializes lwjgl3-bootstrap for Minecraft usage.
     *
     * @param minecraftVersion The Minecraft version in context, in padded
     *                         integer form. (ex. 1.12.2 = 11202)
     * @throws IOException If an I/O error occurs.
     */
    public void initialize(int minecraftVersion) throws IOException {
        // Download the LWJGL3 artifacts
        // Patch the LWJGL3 artifacts if needed
        List<Path> jars =
                Lwjgl3Downloader.INSTANCE.ensureDownloaded(minecraftVersion);
        URL[] urls = jars.stream()
                .map(it -> minecraftVersion <= 11202 ? Lwjgl3PreProcessor.INSTANCE.process(it) : it)
                .map(it -> {
                    try {
                        return it.toUri().toURL();
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }).toArray(URL[]::new);

        // Add the jars to the classpath
        LoaderHook loaderHook = LoaderHook.All.findAppropriate();
        for (URL url : urls) {
            log.trace("Adding {} to classpath.", url);
            loaderHook.addURL(url);
        }

        // Setup LW3 config
        loaderHook.provideClassloader(classLoader -> {
            try {
                Class<?> configClass = Class.forName("org.lwjgl.system.Configuration", true, classLoader);
                Method setMethod = configClass.getMethod("set", Object.class);

                Object extractDirField = configClass.getField("SHARED_LIBRARY_EXTRACT_DIRECTORY").get(null);
                setMethod.invoke(extractDirField, store.getStoreRoot().resolve("natives").toAbsolutePath().toString());
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Couldn't set lwjgl3 system configuration.", e);
            }
        });
    }

    public @NotNull Store getStore() {
        return store;
    }
}
