package dev.imabad.theatrical.fabric;

import dev.architectury.platform.Platform;
import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.api.Fixture;
import dev.imabad.theatrical.fixtures.Fixtures;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class TheatricalClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TheatricalClient.init();
        ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, out) -> {
            for(Fixture fixture : Fixtures.FIXTURES){
                out.accept(fixture.getPanModel());
                out.accept(fixture.getStaticModel());
                out.accept(fixture.getTiltModel());
            }
        });
        if(Platform.isDevelopmentEnvironment()) {
            WorldRenderEvents.AFTER_TRANSLUCENT.register(this::renderWorldLastFabric);
        }
    }

    private void renderWorldLastFabric(WorldRenderContext context){
        TheatricalClient.renderWorldLast(context.matrixStack(), context.projectionMatrix(), context.camera(), context.tickDelta());
    }

}
