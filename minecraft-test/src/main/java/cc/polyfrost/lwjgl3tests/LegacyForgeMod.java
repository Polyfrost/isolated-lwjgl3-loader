//#if MODERN==0 && FORGE
package cc.polyfrost.lwjgl3tests;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = "@ID@", name = "@NAME@", version = "@VER@")
public class LegacyForgeMod {
    public LegacyForgeMod() {
        Loader.load();
    }

    @Mod.EventHandler
    public void onInit(net.minecraftforge.fml.common.event.FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRender(net.minecraftforge.client.event.RenderGameOverlayEvent event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }
        Renderer.render();
    }
}
//#endif
