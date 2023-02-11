//#if FABRIC
package cc.polyfrost.lwjgl3tests;

import net.fabricmc.api.ClientModInitializer;
//#if MODERN
//$$ import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
//#else
import net.legacyfabric.fabric.api.client.rendering.v1.HudRenderCallback;
//#endif

public class TestFabricInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register((matrices, tickDelta) -> {
            Renderer.render(
                    //#if MODERN
                    //$$ matrices
                    //#endif
            );
        });
    }
}
//#endif