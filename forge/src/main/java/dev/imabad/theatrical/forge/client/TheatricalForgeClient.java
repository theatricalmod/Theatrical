package dev.imabad.theatrical.forge.client;

import dev.imabad.theatrical.forge.client.model.TheatricalForgeModelLoader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TheatricalForgeClient {

    @SubscribeEvent
    public static void registerGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(TheatricalForgeModelLoader.CABLE_MODEL_LOADER.getPath(), new TheatricalForgeModelLoader());
    }

}
