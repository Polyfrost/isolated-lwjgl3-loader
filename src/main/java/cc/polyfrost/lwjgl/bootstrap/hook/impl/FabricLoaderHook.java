package cc.polyfrost.lwjgl.bootstrap.hook.impl;

import cc.polyfrost.lwjgl.bootstrap.hook.LoaderHook;
import lombok.SneakyThrows;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.loader.util.UrlUtil;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.loader.impl.launch.common.QuiltLauncherBase;

import java.net.URL;

/**
 * @author xtrm
 */
@SuppressWarnings("deprecation")
public class FabricLoaderHook implements LoaderHook {
    @SuppressWarnings("Convert2MethodRef")
    @Override
    public boolean canApply() {
        return canEval(() -> FabricLauncherBase.getLauncher()) &&
                !canEval(() -> QuiltLauncherBase.getLauncher());
    }

    @SneakyThrows
    @Override
    public void addURL(@NotNull URL url) {
        FabricLauncherBase.getLauncher().addToClassPath(UrlUtil.asPath(url));
    }
}
