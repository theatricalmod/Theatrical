package dev.imabad.theatrical.forge;

import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.api.Fixture;
import dev.imabad.theatrical.fixtures.Fixtures;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class TheatricalForgeClient {

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional additionalEvent) {
        for(Fixture fixture : Fixtures.FIXTURES){
            if(fixture.getStaticModel() != null) {
                additionalEvent.register(fixture.getStaticModel());
            }
            if(fixture.hasPanModel() && fixture.getPanModel() != null) {
                additionalEvent.register(fixture.getPanModel());
            }
            if(fixture.hasTiltModel() && fixture.getTiltModel() != null) {
                additionalEvent.register(fixture.getTiltModel());
            }
        }
    }

    @SubscribeEvent
    public static void onClient(FMLClientSetupEvent event){
        TheatricalClient.init();
        MinecraftForge.EVENT_BUS.addListener((RenderLevelStageEvent renderLevelStageEvent) -> {
            if (renderLevelStageEvent.getStage() == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
                TheatricalClient.renderWorldLastAfterTripwire(renderLevelStageEvent.getLevelRenderer());
            }
//            if(Platform.isDevelopmentEnvironment()) {
            if (renderLevelStageEvent.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
                TheatricalClient.renderWorldLast(renderLevelStageEvent.getPoseStack(), renderLevelStageEvent.getProjectionMatrix(), renderLevelStageEvent.getCamera(), renderLevelStageEvent.getPartialTick());
            }
//            }
        });
    }
}
