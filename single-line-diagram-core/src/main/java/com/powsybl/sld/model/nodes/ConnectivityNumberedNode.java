package com.powsybl.sld.model.nodes;

public class ConnectivityNumberedNode extends ConnectivityNode {
    private final int nodeNumber;

    public ConnectivityNumberedNode(String id, String componentType, int nodeNumber) {
        super(id, componentType);
        this.nodeNumber = nodeNumber;
    }

    public int getNodeNumber() {
        return nodeNumber;
    }
}
