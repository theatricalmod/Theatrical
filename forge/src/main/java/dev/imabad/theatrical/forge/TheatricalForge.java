package dev.imabad.theatrical.forge;

import dev.architectury.platform.forge.EventBuses;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.api.Fixture;
import dev.imabad.theatrical.fixtures.Fixtures;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Theatrical.MOD_ID)
public class TheatricalForge {
    public TheatricalForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(Theatrical.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        Theatrical.init();
        if(ModList.get().isLoaded("create")){
            dev.imabad.theatrical.forge.compat.create.CreateCompat.init();
        }
    }

}
