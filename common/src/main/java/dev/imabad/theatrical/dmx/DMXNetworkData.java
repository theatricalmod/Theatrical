package dev.imabad.theatrical.dmx;

import dev.architectury.utils.GameInstance;
import dev.imabad.theatrical.net.artnet.NotifyNetworks;
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

public class DMXNetworkData extends SavedData {
    private final Set<ServerPlayer> knownSenders = new HashSet<>();
    private final Map<UUID, DMXNetwork> networks = new HashMap<>();
    private static final String KEY = "dmx_networks";

    private static DMXNetworkData INSTANCE;
    private static final SavedData.Factory<DMXNetworkData> factory = new Factory<>(
            DMXNetworkData::new,
            DMXNetworkData::read,
            null
    );

    public static void unloadLevel(){
        INSTANCE = null;
    }

    public static DMXNetworkData getInstance(Level level){
        if(INSTANCE == null){
            INSTANCE = level.getServer()
                    .overworld().getDataStorage().computeIfAbsent(factory, KEY);
        }
        return INSTANCE;
    }

    public static DMXNetworkData getInstance(){
        if(INSTANCE == null){
            INSTANCE = GameInstance.getServer().overworld().getDataStorage().computeIfAbsent(factory, KEY);
        }
        return INSTANCE;
    }


    @Nullable
    public DMXNetwork getNetwork(UUID networkId){
        return networks.get(networkId);
    }

    public DMXNetwork createNetwork(Player player){
        DMXNetwork network = new DMXNetwork(player.getName().getString() + "'s Network");
        network.addMember(player.getUUID(), DMXNetworkMemberRole.ADMIN);
        networks.put(network.id(), network);
        notifyNetworks(player);
        setDirty();
        return network;
    }
    public DMXNetwork createNetwork(String name, DMXNetworkMode mode){
        DMXNetwork network = new DMXNetwork(name);
        network.setMode(mode);
        networks.put(network.id(), network);
        setDirty();
        return network;
    }

    public void deleteNetwork(DMXNetwork dmxNetwork){
        networks.remove(dmxNetwork.id());
        setDirty();
    }

    public void notifyNetworks(Player player){
        Map<UUID, String> collect = getNetworksForPlayer(player.getUUID()).stream().collect(Collectors.toMap(DMXNetwork::id, DMXNetwork::name));
        new NotifyNetworks(collect).sendTo((ServerPlayer) player);
    }

    public List<DMXNetwork> getNetworksForPlayer(UUID player){
        return networks.values()
                .stream().filter(dmxNetwork -> dmxNetwork.mode() == DMXNetworkMode.PUBLIC || dmxNetwork.isMember(player))
                .collect(Collectors.toList());
    }

    public Collection<DMXNetwork> getAllNetworks(){
        return networks.values();
    }

    public DMXNetwork getDefaultNetworkForPlayer(Player player){
        UUID uuid = player.getUUID();
        Optional<DMXNetwork> first = networks.values()
                .stream().filter(dmxNetwork -> {
                    return dmxNetwork.isMember(uuid);
                })
                .findFirst();
        return first.orElseGet(() -> createNetwork(player));
    }

    public static DMXNetworkData read(CompoundTag tag) {
        DMXNetworkData data = new DMXNetworkData();
        ListTag networksTag = tag.getList("networks", Tag.TAG_COMPOUND);
        for (Tag networkTag : networksTag) {
            DMXNetwork network = new DMXNetwork((CompoundTag) networkTag);
            data.networks.put(network.id(), network);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        ListTag networksTag = new ListTag();
        for (DMXNetwork value : networks.values()) {
            networksTag.add(value.save());
        }
        compoundTag.put("networks", networksTag);
        return compoundTag;
    }

//    private final IntObjectMap<Map<BlockPos, DMXConsumer>> universeToNodeMap = new IntObjectHashMap<>();
//    public void addConsumer(BlockPos pos, DMXConsumer consumer){
//        Map<BlockPos, DMXConsumer> universe = universeToNodeMap.computeIfAbsent(consumer.getUniverse(), (uni) -> new HashMap<>());
//        universe.put(pos, consumer);
//        universeToNodeMap.put(consumer.getUniverse(), universe);
//        new NotifyConsumerChange(consumer.getUniverse(),
//                NotifyConsumerChange.ChangeType.ADD,
//                new DMXDevice(
//                        consumer.getDeviceId(), consumer.getChannelStart(), consumer.getChannelCount(),
//                        consumer.getDeviceTypeId(), consumer.getActivePersonality(), consumer.getModelName(), consumer.getFixtureId()))
//                .sendTo(knownSenders);
//    }
//
//    public void updateConsumer(DMXConsumer consumer){
//        new NotifyConsumerChange(consumer.getUniverse(),
//                NotifyConsumerChange.ChangeType.UPDATE,
//                new DMXDevice(consumer.getDeviceId(), consumer.getChannelStart(), consumer.getChannelCount(),
//                        consumer.getDeviceTypeId(),consumer.getActivePersonality(), consumer.getModelName(), consumer.getFixtureId()))
//                .sendTo(knownSenders);
//    }
//
//    public void removeConsumer(DMXConsumer consumer, BlockPos pos){
//        if(!universeToNodeMap.containsKey(consumer.getUniverse())){
//            return;
//        }
//        Map<BlockPos, DMXConsumer> universe = universeToNodeMap.get(consumer.getUniverse());
//        universe.remove(pos);
//        new NotifyConsumerChange(consumer.getUniverse(), NotifyConsumerChange.ChangeType.REMOVE,
//                new DMXDevice(consumer.getDeviceId(), 0, 0,0,
//                        0, "", new ResourceLocation("")))
//                .sendTo(knownSenders);
//    }
//    public Collection<DMXConsumer> getConsumers(int universe){
//        if(universeToNodeMap.get(universe) != null) {
//            return universeToNodeMap.get(universe).values();
//        }
//        return null;
//    }
//
//    public BlockPos getConsumerPos(int universe, RDMDeviceId deviceId){
//        for (Map.Entry<BlockPos, DMXConsumer> blockPosDMXConsumerEntry : universeToNodeMap.get(universe).entrySet()) {
//            if(blockPosDMXConsumerEntry.getValue().getDeviceId().equals(deviceId)){
//                return blockPosDMXConsumerEntry.getKey();
//            }
//        }
//        return null;
//    }
//
//    public Collection<DMXConsumer> getConsumersInRange(int universe, BlockPos fromPos, int radius){
//        Collection<DMXConsumer> consumers = new HashSet<>();
//        if(!universeToNodeMap.containsKey(universe) || universeToNodeMap.get(universe).isEmpty()){
//            return consumers;
//        }
//        for(Map.Entry<BlockPos, DMXConsumer> entry : universeToNodeMap.get(universe).entrySet()){
//            if(Math.sqrt(fromPos.distToCenterSqr(entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ())) <= radius){
//                consumers.add(entry.getValue());
//            }
//        }
//        return consumers;
//    }
//
//    public Set<Integer> getUniverses(){
//        return universeToNodeMap.keySet();
//    }
//
//    public void addKnownSender(ServerPlayer player){
//        this.knownSenders.add(player);
//    }
//
//    public void removeKnownSender(ServerPlayer player){
//        this.knownSenders.remove(player);
//    }
//
//    public boolean isKnownSender(ServerPlayer player){
//        return this.knownSenders.contains(player);
//    }
}
