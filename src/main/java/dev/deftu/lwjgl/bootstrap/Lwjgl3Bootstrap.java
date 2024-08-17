package dev.deftu.lwjgl.bootstrap;

import cc.polyfrost.polyio.api.Store;
import cc.polyfrost.polyio.store.PolyStore;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;

public class Lwjgl3Bootstrap {

    private static final Store store;

    private Lwjgl3Bootstrap() {
    }

    public static void initialize(int paddedMinecraftVersion, String[] lwjglModules) throws IOException {
        Set<Path> jars = Lwjgl3Downloader.download(paddedMinecraftVersion, lwjglModules);
        URL[] urls = jars.stream()
                .map(path -> Lwjgl3Transformer.maybeTransform(paddedMinecraftVersion, path))
                .map(path -> {
                    try {
                        return path.toUri().toURL();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).toArray(URL[]::new);

        Arrays.stream(urls).forEach(Launch.classLoader::addURL);

        clean();
        setLibraryExtractDir();
    }

    public static Store getStore() {
        return store;
    }

    @SuppressWarnings("unchecked")
    private static void clean() {
        try {
            Class<?> lclClass = LaunchClassLoader.class;
            Field f = lclClass.getDeclaredField("classLoaderExceptions");
            f.setAccessible(true);
            Set<String> cle = (Set<String>) f.get(Launch.classLoader);
            cle.remove("org.lwjgl.");
            cle.add("org.lwjgl.open");
            cle.add("org.lwjgl.input.");
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to clear LaunchClassLoader exceptions", e);
        } catch (Throwable ignored) {
        }
    }

    private static void setLibraryExtractDir() {
        // TODO
    }

    static {
        store = PolyStore.GLOBAL_STORE.getSubStore("lwjgl3-bootstrap");
    }

}
