package dev.imabad.theatrical.fabric;

import dev.architectury.event.events.common.PlayerEvent;
import dev.imabad.theatrical.Theatrical;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

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
    }
}
