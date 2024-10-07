package dev.deftu.lwjgl.isolatedloader.classloader;

import dev.deftu.lwjgl.isolatedloader.utils.CombinedEnumeration;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Manifest;

@SuppressWarnings("unused")
public class IsolatedClassLoader extends URLClassLoader {

    private final ClassLoader delegatedClassLoader;

    private final Set<String> loadingExceptions = new HashSet<>();
    private final Map<String, Class<?>> classReferenceCache = new ConcurrentHashMap<>();

    public IsolatedClassLoader(URL[] urls, ClassLoader delegatedClassLoader) {
        // This class loader is in isolation of it's parent class loader as not to taint any existing LWJGL classes and natives.
        // Thus, we use a no-op class loader as our parent.
        // Instead, we store the parent class loader in a field and delegate to it when necessary.
        super(urls, NoOpClassLoader.INSTANCE);

        this.delegatedClassLoader = delegatedClassLoader;

        // Java
        this.loadingExceptions.add("java.");
        this.loadingExceptions.add("javax.");
        this.loadingExceptions.add("sun.");
        this.loadingExceptions.add("com.sun.");
        this.loadingExceptions.add("jdk.");
        this.loadingExceptions.add("org.w3c.");

        // Log4j - Ensures that the logger set up by the launch handler is used
        this.loadingExceptions.add("org.apache.logging.");
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        // Attempt to load the class from the cache
        Class<?> cachedClass = this.classReferenceCache.get(name);
        if (cachedClass != null) {
            return cachedClass;
        }

        // Check if the class should be loaded by the delegated class loader
        for (String exception : this.loadingExceptions) {
            if (name.startsWith(exception)) {
                Class<?> delegatedClass = this.delegatedClassLoader.loadClass(name);
                this.classReferenceCache.put(name, delegatedClass);
                return delegatedClass;
            }
        }

        // Attempt to load the class from the isolated class loader
        synchronized (getClassLoadingLock(name)) {
            Class<?> cls = findLoadedClass(name);
            if (cls == null) {
                cls = findClass(name);
            }

            this.classReferenceCache.put(name, cls);
            return cls;
        }
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            return super.findClass(name);
        } catch (ClassNotFoundException e) {
            // Manually read and define the class using our resources
            String path = name.replace('.', '/').concat(".class");
            URL resource = getResource(path);
            if (resource == null) {
                throw new ClassNotFoundException(name + " - " + path);
            }

            try (InputStream inputStream = resource.openStream()) {
                byte[] classBytes = readAllBytes(inputStream);
                CodeSource codeSource = new CodeSource(resource, (CodeSigner[]) null);
                return defineClass(name, classBytes, 0, classBytes.length, codeSource);
            } catch (IOException ex) {
                throw new ClassNotFoundException(name, ex);
            }
        }
    }

    @Nullable
    public URL getResource(String name) {
        URL isolatedResource = super.findResource(name);
        if (isolatedResource != null) {
            return isolatedResource;
        }

        return this.delegatedClassLoader.getResource(name);
    }

    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> isolatedResources = super.findResources(name);
        Enumeration<URL> delegatedResources = this.delegatedClassLoader.getResources(name);

        return new CombinedEnumeration<>(isolatedResources, delegatedResources);
    }

    public void loadUrl(URL url) {
        addURL(url);
    }

    public void addLoadingException(String exception) {
        this.loadingExceptions.add(exception);
    }

    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        return outputStream.toByteArray();
    }

    static {
        registerAsParallelCapable();
    }

}
