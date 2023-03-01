package cc.polyfrost.lwjgl.bootstrap.hook.impl;

import cc.polyfrost.lwjgl.bootstrap.hook.LoaderHook;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.TransformingClassLoader;
import cpw.mods.modlauncher.api.IEnvironment;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * @author xtrm
 */
@SuppressWarnings({"unchecked", "JavaReflectionMemberAccess"})
public class ModLauncherHook implements LoaderHook {
    private static DelegateClassLoader delegateClassLoader;
    private static final AtomicBoolean hooked = new AtomicBoolean(false);

    @SneakyThrows
    public ModLauncherHook() {
        if (hooked.getAndSet(true)) {
            return;
        }

        try {
            IEnvironment env = Launcher.INSTANCE.environment();
        } catch (LinkageError ignored) {
            // ModLauncher not present
            return;
        }

        Class<Launcher> launcherClass = Launcher.class;
        Field field = launcherClass.getDeclaredField("classLoader");
        field.setAccessible(true);
        TransformingClassLoader classLoader =
                (TransformingClassLoader) field.get(Launcher.INSTANCE);

        try {
            hookClassBytesFinder(classLoader);
            System.out.println("> Hooked class bytes finder");
            delegateClassLoader = new DelegateClassLoader();
        } catch (Throwable any) {
            try {
                hookResourceFinder(classLoader);
                System.out.println("> Hooked resource finder");
            } catch (Throwable any2) {
                try {
                    hookModuleClassLoader(classLoader);
                    System.out.println("> Hooked module class loader");
                } catch (Throwable any3) {
                    any2.addSuppressed(any3);
                    any.addSuppressed(any2);
                    throw any;
                }
            }
        }
    }

    @Override
    public boolean canApply() {
        return canEval(() -> Launcher.INSTANCE);
    }

    @Override
    public void addURL(@NotNull URL url) {
        delegateClassLoader.addURL(url);
    }

    @SneakyThrows
    private static void hookClassBytesFinder(TransformingClassLoader classLoader) {
        Class<?> tcl = ((Object) classLoader).getClass();
        Field field = tcl.getDeclaredField("classBytesFinder");
        field.setAccessible(true);
        Function<String, URL> origin = (Function<String, URL>) field.get(classLoader);
        Function<String, URL> hooked = (name) -> {
            URL url = delegateClassLoader.findResource(name);
            if (url != null) {
                return url;
            }
            return origin.apply(name);
        };
        field.set(classLoader, hooked);
    }

    @SneakyThrows
    private static void hookResourceFinder(TransformingClassLoader classLoader) {
        Class<?> tcl = ((Object) classLoader).getClass();
        Field field = tcl.getDeclaredField("resourceFinder");
        field.setAccessible(true);
        Function<String, Enumeration<URL>> origin = (Function<String, Enumeration<URL>>) field.get(classLoader);
        Function<String, Enumeration<URL>> hooked = (name) -> {
            try {
                Enumeration<URL> urls = delegateClassLoader.findResources(name);
                if (urls != null && urls.hasMoreElements()) {
                    return urls;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return origin.apply(name);
        };
        field.set(classLoader, hooked);
    }

    @SneakyThrows
    private static void hookModuleClassLoader(TransformingClassLoader classLoader) {
        Class<?> mcl = ((Object) classLoader).getClass().getSuperclass();
        Field fallbackField = mcl.getDeclaredField("fallbackClassLoader");
        fallbackField.setAccessible(true);
        ClassLoader fallback = (ClassLoader) fallbackField.get(classLoader);
        delegateClassLoader = new DelegateClassLoader(fallback);
        fallbackField.set(classLoader, delegateClassLoader);

//        Field configField = mcl.getDeclaredField("configuration");
//        configField.setAccessible(true);
//
//        System.out.println("Config: " + configField.get(classLoader));
//
//        Configuration.resolveAndBind()
//
//        IModuleLayerManager mlm = Launcher.INSTANCE.findLayerManager()
//                .orElseThrow(RuntimeException::new);
//        ModuleLayerHandler mlh = (ModuleLayerHandler) mlm;
//        Field completedLayersField = mlh.getClass().getDeclaredField("completedLayers");
//        completedLayersField.setAccessible(true);

    }
}

class DelegateClassLoader extends URLClassLoader {
    public DelegateClassLoader(ClassLoader parent) {
        super(new URL[]{}, parent);
    }

    public DelegateClassLoader() {
        this(ClassLoader.getSystemClassLoader());
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }
}