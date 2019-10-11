/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */

// WE ASSUME THAT IT IS POSSIBLE TO STACK ALL CELLS AND BE ABLE TO ORGANISE THE VOLTAGELEVEL CONSISTENTLY

public class PositionByClustering implements PositionFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(PositionByClustering.class);

    private static final BusCell.Direction DEFAULTDIRECTION = BusCell.Direction.TOP;

    private class Context {
        private final Graph graph;
        private final Map<BusNode, Integer> nodeToNb = new HashMap<>();
        private final List<LegBusSet> legBusSets = new ArrayList<>();
        private final List<LBSCluster> lbsClusterSet = new ArrayList<>();
        private final List<LBSLink> lbsLinks = new ArrayList<>();

        public Context(Graph graph) {
            this.graph = Objects.requireNonNull(graph);
        }
    }

    @Override
    public void buildLayout(Graph graph) {
        LOGGER.info("start BuildLayout");
        Context context = new Context(graph);

        indexBusPosition(context);

        initLegBusSets(context.graph, context.legBusSets, context.nodeToNb);
        linkLegBusSets(context);

        clustering(context);
        establishBusPositions(context);
        establishFeederPositions(context);

        graph.setMaxBusPosition();
        forceSameOrientationForShuntedCell(graph);
    }

    private void indexBusPosition(Context context) {
        int i = 1;
        for (BusNode n : new ArrayList<>(context.graph.getNodeBuses())) {
            context.nodeToNb.put(n, i);
            i++;
        }
    }

    private void initLegBusSets(Graph graph, List<LegBusSet> legBusSets, Map<BusNode, Integer> nodeToNb) {
        graph.getCells().stream()
                .filter(cell -> cell instanceof BusCell)
                .map(BusCell.class::cast)
                .forEach(cell -> {
                    if (cell.getType() == Cell.CellType.INTERN && !((InternCell) cell).isUniLeg()) {
                        pushNewLBS(legBusSets, nodeToNb, cell, Side.LEFT);
                        pushNewLBS(legBusSets, nodeToNb, cell, Side.RIGHT);
                    } else {
                        pushNewLBS(legBusSets, nodeToNb, cell, Side.UNDEFINED);
                    }
                });
        legBusSets.forEach(LegBusSet::checkInternCells);
    }

    private void pushNewLBS(List<LegBusSet> legBusSets, Map<BusNode, Integer> nodeToNb, BusCell busCell, Side side) {
        LegBusSet legBusSet = side == Side.UNDEFINED ?
                new LegBusSet(nodeToNb, busCell) :
                new LegBusSet(nodeToNb, (InternCell) busCell, side);

        for (LegBusSet lbs : legBusSets) {
            if (lbs.contains(legBusSet)) {
                lbs.absorbs(legBusSet);
                return;
            }
        }
        List<LegBusSet> absorbedLBS = new ArrayList<>();
        legBusSets.forEach(lbs -> {
            if (legBusSet.contains(lbs)) {
                absorbedLBS.add(lbs);
                legBusSet.absorbs(lbs);
            }
        });
        legBusSets.removeAll(absorbedLBS);
        legBusSets.add(legBusSet);
    }

    private void linkLegBusSets(Context context) {
        for (int i = 0; i < context.legBusSets.size(); i++) {
            for (int j = i + 1; j < context.legBusSets.size(); j++) {
                new LBSLink(context.lbsLinks, context.legBusSets.get(i), context.legBusSets.get(j));
            }
        }
        context.lbsLinks.sort(Collections.reverseOrder());
    }

    private void clustering(Context context) {
        context.legBusSets.forEach(lbs -> new LBSCluster(context.lbsClusterSet, lbs));

        // Cluster with links: stronger links first
        List<LBSLink> linksToHandle = context.lbsLinks.stream()
                .filter(LBSLink::hasLink)
                .collect(Collectors.toList());
        for (LBSLink lbsLink : linksToHandle) {
            lbsLink.tryToMergeClusters();
        }
        LBSCluster mainCluster = context.lbsClusterSet.get(0);

        // Merge Cluster with no link
        while (context.lbsClusterSet.size() != 1) {
            mainCluster.merge(Side.RIGHT, context.lbsClusterSet.get(1), Side.LEFT);
        }
    }

    private void establishBusPositions(Context context) {
        context.graph.getNodeBuses().forEach(busNode -> busNode.setStructuralPosition(null));
        LBSCluster finalCluster = context.lbsClusterSet.get(0);
        int v = 1;
        Set<BusNode> remainingBuses = new HashSet<>(context.graph.getNodeBuses());
        while (!remainingBuses.isEmpty()) {
            buildLane(finalCluster, remainingBuses, v);
            v++;
        }
    }

    /**
     * BusNodeAndLbsIndex holds the index of the LegBusSet in the LBSCluster, and the node that is to be positioned
     */
    class BusNodeAndLbsIndex {
        BusNode busNode = null;
        int lbsIndex = 0;
    }

    private void buildLane(LBSCluster lbsCluster, Set<BusNode> remainingBuses, int v) {
        Set<BusNode> busOnLeftSide = new HashSet<>();
        int h = 1;
        List<LegBusSet> lbsList = new ArrayList<>(lbsCluster.getLbsList());
        BusNodeAndLbsIndex busIndex = new BusNodeAndLbsIndex();
        while (busIndex.lbsIndex < lbsList.size()) {
            if (busIndex.busNode == null) {
                findABusToPositionInNextLbs(lbsList, busIndex, remainingBuses, busOnLeftSide);
            }
            if (busIndex.busNode != null) {
                busIndex.busNode.setStructuralPosition(new Position(h, v));
                h++;
                remainingBuses.remove(busIndex.busNode);
                int actualIndex = busIndex.lbsIndex;
                getLastIndexContainingCurrentBus(busIndex, lbsList);
                updateBusOnLeftSide(busOnLeftSide, lbsCluster.getLbsList(), actualIndex, busIndex.lbsIndex);
                actualIndex = busIndex.lbsIndex;
                if (getConnectedBusThroughFlatCell(lbsList, busIndex, remainingBuses, busOnLeftSide)) {
                    updateBusOnLeftSide(busOnLeftSide, lbsCluster.getLbsList(), actualIndex, busIndex.lbsIndex);
                } else {
                    busIndex.busNode = null;
                    busIndex.lbsIndex++;
                }
            }
        }
    }

    void findABusToPositionInNextLbs(List<LegBusSet> legBusSetList,
                                     BusNodeAndLbsIndex busIndex,
                                     Set<BusNode> remainingBuses,
                                     Set<BusNode> busOnLeftSide) {
        for (int i = busIndex.lbsIndex; i < legBusSetList.size(); i++) {
            busIndex.lbsIndex = i;
            for (BusNode bus : legBusSetList.get(busIndex.lbsIndex).getBusNodeSet()) {
                if (remainingBuses.contains(bus) && !busOnLeftSide.contains(bus)) {
                    busIndex.busNode = bus;
                    return;
                }
            }
        }
        busIndex.lbsIndex++; // this index is out of range of LegBusSetList, and end the while loop in which it is called
    }

    private void getLastIndexContainingCurrentBus(BusNodeAndLbsIndex busIndex, List<LegBusSet> lbsList) {
        int j = lbsList.size() - 1;
        while (j >= busIndex.lbsIndex) {
            if (lbsList.get(j).getBusNodeSet().contains(busIndex.busNode)) {
                break;
            }
            j--;
        }
        busIndex.lbsIndex = j;
    }

    private void updateBusOnLeftSide(Set<BusNode> busOnLeftSide, List<LegBusSet> legBusSets, int index1,
                                     int index2) {
        for (int i = index1; i <= Math.min(index2, legBusSets.size() - 1); i++) {
            busOnLeftSide.addAll(legBusSets.get(i).getBusNodeSet());
        }
    }

    private boolean getConnectedBusThroughFlatCell(List<LegBusSet> legBusSetList,
                                                   BusNodeAndLbsIndex busIndex,
                                                   Set<BusNode> remainingBuses,
                                                   Set<BusNode> busOnLeftSide) {
        BusNode node = legBusSetList.get(busIndex.lbsIndex)
                .getCandidateFlatCells().keySet().stream()
                .filter(internCell -> internCell.getBusNodes().contains(busIndex.busNode))
                .flatMap(internCell -> internCell.getBusNodes().stream())
                .filter(busNode -> busNode != busIndex.busNode
                        && remainingBuses.contains(busNode)
                        && !busOnLeftSide.contains(busNode))
                .findAny()
                .orElse(null);

        if (node != null) {
            busIndex.busNode = node;
            int j = busIndex.lbsIndex;
            while (j < legBusSetList.size()) {
                if (legBusSetList.get(j).getBusNodeSet().contains(busIndex.busNode)) {
                    break;
                }
                j++;
            }
            busIndex.lbsIndex = j;
            return true;
        } else {
            return false;
        }
    }

    private void establishFeederPositions(Context context) {
        int cellPos = 0;
        int feederPosition = 1;
        for (LegBusSet lbs : context.lbsClusterSet.get(0).getLbsList()) {
            for (ExternCell busCell : lbs.getEmbededCells().stream()
                    .filter(busCell -> busCell.getType() == Cell.CellType.EXTERN)
                    .map(ExternCell.class::cast)
                    .collect(Collectors.toSet())) {
                busCell.setDirection(cellPos % 2 == 0 ? BusCell.Direction.TOP : BusCell.Direction.BOTTOM);
                busCell.setOrder(cellPos);
                cellPos++;
                for (FeederNode feederNode : busCell.getNodes().stream()
                        .filter(n -> n.getType() == Node.NodeType.FEEDER)
                        .map(FeederNode.class::cast).collect(Collectors.toList())) {
                    feederNode.setOrder(feederPosition);
                    feederPosition++;
                }
            }
        }
    }

    private class LegBusSet {
        private Set<BusNode> busNodeSet;
        private Set<BusCell> embededCells;
        private Map<InternCell, Side> candidateFlatCells;
        private Map<InternCell, Side> crossoverInternCells;
        private Map<LBSLink, LegBusSet> linksTolbs;
        LBSCluster lbsCluster;

        LegBusSet(Map<BusNode, Integer> nodeToNb, List<BusNode> busNodes) {
            busNodeSet = new TreeSet<>(Comparator.comparingInt(nodeToNb::get));
            busNodeSet.addAll(busNodes);
            embededCells = new HashSet<>();
            candidateFlatCells = new HashMap<>();
            crossoverInternCells = new HashMap<>();
            linksTolbs = new TreeMap<>();
        }

        LegBusSet(Map<BusNode, Integer> nodeToNb, BusCell cell) {
            this(nodeToNb, cell.getBusNodes());
            embededCells.add(cell);
        }

        LegBusSet(Map<BusNode, Integer> nodeToNb, InternCell internCell, Side side) {
            this(nodeToNb, internCell.getSideBusNodes(side));
            if (internCell.getBusNodes().size() == 1) {
                embededCells.add(internCell);
            } else if (internCell.getBusNodes().size() == 2) {
                candidateFlatCells.put(internCell, side);
            } else {
                crossoverInternCells.put(internCell, side);
            }
        }

        boolean contains(LegBusSet lbs) {
            return busNodeSet.containsAll(lbs.getBusNodeSet());
        }

        void absorbs(LegBusSet lbsToAbsorb) {
            busNodeSet.addAll(lbsToAbsorb.getBusNodeSet());
            embededCells.addAll(lbsToAbsorb.getEmbededCells());
            absorbMap(candidateFlatCells, lbsToAbsorb.getCandidateFlatCells());
            absorbMap(crossoverInternCells, lbsToAbsorb.getCrossoverInternCell());
        }

        void absorbMap(Map<InternCell, Side> myMap, Map<InternCell, Side> map) {
            Set<InternCell> commonCells = new HashSet<>(myMap.keySet());
            Set<InternCell> cellToAbsorb = map.keySet();
            commonCells.retainAll(cellToAbsorb);
            for (InternCell commonCell : commonCells) {
                if (myMap.get(commonCell) == Side.RIGHT && map.get(commonCell) == Side.LEFT
                        || myMap.get(commonCell) == Side.LEFT && map.get(commonCell) == Side.RIGHT) {
                    embededCells.add(commonCell);
                    myMap.remove(commonCell);
                } else {
                    throw new PowsyblException("Absorption of InternCell in a LegBusSet should concern both side of the InternCell");
                }
            }
            cellToAbsorb.removeAll(commonCells);
            cellToAbsorb.forEach(internCell -> myMap.put(internCell, map.get(internCell)));
        }

        void checkInternCells() {
            genericCheckInternCells(candidateFlatCells);
            genericCheckInternCells(crossoverInternCells);
        }

        private void genericCheckInternCells(Map<InternCell, Side> cells) {
            List<InternCell> cellActuallyEmbeded = new ArrayList<>();
            cells.forEach((internCell, side) -> {
                List<BusNode> otherLegBusNodes = internCell
                        .getSideBusNodes(side.getFlip());
                if (busNodeSet.containsAll(otherLegBusNodes)) {
                    cellActuallyEmbeded.add(internCell);
                }
            });
            cellActuallyEmbeded.forEach(cells::remove);
            embededCells.addAll(cellActuallyEmbeded);
        }

        void addLink(LBSLink lbsLink) {
            linksTolbs.put(lbsLink, lbsLink.getOtherLBS(this));
        }

        void setLbsCluster(LBSCluster lbsCluster) {
            this.lbsCluster = lbsCluster;
        }

        LBSCluster getCluster() {
            return lbsCluster;
        }

        Side getMySidInCluster() {
            return lbsCluster.getLbsSide(this);
        }

        Map<InternCell, Side> getCandidateFlatCells() {
            return candidateFlatCells;
        }

        Map<InternCell, Side> getCrossoverInternCell() {
            return crossoverInternCells;
        }

        Set<BusNode> getBusNodeSet() {
            return busNodeSet;
        }

        public Set<BusCell> getEmbededCells() {
            return embededCells;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof LegBusSet) {
                return busNodeSet.equals(((LegBusSet) o).busNodeSet);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return busNodeSet.hashCode();
        }

    }

    private enum LinkCategory {
        COMMONBUSES, FLATCELLS, CROSSOVER//, SHUNT
    }

    private class LBSLink implements Comparable {
        LegBusSet[] lbss = new LegBusSet[2];
        Map<LinkCategory, Integer> categoryToWeight = new EnumMap<>(LinkCategory.class);

        LBSLink(List<LBSLink> lbsLinks, LegBusSet lbs1, LegBusSet lbs2) {
            lbss[0] = lbs1;
            lbss[1] = lbs2;
            assessLink();
            lbsLinks.add(this);
        }

        void assessLink() {
            HashSet<BusNode> nodeBusesIntersect = new HashSet<>(lbss[0].getBusNodeSet());
            nodeBusesIntersect.retainAll(lbss[1].getBusNodeSet());
            categoryToWeight.put(LinkCategory.COMMONBUSES, nodeBusesIntersect.size());

            HashSet<InternCell> flatCellIntersect = new HashSet<>(lbss[0].getCandidateFlatCells().keySet());
            flatCellIntersect.retainAll(lbss[1].getCandidateFlatCells().keySet());
            categoryToWeight.put(LinkCategory.FLATCELLS, flatCellIntersect.size());

            HashSet<InternCell> commonInternCells = new HashSet<>(lbss[0].getCrossoverInternCell().keySet());
            commonInternCells.retainAll(lbss[1].getCrossoverInternCell().keySet());
            categoryToWeight.put(LinkCategory.CROSSOVER, (int) (commonInternCells
                    .stream()
                    .flatMap(internCell -> internCell.getBusNodes().stream())
                    .distinct()
                    .count()));

            for (LinkCategory cat : LinkCategory.values()) {
                if (categoryToWeight.get(cat) != 0) {
                    lbss[0].addLink(this);
                    lbss[1].addLink(this);
                    break;
                }
            }
        }

        int getLinkCategoryWeight(LinkCategory cat) {
            return categoryToWeight.get(cat);
        }

        LegBusSet getOtherLBS(LegBusSet lbs) {
            if (lbs == lbss[0]) {
                return lbss[1];
            }
            if (lbs == lbss[1]) {
                return lbss[0];
            }
            return null;
        }

        LegBusSet getLbs(int i) {
            if (i > 1) {
                return null;
            }
            return lbss[i];
        }

        boolean tryToMergeClusters() {
            if (lbss[0].getCluster() == lbss[1].getCluster()
                    || lbss[0].getMySidInCluster() == Side.UNDEFINED
                    || lbss[1].getMySidInCluster() == Side.UNDEFINED) {
                return false;
            }
            lbss[0].getCluster().merge(lbss[0].getMySidInCluster(),
                    lbss[1].getCluster(), lbss[1].getMySidInCluster());
            return true;
        }

        boolean hasLink() {
            return categoryToWeight.values().stream().mapToInt(Integer::intValue).sum() != 0;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public int compareTo(@Nonnull Object o) {
            if (!(o instanceof LBSLink)) {
                return 0;
            }
            LBSLink lbsLink = (LBSLink) o;
            for (LinkCategory category : LinkCategory.values()) {
                if (lbsLink.getLinkCategoryWeight(category) > getLinkCategoryWeight(category)) {
                    return -1;
                }
                if (lbsLink.getLinkCategoryWeight(category) < getLinkCategoryWeight(category)) {
                    return 1;
                }
            }
            return 0;
        }
    }

/*
    private class LBSClusterLink implements Comparable {
        LBSCluster[] lbsClusters = new LBSCluster[2];
        Map<LinkCategory, Integer> categoryToWeight = new EnumMap<>(LinkCategory.class);

        LBSClusterLink(LBSCluster lbsCluster1, LBSCluster lbsCluster2, Set<LBSClusterLink> LBSClusterLinks) {
            lbsClusters[0] = lbsCluster1;
            lbsClusters[1] = lbsCluster2;
            LBSClusterLinks.add(this);
            assessLink();
        }

        void assessLink() {
            HashSet<BusNode> nodeBusesIntersect = new HashSet<>(lbsClusters[0].getNodeBuses());
            nodeBusesIntersect.retainAll(lbsClusters[1].getNodeBuses());
            categoryToWeight.put(LinkCategory.COMMONBUSES, nodeBusesIntersect.size());

            HashSet<InternCell> flatCellIntersect = new HashSet<>(lbsClusters[0].getCandidateFlatCells());
            flatCellIntersect.retainAll(lbsClusters[1].getCandidateFlatCells());
            categoryToWeight.put(LinkCategory.FLATCELLS, flatCellIntersect.size());

            HashSet<InternCell> commonInternCells = new HashSet<>(lbsClusters[0].getCrossoverCells());
            commonInternCells.retainAll(lbsClusters[1].getCrossoverCells());
            categoryToWeight.put(LinkCategory.CROSSOVER, (int) (commonInternCells
                    .stream()
                    .flatMap(internCell -> internCell.getBusNodes().stream())
                    .distinct()
                    .count()));
        }

        int getLinkCategoryWeight(LinkCategory cat) {
            return categoryToWeight.get(cat);
        }

        LBSCluster getOtherLBS(LBSCluster lbsCluster) {
            if (lbsCluster == lbsClusters[0]) {
                return lbsClusters[1];
            }
            if (lbsCluster == lbsClusters[1]) {
                return lbsClusters[0];
            }
            return null;
        }

        LBSCluster getLbs(int i) {
            if (i > 1) {
                return null;
            }
            return lbsClusters[i];
        }

        boolean tryToMergeClusters() {
            if (lbsClusters[0] == lbsClusters[1]
                    || lbsClusters[0].getMySidInCluster() == Side.UNDEFINED
                    || lbsClusters[1].getMySidInCluster() == Side.UNDEFINED) {
                return false;
            }
            lbsClusters[0].getCluster().merge(lbsClusters[0].getMySidInCluster(),
                    lbsClusters[1].getCluster(), lbsClusters[1].getMySidInCluster());
            return true;
        }


        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public int compareTo(@Nonnull Object o) {
            if (!(o instanceof LBSLink)) {
                return 0;
            }
            LBSLink lbsLink = (LBSLink) o;
            for (LinkCategory category : LinkCategory.values()) {
                if (lbsLink.getLinkCategoryWeight(category) > getLinkCategoryWeight(category)) {
                    return -1;
                }
                if (lbsLink.getLinkCategoryWeight(category) < getLinkCategoryWeight(category)) {
                    return 1;
                }
            }
            return 0;
        }
    }
*/

    private class HorizontalLane {
        List<BusNode> busNodes;

        HorizontalLane(BusNode busNode) {
            busNodes = new ArrayList<>();
            busNodes.add(busNode);
        }

        void reverse() {
            Collections.reverse(busNodes);
        }

        BusNode getSideNode(Side side) {
            if (side == Side.LEFT) {
                return busNodes.get(0);
            }
            if (side == Side.RIGHT) {
                return busNodes.get(busNodes.size() - 1);
            }
            return null;
        }
    }

    private class LBSCluster {
        List<LegBusSet> lbsList;
        Map<Side, LegBusSet> sideToLbs;
        List<HorizontalLane> horizontalLanes;

        List<LBSCluster> lbsClusters;

        LBSCluster(List<LBSCluster> lbsClusters, LegBusSet lbs) {
            lbsList = new ArrayList<>();
            lbsList.add(lbs);
            lbs.setLbsCluster(this);

            horizontalLanes = new ArrayList<>();
            lbs.getBusNodeSet().forEach(busNode -> horizontalLanes.add(new HorizontalLane(busNode)));

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
            lbsList.addAll(otherLbsCluster.lbsList);
            sideToLbs.put(Side.RIGHT, otherLbsCluster.sideToLbs.get(Side.RIGHT));
            lbsClusters.remove(otherLbsCluster);
        }

        void reverse() {
            Collections.reverse(lbsList);
            LegBusSet lbs = sideToLbs.get(Side.LEFT);
            sideToLbs.put(Side.LEFT, sideToLbs.get(Side.RIGHT));
            sideToLbs.put(Side.RIGHT, lbs);
            horizontalLanes.forEach(HorizontalLane::reverse);
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

        List<BusNode> getSideConnectableBuses(Side side) {
            if (side == Side.LEFT || side == Side.RIGHT) {
                return horizontalLanes.stream()
                        .map(horizontalLane -> horizontalLane.getSideNode(side))
                        .collect(Collectors.toList());
            }
            return null;
        }

        Set<BusNode> getNodeBuses() {
            return lbsList.stream().flatMap(legBusSet -> legBusSet.getBusNodeSet().stream()).collect(Collectors.toSet());
        }

        Set<InternCell> getCandidateFlatCells() {
            return lbsList.stream().flatMap(legBusSet -> legBusSet.getCandidateFlatCells().keySet().stream()).collect(Collectors.toSet());
        }

        Set<InternCell> getCrossoverCells() {
            return lbsList.stream().flatMap(legBusSet -> legBusSet.getCrossoverInternCell().keySet()
                    .stream()).collect(Collectors.toSet());
        }

        List<LegBusSet> getLbsList() {
            return lbsList;
        }

    }
}
