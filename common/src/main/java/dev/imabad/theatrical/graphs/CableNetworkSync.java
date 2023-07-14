package dev.imabad.theatrical.graphs;

import dev.imabad.theatrical.api.CableType;
import dev.imabad.theatrical.net.SyncCableNetwork;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntIntImmutablePair;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CableNetworkSync {

    private List<SyncCableNetwork> packetQueue = new ArrayList<>();

    public void serverTick(MinecraftServer server){
        flushPacket();

        if(!packetQueue.isEmpty()){
            for(SyncCableNetwork packet : packetQueue){
                packet.sendToAll(server);
            }
            packetQueue.clear();
        }
    }

    public void nodeAdded(CableNetwork network, CableNode node){
        flushPacket(network);
        currentPacket.addedNodes.put(node.getNodeId(), node.getPosition());
        currentPayload++;
    }

    public void nodeRemoved(CableNetwork network, CableNode node){
        flushPacket(network);
        int nodeId = node.getNodeId();
        if(currentPacket.addedNodes.remove(nodeId) == null) {
            currentPacket.removedNodes.add(nodeId);
        }
    }

    public void networkRemoved(CableNetwork network){
        flushPacket(network);
        currentPacket.packetDeletesNetwork = true;
    }

    public void edgeAdded(CableNetwork network, CableNode node1, CableNode node2, CableEdge edge){
        flushPacket(network);
        currentPacket.addedEdges.add(
                Pair.of(IntIntImmutablePair.of(node1.getNodeId(), node2.getNodeId()), edge.cableType)
        );
        currentPayload++;
    }

    public void sendFullGraphTo(CableNetwork network, ServerPlayer target){
        SyncCableNetwork packet = new SyncCableNetwork(network.getId(), network.getType());
        packet.fullWipe = true;
        int sent = 0;

        for(CableNode node : network.nodesByPosition.values()){
            SyncCableNetwork currentPacket = packet;
            currentPacket.addedNodes.put(node.getNodeId(), node.getPosition());
            if(sent++ < 1000){
                continue;
            }

            sent = 0;
            packet = flushAndCreateNew(network, target, currentPacket);
        }

        for(CableNode node : network.nodesById.values()){
            SyncCableNetwork currentPacket = packet;
            if(!network.edgesByNode.containsKey(node)){
                continue;
            }
            network.edgesByNode.get(node)
                    .forEach((node2, edge) -> {
                        currentPacket.addedEdges.add(Pair.of(IntIntImmutablePair.of(node.getNodeId(), node2.getNodeId()), edge.cableType));
                    });
            if(sent++ < 1000){
                continue;
            }

            sent = 0;
            packet = flushAndCreateNew(network, target, currentPacket);
        }

        if(sent > 0){
            flushAndCreateNew(network, target, packet);
        }
    }

    private SyncCableNetwork flushAndCreateNew(CableNetwork network, ServerPlayer target, SyncCableNetwork packet){
        packet.sendTo(target);
        packet = new SyncCableNetwork(network.getId(), network.getType());
        return packet;
    }

    private SyncCableNetwork currentPacket;
    private int currentPayload;

    private void flushPacket() {
        flushPacket(null, CableType.BUNDLED);
    }

    private void flushPacket(CableNetwork network){
        flushPacket(network.getId(), network.getType());
    }

    private void flushPacket(@Nullable UUID networkId, CableType networkType){
        if(currentPacket != null){
            if(currentPacket.networkId.equals(networkId) && currentPayload < 1000){
                // No need to flush, not a new network or a full payload
                return;
            }
            packetQueue.add(currentPacket);
            currentPacket = null;
            currentPayload = 0;
        }
        if(networkId != null){
            currentPacket = new SyncCableNetwork(networkId, networkType);
            currentPayload = 0;
        }
    }

    public void networkSplit(CableNetwork cableNetwork, Set<CableNetwork> disconnected) {
        flushPacket(cableNetwork);
        for (CableNetwork network : disconnected) {
                currentPacket.splitSubNetworks.put(network.nodesById.keySet().stream().findFirst()
                        .get(), network.getId());
        }
    }
}
