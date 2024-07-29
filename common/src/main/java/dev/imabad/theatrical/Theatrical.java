package dev.imabad.theatrical;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.imabad.theatrical.blockentities.BlockEntities;
import dev.imabad.theatrical.blocks.Blocks;
import dev.imabad.theatrical.commands.CommandArguments;
import dev.imabad.theatrical.commands.DMXNetworkModeArgument;
import dev.imabad.theatrical.commands.MemberRoleArgument;
import dev.imabad.theatrical.commands.NetworkCommand;
import dev.imabad.theatrical.config.ConfigHandler;
import dev.imabad.theatrical.config.TheatricalConfig;
import dev.imabad.theatrical.dmx.*;
import dev.imabad.theatrical.fixtures.Fixtures;
import dev.imabad.theatrical.items.Items;
import dev.imabad.theatrical.mixin.ArgumentTypeInfosAccessor;
import dev.imabad.theatrical.net.artnet.ListConsumers;
import dev.imabad.theatrical.net.TheatricalNet;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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

        DeferredRegister<ArgumentTypeInfo<?, ?>> argTypes = TheatricalRegistry.get(Registries.COMMAND_ARGUMENT_TYPE);
        registerArgument(argTypes, SingletonArgumentInfo.contextFree(DMXNetworkModeArgument::networkMode), "network_mode", DMXNetworkModeArgument.class);
        registerArgument(argTypes, SingletonArgumentInfo.contextFree(MemberRoleArgument::memberRole), "member_role", MemberRoleArgument.class);

        dev.imabad.theatrical.items.Items.ITEMS.register();
        PlayerEvent.PLAYER_JOIN.register((event) -> {
            DMXNetworkData instance = DMXNetworkData.getInstance(event.server.overworld());
            for (DMXNetwork network : instance.getNetworksForPlayer(event.connection.player.getUUID())) {
                for (Integer universe : network.getUniverses()) {
                    List<DMXDevice> devices = new ArrayList<>();
                    network.getConsumers(universe).forEach(consumer -> {
                        devices.add(new DMXDevice(consumer.getDeviceId(), consumer.getChannelStart(),
                                consumer.getChannelCount(), consumer.getDeviceTypeId(), consumer.getActivePersonality(), consumer.getModelName(),
                                consumer.getFixtureId()));
                    });
                    new ListConsumers(universe, devices).sendTo(event.connection.player);
                }
            }
        });
        LifecycleEvent.SERVER_LEVEL_UNLOAD.register(world -> {
            if(world.dimension().equals(Level.OVERWORLD)){
                DMXNetworkData.unloadLevel();
            }
        });
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
            NetworkCommand.register(dispatcher);
        });
    }

    private static void registerArgument(DeferredRegister<ArgumentTypeInfo<?, ?>> argTypes,
                                         ArgumentTypeInfo<?, ?> serializer, String id, Class<?> clazz) {
        argTypes.register(new ResourceLocation(Theatrical.MOD_ID, id), () -> serializer);
        ArgumentTypeInfosAccessor.classMap().put(clazz, serializer);
    }
}
