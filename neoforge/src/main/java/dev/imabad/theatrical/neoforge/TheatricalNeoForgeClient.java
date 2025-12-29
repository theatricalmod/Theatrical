package dev.imabad.theatrical.neoforge;

import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.api.Fixture;
import dev.imabad.theatrical.fixtures.Fixtures;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;

public class TheatricalNeoForgeClient {

    public static void init(IEventBus modBus) {
        modBus.addListener((FMLClientSetupEvent clientSetup) -> {
            TheatricalClient.init();
            modBus.addListener((ModelEvent.RegisterAdditional additionalEvent) -> {
                for(Fixture fixture : Fixtures.FIXTURES){
                    if(fixture.getStaticModel() != null) {
                        additionalEvent.register(ModelResourceLocation.standalone(fixture.getStaticModel()));
                    }
                    if(fixture.hasPanModel() && fixture.getPanModel() != null) {
                        additionalEvent.register(ModelResourceLocation.standalone(fixture.getPanModel()));
                    }
                    if(fixture.hasTiltModel() && fixture.getTiltModel() != null) {
                        additionalEvent.register(ModelResourceLocation.standalone(fixture.getTiltModel()));
                    }
                }
            });
            NeoForge.EVENT_BUS.addListener((RenderLevelStageEvent renderLevelStageEvent) -> {
                if (renderLevelStageEvent.getStage() == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
                    TheatricalClient.renderWorldLastAfterTripwire(renderLevelStageEvent.getLevelRenderer());
                }
//                if(Platform.isDevelopmentEnvironment()) {
                if(renderLevelStageEvent.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES){
                    TheatricalClient.renderWorldLast(renderLevelStageEvent.getPoseStack(),
                            renderLevelStageEvent.getProjectionMatrix(),
                            renderLevelStageEvent.getCamera(),
                            renderLevelStageEvent.getPartialTick().getGameTimeDeltaPartialTick(false));
                }
//                }
            });
        });
    }

}
