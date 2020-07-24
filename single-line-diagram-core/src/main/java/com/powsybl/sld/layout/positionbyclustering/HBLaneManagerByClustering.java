/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.positionbyclustering;

import com.powsybl.sld.layout.HorizontalBusLane;
import com.powsybl.sld.layout.HorizontalBusLaneManager;
import com.powsybl.sld.layout.LBSCluster;
import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.InternCell;
import com.powsybl.sld.model.Side;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HBLaneManagerByClustering implements HorizontalBusLaneManager {

    @Override
    public void mergeHorizontalBusLanes(LBSCluster leftCluster, LBSCluster rightCluster) {
        List<HorizontalBusLane> availableLanesToMerge = new ArrayList<>(leftCluster.getHorizontalBusLanes());
        mergeLaneWithCommonBusNode(leftCluster, rightCluster, availableLanesToMerge);
        mergeLaneWithFlatCell(leftCluster, rightCluster, availableLanesToMerge);
        mergeLaneWithNoLink(leftCluster, rightCluster);
    }

    private void mergeLaneWithCommonBusNode(LBSCluster leftCluster, LBSCluster rightCluster, List<HorizontalBusLane> availableLanesToMerge) {
        List<BusNode> commonNodes = new ArrayList<>(leftCluster.laneSideBuses(Side.RIGHT));
        commonNodes.retainAll(rightCluster.laneSideBuses(Side.LEFT));
        commonNodes.forEach(busNode ->
                finalizeLaneBuilding(leftCluster, rightCluster, busNode, busNode, availableLanesToMerge));
    }

    private void mergeLaneWithFlatCell(LBSCluster leftCluster, LBSCluster rightCluster,
                                       List<HorizontalBusLane> availableLanesToMerge) {
        List<BusNode> myAvailableRightBuses = leftCluster.laneSideBuses(Side.RIGHT, availableLanesToMerge);
        List<InternCell> myConcernedFlatCells = leftCluster.getSideFlatCell(Side.RIGHT)
                .stream().filter(internCell -> {
                    List<BusNode> nodes = internCell.getBusNodes();
                    nodes.retainAll(myAvailableRightBuses);
                    return !myAvailableRightBuses.isEmpty();
                }).collect(Collectors.toList());
        List<InternCell> otherConcernedFlatCells = rightCluster.getSideFlatCell(Side.LEFT);
        myConcernedFlatCells.retainAll(otherConcernedFlatCells);
        myConcernedFlatCells.forEach(internCell -> {
            List<BusNode> busNodes = internCell.getBusNodes();
            BusNode myNode = leftCluster.laneSideBuses(Side.RIGHT).contains(busNodes.get(0)) ? busNodes.get(0) : busNodes.get(1);
            BusNode otherNode = rightCluster.laneSideBuses(Side.LEFT).contains(busNodes.get(0)) ? busNodes.get(0) : busNodes.get(1);
            finalizeLaneBuilding(leftCluster, rightCluster, myNode, otherNode, availableLanesToMerge);
        });
    }

    private void finalizeLaneBuilding(LBSCluster leftCluster, LBSCluster rightCluster,
                                      BusNode myNode, BusNode otherBus, List<HorizontalBusLane> availableLanesToMerge) {
        HorizontalBusLane myLane = leftCluster.getHorizontalLaneFromSideBus(myNode, Side.RIGHT);
        HorizontalBusLane otherLane = rightCluster.getHorizontalLaneFromSideBus(otherBus, Side.LEFT);
        if (otherLane != null && myLane != null) {
            myLane.merge(otherLane);
            rightCluster.removeHorizontalBusLane(otherLane);
            availableLanesToMerge.remove(myLane);
        }
    }
}
