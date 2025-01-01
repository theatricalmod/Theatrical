package dev.imabad.theatrical.networks;

import dev.architectury.utils.GameInstance;
import dev.imabad.theatrical.net.artnet.NotifyNetworks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class AVNetworkData extends SavedData {
    private final Map<UUID, AVNetwork> networks = new HashMap<>();
    private static final String KEY = "av_networks";
    private static final String OLD_KEY = "dmx_networks";

    private static AVNetworkData INSTANCE;
    private static final SavedData.Factory<AVNetworkData> factory = new Factory<>(
            AVNetworkData::migrateOrCreate,
            AVNetworkData::read,
            null
    );

    public static void unloadLevel(){
        INSTANCE = null;
    }

    public static AVNetworkData migrateOrCreate() {
        AVNetworkData networkData = new AVNetworkData();
        AVNetworkData AVNetworkData = GameInstance.getServer()
                        .overworld().getDataStorage().get(factory, OLD_KEY);
        if(AVNetworkData != null){
            return AVNetworkData;
        }
        return networkData;
    }

    public static AVNetworkData getInstance(Level level){
        if(INSTANCE == null){
            INSTANCE = level.getServer()
                    .overworld().getDataStorage().computeIfAbsent(factory, KEY);
        }
        return INSTANCE;
    }

    public static AVNetworkData getInstance(){
        if(INSTANCE == null){
            INSTANCE = GameInstance.getServer().overworld().getDataStorage().computeIfAbsent(factory, KEY);
        }
        return INSTANCE;
    }


    @Nullable
    public AVNetwork getNetwork(UUID networkId){
        return networks.get(networkId);
    }

    public AVNetwork createNetwork(Player player){
        AVNetwork network = new AVNetwork(player.getName().getString() + "'s Network");
        network.addMember(player.getUUID(), AVNetworkMemberRole.ADMIN);
        networks.put(network.id(), network);
        notifyNetworks(player);
        setDirty();
        return network;
    }
    public AVNetwork createNetwork(String name, AVNetworkMode mode){
        AVNetwork network = new AVNetwork(name);
        network.setMode(mode);
        networks.put(network.id(), network);
        setDirty();
        return network;
    }

    public void deleteNetwork(AVNetwork AVNetwork){
        networks.remove(AVNetwork.id());
        setDirty();
    }

    public void notifyNetworks(Player player){
        Map<UUID, String> collect = getNetworksForPlayer(player.getUUID()).stream().collect(Collectors.toMap(AVNetwork::id, AVNetwork::name));
        new NotifyNetworks(collect).sendTo((ServerPlayer) player);
    }

    public List<AVNetwork> getNetworksForPlayer(UUID player){
        return networks.values()
                .stream().filter(dmxNetwork -> dmxNetwork.mode() == AVNetworkMode.PUBLIC || dmxNetwork.isMember(player))
                .collect(Collectors.toList());
    }

    public Collection<AVNetwork> getAllNetworks(){
        return networks.values();
    }

    public AVNetwork getDefaultNetworkForPlayer(Player player){
        UUID uuid = player.getUUID();
        Optional<AVNetwork> first = networks.values()
                .stream().filter(dmxNetwork -> {
                    return dmxNetwork.isMember(uuid);
                })
                .findFirst();
        return first.orElseGet(() -> createNetwork(player));
    }

    public static AVNetworkData read(CompoundTag tag) {
        AVNetworkData data = new AVNetworkData();
        ListTag networksTag = tag.getList("networks", Tag.TAG_COMPOUND);
        for (Tag networkTag : networksTag) {
            AVNetwork network = new AVNetwork((CompoundTag) networkTag);
            data.networks.put(network.id(), network);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        ListTag networksTag = new ListTag();
        for (AVNetwork value : networks.values()) {
            networksTag.add(value.save());
        }
        compoundTag.put("networks", networksTag);
        return compoundTag;
    }

}