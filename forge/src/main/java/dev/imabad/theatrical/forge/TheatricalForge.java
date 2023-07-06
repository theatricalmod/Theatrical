package dev.imabad.theatrical.forge;

import dev.architectury.platform.Platform;
import dev.architectury.platform.forge.EventBuses;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.api.Fixture;
import dev.imabad.theatrical.registry.FixtureRegistry;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Theatrical.MOD_ID)
public class TheatricalForge {
    public TheatricalForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(Theatrical.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(DataEvent::onData);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClient);
        Theatrical.init();
    }

    public void onClient(FMLClientSetupEvent event){
        TheatricalClient.init();
        FMLJavaModLoadingContext.get().getModEventBus().addListener((ModelEvent.RegisterAdditional additionalEvent) -> {
            for(Fixture fixture : FixtureRegistry.entries()){
                additionalEvent.register(fixture.getPanModel());
                additionalEvent.register(fixture.getStaticModel());
                additionalEvent.register(fixture.getTiltModel());
            }
        });
        MinecraftForge.EVENT_BUS.addListener((RenderHighlightEvent.Block renderHighlight) -> {
            TheatricalClient.renderHitBox(renderHighlight.getPoseStack(),renderHighlight.getCamera().getEntity().getLevel(), renderHighlight.getTarget().getBlockPos(), renderHighlight.getCamera().getEntity(), renderHighlight.getCamera());
        });
        if(Platform.isDevelopmentEnvironment()) {
            MinecraftForge.EVENT_BUS.addListener((RenderLevelStageEvent renderLevelStageEvent) -> {
                if(renderLevelStageEvent.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES){
                        TheatricalClient.renderWorldLast(renderLevelStageEvent.getPoseStack(), renderLevelStageEvent.getProjectionMatrix(), renderLevelStageEvent.getCamera(), renderLevelStageEvent.getRenderTick());

                }
            });
        }
    }
}
