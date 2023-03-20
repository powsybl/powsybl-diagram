/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.nodes.BusNode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An horizontalBusSet contains a list of BusNodes that have to be displayed horizontally at the same height (same vPos).
 * The startingIndex of the HorizontalBusSet is its horizontal position in the LBSCluster it belongs to.
 * The horizontal position is the startingIndex in the cluster of the first LegBusSet that contains the first BusNode
 * of the HorizontalBusSet.
 * The length is the spanning of the HorizontalBusSet in the LBSCluster (note that a busNode can span over many
 * LegBusSet in the cluster). Therefore startingIndex + length - 1 = the last position occupied by the HorizontalBusSet in
 * the LBSCluster.
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public class HorizontalBusSet {

    private final List<BusNode> busNodes = new ArrayList<>();
    private int startingIndex;

    private BSCluster bsCluster;

    HorizontalBusSet(BusNode busNode, BSCluster bsCluster) {
        busNodes.add(busNode);
        this.bsCluster = bsCluster;
        startingIndex = 0;
    }

    void reverse(int parentSize) {
        Collections.reverse(busNodes);
        startingIndex = parentSize - getEndingIndex();
    }

    public void shift(int i) {
        startingIndex += i;
    }

    public void merge(HorizontalBusSet otherHbs) {
        BusNode myRightBus = getSideNode(Side.RIGHT);
        for (int i = getEndingIndex(); i < otherHbs.getStartingIndex()
                + (bsCluster == otherHbs.bsCluster ? 0 : bsCluster.getLength()); i++) {
            busNodes.add(myRightBus == otherHbs.getSideNode(Side.LEFT) ? myRightBus : null);
        }
        busNodes.addAll(otherHbs.getBusNodes());
    }

    void establishBusPosition(int v) {
        int h = 1;
        BusNode prevBus = null;
        for (BusNode busNode : busNodes) {
            if (busNode != prevBus) {
                busNode.setBusBarIndexSectionIndex(v, h);
                h++;
            }
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
        return startingIndex + busNodes.size();
    }

    public BusNode getBusNode(int index) {
        if (index < startingIndex || index >= getEndingIndex()) {
            return null;
        }
        return busNodes.get(index - startingIndex);
    }

    public void setBsCluster(BSCluster bsCluster) {
        this.bsCluster = bsCluster;
    }

    @Override
    public String toString() {
        return String.join(";", busNodes.stream().map(node -> node == null ? "null" : node.getId()).collect(Collectors.toList()));
    }
}
