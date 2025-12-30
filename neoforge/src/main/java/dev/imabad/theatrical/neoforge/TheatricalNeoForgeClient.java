package dev.imabad.theatrical.neoforge;

import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.api.Fixture;
import dev.imabad.theatrical.fixtures.Fixtures;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import net.neoforged.neoforge.common.NeoForge;

import java.util.HashMap;
import java.util.Map;

public class TheatricalNeoForgeClient {

    public static final Map<Identifier, StandaloneModelKey<BlockStateModel>> EXTRA_MODELS = new HashMap();
    public static void init(IEventBus modBus) {
        modBus.addListener((FMLClientSetupEvent clientSetup) -> {
            TheatricalClient.init();
            NeoForge.EVENT_BUS.addListener((RenderLevelStageEvent.AfterTripwireBlocks renderLevelStageEvent) -> {
                TheatricalClient.renderWorldLastAfterTripwire(renderLevelStageEvent.getLevelRenderer());
            });
            NeoForge.EVENT_BUS.addListener((RenderLevelStageEvent.AfterEntities renderLevelStageEvent) -> {
                TheatricalClient.renderWorldLast(renderLevelStageEvent.getPoseStack(),
                        renderLevelStageEvent.getLevelRenderState().cameraRenderState);
            });
        });
        modBus.addListener((ModelEvent.RegisterStandalone additionalEvent) -> {
            for(Fixture fixture : Fixtures.FIXTURES){
                if(fixture.getStaticModel() != null) {
                    StandaloneModelKey<BlockStateModel> standaloneModelKey = new StandaloneModelKey<>(() -> fixture.getStaticModel().toString());
                    EXTRA_MODELS.put(fixture.getStaticModel(), standaloneModelKey);
                    additionalEvent.register(standaloneModelKey,
                            SimpleUnbakedStandaloneModel.blockStateModel(fixture.getStaticModel()));
                }
                if(fixture.hasPanModel() && fixture.getPanModel() != null) {
                    StandaloneModelKey<BlockStateModel> standaloneModelKey = new StandaloneModelKey<>(() -> fixture.getPanModel().toString());
                    EXTRA_MODELS.put(fixture.getPanModel(), standaloneModelKey);
                    additionalEvent.register(standaloneModelKey,
                            SimpleUnbakedStandaloneModel.blockStateModel(fixture.getPanModel()));
                }
                if(fixture.hasTiltModel() && fixture.getTiltModel() != null) {
                    StandaloneModelKey<BlockStateModel> standaloneModelKey = new StandaloneModelKey<>(() -> fixture.getTiltModel().toString());
                    EXTRA_MODELS.put(fixture.getTiltModel(), standaloneModelKey);
                    additionalEvent.register(standaloneModelKey,
                            SimpleUnbakedStandaloneModel.blockStateModel(fixture.getTiltModel()));
                }
            }
        });
    }

}
