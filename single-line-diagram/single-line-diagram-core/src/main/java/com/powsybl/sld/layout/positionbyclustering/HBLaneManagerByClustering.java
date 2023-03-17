/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.positionbyclustering;

import com.powsybl.sld.layout.HorizontalBusLane;
import com.powsybl.sld.layout.HorizontalBusLaneManager;
import com.powsybl.sld.layout.LBSCluster;
import com.powsybl.sld.model.cells.InternCell;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.nodes.BusNode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */

public class HBLaneManagerByClustering implements HorizontalBusLaneManager {

    @Override
    public void mergeHorizontalBusLanes(LBSCluster leftCluster, LBSCluster rightCluster) {
        List<HorizontalBusLane> availableLanesToMerge = new ArrayList<>(leftCluster.getHorizontalBusLanes());
        mergeLanesWithCommonBusNode(leftCluster, rightCluster, availableLanesToMerge);
        mergeLanesWithFlatCell(leftCluster, rightCluster, availableLanesToMerge);
        mergeLanesWithNoLink(leftCluster, rightCluster);
    }

    private void mergeLanesWithCommonBusNode(LBSCluster leftCluster, LBSCluster rightCluster, List<HorizontalBusLane> availableLanesToMerge) {
        List<BusNode> commonNodes = new ArrayList<>(leftCluster.laneSideBuses(Side.RIGHT));
        commonNodes.retainAll(rightCluster.laneSideBuses(Side.LEFT));
        commonNodes.forEach(busNode ->
                finalizeLaneBuilding(leftCluster, rightCluster, busNode, busNode, availableLanesToMerge));
    }

    private void mergeLanesWithFlatCell(LBSCluster leftCluster, LBSCluster rightCluster,
                                        List<HorizontalBusLane> availableLanesToMerge) {
        List<BusNode> myAvailableRightBuses = LBSCluster.laneSideBuses(Side.RIGHT, availableLanesToMerge);
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
                                new LBSClusterSide(leftCluster, Side.RIGHT),
                                new LBSClusterSide(rightCluster, Side.LEFT)))
                        .thenComparing(InternCell::getFullId))
                .forEachOrdered(internCell -> {
                    Optional<BusNode> myNode = internCellNodeInLaneSide(leftCluster, Side.RIGHT, internCell);
                    Optional<BusNode> otherNode = internCellNodeInLaneSide(rightCluster, Side.LEFT, internCell);
                    if (myNode.isPresent() && otherNode.isPresent()) {
                        internCell.setFlat();
                        finalizeLaneBuilding(leftCluster, rightCluster, myNode.get(), otherNode.get(), availableLanesToMerge);
                    }
                });
    }

    private Optional<BusNode> internCellNodeInLaneSide(LBSCluster lbsCluster, Side side, InternCell cell) {
        List<BusNode> laneBuses = lbsCluster.laneSideBuses(side);
        laneBuses.retainAll(cell.getBusNodes());
        return laneBuses.stream().findFirst();
    }

    private void finalizeLaneBuilding(LBSCluster leftCluster, LBSCluster rightCluster,
                                      BusNode myNode, BusNode otherBus, List<HorizontalBusLane> availableLanesToMerge) {
        Optional<HorizontalBusLane> myLane = leftCluster.getHorizontalLaneFromSideBus(myNode, Side.RIGHT);
        Optional<HorizontalBusLane> otherLane = rightCluster.getHorizontalLaneFromSideBus(otherBus, Side.LEFT);
        if (otherLane.isPresent() && myLane.isPresent()) {
            myLane.get().merge(otherLane.get());
            rightCluster.removeHorizontalBusLane(otherLane.get());
            availableLanesToMerge.remove(myLane.get());
        }
    }
}
