package cc.polyfrost.example;

import net.fabricmc.api.ClientModInitializer;

public class ExampleInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ExampleRenderer.render();
    }
}
