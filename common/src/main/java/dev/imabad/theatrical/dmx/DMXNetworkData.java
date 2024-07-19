package dev.imabad.theatrical.dmx;

import ch.bildspur.artnet.rdm.RDMDeviceId;
import dev.imabad.theatrical.api.dmx.DMXConsumer;
import dev.imabad.theatrical.net.artnet.NotifyConsumerChange;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class DMXNetworkData {
    private static final DMXNetworkData INSTANCE = new DMXNetworkData();
    public static DMXNetworkData getInstance(){
        return INSTANCE;
    }
    private final Set<ServerPlayer> knownSenders = new HashSet<>();
    private final IntObjectMap<Map<BlockPos, DMXConsumer>> universeToNodeMap = new IntObjectHashMap<>();
    public void addConsumer(BlockPos pos, DMXConsumer consumer){
        Map<BlockPos, DMXConsumer> universe = universeToNodeMap.computeIfAbsent(consumer.getUniverse(), (uni) -> new HashMap<>());
        universe.put(pos, consumer);
        universeToNodeMap.put(consumer.getUniverse(), universe);
        new NotifyConsumerChange(consumer.getUniverse(),
                NotifyConsumerChange.ChangeType.ADD,
                new DMXDevice(
                        consumer.getDeviceId(), consumer.getChannelStart(), consumer.getChannelCount(),
                        consumer.getDeviceTypeId(), consumer.getActivePersonality(), consumer.getModelName(), consumer.getFixtureId()))
                .sendTo(knownSenders);
    }

    public void updateConsumer(DMXConsumer consumer){
        new NotifyConsumerChange(consumer.getUniverse(),
                NotifyConsumerChange.ChangeType.UPDATE,
                new DMXDevice(consumer.getDeviceId(), consumer.getChannelStart(), consumer.getChannelCount(),
                        consumer.getDeviceTypeId(),consumer.getActivePersonality(), consumer.getModelName(), consumer.getFixtureId()))
                .sendTo(knownSenders);
    }

    public void removeConsumer(DMXConsumer consumer, BlockPos pos){
        if(!universeToNodeMap.containsKey(consumer.getUniverse())){
            return;
        }
        Map<BlockPos, DMXConsumer> universe = universeToNodeMap.get(consumer.getUniverse());
        universe.remove(pos);
        new NotifyConsumerChange(consumer.getUniverse(), NotifyConsumerChange.ChangeType.REMOVE,
                new DMXDevice(consumer.getDeviceId(), 0, 0,0,
                        0, "", new ResourceLocation("")))
                .sendTo(knownSenders);
    }
    public Collection<DMXConsumer> getConsumers(int universe){
        if(universeToNodeMap.get(universe) != null) {
            return universeToNodeMap.get(universe).values();
        }
        return null;
    }

    public BlockPos getConsumerPos(int universe, RDMDeviceId deviceId){
        for (Map.Entry<BlockPos, DMXConsumer> blockPosDMXConsumerEntry : universeToNodeMap.get(universe).entrySet()) {
            if(blockPosDMXConsumerEntry.getValue().getDeviceId().equals(deviceId)){
                return blockPosDMXConsumerEntry.getKey();
            }
        }
        return null;
    }

    public Collection<DMXConsumer> getConsumersInRange(int universe, BlockPos fromPos, int radius){
        Collection<DMXConsumer> consumers = new HashSet<>();
        if(!universeToNodeMap.containsKey(universe) || universeToNodeMap.get(universe).isEmpty()){
            return consumers;
        }
        for(Map.Entry<BlockPos, DMXConsumer> entry : universeToNodeMap.get(universe).entrySet()){
            if(Math.sqrt(fromPos.distToCenterSqr(entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ())) <= radius){
                consumers.add(entry.getValue());
            }
        }
        return consumers;
    }

    public Set<Integer> getUniverses(){
        return universeToNodeMap.keySet();
    }

    public void addKnownSender(ServerPlayer player){
        this.knownSenders.add(player);
    }

    public void removeKnownSender(ServerPlayer player){
        this.knownSenders.remove(player);
    }

    public boolean isKnownSender(ServerPlayer player){
        return this.knownSenders.contains(player);
    }
}
