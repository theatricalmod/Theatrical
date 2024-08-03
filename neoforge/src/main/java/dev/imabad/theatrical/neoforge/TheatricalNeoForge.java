package dev.imabad.theatrical.neoforge;

import dev.architectury.platform.Platform;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.api.Fixture;
import dev.imabad.theatrical.fixtures.Fixtures;
import net.minecraft.core.registries.Registries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(Theatrical.MOD_ID)
public class TheatricalNeoForge {

    public TheatricalNeoForge() {
        Theatrical.init();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(DataEvent::onData);
    }
    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Theatrical.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void setupClient(FMLClientSetupEvent event) {
            TheatricalClient.init();
            FMLJavaModLoadingContext.get().getModEventBus().addListener((ModelEvent.RegisterAdditional additionalEvent) -> {
                for(Fixture fixture : Fixtures.FIXTURES){
                    additionalEvent.register(fixture.getStaticModel());
                    if(fixture.hasPanModel()) {
                        additionalEvent.register(fixture.getPanModel());
                    }
                    if(fixture.hasTiltModel()) {
                        additionalEvent.register(fixture.getTiltModel());
                    }
                }
            });
            NeoForge.EVENT_BUS.addListener((RenderLevelStageEvent renderLevelStageEvent) -> {
                if (renderLevelStageEvent.getStage() == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
                    TheatricalClient.renderWorldLastAfterTripwire(renderLevelStageEvent.getLevelRenderer());
                }
//                if(Platform.isDevelopmentEnvironment()) {
                if(renderLevelStageEvent.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES){
                    TheatricalClient.renderWorldLast(renderLevelStageEvent.getPoseStack(), renderLevelStageEvent.getProjectionMatrix(), renderLevelStageEvent.getCamera(), renderLevelStageEvent.getPartialTick());
                }
//                }
            });
        }
    }
}
