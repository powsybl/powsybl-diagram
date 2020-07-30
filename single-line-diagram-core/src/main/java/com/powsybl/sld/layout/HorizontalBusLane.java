/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.Position;
import com.powsybl.sld.model.Side;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An horizontalBusLane contains a list of BusNodes that have to be displayed horizontally at the same height (same vPos).
 * The startingIndex of the HorizontalBusLane is its horizontal position in the LBSCluster it belongs to.
 * The horizontal position is the startingIndex in the cluster of the first LegBusSet that contains the first BusNode
 * of the HorizontalBusLane.
 * The length is the spanning of the HorizontalBusLane in the LBSCluster (note that a busNode can span over many
 * LegBusSet in the cluster). Therefore startingIndex + length - 1 = the last position occupied by the HorizontalBusLane in
 * the LBSCluster.
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public class HorizontalBusLane {

    private final List<BusNode> busNodes = new ArrayList<>();
    private int startingIndex;
    private int endingIndex;
    private LBSCluster lbsCluster;

    HorizontalBusLane(BusNode busNode, LBSCluster lbsCluster) {
        busNodes.add(busNode);
        this.lbsCluster = lbsCluster;
        startingIndex = 0;
        endingIndex = 1;
    }

    void reverse(int parentSize) {
        Collections.reverse(busNodes);
        int previousStartingIndex = startingIndex;
        startingIndex = parentSize - endingIndex;
        endingIndex = parentSize - previousStartingIndex;
    }

    public void shift(int i) {
        startingIndex += i;
        endingIndex += i;
    }

    public void merge(HorizontalBusLane otherLane) {
        List<BusNode> otherBuses = new ArrayList<>(otherLane.getBusNodes());
        if (busNodes.get(busNodes.size() - 1).equals(otherBuses.get(0))) {
            otherBuses.remove(0);
        }
        busNodes.addAll(otherBuses);
        if (lbsCluster == otherLane.getLbsCluster()) {
            endingIndex = otherLane.getEndingIndex();
        } else {
            endingIndex = lbsCluster.getLength() + otherLane.getEndingIndex();
        }
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

    public BusNode getSideNode(Side side) {
        if (side == Side.UNDEFINED || busNodes.isEmpty()) {
            return null;
        }
        if (side == Side.LEFT) {
            return busNodes.get(0);
        }
        return busNodes.get(busNodes.size() - 1);
    }

    public int getStartingIndex() {
        return startingIndex;
    }

    public int getEndingIndex() {
        return endingIndex;
    }

    LBSCluster getLbsCluster() {
        return lbsCluster;
    }

    @Override
    public String toString() {
        return String.join(";", busNodes.stream().map(BusNode::getId).collect(Collectors.toList()));
    }
}
