package dev.imabad.theatrical.networks;

import ch.bildspur.artnet.rdm.RDMDeviceId;
import dev.architectury.networking.NetworkManager;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.api.dmx.DMXConsumer;
import dev.imabad.theatrical.dmx.DMXDevice;
import dev.imabad.theatrical.net.artnet.NotifyConsumerChange;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkDMXManager {

    private final IntObjectMap<Map<BlockPos, DMXConsumer>> dmxUniverseToNodeMap = new IntObjectHashMap<>();
    private final Set<ServerPlayer> knownSenders = new HashSet<>();

    public void addConsumer(BlockPos pos, DMXConsumer consumer){
        Map<BlockPos, DMXConsumer> universe = dmxUniverseToNodeMap.computeIfAbsent(consumer.getUniverse(), (uni) -> new ConcurrentHashMap<>());
        universe.put(pos, consumer);
        dmxUniverseToNodeMap.put(consumer.getUniverse(), universe);
        NetworkManager.sendToPlayers(knownSenders,
                new NotifyConsumerChange(consumer.getUniverse(),
                        NotifyConsumerChange.ChangeType.ADD,
                        new DMXDevice(
                                consumer.getDeviceId(), consumer.getChannelStart(), consumer.getChannelCount(),
                                consumer.getDeviceTypeId(), consumer.getActivePersonality(), consumer.getModelName(), consumer.getFixtureId())));
    }

    public void updateConsumer(DMXConsumer consumer){
        NetworkManager.sendToPlayers(knownSenders, new NotifyConsumerChange(consumer.getUniverse(),
                NotifyConsumerChange.ChangeType.UPDATE,
                new DMXDevice(consumer.getDeviceId(), consumer.getChannelStart(), consumer.getChannelCount(),
                        consumer.getDeviceTypeId(),consumer.getActivePersonality(), consumer.getModelName(), consumer.getFixtureId())));
    }

    public void removeConsumer(DMXConsumer consumer, BlockPos pos){
        if(!dmxUniverseToNodeMap.containsKey(consumer.getUniverse())){
            return;
        }
        Map<BlockPos, DMXConsumer> universe = dmxUniverseToNodeMap.get(consumer.getUniverse());
        universe.remove(pos);
        NetworkManager.sendToPlayers(knownSenders, new NotifyConsumerChange(consumer.getUniverse(), NotifyConsumerChange.ChangeType.REMOVE,
                        new DMXDevice(consumer.getDeviceId(), 0, 0,0,
                                0, "", Theatrical.location(""))));
    }
    @Nullable
    public Collection<DMXConsumer> getConsumers(int universe){
        if(dmxUniverseToNodeMap.get(universe) != null) {
            return dmxUniverseToNodeMap.get(universe).values();
        }
        return null;
    }

    public BlockPos getConsumerPos(int universe, RDMDeviceId deviceId){
        for (Map.Entry<BlockPos, DMXConsumer> blockPosDMXConsumerEntry : dmxUniverseToNodeMap.get(universe).entrySet()) {
            if(blockPosDMXConsumerEntry.getValue().getDeviceId().equals(deviceId)){
                return blockPosDMXConsumerEntry.getKey();
            }
        }
        return null;
    }

    public Collection<DMXConsumer> getConsumersInRange(int universe, BlockPos fromPos, int radius){
        Collection<DMXConsumer> consumers = new HashSet<>();
        if(!dmxUniverseToNodeMap.containsKey(universe) || dmxUniverseToNodeMap.get(universe).isEmpty()){
            return consumers;
        }
        for(Map.Entry<BlockPos, DMXConsumer> entry : dmxUniverseToNodeMap.get(universe).entrySet()){
            if(Math.sqrt(fromPos.distToCenterSqr(entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ())) <= radius){
                consumers.add(entry.getValue());
            }
        }
        return consumers;
    }

    public Set<Integer> getUniverses(){
        return dmxUniverseToNodeMap.keySet();
    }
}
