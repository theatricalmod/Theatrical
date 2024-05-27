package dev.imabad.theatrical.fabric;

import dev.architectury.platform.Platform;
import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.api.Fixture;
import dev.imabad.theatrical.fixtures.Fixtures;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

public class TheatricalClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TheatricalClient.init();
        ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, out) -> {
            for(Fixture fixture : Fixtures.FIXTURES){
                out.accept(fixture.getStaticModel());
                if(fixture.hasPanModel()) {
                    out.accept(fixture.getPanModel());
                }
                if(fixture.hasTiltModel()) {
                    out.accept(fixture.getTiltModel());
                }
            }
        });
        WorldRenderEvents.START.register(this::renderWorldStartFabric);
        if(Platform.isDevelopmentEnvironment()) {
            WorldRenderEvents.AFTER_TRANSLUCENT.register(this::renderWorldLastFabric);
        }
    }


    private void renderWorldStartFabric(WorldRenderContext context) {
        TheatricalClient.renderWorldLastAfterTripwire(context.worldRenderer());
    }

    private void renderWorldLastFabric(WorldRenderContext context){
        TheatricalClient.renderWorldLast(context.matrixStack(), context.projectionMatrix(), context.camera(), context.tickDelta());
    }

}
