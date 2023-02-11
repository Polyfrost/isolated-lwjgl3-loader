package cc.polyfrost.lwjgl3tests;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod("minecraft_test")
public class ModernForgeMod {
    public ModernForgeMod() {
        System.out.println("ModernForgeMod constructor");
        System.out.flush();

        MinecraftForge.EVENT_BUS.addListener((RenderGameOverlayEvent event) -> {
            if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
                return;
            }
            Renderer.render(event.getMatrixStack());
        });
    }

    static {
        System.out.println("ModernForgeMod static-constructor");
        System.out.flush();
    }
}