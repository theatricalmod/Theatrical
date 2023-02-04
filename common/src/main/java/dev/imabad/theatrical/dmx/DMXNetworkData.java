package dev.imabad.theatrical.dmx;

import dev.imabad.theatrical.api.dmx.DMXConsumer;
import net.minecraft.core.BlockPos;

import java.util.*;

public class DMXNetworkData {
    private static final DMXNetworkData INSTANCE = new DMXNetworkData();
    public static DMXNetworkData getInstance(){
        return INSTANCE;
    }
    private final Map<BlockPos, DMXConsumer> dmxNodes = new HashMap<>();
    public void addConsumer(BlockPos pos, DMXConsumer consumer){
        dmxNodes.put(pos, consumer);
    }
    public void removeConsumer(BlockPos pos){
        dmxNodes.remove(pos);
    }
    public Collection<DMXConsumer> getConsumers(){
        return dmxNodes.values();
    }

    public Collection<DMXConsumer> getConsumersInRange(BlockPos fromPos, int radius){
        Collection<DMXConsumer> consumers = new HashSet<>();
        for(Map.Entry<BlockPos, DMXConsumer> entry : dmxNodes.entrySet()){
            if(fromPos.distToCenterSqr(entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ()) <= radius){
                consumers.add(entry.getValue());
            }
        }
        return consumers;
    }

}
