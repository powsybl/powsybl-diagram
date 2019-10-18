/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.positionbyclustering;

import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.Side;

import java.util.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
class HorizontalLane {

    List<BusNode> busNodes;
    Map<Side, Integer> sideToLbsIndex = new EnumMap<>(Side.class);

    HorizontalLane(BusNode busNode, int leftIndex, int rightIndex) {
        this.busNodes = new ArrayList<>();
        this.busNodes.add(busNode);
        sideToLbsIndex.put(Side.LEFT, leftIndex);
        sideToLbsIndex.put(Side.RIGHT, rightIndex);
    }

    void reverse() {
        Collections.reverse(busNodes);
    }

    void merge(LBSCluster otherLbsCluster, HorizontalLane otherLane, int actualMaxLBSIndex) {
        busNodes.addAll(otherLane.getBusNodes());
        sideToLbsIndex.put(Side.RIGHT, actualMaxLBSIndex + otherLane.getSideLbsIndex(Side.RIGHT));
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

    int getSideLbsIndex(Side side) {
        return sideToLbsIndex.get(side);
    }

}
