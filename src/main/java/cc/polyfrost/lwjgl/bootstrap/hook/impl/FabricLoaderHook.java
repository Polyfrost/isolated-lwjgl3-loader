package cc.polyfrost.lwjgl.bootstrap.hook.impl;

import cc.polyfrost.lwjgl.bootstrap.hook.LoaderHook;
import lombok.SneakyThrows;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.loader.util.UrlUtil;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.function.Consumer;

/**
 * @author xtrm
 */
@SuppressWarnings("deprecation")
public class FabricLoaderHook implements LoaderHook {
    @Override
    public boolean canApply() {
        try {
            Class.forName("org.quiltmc.loader.api.QuiltLoader");
            return false;
        } catch (Throwable ignored) {
        }

        try {
            Class.forName("net.minecraft.launchwrapper.Launch");
            return false;
        } catch (Throwable ignored) {
        }

        try {
            Class.forName("net.fabricmc.loader.api.FabricLoader");
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @SneakyThrows
    @Override
    public void addURL(@NotNull URL url) {
        FabricLauncherBase.getLauncher().addToClassPath(UrlUtil.asPath(url));
    }

    @Override
    public void provideClassloader(@NotNull Consumer<ClassLoader> consumer) {
        consumer.accept(FabricLauncherBase.getLauncher().getTargetClassLoader());
    }
}
