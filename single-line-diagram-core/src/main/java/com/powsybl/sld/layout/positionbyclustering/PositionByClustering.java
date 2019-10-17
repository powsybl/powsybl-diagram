/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.positionbyclustering;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.layout.PositionFinder;
import com.powsybl.sld.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

// WE ASSUME THAT IT IS POSSIBLE TO STACK ALL CELLS AND BE ABLE TO ORGANISE THE VOLTAGELEVEL CONSISTENTLY

public class PositionByClustering implements PositionFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(PositionByClustering.class);

    private static final BusCell.Direction DEFAULTDIRECTION = BusCell.Direction.TOP;

    private class Context {
        private final Graph graph;
        private final Map<BusNode, Integer> busToNb = new HashMap<>();
        private final List<LegBusSet> legBusSets = new ArrayList<>();
        private final Map<BusNode, List<LegBusSet>> busToLBSs = new HashMap<>();
        private final List<LBSCluster> lbsClusterSets = new ArrayList<>();

        Context(Graph graph) {
            this.graph = Objects.requireNonNull(graph);
        }
    }

    @Override
    public void buildLayout(Graph graph) {
        LOGGER.info("start BuildLayout");
        Context context = new Context(graph);

        indexBusPosition(context);

        initLegBusSets(context.graph, context.legBusSets, context.busToNb);
        mapBusToLbs(context.legBusSets, context.busToLBSs);

//        clusteringByLBSLink(context);
        clusteringByLBSClusterLink(context);
        establishBusPositions(context);
        establishFeederPositions(context);

        graph.setMaxBusPosition();
        forceSameOrientationForShuntedCell(graph);
    }

    private void indexBusPosition(Context context) {
        int i = 1;
        for (BusNode n : new ArrayList<>(context.graph.getNodeBuses())) {
            context.busToNb.put(n, i);
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
        // find orphan busNodes and build their LBS
        List<BusNode> allBusNodes = new ArrayList<>(graph.getNodeBuses());
        allBusNodes.removeAll(legBusSets.stream()
                .flatMap(legBusSet -> legBusSet.getBusNodeSet().stream()).collect(Collectors.toList()));
        allBusNodes.forEach(busNode -> legBusSets.add(new LegBusSet(nodeToNb, busNode)));
        legBusSets.forEach(LegBusSet::checkInternCells);
    }

    private void mapBusToLbs(List<LegBusSet> legBusSets, Map<BusNode, List<LegBusSet>> busToLBSs) {
        legBusSets.forEach(lbs -> lbs.getBusNodeSet().forEach(busNode -> {
            busToLBSs.putIfAbsent(busNode, new ArrayList<>());
            busToLBSs.get(busNode).add(lbs);
        }));
    }

    private void pushNewLBS(List<LegBusSet> legBusSets, Map<BusNode, Integer> nodeToNb, BusCell busCell, Side
            side) {
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

    private void clusteringByLBSLink(Context context) {
        context.legBusSets.forEach(lbs -> new LBSCluster(context.lbsClusterSets, lbs));
        Links<LegBusSet> links = new Links<>(context.legBusSets);

        // Cluster with lbslinks: stronger lbslinks first
        List<Link<LegBusSet>> linksToHandle = links.getLinkSet().stream()
                .filter(Link::hasLink)
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toList());
        for (Link<LegBusSet> link : linksToHandle) {
            link.mergeClusters();
        }
        LBSCluster mainCluster = context.lbsClusterSets.get(0);

        // Merge Cluster with no link
        while (context.lbsClusterSets.size() != 1) {
            mainCluster.merge(Side.RIGHT, context.lbsClusterSets.get(1), Side.LEFT);
        }
    }

    private void clusteringByLBSClusterLink(Context context) {
        context.legBusSets.forEach(lbs -> new LBSCluster(context.lbsClusterSets, lbs));
        Links<LBSClusterSide> links = new Links<>();
        context.lbsClusterSets.forEach(lbsCluster -> {
            links.addLinkable(new LBSClusterSide(lbsCluster, Side.LEFT));
            links.addLinkable(new LBSClusterSide(lbsCluster, Side.RIGHT));
        });
        while (!links.isEmpty()) {
            Link<LBSClusterSide> link = links.getStrongerLink();
            link.mergeClusters();
            LBSCluster mergedCluster = link.getLinkable(0).getCluster();
            links.removeLinkable(link.getLinkable(0));
            links.removeLinkable(link.getLinkable(1));
            links.removeLinkable(link.getLinkable(0).getOtherSameRoot(links.getLinkables()));
            links.removeLinkable(link.getLinkable(1).getOtherSameRoot(links.getLinkables()));
            link.unregister();

            links.addLinkable(new LBSClusterSide(mergedCluster, Side.LEFT));
            links.addLinkable(new LBSClusterSide(mergedCluster, Side.RIGHT));
        }
    }

    private void establishBusPositions(Context context) {
        context.graph.getNodeBuses().forEach(busNode -> busNode.setStructuralPosition(null));
        LBSCluster finalCluster = context.lbsClusterSets.get(0);
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

    private void findABusToPositionInNextLbs(List<LegBusSet> legBusSetList,
                                             BusNodeAndLbsIndex busIndex,
                                             Set<BusNode> remainingBuses,
                                             Set<BusNode> busOnLeftSide) {
        for (int i = busIndex.lbsIndex; i < legBusSetList.size(); i++) {
            busIndex.lbsIndex = i;
            LegBusSet lbs = legBusSetList.get(busIndex.lbsIndex);
            for (BusNode bus : lbs.getBusNodeSet()) {
                if (remainingBuses.contains(bus)
                        && !busOnLeftSide.contains(bus)
                    // if bus is connected through a flatCell to a bus that is remaining and on left, then, this bus should be in a next lane
/*
                        && !lbs.getCandidateFlatCells().keySet().stream()
                        .filter(internCell -> internCell.getBusNodes().contains(bus))
                        .flatMap(internCell -> internCell.getBusNodes().stream())
                        .anyMatch(busNode -> busNode != bus
                                && remainingBuses.contains(busNode)
                                && busOnLeftSide.contains(busNode))
*/
                ) {
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
        Set<BusNode> candidateFlatConnectedBusNode = legBusSetList.get(busIndex.lbsIndex)
                .getCandidateFlatCells().keySet().stream()
                .filter(internCell -> internCell.getBusNodes().contains(busIndex.busNode))
                .flatMap(internCell -> internCell.getBusNodes().stream())
                .filter(busNode -> busNode != busIndex.busNode
                        && remainingBuses.contains(busNode)
                        && !busOnLeftSide.contains(busNode))
                .collect(Collectors.toSet());

        List<BusNode> nodes;
        for (int i = 0; i < legBusSetList.size(); i++) {
            LegBusSet lbs = legBusSetList.get(i);
            nodes = new ArrayList<>(lbs.getBusNodeSet());
            nodes.retainAll(candidateFlatConnectedBusNode);
            if (!nodes.isEmpty()) {
                if (i < busIndex.lbsIndex && lbs.getCandidateFlatCells().size() == 1) {
                    candidateFlatConnectedBusNode.removeAll(nodes);
                } else {
                    busIndex.busNode = nodes.get(0);
                    busIndex.lbsIndex = i;
                    return true;
                }
            }
        }
        return false;
    }

    private void establishFeederPositions(Context context) {
        int cellPos = 0;
        int feederPosition = 1;
        for (LegBusSet lbs : context.lbsClusterSets.get(0).getLbsList()) {
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

    abstract class AbstractLinkable {
        abstract Set<BusNode> getBusNodeSet();

        abstract List<InternCell> getCandidateFlatCellList();

        abstract List<InternCell> getCrossOverCellList();

        abstract LBSCluster getCluster();

        abstract Side getMySidInCluster();

        abstract boolean hasSameRoot(Object other);

        abstract <T extends AbstractLinkable> void addLink(Link<T> link);

        abstract <T extends AbstractLinkable> void removeLink(Link<T> link);

        abstract <T extends AbstractLinkable> List<Link<T>> getLinks();

        <T extends AbstractLinkable> T getOtherSameRoot(List<T> linkables) {
            return null;
        }

    }

    private class LegBusSet extends AbstractLinkable {
        private Set<BusNode> busNodeSet;
        private Set<BusCell> embededCells;
        private Map<InternCell, Side> candidateFlatCells;
        private Map<InternCell, Side> crossoverInternCells;
        LBSCluster lbsCluster;
        List<Link<LegBusSet>> myLinks;

        LegBusSet(Map<BusNode, Integer> nodeToNb, List<BusNode> busNodes) {
            busNodeSet = new TreeSet<>(Comparator.comparingInt(nodeToNb::get));
            busNodeSet.addAll(busNodes);
            embededCells = new HashSet<>();
            candidateFlatCells = new HashMap<>();
            crossoverInternCells = new HashMap<>();
            myLinks = new ArrayList<>();
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

        LegBusSet(Map<BusNode, Integer> nodeToNb, BusNode busNode) {
            this(nodeToNb, Collections.singletonList(busNode));
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

        void setLbsCluster(LBSCluster lbsCluster) {
            this.lbsCluster = lbsCluster;
        }

        public LBSCluster getCluster() {
            return lbsCluster;
        }

        public Side getMySidInCluster() {
            return lbsCluster.getLbsSide(this);
        }

        Map<InternCell, Side> getCandidateFlatCells() {
            return candidateFlatCells;
        }

        public List<InternCell> getCandidateFlatCellList() {
            return new ArrayList<>(candidateFlatCells.keySet());
        }

        Map<InternCell, Side> getCrossoverInternCell() {
            return crossoverInternCells;
        }

        public List<InternCell> getCrossOverCellList() {
            return new ArrayList<>(crossoverInternCells.keySet());
        }

        public Set<BusNode> getBusNodeSet() {
            return busNodeSet;
        }

        boolean hasSameRoot(Object other) {
            if (other.getClass() != LegBusSet.class) {
                return false;
            }
            return this == other;
        }

        @SuppressWarnings("unchecked")
        <T extends AbstractLinkable> void addLink(Link<T> link) {
            myLinks.add((Link<LegBusSet>) link);
        }

        @SuppressWarnings("unchecked")
        <T extends AbstractLinkable> void removeLink(Link<T> link) {
            myLinks.remove((Link<LegBusSet>) link);
        }

        @SuppressWarnings("unchecked")
        List<Link<LegBusSet>> getLinks() {
            return myLinks;
        }

        Set<BusCell> getEmbededCells() {
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

    private class Link<T extends AbstractLinkable> implements Comparable {
        private T linkable1;
        private T linkable2;
        private Map<LinkCategory, Integer> categoryToWeight = new EnumMap<>(LinkCategory.class);

        Link(T linkable1, T linkable2) {
            this.linkable1 = linkable1;
            this.linkable2 = linkable2;
            linkable1.addLink(this);
            linkable2.addLink(this);
            assessLink();
        }

        void assessLink() {
            HashSet<BusNode> nodeBusesIntersect = new HashSet<>(linkable1.getBusNodeSet());
            nodeBusesIntersect.retainAll(linkable2.getBusNodeSet());
            categoryToWeight.put(LinkCategory.COMMONBUSES, nodeBusesIntersect.size());

            HashSet<InternCell> flatCellIntersect = new HashSet<>(linkable1.getCandidateFlatCellList());
            flatCellIntersect.retainAll(linkable2.getCandidateFlatCellList());
            categoryToWeight.put(LinkCategory.FLATCELLS, flatCellIntersect.size());

            HashSet<InternCell> commonInternCells = new HashSet<>(linkable1.getCrossOverCellList());
            commonInternCells.retainAll(linkable2.getCrossOverCellList());
            categoryToWeight.put(LinkCategory.CROSSOVER, (int) (commonInternCells
                    .stream()
                    .flatMap(internCell -> internCell.getBusNodes().stream())
                    .distinct()
                    .count()));
        }

        int getLinkCategoryWeight(LinkCategory cat) {
            return categoryToWeight.get(cat);
        }

        T getOtherLinkable(T linkable) {
            if (linkable == linkable1) {
                return linkable2;
            }
            if (linkable == linkable2) {
                return linkable1;
            }
            return null;
        }

        T getLinkable(int i) {
            if (i == 0) {
                return linkable1;
            } else if (i == 1) {
                return linkable2;
            }
            return null;
        }

        void mergeClusters() {
            if (linkable1.getCluster() == linkable2.getCluster()
                    || linkable1.getMySidInCluster() == Side.UNDEFINED
                    || linkable2.getMySidInCluster() == Side.UNDEFINED) {
                return;
            }
            linkable1.getCluster().merge(
                    linkable1.getMySidInCluster(),
                    linkable2.getCluster(),
                    linkable2.getMySidInCluster());
        }

        boolean hasLink() {
            return categoryToWeight.values().stream().mapToInt(Integer::intValue).sum() != 0;
        }

        void unregister() {
            linkable1.removeLink(this);
            linkable2.removeLink(this);
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
            if (!(o instanceof Link)) {
                return 0;
            }
            Link link = (Link) o;
            for (LinkCategory category : LinkCategory.values()) {
                if (link.getLinkCategoryWeight(category) > getLinkCategoryWeight(category)) {
                    return -1;
                }
                if (link.getLinkCategoryWeight(category) < getLinkCategoryWeight(category)) {
                    return 1;
                }
            }
            return this.hashCode() - o.hashCode();
        }
    }

    private class Links<T extends AbstractLinkable> {
        List<T> linkables;
        TreeSet<Link<T>> linkSet = new TreeSet<>();

        Links(List<T> linkables) {
            this.linkables = linkables;
            for (int i = 0; i < linkables.size(); i++) {
                for (int j = i + 1; j < linkables.size(); j++) {
                    buildNewLink(linkables.get(i), linkables.get(j));
                }
            }
        }

        Links() {
            this.linkables = new ArrayList<>();
        }

        void addLinkable(T linkable) {
            linkables.add(linkable);
            linkables.forEach(lk -> buildNewLink(lk, linkable));
        }

        private void buildNewLink(T linkable1, T linkable2) {
            if (!linkable1.hasSameRoot(linkable2)) {
                linkSet.add(new Link<>(linkable1, linkable2));
            }
        }

        Link<T> getStrongerLink() {
            return linkSet.last();
        }

        void removeLinkable(T linkable) {
            linkables.remove(linkable);
            removeLinksToLinkable(linkable);
        }

        private void removeLinksToLinkable(T linkable) {
            List<Link<T>> linksCopy = new ArrayList<>(linkable.getLinks());
            linksCopy.forEach(link -> {
                link.unregister();
                linkSet.remove(link);
            });
        }

        Set<Link<T>> getLinkSet() {
            return linkSet;
        }

        boolean isEmpty() {
            return linkSet.isEmpty();
        }

        List<T> getLinkables() {
            return linkables;
        }
    }

    class HorizontalLane {

        List<BusNode> busNodes;
        Map<Side, Integer> sideToLbsIndex = new EnumMap<>(Side.class);

        HorizontalLane(BusNode busNode, int leftIndex, int rightIndex) {
            this.busNodes = new ArrayList<>();
            this.busNodes.add(busNode);
            sideToLbsIndex.put(Side.LEFT, leftIndex);
            sideToLbsIndex.put(Side.RIGHT, rightIndex);
        }

        void reverse() {
            Collections.reverse(busNodes);
        }

        void merge(LBSCluster otherLbsCluster, HorizontalLane otherLane, int actualMaxLBSIndex) {
            busNodes.addAll(otherLane.getBusNodes());
            sideToLbsIndex.put(Side.RIGHT, actualMaxLBSIndex + otherLane.getSideLbsIndex(Side.RIGHT));
            otherLbsCluster.removeLane(otherLane);
        }

        public List<BusNode> getBusNodes() {
            return busNodes;
        }

        BusNode getSideNode(Side side) {
            if (side == Side.UNDEFINED || busNodes.isEmpty()) {
                return null;
            }
            if (side == Side.LEFT) {
                return busNodes.get(0);
            }
            return busNodes.get(busNodes.size() - 1);
        }

        int getSideLbsIndex(Side side) {
            return sideToLbsIndex.get(side);
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
            horizontalLanes = new ArrayList<>();
            lbs.getBusNodeSet().forEach(nodeBus -> horizontalLanes.add(new HorizontalLane(nodeBus, 0, 0)));
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
            mergeHorizontalLanes(otherLbsCluster);
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

        void mergeHorizontalLanes(LBSCluster otherCluster) {
            List<HorizontalLane> availableLanesToMerge = new ArrayList<>(horizontalLanes);
            mergeCommonBusNode(otherCluster, availableLanesToMerge);
            mergeFlatCell(otherCluster, availableLanesToMerge);
            mergeNoStrongLink(otherCluster);
        }

        void removeLane(HorizontalLane lane) {
            horizontalLanes.remove(lane);
        }

        private void mergeCommonBusNode(LBSCluster otherCluster, List<HorizontalLane> availableLanesToMerge) {
            List<BusNode> commonNodes = new ArrayList<>(laneSideBuses(Side.RIGHT));
            commonNodes.retainAll(otherCluster.laneSideBuses(Side.LEFT));
            commonNodes.forEach(busNode ->
                    finalizeMerge(otherCluster, busNode, busNode, availableLanesToMerge));
        }

        private void mergeFlatCell(LBSCluster otherCluster, List<HorizontalLane> availableLanesToMerge) {
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
                finalizeMerge(otherCluster, myNode, otherNode, availableLanesToMerge);
            });
        }

        private void mergeNoStrongLink(LBSCluster otherCluster) {
            horizontalLanes.addAll(otherCluster.getHorizontalLanes());
        }

        private void finalizeMerge(LBSCluster otherCluster, BusNode myNode, BusNode otherBus, List<HorizontalLane> availableLanesToMerge) {
            HorizontalLane myLane = getHorizontalLaneFromSideBus(myNode, Side.RIGHT);
            HorizontalLane otherLane = otherCluster.getHorizontalLaneFromSideBus(otherBus, Side.LEFT);
            if (otherLane != null && myLane != null) {
                myLane.merge(otherCluster, otherLane, lbsList.size());
                availableLanesToMerge.remove(myLane);
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

    private class LBSClusterSide extends AbstractLinkable {
        LBSCluster lbsCluster;
        Side side;
        List<Link<LBSClusterSide>> myLinks;

        LBSClusterSide(LBSCluster lbsCluster, Side side) {
            this.lbsCluster = lbsCluster;
            this.side = side;
            myLinks = new ArrayList<>();
        }

        public Set<BusNode> getBusNodeSet() {
            return new HashSet<>(lbsCluster.laneSideBuses(side));
        }

        public List<InternCell> getCandidateFlatCellList() {
            return lbsCluster.getSideFlatCell(side);
        }

        public List<InternCell> getCrossOverCellList() {
            return lbsCluster.getCrossoverCells();
        }

        public LBSCluster getCluster() {
            return lbsCluster;
        }

        public Side getMySidInCluster() {
            return side;
        }

        boolean hasSameRoot(Object other) {
            if (other.getClass() != LBSClusterSide.class) {
                return false;
            }
            return this.lbsCluster == ((LBSClusterSide) other).getCluster();
        }

        @Override
        <T extends AbstractLinkable> T getOtherSameRoot(List<T> linkables) {
            return linkables.stream().filter(linkable ->
                    linkable.getCluster() == lbsCluster
                            && side.getFlip() == ((LBSClusterSide) linkable).getSide()).findAny().orElse(null);
        }

        @SuppressWarnings("unchecked")
        <T extends AbstractLinkable> void addLink(Link<T> link) {
            myLinks.add((Link<LBSClusterSide>) link);
        }

        @SuppressWarnings("unchecked")
        <T extends AbstractLinkable> void removeLink(Link<T> link) {
            myLinks.remove((Link<LBSClusterSide>) link);
        }

        @SuppressWarnings("unchecked")
        public List<Link<LBSClusterSide>> getLinks() {
            return myLinks;
        }

        public Side getSide() {
            return side;
        }
    }
}
