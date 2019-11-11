/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.positionbyclustering;

import com.powsybl.sld.layout.PositionFinder;
import com.powsybl.sld.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */

// WE ASSUME THAT IT IS POSSIBLE TO STACK ALL CELLS AND BE ABLE TO ORGANISE THE VOLTAGELEVEL CONSISTENTLY

public class PositionByClustering implements PositionFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(PositionByClustering.class);

    boolean useLBSLinkOnly;

    public PositionByClustering(boolean useLBSLinkOnly) {
        this.useLBSLinkOnly = useLBSLinkOnly;
    }

    public PositionByClustering() {
        this(false);
    }

    @Override
    public void buildLayout(Graph graph) {
        LOGGER.info("start BuildLayout");
        Map<BusNode, Integer> busToNb = indexBusPosition(graph.getNodeBuses());

        List<LegBusSet> legBusSets = initLegBusSets(graph, busToNb);
        LBSCluster lbsCluster;

        if (useLBSLinkOnly) {
            lbsCluster = clusteringByLBSLink(graph, legBusSets);
        } else {
            lbsCluster = clusteringByLBSClusterLink(legBusSets);
        }
        establishFeederPositions(lbsCluster);

        graph.setMaxBusPosition();
        forceSameOrientationForShuntedCell(graph);
    }

    private Map<BusNode, Integer> indexBusPosition(List<BusNode> busNodes) {
        Map<BusNode, Integer> busToNb = new HashMap<>();
        int i = 1;
        for (BusNode n : busNodes.stream()
                .sorted(Comparator.comparing(BusNode::getId))
                .collect(Collectors.toList())) {
            busToNb.put(n, i);
            i++;
        }
        return busToNb;
    }

    private List<LegBusSet> initLegBusSets(Graph graph, Map<BusNode, Integer> nodeToNb) {
        List<LegBusSet> legBusSets = new ArrayList<>();
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
        return legBusSets;
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

    private LBSCluster clusteringByLBSLink(Graph graph, List<LegBusSet> legBusSets) {
        List<LBSCluster> lbsClusters = new ArrayList<>();
        legBusSets.forEach(lbs -> new LBSCluster(lbsClusters, lbs));
        Links<LegBusSet> links = new Links<>(legBusSets);

        // Cluster with lbslinks: stronger lbslinks first
        List<Link<LegBusSet>> linksToHandle = links.getLinkSet().stream()
                .filter(Link::hasLink)
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toList());
        for (Link<LegBusSet> link : linksToHandle) {
            link.mergeClusters();
        }
        LBSCluster mainCluster = lbsClusters.get(0);

        // Merge Cluster with no link
        while (lbsClusters.size() != 1) {
            mainCluster.merge(Side.RIGHT, lbsClusters.get(1), Side.LEFT);
        }
        establishBusPositions(graph, lbsClusters.get(0));
        return lbsClusters.get(0);
    }

    private LBSCluster clusteringByLBSClusterLink(List<LegBusSet> legBusSets) {
        List<LBSCluster> lbsClusters = new ArrayList<>();
        legBusSets.forEach(lbs -> new LBSCluster(lbsClusters, lbs));
        Links<LBSClusterSide> links = new Links<>();
        lbsClusters.forEach(lbsCluster -> {
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

        lbsClusters.get(0).mergeHorizontalLanes();
        lbsClusters.get(0).establishBusNodePosition();
        return lbsClusters.get(0);
    }

    private void establishBusPositions(Graph graph, LBSCluster lbsCluster) {
        graph.getNodeBuses().forEach(busNode -> busNode.setStructuralPosition(null));
        int v = 1;
        Set<BusNode> remainingBuses = new HashSet<>(graph.getNodeBuses());
        while (!remainingBuses.isEmpty()) {
            buildLane(lbsCluster, remainingBuses, v);
            v++;
        }
    }

    /**
     * BusNodeAndLbsIndex holds the index of the LegBusSet in the LBSCluster, and the node that is to be positioned
     */
    private class BusNodeAndLbsIndex {
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

    private void establishFeederPositions(LBSCluster lbsCluster) {
        int cellPos = 0;
        int feederPosition = 1;
        for (LegBusSet lbs : lbsCluster.getLbsList()) {
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

}
