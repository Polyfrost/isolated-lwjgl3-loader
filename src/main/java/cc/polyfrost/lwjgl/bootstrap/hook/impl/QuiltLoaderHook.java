package cc.polyfrost.lwjgl.bootstrap.hook.impl;

import cc.polyfrost.lwjgl.bootstrap.hook.LoaderHook;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.loader.impl.launch.common.QuiltLauncherBase;
import org.quiltmc.loader.impl.util.UrlUtil;

import java.net.URL;
import java.util.function.Consumer;

/**
 * @author xtrm
 */
public class QuiltLoaderHook implements LoaderHook {
    @Override
    public boolean canApply() {
        try {
            Class.forName("org.quiltmc.loader.api.QuiltLoader");
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    public void addURL(@NotNull URL url) {
        QuiltLauncherBase.getLauncher().addToClassPath(UrlUtil.asPath(url));
    }

    @Override
    public void provideClassloader(@NotNull Consumer<ClassLoader> consumer) {
        consumer.accept(QuiltLauncherBase.getLauncher().getTargetClassLoader());
    }
}
