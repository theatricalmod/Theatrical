package dev.imabad.theatrical.forge;

import dev.imabad.theatrical.Theatrical;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class WorldEvents {

    @SubscribeEvent
    public static void onLoadWorld(LevelEvent.Load event) {
        Theatrical.CABLES.levelLoaded(event.getLevel());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event){
        if(event.phase == TickEvent.Phase.START){
            return;
        }
        Theatrical.CABLES.sync.serverTick(event.getServer());
    }
}
