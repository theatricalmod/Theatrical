package dev.imabad.theatrical.networks;

import dev.architectury.utils.GameInstance;
import dev.imabad.theatrical.net.artnet.NotifyNetworks;
import dev.imabad.theatrical.networks.members.TheatricalNetworkMemberRole;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class TheatricalNetworkData extends SavedData {
    private final Set<ServerPlayer> knownSenders = new HashSet<>();
    private final Map<UUID, TheatricalNetwork> networks = new HashMap<>();
    private static final String KEY = "dmx_networks";

    private static TheatricalNetworkData INSTANCE;

    public static void unloadLevel(){
        INSTANCE = null;
    }

    public static TheatricalNetworkData getInstance(Level level){
        if(INSTANCE == null){
            INSTANCE = level.getServer()
                    .overworld().getDataStorage().computeIfAbsent(TheatricalNetworkData::read, TheatricalNetworkData::new, KEY);
        }
        return INSTANCE;
    }

    public static TheatricalNetworkData getInstance(){
        if(INSTANCE == null){
            INSTANCE = GameInstance.getServer().overworld().getDataStorage()
                    .computeIfAbsent(TheatricalNetworkData::read, TheatricalNetworkData::new, KEY);
        }
        return INSTANCE;
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
        new NotifyNetworks(collect).sendTo((ServerPlayer) player);
    }

    public List<TheatricalNetwork> getNetworksForPlayer(UUID player){
        return networks.values()
                .stream().filter(dmxNetwork -> dmxNetwork.mode() == TheatricalNetworkMode.PUBLIC || dmxNetwork.members().isMember(player))
                .collect(Collectors.toList());
    }

    public Collection<TheatricalNetwork> getAllNetworks(){
        return networks.values();
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

    public static TheatricalNetworkData read(CompoundTag tag) {
        TheatricalNetworkData data = new TheatricalNetworkData();
        ListTag networksTag = tag.getList("networks", Tag.TAG_COMPOUND);
        for (Tag networkTag : networksTag) {
            TheatricalNetwork network = new TheatricalNetwork((CompoundTag) networkTag);
            data.networks.put(network.id(), network);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        ListTag networksTag = new ListTag();
        for (TheatricalNetwork value : networks.values()) {
            networksTag.add(value.save());
        }
        compoundTag.put("networks", networksTag);
        return compoundTag;
    }
}
