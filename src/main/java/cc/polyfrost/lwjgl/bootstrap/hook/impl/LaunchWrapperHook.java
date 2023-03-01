package cc.polyfrost.lwjgl.bootstrap.hook.impl;

import cc.polyfrost.lwjgl.bootstrap.hook.LoaderHook;
import net.minecraft.launchwrapper.Launch;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

/**
 * @author xtrm
 */
public class LaunchWrapperHook implements LoaderHook {
    @Override
    public boolean canApply() {
        return canEval(() -> Launch.classLoader);
    }

    @Override
    public void addURL(@NotNull URL url) {
        Launch.classLoader.addURL(url);
    }
}
