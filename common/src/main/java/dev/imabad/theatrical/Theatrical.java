package dev.imabad.theatrical;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.CreativeTabRegistry;
import dev.imabad.theatrical.blockentities.BlockEntities;
import dev.imabad.theatrical.blocks.Blocks;
import dev.imabad.theatrical.config.ConfigHandler;
import dev.imabad.theatrical.config.TheatricalConfig;
import dev.imabad.theatrical.fixtures.Fixtures;
import dev.imabad.theatrical.graphs.GlobalCableManager;
import dev.imabad.theatrical.items.Items;
import dev.imabad.theatrical.net.TheatricalNet;
import dev.imabad.theatrical.registry.FixtureRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class Theatrical {
    public static final String MOD_ID = "theatrical";
    // Registering a new creative tab
    public static final CreativeModeTab THEATRICAL_TAB = CreativeTabRegistry.create(new ResourceLocation(MOD_ID, "theatrical"), () ->
            new ItemStack(Items.ART_NET_INTERFACE.get()));

    public static final GlobalCableManager CABLES = new GlobalCableManager();

    public static void init() {
        ConfigHandler configHandler = new ConfigHandler(Platform.getConfigFolder());
        TheatricalConfig.INSTANCE.register(configHandler);
        registerFixtures();
        TheatricalNet.init();
        Blocks.BLOCKS.register();
        BlockEntities.BLOCK_ENTITIES.register();
        dev.imabad.theatrical.items.Items.ITEMS.register();
        PlayerEvent.PLAYER_JOIN.register(CABLES::playerLogin);
    }

    private static void registerFixtures(){
        FixtureRegistry.buildRegistry();
        Fixtures.FIXTURES.register();
    }
}
