package dev.imabad.theatrical.forge;

import dev.imabad.theatrical.Theatrical;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
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

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if(event.getLevel() instanceof ServerLevel serverLevel && event.getPlayer() instanceof ServerPlayer serverPlayer){
            if(Theatrical.handleBlockBreak(serverLevel, event.getPos(), event.getState(), serverPlayer)){
                event.setCanceled(true);
            }
        }
    }
}
