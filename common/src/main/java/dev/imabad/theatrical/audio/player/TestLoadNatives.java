package dev.imabad.theatrical.audio.player;

import com.sedmelluq.discord.lavaplayer.natives.ConnectorNativeLibLoader;
import com.sedmelluq.lava.common.natives.NativeLibraryLoader;
import com.sedmelluq.lava.common.natives.architecture.DefaultOperatingSystemTypes;

public class TestLoadNatives {
    private static final NativeLibraryLoader[] loaders = new NativeLibraryLoader[] {
            NativeLibraryLoader.createFiltered(TestLoadNatives.class, "libmpg123-0",
                    it -> it.osType == DefaultOperatingSystemTypes.WINDOWS),
            NativeLibraryLoader.create(TestLoadNatives.class, "connector")
    };

    /**
     * Loads the connector library with its dependencies for the current system
     */
    public static void loadConnectorLibrary() {
        for (NativeLibraryLoader loader : loaders) {
            loader.load();
        }
    }
}
