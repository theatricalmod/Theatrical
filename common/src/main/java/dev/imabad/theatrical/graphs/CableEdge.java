package dev.imabad.theatrical.graphs;

import dev.imabad.theatrical.api.CableType;

public class CableEdge {

    public CableNode node1;
    public CableNode node2;
    public CableType cableType;

    public CableEdge(CableNode node1, CableNode node2, CableType cableType){
        this.node1 = node1;
        this.node2 = node2;
        this.cableType = cableType;
    }
}
