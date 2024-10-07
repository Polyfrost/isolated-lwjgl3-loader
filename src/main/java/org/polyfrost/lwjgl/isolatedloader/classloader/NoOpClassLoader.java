package org.polyfrost.lwjgl.isolatedloader.classloader;

import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;

class NoOpClassLoader extends ClassLoader {

    public static final NoOpClassLoader INSTANCE = new NoOpClassLoader();

    private NoOpClassLoader() {
        super(null);
    }

    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        throw new ClassNotFoundException("no-op - " + name);
    }

    @Nullable
    public URL getResource(String name) {
        return null;
    }

    public Enumeration<URL> getResources(String name) {
        return Collections.emptyEnumeration();
    }

}
