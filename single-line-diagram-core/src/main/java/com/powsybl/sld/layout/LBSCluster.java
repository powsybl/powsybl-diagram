/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * LBSCluster contains a list of LegBusSets (LBS) that is orderly build by successively merging LBSCluster initially
 * containing a single LBS.
 * LBSCluster handles the building of the horizontalBusLanes that are an horizontal strings of busNodes.
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public class LBSCluster {

    private static final Logger LOGGER = LoggerFactory.getLogger(LBSCluster.class);

    private final List<LegBusSet> lbsList = new ArrayList<>();
    private final Map<Side, LegBusSet> sideToLbs = new EnumMap<>(Side.class);
    private final List<HorizontalBusLane> horizontalBusLanes = new ArrayList<>();
    private final int nb;

    public LBSCluster(LegBusSet lbs, int nb) {
        Objects.requireNonNull(lbs);
        lbsList.add(lbs);
        lbs.getBusNodeSet().forEach(nodeBus -> horizontalBusLanes.add(new HorizontalBusLane(nodeBus, this)));

        sideToLbs.put(Side.LEFT, lbs);
        sideToLbs.put(Side.RIGHT, lbs);

        this.nb = nb;
    }

    public static List<LBSCluster> createLBSClusters(List<LegBusSet> legBusSets) {
        List<LBSCluster> lbsClusters = new ArrayList<>();
        int nb = 0;
        for (LegBusSet lbs : legBusSets) {
            lbsClusters.add(new LBSCluster(lbs, nb++));
        }
        return lbsClusters;
    }

    public void merge(Side myConcernedSide, LBSCluster otherLbsCluster, Side otherSide, HorizontalBusLaneManager hblManager) {
        if (myConcernedSide == Side.LEFT) {
            reverse();
        }
        if (otherSide == Side.RIGHT) {
            otherLbsCluster.reverse();
        }
        hblManager.mergeHorizontalBusLanes(this, otherLbsCluster);
        lbsList.addAll(otherLbsCluster.lbsList);
        sideToLbs.put(Side.RIGHT, otherLbsCluster.sideToLbs.get(Side.RIGHT));
    }

    public List<BusNode> laneSideBuses(Side side) {
        return laneSideBuses(side, horizontalBusLanes);
    }

    public List<BusNode> laneSideBuses(Side side, List<HorizontalBusLane> horizontalBusLaneList) {
        return horizontalBusLaneList.stream()
                .map(hl -> hl.getSideNode(side)).collect(Collectors.toList());
    }

    public void removeHorizontalBusLane(HorizontalBusLane lane) {
        horizontalBusLanes.remove(lane);
    }

    public void establishBusNodePosition() {
        int v = 1;
        for (HorizontalBusLane lane : horizontalBusLanes) {
            lane.establishBusPosition(v);
            v++;
        }
    }

    Graph getGraph() {
        return horizontalBusLanes.get(0).getBusNodes().get(0).getGraph();
    }

    public HorizontalBusLane getHorizontalLaneFromSideBus(BusNode busNode, Side side) {
        return horizontalBusLanes
                .stream()
                .filter(horizontalBusLane -> horizontalBusLane.getSideNode(side) == busNode)
                .findAny()
                .orElse(null);
    }

    public int getLength() {
        return lbsList.size();
    }

    private LegBusSet getLbsSideFromBusNode(BusNode busNode, Side side) {
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

    public List<InternCell> getSideFlatCell(Side side) {
        return laneSideBuses(side).stream()
                .map(busNode -> getLbsSideFromBusNode(busNode, side))
                .distinct().filter(Objects::nonNull)
                .flatMap(lbs -> lbs.getCandidateFlatCells().keySet().stream())
                .collect(Collectors.toList());
    }

    private void reverse() {
        Collections.reverse(lbsList);
        LegBusSet lbs = sideToLbs.get(Side.LEFT);
        sideToLbs.put(Side.LEFT, sideToLbs.get(Side.RIGHT));
        sideToLbs.put(Side.RIGHT, lbs);
        horizontalBusLanes.forEach(lane -> lane.reverse(lbsList.size()));
    }

    void identifyFlatCells() {
        lbsList.stream()
                .flatMap(lbs -> lbs.getCandidateFlatCells().keySet().stream())
                .forEach(InternCell::identifyIfFlat);
    }
    //TODO : slip legs of interneCell to be to the closest LBS to the edge.

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

    public List<InternCell> getCrossoverCells() {
        return lbsList.stream().flatMap(legBusSet -> legBusSet.getCrossoverInternCell().keySet()
                .stream()).collect(Collectors.toList());
    }

    public List<HorizontalBusLane> getHorizontalBusLanes() {
        return horizontalBusLanes;
    }

    public List<LegBusSet> getLbsList() {
        return lbsList;
    }

    int getNb() {
        return nb;
    }
}
