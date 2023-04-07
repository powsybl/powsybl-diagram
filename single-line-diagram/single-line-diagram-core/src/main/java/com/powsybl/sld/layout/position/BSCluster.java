/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.position;

import com.powsybl.sld.model.cells.InternCell;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.nodes.BusNode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * BSCluster contains a list of VerticalBusSets (VBS) that is orderly build by successively merging BSCluster initially
 * containing a single VBS.
 * BSCluster handles the building of the horizontalBusLists that are an horizontal strings of busNodes.
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public class BSCluster {

    private final List<VerticalBusSet> verticalBusSets = new ArrayList<>();
    private final List<HorizontalBusList> horizontalBusLists = new ArrayList<>();

    public BSCluster(VerticalBusSet vbs) {
        Objects.requireNonNull(vbs);
        verticalBusSets.add(vbs);
        vbs.getBusNodeSet().forEach(nodeBus -> horizontalBusLists.add(new HorizontalBusList(nodeBus, this)));
    }

    public static List<BSCluster> createBSClusters(List<VerticalBusSet> verticalBusSets) {
        List<BSCluster> bsClusters = new ArrayList<>();
        for (VerticalBusSet vbs : verticalBusSets) {
            bsClusters.add(new BSCluster(vbs));
        }
        return bsClusters;
    }

    public void merge(Side myConcernedSide, BSCluster otherBsCluster, Side otherSide, HorizontalBusListManager hblManager) {
        if (myConcernedSide == Side.LEFT) {
            reverse();
        }
        if (otherSide == Side.RIGHT) {
            otherBsCluster.reverse();
        }
        hblManager.mergeHbl(this, otherBsCluster);
        verticalBusSets.addAll(otherBsCluster.verticalBusSets);
    }

    public List<BusNode> hblSideBuses(Side side) {
        return hblSideBuses(side, horizontalBusLists);
    }

    public static List<BusNode> hblSideBuses(Side side, List<HorizontalBusList> hblList) {
        return hblList.stream()
                .map(hl -> hl.getSideNode(side)).collect(Collectors.toList());
    }

    public void removeHbl(HorizontalBusList hbl) {
        horizontalBusLists.remove(hbl);
    }

    public void establishBusNodePosition() {
        int v = 1;
        for (HorizontalBusList hbl : horizontalBusLists) {
            hbl.establishBusPosition(v);
            v++;
        }
    }

    public Optional<HorizontalBusList> getHblFromSideBus(BusNode busNode, Side side) {
        return horizontalBusLists
                .stream()
                .filter(hbl -> hbl.getSideNode(side) == busNode)
                .findAny();
    }

    List<BusNode> getVerticalBusNodes(int i) {
        return horizontalBusLists.stream().map(hbl -> hbl.getBusNode(i)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private void reverse() {
        Collections.reverse(verticalBusSets);
        horizontalBusLists.forEach(hbl -> hbl.reverse(verticalBusSets.size()));
    }

    private VerticalBusSet getVbsSideFromBusNode(BusNode busNode, Side side) {
        if (side != Side.RIGHT && side != Side.LEFT) {
            return null;
        }
        for (int i = 0; i < verticalBusSets.size(); i++) {
            int j = side == Side.LEFT ? i : verticalBusSets.size() - i - 1;
            if (verticalBusSets.get(j).getBusNodeSet().contains(busNode)) {
                return verticalBusSets.get(j);
            }
        }
        return null;
    }

    public List<InternCell> getSideCandidateFlatCell(Side side) {
        return hblSideBuses(side).stream()
                .map(busNode -> getVbsSideFromBusNode(busNode, side))
                .distinct().filter(Objects::nonNull)
                .flatMap(vbs -> vbs.getInternCellsFromShape(InternCell.Shape.MAYBE_FLAT).stream())
                .collect(Collectors.toList());
    }

    public List<InternCell> getInternCellsFromShape(InternCell.Shape shape) {
        return verticalBusSets.stream()
                .flatMap(verticalBusSet -> verticalBusSet.getInternCellsFromShape(shape).stream())
                .collect(Collectors.toList());
    }

    public void sortHblByVPos() {
        horizontalBusLists.sort(Comparator.comparingInt(hbl -> hbl.getBusNodes().get(0).getBusbarIndex()));
    }

    public int getLength() {
        return verticalBusSets.size();
    }

    public List<HorizontalBusList> getHorizontalBusLists() {
        return horizontalBusLists;
    }

    public List<VerticalBusSet> getVerticalBusSets() {
        return verticalBusSets;
    }

    @Override
    public String toString() {
        return verticalBusSets.toString() + "\n" + horizontalBusLists.toString();
    }

}
