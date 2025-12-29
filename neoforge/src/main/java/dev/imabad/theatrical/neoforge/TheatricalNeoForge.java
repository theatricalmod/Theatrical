package dev.imabad.theatrical.neoforge;

import dev.imabad.theatrical.Theatrical;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(Theatrical.MOD_ID)
public class TheatricalNeoForge {

    public TheatricalNeoForge(IEventBus modBus) {
        Theatrical.init();
        modBus.addListener(DataEvent::onData);
        if (FMLEnvironment.dist.isClient()) {
            TheatricalNeoForgeClient.init(modBus);
        }
    }
}
