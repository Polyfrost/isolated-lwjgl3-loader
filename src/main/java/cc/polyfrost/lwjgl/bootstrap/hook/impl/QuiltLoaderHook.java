package cc.polyfrost.lwjgl.bootstrap.hook.impl;

import cc.polyfrost.lwjgl.bootstrap.hook.LoaderHook;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.loader.impl.launch.common.QuiltLauncherBase;
import org.quiltmc.loader.impl.util.UrlUtil;

import java.net.URL;

/**
 * @author xtrm
 */
public class QuiltLoaderHook implements LoaderHook {
    @SuppressWarnings("Convert2MethodRef")
    @Override
    public boolean canApply() {
        return canEval(() -> QuiltLauncherBase.getLauncher());
    }

    @Override
    public void addURL(@NotNull URL url) {
        QuiltLauncherBase.getLauncher().addToClassPath(UrlUtil.asPath(url));
    }
}
