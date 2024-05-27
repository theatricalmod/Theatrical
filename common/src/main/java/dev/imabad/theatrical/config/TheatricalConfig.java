package dev.imabad.theatrical.config;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.config.api.TheatricalConfigItem;

public class TheatricalConfig {

    public static TheatricalConfig INSTANCE = new TheatricalConfig();

    public ClientConfig CLIENT;
    public ServerConfig COMMON;

    public void register(ConfigHandler handler){
        CLIENT = handler.registerConfig(Theatrical.MOD_ID, ConfigHandler.ConfigSide.CLIENT, ClientConfig::new);
        COMMON = handler.registerConfig(Theatrical.MOD_ID, ConfigHandler.ConfigSide.COMMON, ServerConfig::new);
    }

    public static class ClientConfig extends BaseConfig{
        @TheatricalConfigItem(minValue = "0", maxValue = "1")
        public double beamOpacity = 0.4;

        @TheatricalConfigItem(minValue = "0")
        public int renderDistance = 64;

        @TheatricalConfigItem
        public boolean doOwnerCheck = true;

        @TheatricalConfigItem
        public boolean artnetEnabled = false;
        @TheatricalConfigItem
        public String artNetIP = "";
        @TheatricalConfigItem(minValue = "0", maxValue = "16")
        public int universe1 = 1;
        @TheatricalConfigItem(minValue = "-1", maxValue = "16")
        public int universe2 = -1;
        @TheatricalConfigItem(minValue = "-1", maxValue = "16")
        public int universe3 = -1;
        @TheatricalConfigItem(minValue = "-1", maxValue = "16")
        public int universe4 = -1;

    }

    public static class ServerConfig extends BaseConfig{
        @TheatricalConfigItem
        public boolean shouldEmitLight = true;

        @TheatricalConfigItem(minValue = "0")
        public int wirelessDMXRadius = 40;
        @TheatricalConfigItem(minValue = "25")
        public int defaultMaxLightDist = 25;
    }
}
