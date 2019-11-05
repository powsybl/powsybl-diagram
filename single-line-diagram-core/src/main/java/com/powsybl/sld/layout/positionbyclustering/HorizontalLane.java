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

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
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
