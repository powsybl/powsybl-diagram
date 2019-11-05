/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.positionbyclustering;

import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.InternCell;
import com.powsybl.sld.model.Side;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
class LBSClusterSide extends AbstractLinkable {
    LBSCluster lbsCluster;
    Side side;
    List<Link<LBSClusterSide>> myLinks;

    LBSClusterSide(LBSCluster lbsCluster, Side side) {
        this.lbsCluster = lbsCluster;
        this.side = side;
        myLinks = new ArrayList<>();
    }

    public Set<BusNode> getBusNodeSet() {
        return new HashSet<>(lbsCluster.laneSideBuses(side));
    }

    public List<InternCell> getCandidateFlatCellList() {
        return lbsCluster.getSideFlatCell(side);
    }

    public List<InternCell> getCrossOverCellList() {
        return lbsCluster.getCrossoverCells();
    }

    public LBSCluster getCluster() {
        return lbsCluster;
    }

    public Side getMySidInCluster() {
        return side;
    }

    boolean hasSameRoot(Object other) {
        if (other.getClass() != LBSClusterSide.class) {
            return false;
        }
        return this.lbsCluster == ((LBSClusterSide) other).getCluster();
    }

    @Override
    <T extends AbstractLinkable> T getOtherSameRoot(List<T> linkables) {
        return linkables.stream().filter(linkable ->
                linkable.getCluster() == lbsCluster
                        && side.getFlip() == ((LBSClusterSide) linkable).getSide()).findAny().orElse(null);
    }

    @Override
    int getDistanceToEdge(InternCell internCell) {
        List<BusNode> buses = internCell.getBusNodes();
        buses.retainAll(getBusNodeSet());
        if (buses.isEmpty()) {
            return 0;
        }
        BusNode busNode = buses.get(0);
        HorizontalLane horizontalLane = lbsCluster.getHorizontalLanes()
                .stream()
                .filter(lane -> side == Side.LEFT && lane.getBusNodes().get(0) == busNode
                        || side == Side.RIGHT && lane.getBusNodes().get(lane.getBusNodes().size() - 1) == busNode)
                .findFirst().orElse(null);
        if (horizontalLane == null) {
            return 0;
        } else {
            if (side == Side.LEFT) {
                return horizontalLane.getIndex();
            } else {
                return lbsCluster.getLbsList().size() - horizontalLane.getIndex() - horizontalLane.getLength();
            }
        }
    }

    @SuppressWarnings("unchecked")
    <T extends AbstractLinkable> void addLink(Link<T> link) {
        myLinks.add((Link<LBSClusterSide>) link);
    }

    @SuppressWarnings("unchecked")
    <T extends AbstractLinkable> void removeLink(Link<T> link) {
        myLinks.remove((Link<LBSClusterSide>) link);
    }

    @SuppressWarnings("unchecked")
    public List<Link<LBSClusterSide>> getLinks() {
        return myLinks;
    }

    public Side getSide() {
        return side;
    }
}
