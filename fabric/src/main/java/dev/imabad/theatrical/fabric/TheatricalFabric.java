package dev.imabad.theatrical.fabric;

import dev.architectury.event.events.common.PlayerEvent;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.util.ExtServerPlayerGameMode;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TheatricalFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Theatrical.init();
        ServerWorldEvents.LOAD.register((server, world) -> {
            Theatrical.CABLES.levelLoaded(world);
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Theatrical.CABLES.sync.serverTick(server);
        });
        PlayerBlockBreakEvents.BEFORE.register((level, player, blockPos, blockState, blockEntity) -> {
            if(player instanceof ServerPlayer serverPlayer){
                return !Theatrical.handleBlockBreak(level, blockPos, blockState, serverPlayer);
            }
            return true;
        });
        PlayerBlockBreakEvents.CANCELED.register((level, player, pos, state, blockEntity) -> {
            if(player instanceof ServerPlayer serverPlayer) {
                if (((ExtServerPlayerGameMode) serverPlayer.gameMode).shouldCaptureSentBlockEntities()) {
                    ((ExtServerPlayerGameMode) serverPlayer.gameMode).setCapturedBlockEntity(true);
                } else {
                    if (blockEntity != null)
                    {
                        Packet<?> pkt = blockEntity.getUpdatePacket();
                        if (pkt != null)
                        {
                            serverPlayer.connection.send(pkt);
                        }
                    }
                }
            }
        });
    }
}
