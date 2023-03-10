package cc.polyfrost.lwjgl.bootstrap.hook.impl;

import cc.polyfrost.lwjgl.bootstrap.hook.LoaderHook;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author xtrm
 */
@SuppressWarnings("unchecked")
public class LaunchWrapperHook implements LoaderHook {
    static void clean() {
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

    @Override
    public boolean canApply() {
        try {
            Class.forName("net.minecraft.launchwrapper.Launch");
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    public void addURL(@NotNull URL url) {
        Launch.classLoader.addURL(url);
    }

    @Override
    public void provideClassloader(@NotNull Consumer<ClassLoader> consumer) {
        clean();
        consumer.accept(Launch.classLoader);
    }
}
