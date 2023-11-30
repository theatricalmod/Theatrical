package dev.imabad.theatrical.fabric;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.util.ExtServerPlayerGameMode;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;

public class TheatricalFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Theatrical.init();
    }
}
