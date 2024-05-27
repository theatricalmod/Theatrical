package dev.imabad.theatrical;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.imabad.theatrical.blockentities.BlockEntities;
import dev.imabad.theatrical.blocks.Blocks;
import dev.imabad.theatrical.config.ConfigHandler;
import dev.imabad.theatrical.config.TheatricalConfig;
import dev.imabad.theatrical.dmx.DMXDevice;
import dev.imabad.theatrical.dmx.DMXNetworkData;
import dev.imabad.theatrical.fixtures.Fixtures;
import dev.imabad.theatrical.items.Items;
import dev.imabad.theatrical.net.artnet.ListConsumers;
import dev.imabad.theatrical.net.TheatricalNet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Theatrical {
    public static final String MOD_ID = "theatrical";
    // Registering a new creative tab

    public static final DeferredRegister<CreativeModeTab> TABS = TheatricalRegistry.get(Registries.CREATIVE_MODE_TAB);
    public static final RegistrySupplier<CreativeModeTab> TAB = TABS.register(
            Theatrical.MOD_ID,
            () -> CreativeTabRegistry.create(
                Component.translatable("itemGroup." + Theatrical.MOD_ID),
                () -> new ItemStack(Items.ART_NET_INTERFACE.get())
            )
        );

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        ConfigHandler configHandler = ConfigHandler.initialize(Platform.getConfigFolder());
        TheatricalConfig.INSTANCE.register(configHandler);
        TABS.register();
        Fixtures.init();
        TheatricalNet.init();
        Blocks.BLOCKS.register();
        BlockEntities.BLOCK_ENTITIES.register();
        dev.imabad.theatrical.items.Items.ITEMS.register();
        PlayerEvent.PLAYER_JOIN.register((event) -> {
            if(event.connection.player.hasPermissions(event.getServer().getOperatorUserPermissionLevel())){
                DMXNetworkData.getInstance().addKnownSender(event.connection.player);
            }
            for (Integer universe : DMXNetworkData.getInstance().getUniverses()) {
                List<DMXDevice> devices = new ArrayList<>();
                DMXNetworkData.getInstance().getConsumers(universe).forEach(consumer -> {
                    devices.add(new DMXDevice(consumer.getDeviceId(), consumer.getChannelStart(),
                            consumer.getChannelCount(), consumer.getDeviceTypeId(), consumer.getActivePersonality(), consumer.getModelName(),
                            consumer.getFixtureId()));
                });
                new ListConsumers(universe, devices).sendTo(event.connection.player);
            }
        });
        PlayerEvent.PLAYER_QUIT.register((event) -> {
            DMXNetworkData.getInstance().removeKnownSender(event.connection.player);
        });
    }
}
