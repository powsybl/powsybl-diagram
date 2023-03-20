/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.positionbyclustering;

import com.powsybl.sld.layout.HorizontalBusSet;
import com.powsybl.sld.layout.HorizontalBusSetManager;
import com.powsybl.sld.layout.BSCluster;
import com.powsybl.sld.model.cells.InternCell;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.nodes.BusNode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */

public class HBSManagerByClustering implements HorizontalBusSetManager {

    @Override
    public void mergeHbs(BSCluster leftCluster, BSCluster rightCluster) {
        List<HorizontalBusSet> availableHbsToMerge = new ArrayList<>(leftCluster.getHorizontalBusSets());
        mergeHbsWithCommonBusNode(leftCluster, rightCluster, availableHbsToMerge);
        mergeHbsWithFlatCell(leftCluster, rightCluster, availableHbsToMerge);
        mergeHbsWithNoLink(leftCluster, rightCluster);
    }

    private void mergeHbsWithCommonBusNode(BSCluster leftCluster, BSCluster rightCluster, List<HorizontalBusSet> availableHbsToMerge) {
        List<BusNode> commonNodes = new ArrayList<>(leftCluster.hbsSideBuses(Side.RIGHT));
        commonNodes.retainAll(rightCluster.hbsSideBuses(Side.LEFT));
        commonNodes.forEach(busNode ->
                finalizeHbsBuilding(leftCluster, rightCluster, busNode, busNode, availableHbsToMerge));
    }

    private void mergeHbsWithFlatCell(BSCluster leftCluster, BSCluster rightCluster,
                                        List<HorizontalBusSet> availableHbsToMerge) {
        List<BusNode> myAvailableRightBuses = BSCluster.hbsSideBuses(Side.RIGHT, availableHbsToMerge);
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
                    Optional<BusNode> myNode = internCellNodeInHbsSide(leftCluster, Side.RIGHT, internCell);
                    Optional<BusNode> otherNode = internCellNodeInHbsSide(rightCluster, Side.LEFT, internCell);
                    if (myNode.isPresent() && otherNode.isPresent()) {
                        internCell.setFlat();
                        finalizeHbsBuilding(leftCluster, rightCluster, myNode.get(), otherNode.get(), availableHbsToMerge);
                    }
                });
    }

    private Optional<BusNode> internCellNodeInHbsSide(BSCluster bsCluster, Side side, InternCell cell) {
        List<BusNode> hbsBuses = bsCluster.hbsSideBuses(side);
        hbsBuses.retainAll(cell.getBusNodes());
        return hbsBuses.stream().findFirst();
    }

    private void finalizeHbsBuilding(BSCluster leftCluster, BSCluster rightCluster,
                                      BusNode myNode, BusNode otherBus, List<HorizontalBusSet> availableHbsToMerge) {
        Optional<HorizontalBusSet> myHbs = leftCluster.getHbsFromSideBus(myNode, Side.RIGHT);
        Optional<HorizontalBusSet> otherHbs = rightCluster.getHbsFromSideBus(otherBus, Side.LEFT);
        if (otherHbs.isPresent() && myHbs.isPresent()) {
            myHbs.get().merge(otherHbs.get());
            rightCluster.removeHbs(otherHbs.get());
            availableHbsToMerge.remove(myHbs.get());
        }
    }
}
