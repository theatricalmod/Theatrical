package dev.imabad.theatrical.graphs;

public class CableNode {

    int nodeId;
    CableNodePos position;

    public CableNode(CableNodePos pos, int nodeId){
        this.position  = pos;
        this.nodeId = nodeId;
    }

    public int getNodeId() {
        return nodeId;
    }

    public CableNodePos getPosition() {
        return position;
    }
}
