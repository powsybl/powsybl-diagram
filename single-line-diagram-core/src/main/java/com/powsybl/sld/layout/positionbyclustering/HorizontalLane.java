package com.powsybl.sld.layout.positionbyclustering;

import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.Side;

import java.util.*;

class HorizontalLane {

    List<BusNode> busNodes;
    int index;
    int length;

    HorizontalLane(BusNode busNode) {
        this.busNodes = new ArrayList<>();
        this.busNodes.add(busNode);
        index = 0;
        length = 1;
    }

    void reverse(int parentSize) {
        Collections.reverse(busNodes);
        index = parentSize - index - length;
    }

    void merge(LBSCluster otherLbsCluster, HorizontalLane otherLane, int actualMaxLBSIndex) {
        List<BusNode> otherBuses = new ArrayList<>(otherLane.getBusNodes());
        if (busNodes.get(busNodes.size() - 1).equals(otherBuses.get(0))) {
            otherBuses.remove(0);
        }
        busNodes.addAll(otherBuses);
        length = actualMaxLBSIndex - index + otherLane.getLength();
        otherLbsCluster.removeLane(otherLane);
    }

    public List<BusNode> getBusNodes() {
        return busNodes;
    }

    BusNode getSideNode(Side side) {
        if (side == Side.UNDEFINED || busNodes.isEmpty()) {
            return null;
        }
        if (side == Side.LEFT) {
            return busNodes.get(0);
        }
        return busNodes.get(busNodes.size() - 1);
    }

    public int getLength() {
        return length;
    }

    public void shift(int i) {
        index += i;
    }

    public int getIndex() {
        return index;
    }
}
