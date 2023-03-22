/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.positionbyclustering;

import com.powsybl.sld.layout.HorizontalBusSet;
import com.powsybl.sld.layout.BSCluster;
import com.powsybl.sld.layout.VerticalBusSet;
import com.powsybl.sld.model.cells.ExternCell;
import com.powsybl.sld.model.cells.InternCell;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.nodes.BusNode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * VBSClusterSide is a ClusterConnector defined by one Side (LEFT/RIGHT) of a VBSCluster.
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
class VBSClusterSide {

    private final BSCluster bsCluster;
    private final Side side;
    private final List<Link> myLinks = new ArrayList<>();
    private VBSClusterSide otherSameRoot;

    VBSClusterSide(BSCluster bsCluster, Side side) {
        this.bsCluster = Objects.requireNonNull(bsCluster);
        this.side = Objects.requireNonNull(side);
    }

    void setOtherSameRoot(VBSClusterSide otherSameRoot) {
        this.otherSameRoot = otherSameRoot;
    }

    Set<BusNode> getBusNodeSet() {
        return new LinkedHashSet<>(bsCluster.hbsSideBuses(side));
    }

    List<InternCell> getCandidateFlatCellList() {
        return bsCluster.getSideCandidateFlatCell(side);
    }

    List<ExternCell> getExternCells() {
        return bsCluster.getVerticalBusSetList().stream().flatMap(vbs -> vbs.getExternCells().stream()).collect(Collectors.toList());
    }

    int getExternCellAttractionToEdge(ExternCell cell) {
        List<VerticalBusSet> vbsList = bsCluster.getVerticalBusSetList();
        return vbsList.stream().filter(vbs -> vbs.getExternCells().contains(cell)).findFirst()
                .map(vbs -> side == Side.LEFT ? (vbsList.size() - vbsList.indexOf(vbs))
                        : (vbsList.indexOf(vbs) + 1)).orElse(0);
    }

    List<InternCell> getInternCellsFromShape(InternCell.Shape shape) {
        return bsCluster.getInternCellsFromShape(shape);
    }

    BSCluster getCluster() {
        return bsCluster;
    }

    Side getMySideInCluster() {
        return side;
    }

    boolean hasSameRoot(Object other) {
        if (other.getClass() != VBSClusterSide.class) {
            return false;
        }
        return this.bsCluster == ((VBSClusterSide) other).getCluster();
    }

    VBSClusterSide getOtherSameRoot() {
        return otherSameRoot;
    }

    int getCandidateFlatCellDistanceToEdge(InternCell internCell) {
        List<BusNode> buses = internCell.getBusNodes();
        buses.retainAll(getBusNodeSet());
        if (buses.isEmpty()) {
            return 100;
        }
        BusNode busNode = buses.get(0); //shall have only one as used for a flatCell
        Optional<HorizontalBusSet> horizontalBusSet = bsCluster.getHorizontalBusSets()
                .stream()
                .filter(hbs -> side == Side.LEFT && hbs.getBusNodes().get(0) == busNode
                        || side == Side.RIGHT && hbs.getBusNodes().get(hbs.getBusNodes().size() - 1) == busNode)
                .findFirst();
        if (!horizontalBusSet.isPresent()) {
            return 100;
        } else {
            if (side == Side.LEFT) {
                return horizontalBusSet.get().getStartingIndex();
            } else {
                return bsCluster.getVerticalBusSetList().size() - horizontalBusSet.get().getEndingIndex();
            }
        }
    }

    void addLink(Link link) {
        myLinks.add(link);
    }

    void removeLink(Link link) {
        myLinks.remove(link);
    }

    List<Link> getLinks() {
        return myLinks;
    }

    @Override
    public String toString() {
        return side.toString() + " " + bsCluster.hbsSideBuses(side).toString();
    }
}
