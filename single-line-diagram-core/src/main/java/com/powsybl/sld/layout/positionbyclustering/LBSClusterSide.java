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

import java.util.*;

/**
 * LBSClusterSide is a ClusterConnector defined by one Side (LEFT/RIGHT) of a LBSCluster.
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
class LBSClusterSide implements ClusterConnector<LBSClusterSide> {

    private final LBSCluster lbsCluster;
    private final Side side;
    private final List<Link<LBSClusterSide>> myLinks = new ArrayList<>();

    LBSClusterSide(LBSCluster lbsCluster, Side side) {
        this.lbsCluster = Objects.requireNonNull(lbsCluster);
        this.side = Objects.requireNonNull(side);
    }

    public Set<BusNode> getBusNodeSet() {
        return new LinkedHashSet<>(lbsCluster.laneSideBuses(side));
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
    public LBSClusterSide getOtherSameRoot(List<LBSClusterSide> clusterConnectors) {
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

    public void addLink(Link<LBSClusterSide> link) {
        myLinks.add(link);
    }

    public void removeLink(Link<LBSClusterSide> link) {
        myLinks.remove(link);
    }

    public List<Link<LBSClusterSide>> getLinks() {
        return myLinks;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LBSClusterSide)) {
            return false;
        }
        LBSClusterSide other = (LBSClusterSide) o;
        return myLinks == other.myLinks
                && lbsCluster == other.lbsCluster
                && side == other.side;
    }

    @Override
    public int hashCode() {
        return (side == Side.LEFT ? 0 : 43) + 47 * lbsCluster.getNb();
    }

    @Override
    public String toString() {
        return side.toString() + " " + lbsCluster.laneSideBuses(side).toString();
    }
}
