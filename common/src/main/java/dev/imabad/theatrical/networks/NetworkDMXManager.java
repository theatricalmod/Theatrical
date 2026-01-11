package dev.imabad.theatrical.networks;

import ch.bildspur.artnet.rdm.RDMDeviceId;
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

    private final IntObjectMap<Set<DMXConsumer>> dmxUniverseToNodeMap = new IntObjectHashMap<>();
    private final Set<ServerPlayer> knownSenders = new HashSet<>();

    public void addConsumer(DMXConsumer consumer){
        Set<DMXConsumer> universe = dmxUniverseToNodeMap.computeIfAbsent(consumer.getUniverse(), (uni) -> new HashSet<>());
        universe.add(consumer);
        dmxUniverseToNodeMap.put(consumer.getUniverse(), universe);
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

    public void removeConsumer(DMXConsumer consumer){
        if(!dmxUniverseToNodeMap.containsKey(consumer.getUniverse())){
            return;
        }
        Set<DMXConsumer> universe = dmxUniverseToNodeMap.get(consumer.getUniverse());
        universe.remove(consumer);
        new NotifyConsumerChange(consumer.getUniverse(), NotifyConsumerChange.ChangeType.REMOVE,
                new DMXDevice(consumer.getDeviceId(), 0, 0,0,
                        0, "", new ResourceLocation("")))
                .sendTo(knownSenders);
    }

    public void handleDMXData(int universe, byte[] data){
        Collection<DMXConsumer> consumers = getConsumers(universe);
        if(consumers != null) {
            consumers.forEach(consumer -> {
                consumer.consume(data);
            });
        }
    }

    @Nullable
    public Collection<DMXConsumer> getConsumers(int universe){
        if(dmxUniverseToNodeMap.get(universe) != null) {
            return dmxUniverseToNodeMap.get(universe);
        }
        return null;
    }

    public DMXConsumer getConsumer(int universe, RDMDeviceId deviceId){
        for (DMXConsumer dmxConsumer : dmxUniverseToNodeMap.get(universe)) {
            if(dmxConsumer.getDeviceId().equals(deviceId)){
                return dmxConsumer;
            }
        }
        return null;
    }

    public Set<Integer> getUniverses(){
        return dmxUniverseToNodeMap.keySet();
    }
}
