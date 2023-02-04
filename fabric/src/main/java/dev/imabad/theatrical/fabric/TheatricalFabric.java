package dev.imabad.theatrical.fabric;

import dev.imabad.theatrical.Theatrical;
import net.fabricmc.api.ModInitializer;

public class TheatricalFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Theatrical.init();
    }
}
