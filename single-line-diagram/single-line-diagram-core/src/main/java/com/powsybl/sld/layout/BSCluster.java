/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.cells.InternCell;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.nodes.BusNode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * BSCluster contains a list of VerticalBusSets (VBS) that is orderly build by successively merging BSCluster initially
 * containing a single VBS.
 * BSCluster handles the building of the horizontalBusSets that are an horizontal strings of busNodes.
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public class BSCluster {

    private final List<VerticalBusSet> verticalBusSetList = new ArrayList<>();
    private final List<HorizontalBusSet> horizontalBusSets = new ArrayList<>();

    public BSCluster(VerticalBusSet vbs) {
        Objects.requireNonNull(vbs);
        verticalBusSetList.add(vbs);
        vbs.getBusNodeSet().forEach(nodeBus -> horizontalBusSets.add(new HorizontalBusSet(nodeBus, this)));
    }

    public static List<BSCluster> createBSClusters(List<VerticalBusSet> verticalBusSets) {
        List<BSCluster> bsClusters = new ArrayList<>();
        for (VerticalBusSet vbs : verticalBusSets) {
            bsClusters.add(new BSCluster(vbs));
        }
        return bsClusters;
    }

    public void merge(Side myConcernedSide, BSCluster otherBsCluster, Side otherSide, HorizontalBusSetManager hbsManager) {
        if (myConcernedSide == Side.LEFT) {
            reverse();
        }
        if (otherSide == Side.RIGHT) {
            otherBsCluster.reverse();
        }
        hbsManager.mergeHbs(this, otherBsCluster);
        verticalBusSetList.addAll(otherBsCluster.verticalBusSetList);
    }

    public List<BusNode> hbsSideBuses(Side side) {
        return hbsSideBuses(side, horizontalBusSets);
    }

    public static List<BusNode> hbsSideBuses(Side side, List<HorizontalBusSet> hbsList) {
        return hbsList.stream()
                .map(hl -> hl.getSideNode(side)).collect(Collectors.toList());
    }

    public void removeHbs(HorizontalBusSet hbs) {
        horizontalBusSets.remove(hbs);
    }

    public void establishBusNodePosition() {
        int v = 1;
        for (HorizontalBusSet hbs : horizontalBusSets) {
            hbs.establishBusPosition(v);
            v++;
        }
    }

    public Optional<HorizontalBusSet> getHbsFromSideBus(BusNode busNode, Side side) {
        return horizontalBusSets
                .stream()
                .filter(hbs -> hbs.getSideNode(side) == busNode)
                .findAny();
    }

    List<BusNode> getVerticalBusNodes(int i) {
        return horizontalBusSets.stream().map(hbl -> hbl.getBusNode(i)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private void reverse() {
        Collections.reverse(verticalBusSetList);
        horizontalBusSets.forEach(hbs -> hbs.reverse(verticalBusSetList.size()));
    }

    private VerticalBusSet getVbsSideFromBusNode(BusNode busNode, Side side) {
        if (side != Side.RIGHT && side != Side.LEFT) {
            return null;
        }
        for (int i = 0; i < verticalBusSetList.size(); i++) {
            int j = side == Side.LEFT ? i : verticalBusSetList.size() - i - 1;
            if (verticalBusSetList.get(j).getBusNodeSet().contains(busNode)) {
                return verticalBusSetList.get(j);
            }
        }
        return null;
    }

    public List<InternCell> getSideCandidateFlatCell(Side side) {
        return hbsSideBuses(side).stream()
                .map(busNode -> getVbsSideFromBusNode(busNode, side))
                .distinct().filter(Objects::nonNull)
                .flatMap(vbs -> vbs.getInternCellsFromShape(InternCell.Shape.MAYBE_FLAT).stream())
                .collect(Collectors.toList());
    }

    public List<InternCell> getInternCellsFromShape(InternCell.Shape shape) {
        return verticalBusSetList.stream()
                .flatMap(verticalBusSet -> verticalBusSet.getInternCellsFromShape(shape).stream())
                .collect(Collectors.toList());
    }

    public void sortHbsByVPos() {
        horizontalBusSets.sort(Comparator.comparingInt(hbl -> hbl.getBusNodes().get(0).getBusbarIndex()));
    }

    public int getLength() {
        return verticalBusSetList.size();
    }

    public List<HorizontalBusSet> getHorizontalBusSets() {
        return horizontalBusSets;
    }

    public List<VerticalBusSet> getVerticalBusSetList() {
        return verticalBusSetList;
    }

    @Override
    public String toString() {
        return verticalBusSetList.toString() + "\n" + horizontalBusSets.toString();
    }

}
