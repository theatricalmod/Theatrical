package dev.imabad.theatrical.graphs;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.api.CableType;
import dev.imabad.theatrical.blocks.CableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class GlobalCableManager {

    // Frontier is like our front line of nodes that we've searched.
    static class FrontierEntry {

        CableNodePos.DiscoveredPosition prevNode;

        CableNodePos.DiscoveredPosition currentNode;

        CableNodePos.DiscoveredPosition parentNode;

        public FrontierEntry(CableNodePos.DiscoveredPosition parent, CableNodePos.DiscoveredPosition previous, CableNodePos.DiscoveredPosition current){
            parentNode = parent;
            prevNode = previous;
            currentNode = current;
        }
    }

    private Map<UUID, CableNetwork> cableNetworks;

    private SavedCableData savedCableData;

    public CableNetworkSync sync;

    public GlobalCableManager(){
        cleanUp();
    }

    public void levelLoaded(LevelAccessor level){
        MinecraftServer server = level.getServer();
        if(server == null || server.overworld() != level)
            return;
        cleanUp();
        savedCableData = null;
        loadSavedData(server);
    }

    public void playerLogin(Player player){
        if(player instanceof ServerPlayer serverPlayer){
            loadSavedData(serverPlayer.getServer());
            cableNetworks.values().forEach(network -> sync.sendFullGraphTo(network, serverPlayer));
        }
    }

    private void loadSavedData(MinecraftServer server){
        if(savedCableData != null)
            return;
        savedCableData = SavedCableData.load(server);
        cableNetworks = savedCableData.getCableNetworks();
    }

    public Map<UUID, CableNetwork> getCableNetworks() {
        return cableNetworks;
    }

    private void cleanUp(){
        cableNetworks = new HashMap<>();
        sync = new CableNetworkSync();
    }

    public void markDirty(){
        if(savedCableData != null)
            savedCableData.setDirty();
    }

    public void putNetwork(CableNetwork cableNetwork) {
        cableNetworks.put(cableNetwork.getId(), cableNetwork);
        markDirty();
    }

    public void removeNetwork(CableNetwork cableNetwork){
        cableNetworks.remove(cableNetwork.getId());
        markDirty();
    }
//0 -60 -3
    public List<CableNetwork> getIntersectingNetworks(CableNodePos cablePos){
        if(cableNetworks == null) {
            return Collections.emptyList();
        }
        List<CableNetwork> intersectingNetworks = new ArrayList<>();
        for(CableNetwork network : cableNetworks.values()) {
            if(network.locateNode(cablePos) != null) {
                intersectingNetworks.add(network);
            }
        }
        return intersectingNetworks;
    }

    public CableNetwork getOrCreateNetwork(UUID networkId, CableType cableType) {
        return cableNetworks.computeIfAbsent(networkId, uuid -> {
            CableNetwork cableNetwork = new CableNetwork(networkId, cableType);
            return cableNetwork;
        });
    }
    public static void onCableSideRemoved(LevelAccessor level, BlockPos pos, BlockState state, Direction side){
        if(!(state.getBlock() instanceof CableBlock cable))
            return;

        Collection<CableNodePos.DiscoveredPosition> ends = cable.getPossibleNodesForSide(side, level, pos);
        GlobalCableManager MANAGER = Theatrical.CABLES;
        CableNetworkSync sync = MANAGER.sync;

        for (CableNodePos.DiscoveredPosition removedLocation : ends) {
            List<CableNetwork> intersectingNetworks = MANAGER.getIntersectingNetworks(removedLocation);
            for (CableNetwork network : intersectingNetworks) {
                CableNode removedNode = network.locateNode(removedLocation);
                if(removedNode == null){
                    continue;
                }
                network.removeNode(level, removedLocation);
                sync.nodeRemoved(network, removedNode);
                if(!network.isEmpty()){
                    continue;
                }
                MANAGER.removeNetwork(network);
                sync.networkRemoved(network);
            }
        }

        Set<BlockPos> endsToUpdate = new HashSet<>();
        for(CableNodePos.DiscoveredPosition removedEnd : ends){
            endsToUpdate.addAll(removedEnd.allAdjacent());
        }

        Set<CableNetwork> toUpdate = new HashSet<>();
        for(BlockPos bPos : endsToUpdate){
            if(!bPos.equals(pos)){
                CableNetwork onCableAdded = onCableAdded(level, bPos, level.getBlockState(bPos));
                if(onCableAdded != null){
                    toUpdate.add(onCableAdded);
                }
            }
        }

        for (CableNetwork cableNetwork : toUpdate) {
            MANAGER.updateSplitNetwork(level, cableNetwork);
        }

        MANAGER.markDirty();
    }
    public static void onCableRemoved(LevelAccessor level, BlockPos pos, BlockState state){
        if(!(state.getBlock() instanceof CableBlock cable))
            return;

        Collection<CableNodePos.DiscoveredPosition> ends = cable.getConnected(null, level, pos, state);
        GlobalCableManager MANAGER = Theatrical.CABLES;
        CableNetworkSync sync = MANAGER.sync;

        for (CableNodePos.DiscoveredPosition removedLocation : ends) {
            List<CableNetwork> intersectingNetworks = MANAGER.getIntersectingNetworks(removedLocation);
            for (CableNetwork network : intersectingNetworks) {
                CableNode removedNode = network.locateNode(removedLocation);
                if(removedNode == null){
                    continue;
                }
                network.removeNode(level, removedLocation);
                sync.nodeRemoved(network, removedNode);
                if(!network.isEmpty()){
                    continue;
                }
                MANAGER.removeNetwork(network);
                sync.networkRemoved(network);
            }
        }

        Set<BlockPos> endsToUpdate = new HashSet<>();
        for(CableNodePos.DiscoveredPosition removedEnd : ends){
            endsToUpdate.addAll(removedEnd.allAdjacent());
        }

        Set<CableNetwork> toUpdate = new HashSet<>();
        for(BlockPos bPos : endsToUpdate){
            if(!bPos.equals(pos)){
                CableNetwork onCableAdded = onCableAdded(level, bPos, level.getBlockState(bPos));
                if(onCableAdded != null){
                    toUpdate.add(onCableAdded);
                }
            }
        }

        for (CableNetwork cableNetwork : toUpdate) {
            MANAGER.updateSplitNetwork(level, cableNetwork);
        }

        MANAGER.markDirty();
    }

    public void updateSplitNetwork(LevelAccessor level, CableNetwork cableNetwork) {
        Set<CableNetwork> disconnected = cableNetwork.findDisconnectedNetworks(level, null);
        disconnected.forEach(this::putNetwork);
        if(!disconnected.isEmpty()){
            sync.networkSplit(cableNetwork, disconnected);
            markDirty();
        }
    }

    public static CableNetwork onCableAdded(LevelAccessor level, BlockPos pos, BlockState state){
        // We only care about blocks that are cables.
        if(!(state.getBlock() instanceof CableBlock cable))
            return null;

        // First step

        GlobalCableManager MANAGER = Theatrical.CABLES;
        CableNetworkSync sync = MANAGER.sync;
        // Our front line of searched nodes
        List<FrontierEntry> frontier = new ArrayList<>();
        // A list of places we've already been to
        Set<CableNodePos.DiscoveredPosition> visited = new HashSet<>();
        // A list of networks we've found / are connected to.
        Set<CableNetwork> connectedNetworks = new HashSet<>();

        addInitialEndsOf(level, pos, state, cable, frontier);

        while(!frontier.isEmpty()){
            FrontierEntry entry = frontier.remove(0);
            List<CableNetwork> intersectingNetworks = MANAGER.getIntersectingNetworks(entry.currentNode);
            for(CableNetwork network : intersectingNetworks){
                CableNode cableNode = network.locateNode(entry.currentNode);
                network.removeNode(level, entry.currentNode);
                sync.nodeRemoved(network, cableNode);
                connectedNetworks.add(network);
                continue;
            }

            if(!intersectingNetworks.isEmpty()) {
                continue;
            }

            Collection<CableNodePos.DiscoveredPosition> ends = CableBlock.walkCable(level, entry.currentNode);
            if(entry.prevNode != null) {
                ends.remove(entry.prevNode);
            }
            continueToSearch(frontier, visited, entry, ends);
        }

        frontier.clear();
        visited.clear();
        CableNetwork network = null;

        // Remove empty networks

        for(Iterator<CableNetwork> iterator = connectedNetworks.iterator(); iterator.hasNext();){
            CableNetwork cableNetwork = iterator.next();
            if(!cableNetwork.isEmpty() || connectedNetworks.size() == 1){
                continue;
            }
            MANAGER.removeNetwork(cableNetwork);
            sync.networkRemoved(cableNetwork);
            iterator.remove();
        }

        // Handle multiple graphs

        if(connectedNetworks.size() > 1){
            for(CableNetwork other : connectedNetworks){
                if(network == null){
                    network = other;
                } else {
                    other.transferAll(network);
                    MANAGER.removeNetwork(other);
                    sync.networkRemoved(other);
                }
            }
        } else if(connectedNetworks.size() == 1){
            network = connectedNetworks.stream().findFirst().get();
        } else{
            MANAGER.putNetwork(network = new CableNetwork(UUID.randomUUID(), state.getValue(CableBlock.CABLE_TYPE)));
        }

        CableNodePos.DiscoveredPosition startNode = null;

        addInitialEndsOf(level, pos, state, cable, frontier);

        while(!frontier.isEmpty()){

            FrontierEntry entry = frontier.remove(0);
            Collection<CableNodePos.DiscoveredPosition> ends = CableBlock.walkCable(level, entry.currentNode);
            boolean first = entry.prevNode == null;
            if(!first){
                ends.remove(entry.prevNode);
            }
            if(isValidNetworkNodeLocation(entry.currentNode, ends, first)){
                startNode = entry.currentNode;
                break;
            }

            continueToSearch(frontier, visited, entry, ends);
        }

        frontier.clear();
        Set<CableNode> addedNodes = new HashSet<>();
        network.createNodeIfAbsent(startNode);
        frontier.add(new FrontierEntry(startNode, null, startNode));

        while(!frontier.isEmpty()) {

            FrontierEntry entry = frontier.remove(0);
            CableNodePos.DiscoveredPosition parentNode = entry.parentNode;
            Collection<CableNodePos.DiscoveredPosition> ends = CableBlock.walkCable(level, entry.currentNode);
            boolean first = entry.prevNode == null;
            if(!first){
                ends.remove(entry.prevNode);
            }

            if(isValidNetworkNodeLocation(entry.currentNode, ends, first) && entry.currentNode != startNode){
                boolean nodeIsNew = network.createNodeIfAbsent(entry.currentNode);
                network.connectNodes(level, parentNode, entry.currentNode);
                addedNodes.add(network.locateNode(entry.currentNode));
                parentNode = entry.currentNode;
                if(!nodeIsNew){
                    continue;
                }
            }

            continueToSearchWithParent(frontier, entry, parentNode, ends);
        }
        MANAGER.markDirty();
        return network;
    }

    private static void continueToSearchWithParent(List<FrontierEntry> frontier, FrontierEntry entry, CableNodePos.DiscoveredPosition parentNode, Collection<CableNodePos.DiscoveredPosition> ends) {
        for(CableNodePos.DiscoveredPosition position : ends){
            frontier.add(new FrontierEntry(parentNode, entry.currentNode, position));
        }
    }

    private static void addInitialEndsOf(LevelAccessor level, BlockPos pos, BlockState state, CableBlock cable, List<FrontierEntry> frontier){
        for(CableNodePos.DiscoveredPosition initial : cable.getConnected(null, level, pos, state)){
            frontier.add(new FrontierEntry(null, null, initial));
        }
    }

    private static void continueToSearch(List<FrontierEntry> frontier, Set<CableNodePos.DiscoveredPosition> visitedPos, FrontierEntry currentEntry, Collection<CableNodePos.DiscoveredPosition> ends){
        for(CableNodePos.DiscoveredPosition position : ends){
            if(visitedPos.add(position)){
                frontier.add(new FrontierEntry(null, currentEntry.currentNode, position));
            }
        }
    }

    public static boolean isValidNetworkNodeLocation(CableNodePos.DiscoveredPosition position, Collection<CableNodePos.DiscoveredPosition> next, boolean first){
        int size = next.size() - (first ? 1 : 0);
        if(size != 1) {
            return true;
        }
        if(position.isDifferentTypes()){
            return true;
        }
        return false;
//        // TODO: Figure out why this is the way it is?
//        Vec3 vec = position.getLocation();
//        boolean centeredX = !Mth.equal(vec.x, Math.round(vec.x));
//        boolean centeredZ = !Mth.equal(vec.z, Math.round(vec.z));
//        if (centeredX && !centeredZ)
//            return ((int) Math.round(vec.z)) % 16 == 0;
//        return ((int) Math.round(vec.x)) % 16 == 0;
    }
}
