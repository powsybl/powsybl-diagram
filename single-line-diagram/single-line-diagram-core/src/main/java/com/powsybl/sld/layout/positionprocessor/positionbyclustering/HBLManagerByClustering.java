/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.positionprocessor.positionbyclustering;

import com.powsybl.sld.layout.positionprocessor.BSCluster;
import com.powsybl.sld.layout.positionprocessor.HorizontalBusList;
import com.powsybl.sld.layout.positionprocessor.HorizontalBusListManager;
import com.powsybl.sld.model.cells.InternCell;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.nodes.BusNode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */

public class HBLManagerByClustering implements HorizontalBusListManager {

    @Override
    public void mergeHbl(BSCluster leftCluster, BSCluster rightCluster) {
        List<HorizontalBusList> availableHblToMerge = new ArrayList<>(leftCluster.getHorizontalBusLists());
        mergeHblWithCommonBusNode(leftCluster, rightCluster, availableHblToMerge);
        mergeHblWithFlatCell(leftCluster, rightCluster, availableHblToMerge);
        mergeHblWithNoLink(leftCluster, rightCluster);
    }

    private void mergeHblWithCommonBusNode(BSCluster leftCluster, BSCluster rightCluster, List<HorizontalBusList> availableHblToMerge) {
        List<BusNode> commonNodes = new ArrayList<>(leftCluster.hblSideBuses(Side.RIGHT));
        commonNodes.retainAll(rightCluster.hblSideBuses(Side.LEFT));
        commonNodes.forEach(busNode ->
                finalizeHblBuilding(leftCluster, rightCluster, busNode, busNode, availableHblToMerge));
    }

    private void mergeHblWithFlatCell(BSCluster leftCluster, BSCluster rightCluster,
                                        List<HorizontalBusList> availableHblToMerge) {
        List<BusNode> myAvailableRightBuses = BSCluster.hblSideBuses(Side.RIGHT, availableHblToMerge);
        List<InternCell> myConcernedFlatCells = leftCluster.getSideCandidateFlatCell(Side.RIGHT)
                .stream().filter(internCell -> {
                    List<BusNode> nodes = internCell.getBusNodes();
                    nodes.retainAll(myAvailableRightBuses);
                    return !myAvailableRightBuses.isEmpty();
                }).sorted(Comparator.comparing(InternCell::getFullId)) //avoid randomness
                .collect(Collectors.toList());
        List<InternCell> otherConcernedFlatCells = rightCluster.getSideCandidateFlatCell(Side.LEFT);
        myConcernedFlatCells.retainAll(otherConcernedFlatCells);

        myConcernedFlatCells.stream()
                .sorted(Comparator
                        .<InternCell>comparingInt(cell -> Link.flatCellDistanceToEdges(cell,
                                new VBSClusterSide(leftCluster, Side.RIGHT),
                                new VBSClusterSide(rightCluster, Side.LEFT)))
                        .thenComparing(InternCell::getFullId))
                .forEachOrdered(internCell -> {
                    Optional<BusNode> myNode = internCellNodeInHblSide(leftCluster, Side.RIGHT, internCell);
                    Optional<BusNode> otherNode = internCellNodeInHblSide(rightCluster, Side.LEFT, internCell);
                    if (myNode.isPresent() && otherNode.isPresent()) {
                        internCell.setFlat();
                        finalizeHblBuilding(leftCluster, rightCluster, myNode.get(), otherNode.get(), availableHblToMerge);
                    }
                });
    }

    private Optional<BusNode> internCellNodeInHblSide(BSCluster bsCluster, Side side, InternCell cell) {
        List<BusNode> hblBuses = bsCluster.hblSideBuses(side);
        hblBuses.retainAll(cell.getBusNodes());
        return hblBuses.stream().findFirst();
    }

    private void finalizeHblBuilding(BSCluster leftCluster, BSCluster rightCluster,
                                      BusNode myNode, BusNode otherBus, List<HorizontalBusList> availableHblToMerge) {
        Optional<HorizontalBusList> myHbl = leftCluster.getHblFromSideBus(myNode, Side.RIGHT);
        Optional<HorizontalBusList> otherHbl = rightCluster.getHblFromSideBus(otherBus, Side.LEFT);
        if (otherHbl.isPresent() && myHbl.isPresent()) {
            myHbl.get().merge(otherHbl.get());
            rightCluster.removeHbl(otherHbl.get());
            availableHblToMerge.remove(myHbl.get());
        }
    }
}
