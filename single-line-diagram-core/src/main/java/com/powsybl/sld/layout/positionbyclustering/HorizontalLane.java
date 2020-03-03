/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.positionbyclustering;

import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.Position;
import com.powsybl.sld.model.Side;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An horizontalLane contains a list of BusNodes that have to be displayed horizontally connected.
 * The index of the HorizontalLane is its horizontal position in the LBSCluster it belongs. The horizontal position is
 * the index in the cluster of the first LegBusSet that contains the first BusNode of the HorizontalLane.
 * The length is the spanning of the HorizontalLane in the LBSCluster (note that a busNode can span over many
 * LegBusSet in the cluster). Therefore index + length - 1 = the last position occupied by the HorizontalLane in
 * the LBSCluster.
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
class HorizontalLane {

    private final List<BusNode> busNodes = new ArrayList<>();
    private int index = 0;
    private int length = 1;

    HorizontalLane(BusNode busNode) {
        busNodes.add(busNode);
    }

    void reverse(int parentSize) {
        Collections.reverse(busNodes);
        index = parentSize - index - length;
    }

    void merge(HorizontalLane otherLane, int actualMaxLBSIndex) {
        List<BusNode> otherBuses = new ArrayList<>(otherLane.getBusNodes());
        if (busNodes.get(busNodes.size() - 1).equals(otherBuses.get(0))) {
            otherBuses.remove(0);
        }
        busNodes.addAll(otherBuses);
        length = actualMaxLBSIndex - index + otherLane.getIndex() + otherLane.getLength();
    }

    void establishBusPosition(int v) {
        int h = 1;
        for (BusNode busNode : busNodes) {
            busNode.setStructuralPosition(new Position(h, v));
            h++;
        }
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

    int getLength() {
        return length;
    }

    void shift(int i) {
        index += i;
    }

    int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return String.join(";", busNodes.stream().map(BusNode::getId).collect(Collectors.toList()));
    }
}
