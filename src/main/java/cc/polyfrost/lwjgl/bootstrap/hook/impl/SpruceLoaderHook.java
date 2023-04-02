package cc.polyfrost.lwjgl.bootstrap.hook.impl;

import cc.polyfrost.lwjgl.bootstrap.hook.LoaderHook;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.function.Consumer;

/**
 * TODO: actually implement
 *
 * @author xtrm
 */
public class SpruceLoaderHook implements LoaderHook {
    @Override
    public boolean canApply() {
        try {
            Class.forName("xyz.spruceloader.trunk.Trunk");
            return true;
        } catch (Throwable ignored) {
        }
        return false;
    }

    @Override
    public void addURL(@NotNull URL url) {
        throw new LinkageError("haha cringe");
    }

    @Override
    public void provideClassloader(@NotNull Consumer<ClassLoader> consumer) {
        throw new LinkageError("haha cringe");
    }
}
