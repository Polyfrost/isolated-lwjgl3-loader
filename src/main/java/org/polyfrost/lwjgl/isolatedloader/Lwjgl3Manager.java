package org.polyfrost.lwjgl.isolatedloader;

import dev.deftu.filestream.FileStream;
import dev.deftu.filestream.api.Store;
import dev.deftu.filestream.store.FileStore;
import org.polyfrost.lwjgl.isolatedloader.classloader.IsolatedClassLoader;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.file.Path;
import java.util.Set;

public class Lwjgl3Manager {

    private static Store store;
    private static IsolatedClassLoader classLoader;
    private static ClassLoader parentClassLoader;

    private Lwjgl3Manager() {
    }

    public static void initialize(ClassLoader parent, String[] lwjglModules) throws IOException {
        if (store == null) {
            Store globalStore = new FileStore(
                    FileStream.getLocalStorage(),
                    "Deftu",
                    Store.ObjectSchema.DIRECT
            );

            store = globalStore.getSubStore("Isolated LWJGL3 Loader");
        }

        if (parentClassLoader != null && parentClassLoader != parent) {
            throw new IllegalArgumentException("Parent class loader cannot be changed");
        } else {
            parentClassLoader = parent;
        }

        Set<Path> jars = Lwjgl3Downloader.download(lwjglModules);
        URL[] urls = jars.stream()
                .map(Lwjgl3Transformer::maybeTransform)
                .map(path -> {
                    try {
                        return path.toUri().toURL();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).distinct().toArray(URL[]::new);

        if (classLoader == null) {
            // Lazy-load our class loader
            classLoader = new IsolatedClassLoader(urls, parent);
        } else {
            // Add the new URLs to the existing class loader
            for (URL url : urls) {
                classLoader.loadUrl(url);
            }
        }

        try {
            // Bootstrap our isolated LWJGL3 environment inside the isolated class loader
            Path nativesDir = store.getSubStore("natives", Store.ObjectSchema.DIRECT).getStoreRoot();
            System.out.println(nativesDir);
            classLoader.loadClass("org.polyfrost.lwjgl.isolatedloader.bootstrap.Lwjgl3Bootstrapper")
                    .getMethod("setup", Path.class)
                    .invoke(null, nativesDir);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Get an instance of a class from the isolated class loader.
     * <p>
     * You may need to add a loading exception to your API class (or ideally, package) using {@link IsolatedClassLoader#addLoadingException(String)}.
     * <p>
     * Using any class from your main class loader inside your implementation may cause issues! Please find ways to avoid this.
     * For example, you can use {@code (int rgba)} instead of {@code (java.awt.Color color)} in your methods.
     *
     * @param clazz The class to get an instance of
     * @param implementationName The full name (f.ex: "dev.deftu.lwjgl.isolatedloader.MyClass") of the implementation class
     * @param args The arguments to pass to the constructor
     * @param <T> The type of the class
     * @return An instance of the class
     *
     * @author Deftu
     * @since 0.2.0
     */
    public static <T> T getIsolated(Class<T> clazz, String implementationName, Object... args) {
        try {
            Class<?> implementationClass = classLoader.loadClass(implementationName);
            Class<?>[] parameterTypes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                parameterTypes[i] = args[i].getClass();
            }

            Constructor<?> constructor = implementationClass.getConstructor(parameterTypes);
            constructor.setAccessible(true);
            Object instance = constructor.newInstance(args);
            return clazz.cast(instance);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static Store getStore() {
        return store;
    }

    public static IsolatedClassLoader getClassLoader() {
        return classLoader;
    }

}
