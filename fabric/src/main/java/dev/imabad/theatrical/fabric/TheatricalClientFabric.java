package dev.imabad.theatrical.fabric;

import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.api.Fixture;
import dev.imabad.theatrical.fixtures.Fixtures;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldTerrainRenderContext;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public class TheatricalClientFabric implements ClientModInitializer {

    public static final Map<Identifier, ExtraModelKey<BlockStateModel>> EXTRA_MODELS = new HashMap();

    @Override
    public void onInitializeClient() {
        TheatricalClient.init();
        ModelLoadingPlugin.register(pluginContext -> {
            for(Fixture fixture : Fixtures.FIXTURES){
                if(fixture.getStaticModel() != null) {
                    ExtraModelKey<BlockStateModel> key = ExtraModelKey.create(() -> fixture.getStaticModel().toString());
                    EXTRA_MODELS.put(fixture.getStaticModel(), key);
                    pluginContext.addModel(key,
                            SimpleUnbakedExtraModel.blockStateModel(fixture.getStaticModel()));
                }
                if(fixture.hasPanModel() && fixture.getPanModel() != null) {
                    ExtraModelKey<BlockStateModel> key = ExtraModelKey.create(() -> fixture.getPanModel().toString());
                    EXTRA_MODELS.put(fixture.getPanModel(), key);
                    pluginContext.addModel(key,
                            SimpleUnbakedExtraModel.blockStateModel(fixture.getPanModel()));
                }
                if(fixture.hasTiltModel() && fixture.getTiltModel() != null) {
                    ExtraModelKey<BlockStateModel> key = ExtraModelKey.create(() -> fixture.getTiltModel().toString());
                    EXTRA_MODELS.put(fixture.getTiltModel(), key);
                    pluginContext.addModel(key,
                            SimpleUnbakedExtraModel.blockStateModel(fixture.getTiltModel()));
                }
            }
        });
        WorldRenderEvents.START_MAIN.register(this::renderWorldStartFabric);
//        if(Platform.isDevelopmentEnvironment()) {
        WorldRenderEvents.END_MAIN.register(this::renderWorldLastFabric);
//        }
    }


    private void renderWorldStartFabric(WorldTerrainRenderContext context) {
        TheatricalClient.renderWorldLastAfterTripwire(context.worldRenderer());
    }

    private void renderWorldLastFabric(WorldRenderContext context){
        TheatricalClient.renderWorldLast(context.matrices(), context.worldState().cameraRenderState);
    }

}
