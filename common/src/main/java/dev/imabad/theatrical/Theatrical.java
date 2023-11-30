package dev.imabad.theatrical;

import dev.architectury.platform.Platform;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.imabad.theatrical.blockentities.BlockEntities;
import dev.imabad.theatrical.blocks.Blocks;
import dev.imabad.theatrical.config.ConfigHandler;
import dev.imabad.theatrical.config.TheatricalConfig;
import dev.imabad.theatrical.fixtures.Fixtures;
import dev.imabad.theatrical.items.Items;
import dev.imabad.theatrical.net.TheatricalNet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Theatrical {
    public static final String MOD_ID = "theatrical";
    // Registering a new creative tab
    public static final RegistrySupplier<CreativeModeTab> TAB =
        TheatricalRegistry.get(Registries.CREATIVE_MODE_TAB).register(
            Theatrical.MOD_ID,
            () -> CreativeTabRegistry.create(
                Component.translatable("itemGroup." + Theatrical.MOD_ID),
                () -> new ItemStack(Items.ART_NET_INTERFACE.get())
            )
        );

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        ConfigHandler configHandler = new ConfigHandler(Platform.getConfigFolder());
        TheatricalConfig.INSTANCE.register(configHandler);
        Fixtures.init();
        TheatricalNet.init();
        Blocks.BLOCKS.register();
        BlockEntities.BLOCK_ENTITIES.register();
        dev.imabad.theatrical.items.Items.ITEMS.register();
    }


}
