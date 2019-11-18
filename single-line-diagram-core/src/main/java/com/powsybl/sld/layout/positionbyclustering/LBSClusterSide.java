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
 * LBSClusterSide is a ClusterConnector defined by one Side (LEFT/RIGHT) of a LBSCluster.
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
class LBSClusterSide implements ClusterConnector {
    private LBSCluster lbsCluster;
    private Side side;
    private List<Link<LBSClusterSide>> myLinks;

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

    public Side getMySideInCluster() {
        return side;
    }

    public boolean hasSameRoot(Object other) {
        if (other.getClass() != LBSClusterSide.class) {
            return false;
        }
        return this.lbsCluster == ((LBSClusterSide) other).getCluster();
    }

    @Override
    public <T extends ClusterConnector> T getOtherSameRoot(List<T> clusterConnectors) {
        return clusterConnectors.stream().filter(clusterConnector ->
                clusterConnector.getCluster() == lbsCluster
                        && side.getFlip() == clusterConnector.getMySideInCluster()).findAny().orElse(null);
    }

    @Override
    public int getDistanceToEdge(InternCell internCell) {
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
    public <T extends ClusterConnector> void addLink(Link<T> link) {
        myLinks.add((Link<LBSClusterSide>) link);
    }

    @SuppressWarnings("unchecked")
    public <T extends ClusterConnector> void removeLink(Link<T> link) {
        myLinks.remove(link);
    }

    @SuppressWarnings("unchecked")
    public List<Link<LBSClusterSide>> getLinks() {
        return myLinks;
    }

}
