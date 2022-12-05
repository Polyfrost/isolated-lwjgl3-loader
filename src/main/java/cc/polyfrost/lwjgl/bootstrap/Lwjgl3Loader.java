package cc.polyfrost.lwjgl.bootstrap;

import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * A custom class loader to isolate and define LWJGL3 classes.
 *
 * @author xtrm
 * @since 0.0.1
 */
class Lwjgl3Loader extends URLClassLoader {
    Lwjgl3Loader(@Nullable ClassLoader parent) {
        super(new URL[0], parent);
    }

    public void addURL(URL url) {
        super.addURL(url);
    }
}
