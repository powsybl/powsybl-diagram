/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.position.clustering;

import com.powsybl.sld.layout.position.BSCluster;
import com.powsybl.sld.layout.position.HorizontalBusList;
import com.powsybl.sld.layout.position.VerticalBusSet;
import com.powsybl.sld.model.cells.ExternCell;
import com.powsybl.sld.model.cells.InternCell;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.nodes.BusNode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * BSClusterSide is the composition of a BSCluster and a Side (LEFT/RIGHT).
 *
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 */
class BSClusterSide {

    private final BSCluster bsCluster;
    private final Side side;
    private BSClusterSide otherSameRoot;

    BSClusterSide(BSCluster bsCluster, Side side) {
        this.bsCluster = Objects.requireNonNull(bsCluster);
        this.side = Objects.requireNonNull(side);
    }

    void setOtherSameRoot(BSClusterSide otherSameRoot) {
        this.otherSameRoot = otherSameRoot;
    }

    Set<BusNode> getBusNodeSet() {
        return new LinkedHashSet<>(bsCluster.hblSideBuses(side));
    }

    List<InternCell> getCandidateFlatCellList() {
        return bsCluster.getSideCandidateFlatCell(side);
    }

    List<ExternCell> getExternCells() {
        return bsCluster.getVerticalBusSets().stream().flatMap(vbs -> vbs.getExternCells().stream()).collect(Collectors.toList());
    }

    int getExternCellAttractionToEdge(ExternCell cell) {
        List<VerticalBusSet> vbsList = bsCluster.getVerticalBusSets();
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

    BSClusterSide getOtherSameRoot() {
        return otherSameRoot;
    }

    int getCandidateFlatCellDistanceToEdge(InternCell internCell) {
        List<BusNode> buses = internCell.getBusNodes();
        buses.retainAll(getBusNodeSet());
        if (buses.isEmpty()) {
            return 100;
        }
        BusNode busNode = buses.get(0); //shall have only one as used for a flatCell
        Optional<HorizontalBusList> horizontalBusList = bsCluster.getHorizontalBusLists()
                .stream()
                .filter(hbl -> side == Side.LEFT && hbl.getBusNodes().get(0) == busNode
                        || side == Side.RIGHT && hbl.getBusNodes().get(hbl.getBusNodes().size() - 1) == busNode)
                .findFirst();
        if (!horizontalBusList.isPresent()) {
            return 100;
        } else {
            if (side == Side.LEFT) {
                return horizontalBusList.get().getStartingIndex();
            } else {
                return bsCluster.getVerticalBusSets().size() - horizontalBusList.get().getEndingIndex();
            }
        }
    }

    @Override
    public String toString() {
        return side.toString() + " " + bsCluster.hblSideBuses(side).toString();
    }
}
