package dev.imabad.theatrical.networks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.GameInstance;
import dev.imabad.theatrical.net.artnet.NotifyNetworks;
import dev.imabad.theatrical.networks.members.TheatricalNetworkMemberRole;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class TheatricalNetworkData extends SavedData {
    private final Set<ServerPlayer> knownSenders = new HashSet<>();
    private final Map<UUID, TheatricalNetwork> networks = new HashMap<>();
    private static final String KEY = "dmx_networks";
    public static final Codec<TheatricalNetworkData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING.xmap(UUID::fromString, UUID::toString), TheatricalNetwork.CODEC)
                    .fieldOf("networks")
                    .forGetter(TheatricalNetworkData::getNetworks)
    ).apply(instance, TheatricalNetworkData::new));

    public static final SavedDataType<TheatricalNetworkData> TYPE = new SavedDataType<>(
            KEY,
            TheatricalNetworkData::new,
            TheatricalNetworkData.CODEC,
            null
    );

    private static TheatricalNetworkData INSTANCE;

    public static void unloadLevel(){
        INSTANCE = null;
    }

    public static TheatricalNetworkData getInstance(Level level){
        if(INSTANCE == null){
            INSTANCE = level.getServer()
                    .overworld().getDataStorage().computeIfAbsent(TYPE);
        }
        return INSTANCE;
    }

    public static TheatricalNetworkData getInstance(){
        if(INSTANCE == null){
            INSTANCE = GameInstance.getServer().overworld().getDataStorage()
                    .computeIfAbsent(TYPE);
        }
        return INSTANCE;
    }

    private TheatricalNetworkData(){
    }

    private TheatricalNetworkData(Map<UUID, TheatricalNetwork> networks){
        this.networks.putAll(networks);
    }

    @Nullable
    public TheatricalNetwork getNetwork(UUID networkId){
        return networks.get(networkId);
    }

    public TheatricalNetwork createNetwork(Player player){
        TheatricalNetwork network = new TheatricalNetwork(player.getName().getString() + "'s Network");
        network.members().addMember(player.getUUID(), TheatricalNetworkMemberRole.ADMIN);
        networks.put(network.id(), network);
        notifyNetworks(player);
        setDirty();
        return network;
    }

    public TheatricalNetwork createNetwork(String name, TheatricalNetworkMode mode){
        TheatricalNetwork network = new TheatricalNetwork(name);
        network.setMode(mode);
        networks.put(network.id(), network);
        setDirty();
        return network;
    }

    public void deleteNetwork(TheatricalNetwork theatricalNetwork){
        networks.remove(theatricalNetwork.id());
        setDirty();
    }

    public void notifyNetworks(Player player){
        Map<UUID, String> collect = getNetworksForPlayer(player.getUUID()).stream().collect(Collectors.toMap(TheatricalNetwork::id, TheatricalNetwork::name));
        NetworkManager.sendToPlayer((ServerPlayer) player, new NotifyNetworks(collect));
    }

    public List<TheatricalNetwork> getNetworksForPlayer(UUID player){
        return networks.values()
                .stream().filter(dmxNetwork -> dmxNetwork.mode() == TheatricalNetworkMode.PUBLIC || dmxNetwork.members().isMember(player))
                .collect(Collectors.toList());
    }

    public Collection<TheatricalNetwork> getAllNetworks(){
        return networks.values();
    }

    private Map<UUID, TheatricalNetwork> getNetworks(){
        return networks;
    }

    public TheatricalNetwork getDefaultNetworkForPlayer(Player player){
        UUID uuid = player.getUUID();
        Optional<TheatricalNetwork> first = networks.values()
                .stream().filter(dmxNetwork -> {
                    return dmxNetwork.members().isMember(uuid);
                })
                .findFirst();
        return first.orElseGet(() -> createNetwork(player));
    }

}
