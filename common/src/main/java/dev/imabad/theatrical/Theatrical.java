package dev.imabad.theatrical;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.architectury.event.events.common.CommandRegistrationEvent;
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
import dev.imabad.theatrical.dmx.DMXNetwork;
import dev.imabad.theatrical.dmx.DMXNetworkData;
import dev.imabad.theatrical.dmx.DMXNetworkMember;
import dev.imabad.theatrical.fixtures.Fixtures;
import dev.imabad.theatrical.items.Items;
import dev.imabad.theatrical.net.artnet.ListConsumers;
import dev.imabad.theatrical.net.TheatricalNet;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
            dispatcher.register(Commands.literal("theatrical")
                    .then(Commands.literal("networks")
                            .executes(context -> {
                                DMXNetworkData instance = DMXNetworkData.
                                        getInstance(context.getSource().getServer().overworld());
                                String networks;
                                if(context.getSource().hasPermission(context.getSource().getServer().getOperatorUserPermissionLevel())){
                                    networks = instance.getAllNetworks().stream()
                                            .map(DMXNetwork::name).collect(Collectors.joining(","));
                                } else {
                                    networks = instance.getNetworksForPlayer(context.getSource().getPlayer().getUUID()).stream()
                                            .map(DMXNetwork::name).collect(Collectors.joining(","));
                                }
                                context.getSource().sendSystemMessage(Component.literal("Networks: " + networks));
                                return 1;
                            })
                    )
                    .then(Commands.literal("network")
                            .then(Commands.argument("id", StringArgumentType.string())
                                    .then(Commands.literal("members")
                                            .executes(context -> {
                                                DMXNetworkData instance = DMXNetworkData.
                                                        getInstance(context.getSource().getServer().overworld());
                                                try {
                                                    String id = context.getArgument("id", String.class);
                                                    UUID uuid = UUID.fromString(id);
                                                    DMXNetwork network = instance.getNetwork(uuid);
                                                    if(network != null) {
                                                        GameProfileCache profileCache = context.getSource().getServer().getProfileCache();
                                                        String collect = network.members()
                                                                .stream().map(DMXNetworkMember::playerId)
                                                                .map((playerUUID) -> new Tuple<>(playerUUID, profileCache.get(playerUUID)))
                                                                .map(uuidOptionalTuple -> uuidOptionalTuple.getB().isPresent() ? uuidOptionalTuple.getB().get().getName() : uuidOptionalTuple.getA().toString())
                                                                .toList().stream().collect(Collectors.joining(","));
                                                        context.getSource().sendSystemMessage(Component.literal("Members: " + collect));
                                                    }
                                                } catch (Exception e){
                                                    context.getSource().sendSystemMessage(Component.literal("Invalid network"));
                                                    return -1;
                                                }
                                                return 1;
                                            })
                                    )
                            )
                    )
            );
        });
    }
}
