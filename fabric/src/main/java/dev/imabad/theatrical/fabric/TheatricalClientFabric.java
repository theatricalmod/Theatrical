package dev.imabad.theatrical.fabric;

import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.api.Fixture;
import dev.imabad.theatrical.fixtures.Fixtures;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class TheatricalClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TheatricalClient.init();
        ModelLoadingPlugin.register(pluginContext -> {
            for(Fixture fixture : Fixtures.FIXTURES){
                if(fixture.getStaticModel() != null) {
                    pluginContext.addModels(fixture.getStaticModel());
                }
                if(fixture.hasPanModel() && fixture.getPanModel() != null) {
                    pluginContext.addModels(fixture.getPanModel());
                }
                if(fixture.hasTiltModel() && fixture.getTiltModel() != null) {
                    pluginContext.addModels(fixture.getTiltModel());
                }
            }
        });
        WorldRenderEvents.START.register(this::renderWorldStartFabric);
//        if(Platform.isDevelopmentEnvironment()) {
            WorldRenderEvents.AFTER_TRANSLUCENT.register(this::renderWorldLastFabric);
//        }
    }


    private void renderWorldStartFabric(WorldRenderContext context) {
        TheatricalClient.renderWorldLastAfterTripwire(context.worldRenderer());
    }

    private void renderWorldLastFabric(WorldRenderContext context){
        TheatricalClient.renderWorldLast(context.matrixStack(), context.projectionMatrix(), context.camera(),
                context.tickCounter().getGameTimeDeltaPartialTick(false));
    }

}
