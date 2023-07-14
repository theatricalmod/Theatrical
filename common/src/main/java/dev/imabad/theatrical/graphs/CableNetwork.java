package dev.imabad.theatrical.graphs;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.api.CableType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.LevelAccessor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CableNetwork {

    public static final AtomicInteger nodeIdGenerator = new AtomicInteger();
    private UUID id;
    private CableType type;

    Map<CableNodePos, CableNode> nodesByPosition;
    Map<Integer, CableNode> nodesById;
    Map<CableNode, Map<CableNode, CableEdge>> edgesByNode;

    public CableNetwork(UUID networkId){
        this(networkId, CableType.BUNDLED);
    }

    public CableNetwork(UUID networkId, CableType cableType){
        this.id = networkId;
        this.type = cableType;
        nodesByPosition = new HashMap<>();
        nodesById = new HashMap<>();
        edgesByNode = new IdentityHashMap<>();
    }

    public UUID getId() {
        return id;
    }

    public CableType getType() {
        return type;
    }

    public boolean createNodeIfAbsent(CableNodePos.DiscoveredPosition position){
        if(!addNodeIfAbsent(new CableNode(position, getNextNodeId())))
            return false;
        CableNode newNode = nodesByPosition.get(position);
        Theatrical.CABLES.sync.nodeAdded(this, newNode);
        Theatrical.CABLES.markDirty();
        return true;
    }

    public boolean addNodeIfAbsent(CableNode node){
        if(nodesByPosition.putIfAbsent(node.getPosition(), node) != null)
            return false;
        nodesById.put(node.getNodeId(), node);
        return true;
    }

    public void addNode(CableNode node){
        CableNodePos pos = node.getPosition();
        if(nodesByPosition.containsKey(pos))
            removeNode(null, pos);
        nodesByPosition.put(pos, node);
        nodesById.put(node.getNodeId(), node);
    }

    public Map<CableNode, CableEdge> getEdges(CableNode node){
        return edgesByNode.get(node);
    }

    public static CableNetwork load(CompoundTag tag) {
        CableNetwork cableNetwork = new CableNetwork(tag.getUUID("Id"));
        cableNetwork.type = CableType.valueOf(tag.getString("Type"));

        DimensionMap dimensionMap = DimensionMap.read(tag);

        Map<Integer, CableNode> indexTracker = new HashMap<>();
        ListTag nodes = tag.getList("Nodes", Tag.TAG_COMPOUND);

        int i =0;
        for(Tag t : nodes){
            CompoundTag nodeTag = (CompoundTag) t;
            CableNodePos pos = CableNodePos.read(nodeTag.getCompound("Location"), dimensionMap);
            cableNetwork.loadNode(pos, getNextNodeId());
            indexTracker.put(i, cableNetwork.locateNode(pos));
            i++;
        }

        i = 0;
        for(Tag t : nodes){
            CompoundTag nodeTag = (CompoundTag) t;
            CableNode node1 = indexTracker.get(i);
            i++;

            if(!nodeTag.contains("Connections")){
                continue;
            }
            ListTag connections = nodeTag.getList("Connections", Tag.TAG_COMPOUND);
            for(Tag t1 : connections){
                CompoundTag c = (CompoundTag) t1;
                CableNode node2 = indexTracker.get(c.getInt("To"));
                CableEdge edge = new CableEdge(node1, node2, cableNetwork.type);
                cableNetwork.putEdge(node1, node2, edge);
            }
        }
        return cableNetwork;
    }

    public void loadNode(CableNodePos pos, int nextNodeId) {
        addNodeIfAbsent(new CableNode(pos,  nextNodeId));
    }

    public CompoundTag write(){
        CompoundTag output = new CompoundTag();
        output.putUUID("Id", id);
        output.putString("Type", type.name());

        DimensionMap dimensionMap = new DimensionMap();
        Map<CableNode, Integer> indexTracker = new HashMap<>();
        ListTag nodesList = new ListTag();
        int i = 0;
        for(CableNode cableNode : nodesByPosition.values()){
            indexTracker.put(cableNode, i);
            CompoundTag nodeTag = new CompoundTag();
            nodeTag.put("Location", cableNode.getPosition().write(dimensionMap));
            nodesList.add(nodeTag);
            i++;
        }

        edgesByNode.forEach((node1, map) -> {
            Integer index1 = indexTracker.get(node1);
            if(index1 == null){
                return;
            }
            CompoundTag nodeTag = (CompoundTag) nodesList.get(index1);
            ListTag connectionsList = new ListTag();
            map.forEach((node2, edge) -> {
                CompoundTag connectionTag = new CompoundTag();
                Integer index2 = indexTracker.get(node2);
                if(index2 == null){
                    return;
                }
                connectionTag.putInt("To", index2);
                connectionsList.add(connectionTag);
            });
            nodeTag.put("Connections", connectionsList);
        });

        output.put("Nodes", nodesList);
        dimensionMap.write(output);
        return output;
    }

    public static int getNextNodeId(){
        return nodeIdGenerator.incrementAndGet();
    }

    public CableNode locateNode(CableNodePos cablePos) {
        return nodesByPosition.get(cablePos);
    }

    public CableNode getNodeById(int id){
        return nodesById.get(id);
    }

    public boolean removeNode(LevelAccessor level, CableNodePos nodePos) {
        // Attempt to remove the node from the position list.
        CableNode removed = nodesByPosition.remove(nodePos);
        if(removed == null) {
            return false;
        }

        // Remove from ID map
        nodesById.remove(removed.nodeId);

        // If there was nothing connecting to this node, we're done!
        if(!edgesByNode.containsKey(removed)) {
            return true;
        }

        // Remove any edges that existed to this node.
        Map<CableNode, CableEdge> edges = edgesByNode.remove(removed);
        for (CableNode cableNode : edges.keySet()) {
            if(edgesByNode.containsKey(cableNode)){
                edgesByNode.get(cableNode)
                        .remove(removed);
            }
        }
        return true;
    }

    public boolean putEdge(CableNode node1, CableNode node2, CableEdge edge){
        Map<CableNode, CableEdge> edges = edgesByNode.computeIfAbsent(node1, n -> new IdentityHashMap<>());
        if(edges.containsKey(node2)) {
           return false;
        }
        return edges.put(node2, edge) == null;
    }

    public boolean isEmpty() {
        return nodesByPosition.isEmpty();
    }

    public void transferAll(CableNetwork network) {
        nodesByPosition.forEach((pos, node) -> {
            if(network.addNodeIfAbsent(node)){
                Theatrical.CABLES.sync.nodeAdded(network, node);
            }
        });

        edgesByNode.forEach((node1, map) -> map.forEach((node2, edge) -> {
            CableNode n1 = network.locateNode(node1.position);
            CableNode n2 = network.locateNode(node2.position);
            if(n1 == null || n2  == null){
                return;
            }
            if(network.putEdge(n1, n2, edge)){
                Theatrical.CABLES.sync.edgeAdded(network, n1, n2, edge);
            }
        }));

        nodesByPosition.clear();
        edgesByNode.clear();

    }

    public void connectNodes(LevelAccessor level, CableNodePos.DiscoveredPosition parentNode, CableNodePos.DiscoveredPosition currentNode) {
        CableNode node1 = nodesByPosition.get(parentNode);
        CableNode node2 = nodesByPosition.get(currentNode);
        CableType cableType = currentNode.cableTypeA;
        CableEdge edge = new CableEdge(node1, node2, cableType);
        CableEdge edge2 = new CableEdge(node2, node1, cableType);

        putEdge(node1, node2, edge);
        putEdge(node2, node1, edge2);
        Theatrical.CABLES.sync.edgeAdded(this, node1, node2, edge);
        Theatrical.CABLES.sync.edgeAdded(this, node2, node1, edge2);

        Theatrical.CABLES.markDirty();
    }

    public Map<CableNodePos, CableNode> getNodes() {
        return nodesByPosition;
    }

    public Set<CableNetwork> findDisconnectedNetworks(LevelAccessor level, Map<Integer, UUID> splitSubGraphs) {
        Set<CableNetwork> discovered = new HashSet<>();
        Set<CableNodePos> vertices = new HashSet<>(nodesByPosition.keySet());
        List<CableNodePos> frontier = new ArrayList<>();
        CableNetwork target = null;

        while(!vertices.isEmpty()){
            if(target != null){
                discovered.add(target);
            }

            CableNodePos start = vertices.stream().findFirst().get();
            frontier.add(start);
            vertices.remove(start);

            while(!frontier.isEmpty()){
                CableNodePos currentPos = frontier.remove(0);
                CableNode currentNode = locateNode(currentPos);

                Map<CableNode, CableEdge> connections = getEdgesFrom(currentNode);
                for (CableNode cableNode : connections.keySet()) {
                    if(vertices.remove(cableNode.getPosition())){
                        frontier.add(cableNode.getPosition());
                    }
                }

                if(target != null){
                    if(splitSubGraphs != null && splitSubGraphs.containsKey(currentNode.getNodeId())){
                        UUID id = splitSubGraphs.get(currentNode.getNodeId());
                        target.id = id;
                    }
                    transfer(currentNode, target);
                }
            }

            frontier.clear();
            target = new CableNetwork(UUID.randomUUID());
        }
        return discovered;
    }

    private void transfer(CableNode currentNode, CableNetwork target) {
        target.addNode(currentNode);

        CableNodePos position = currentNode.getPosition();
        Map<CableNode, CableEdge> edges = getEdgesFrom(currentNode);

        if(!edges.isEmpty()){
            target.edgesByNode.put(currentNode, edges);
        }

        nodesById.remove(currentNode.getNodeId());
        nodesByPosition.remove(position);
        edgesByNode.remove(currentNode);
    }

    private Map<CableNode, CableEdge> getEdgesFrom(CableNode currentNode) {
        if (currentNode == null)
            return null;
        return edgesByNode.getOrDefault(currentNode, new HashMap<>());
    }
}
