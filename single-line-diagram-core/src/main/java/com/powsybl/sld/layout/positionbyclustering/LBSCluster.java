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
import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
class LBSCluster {
    List<LegBusSet> lbsList;
    Map<Side, LegBusSet> sideToLbs;

    List<HorizontalLane> horizontalLanes;

    List<LBSCluster> lbsClusters;

    LBSCluster(List<LBSCluster> lbsClusters, LegBusSet lbs) {
        lbsList = new ArrayList<>();
        lbsList.add(lbs);
        horizontalLanes = new ArrayList<>();
        lbs.getBusNodeSet().forEach(nodeBus -> horizontalLanes.add(new HorizontalLane(nodeBus)));
        lbs.setLbsCluster(this);

        sideToLbs = new EnumMap<>(Side.class);
        sideToLbs.put(Side.LEFT, lbs);
        sideToLbs.put(Side.RIGHT, lbs);

        this.lbsClusters = lbsClusters;
        this.lbsClusters.add(this);
    }

    void merge(Side myConcernedSide, LBSCluster otherLbsCluster, Side otherSide) {
        if (myConcernedSide == Side.LEFT) {
            reverse();
        }
        if (otherSide == Side.RIGHT) {
            otherLbsCluster.reverse();
        }
        otherLbsCluster.getLbsList().forEach(legBusSet -> legBusSet.setLbsCluster(this));
        buildHorizontalLanes(otherLbsCluster, lbsList.size());
        lbsList.addAll(otherLbsCluster.lbsList);
        sideToLbs.put(Side.RIGHT, otherLbsCluster.sideToLbs.get(Side.RIGHT));
        lbsClusters.remove(otherLbsCluster);
    }

    List<BusNode> laneSideBuses(Side side) {
        return laneSideBuses(side, horizontalLanes);
    }

    List<BusNode> laneSideBuses(Side side, List<HorizontalLane> horizontalLaneList) {
        return horizontalLaneList.stream()
                .map(hl -> hl.getSideNode(side)).collect(Collectors.toList());
    }

    private void buildHorizontalLanes(LBSCluster otherCluster, int lastIndexBeforeMerge) {
        List<HorizontalLane> availableLanesToMerge = new ArrayList<>(horizontalLanes);
        buildLaneWithCommonBusNode(otherCluster, availableLanesToMerge, lastIndexBeforeMerge);
        buildLaneWithFlatCell(otherCluster, availableLanesToMerge, lastIndexBeforeMerge);
        buildLaneWithNoStrongLink(otherCluster, lastIndexBeforeMerge);
    }

    void removeLane(HorizontalLane lane) {
        horizontalLanes.remove(lane);
    }

    private void buildLaneWithCommonBusNode(LBSCluster otherCluster, List<HorizontalLane> availableLanesToMerge, int lastIndexBeforeMerge) {
        List<BusNode> commonNodes = new ArrayList<>(laneSideBuses(Side.RIGHT));
        commonNodes.retainAll(otherCluster.laneSideBuses(Side.LEFT));
        commonNodes.forEach(busNode ->
                finalizeLaneBuilding(otherCluster, busNode, busNode, availableLanesToMerge, lastIndexBeforeMerge));
    }

    private void buildLaneWithFlatCell(LBSCluster otherCluster,
                                       List<HorizontalLane> availableLanesToMerge,
                                       int lastIndexBeforeMerge) {
        List<BusNode> myAvailableRightBuses = laneSideBuses(Side.RIGHT, availableLanesToMerge);
        List<InternCell> myConcernedFlatCells = getSideFlatCell(Side.RIGHT)
                .stream().filter(internCell -> {
                    List<BusNode> nodes = internCell.getBusNodes();
                    nodes.retainAll(myAvailableRightBuses);
                    return !myAvailableRightBuses.isEmpty();
                }).collect(Collectors.toList());
        List<InternCell> otherConcernedFlatCells = otherCluster.getSideFlatCell(Side.LEFT);
        myConcernedFlatCells.retainAll(otherConcernedFlatCells);
        myConcernedFlatCells.forEach(internCell -> {
            List<BusNode> busNodes = internCell.getBusNodes();
            BusNode myNode = laneSideBuses(Side.RIGHT).contains(busNodes.get(0)) ? busNodes.get(0) : busNodes.get(1);
            BusNode otherNode = otherCluster.laneSideBuses(Side.LEFT).contains(busNodes.get(0)) ? busNodes.get(0) : busNodes.get(1);
            finalizeLaneBuilding(otherCluster, myNode, otherNode, availableLanesToMerge, lastIndexBeforeMerge);
        });
    }

    private void buildLaneWithNoStrongLink(LBSCluster otherCluster, int lastIndexBeforeMerge) {
        otherCluster.getHorizontalLanes().forEach(lane -> lane.shift(lastIndexBeforeMerge));
        horizontalLanes.addAll(otherCluster.getHorizontalLanes());
        otherCluster.getHorizontalLanes().removeAll(otherCluster.getHorizontalLanes());
    }

    private void finalizeLaneBuilding(LBSCluster otherCluster,
                                      BusNode myNode,
                                      BusNode otherBus,
                                      List<HorizontalLane> availableLanesToMerge,
                                      int lastIndexBeforeMerge) {
        HorizontalLane myLane = getHorizontalLaneFromSideBus(myNode, Side.RIGHT);
        HorizontalLane otherLane = otherCluster.getHorizontalLaneFromSideBus(otherBus, Side.LEFT);
        if (otherLane != null && myLane != null) {
            myLane.merge(otherLane, lastIndexBeforeMerge);
            otherCluster.removeLane(otherLane);
            availableLanesToMerge.remove(myLane);
        }
    }

    public void mergeHorizontalLanes() {
        List<HorizontalLane> sortedLanes = horizontalLanes.stream()
                .sorted(Comparator.comparingInt(HorizontalLane::getIndex))
                .collect(Collectors.toList());
        int clusterLength = sortedLanes.stream()
                .mapToInt(l -> l.getIndex() + l.getLength())
                .max().orElse(0);
        int i = 0;
        while (i < sortedLanes.size()) {
            HorizontalLane lane = sortedLanes.get(i);
            int actualMaxIndex = lane.getIndex() + lane.getLength();
            while (actualMaxIndex < clusterLength) {
                int finalActualMax = actualMaxIndex;
                HorizontalLane laneToAdd = sortedLanes.stream()
                        .filter(l -> l.getIndex() >= finalActualMax)
                        .findFirst().orElse(null);
                if (laneToAdd != null) {
                    lane.merge(laneToAdd, 0);
                    sortedLanes.remove(laneToAdd);
                    horizontalLanes.remove(laneToAdd);
                    actualMaxIndex = lane.getIndex() + lane.getLength();
                } else {
                    i++;
                    break;
                }
            }
            i++;
        }
    }

    void establishBusNodePosition() {
        int v = 1;
        for (HorizontalLane lane : horizontalLanes) {
            lane.establishBusPosition(v);
            v++;
        }
    }

    private HorizontalLane getHorizontalLaneFromSideBus(BusNode busNode, Side side) {
        return horizontalLanes
                .stream()
                .filter(horizontalLane -> horizontalLane.getSideNode(side) == busNode)
                .findAny()
                .orElse(null);
    }

    LegBusSet getLbsSideFromBusNode(BusNode busNode, Side side) {
        if (side != Side.RIGHT && side != Side.LEFT) {
            return null;
        }
        for (int i = 0; i < lbsList.size(); i++) {
            int j = side == Side.LEFT ? i : lbsList.size() - i - 1;
            if (lbsList.get(j).getBusNodeSet().contains(busNode)) {
                return lbsList.get(j);
            }
        }
        return null;
    }

    List<InternCell> getSideFlatCell(Side side) {
        return laneSideBuses(side).stream()
                .map(busNode -> getLbsSideFromBusNode(busNode, side))
                .distinct()
                .flatMap(lbs -> lbs.getCandidateFlatCells().keySet().stream())
                .collect(Collectors.toList());
    }

    void reverse() {
        Collections.reverse(lbsList);
        LegBusSet lbs = sideToLbs.get(Side.LEFT);
        sideToLbs.put(Side.LEFT, sideToLbs.get(Side.RIGHT));
        sideToLbs.put(Side.RIGHT, lbs);
        horizontalLanes.forEach(lane -> lane.reverse(lbsList.size()));
    }

    Side getLbsSide(LegBusSet lbs) {
        if (sideToLbs.get(Side.RIGHT) == lbs) {
            return Side.RIGHT;
        }
        if (sideToLbs.get(Side.LEFT) == lbs) {
            return Side.LEFT;
        }
        return Side.UNDEFINED;
    }

    Set<InternCell> getCandidateFlatCells() {
        return lbsList.stream().flatMap(legBusSet -> legBusSet.getCandidateFlatCells().keySet().stream()).collect(Collectors.toSet());
    }

    List<InternCell> getCrossoverCells() {
        return lbsList.stream().flatMap(legBusSet -> legBusSet.getCrossoverInternCell().keySet()
                .stream()).collect(Collectors.toList());
    }

    List<HorizontalLane> getHorizontalLanes() {
        return horizontalLanes;
    }

    List<LegBusSet> getLbsList() {
        return lbsList;
    }
}
