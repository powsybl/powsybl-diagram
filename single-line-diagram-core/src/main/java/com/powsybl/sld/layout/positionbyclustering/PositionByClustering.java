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
 * PositionByClustering finds adequate positions for the busBars with the following principles:
 * All the connections to the BusBar of the leg of an ExternCell, or each leg of an InternCell shall be stackable
 * (ie could be displayed with disconnectors to busbar vertically aligned).
 * This implies that the busBars of a leg shall be spread in different vertical structuralPosition,
 * (and in many case having the same horizontal structuralPosition).
 * The first step consists in building LegBusSets that contains busBars that shall be vertically aligned (considering
 * they have legs of cell that impose it).
 * Then LBSClusters are initiated by building one LBSCluster per LegBusSet.
 * The LBSClusters are then merged 2 by 2 starting by LBSclusters that have the strongest Link.
 * Two strategies of strength assessment of the links between clusters are implemented:
 * <ul>
 * <li>
 * if useLBSLinkOnly is true: the strength between LegBusSets is considered: this means that the strength of
 * the link between two clusters is the one of the strongest link between two LegBusSets (one per cluster). This
 * is a simple implementation that is limited as it it does not consider the difference between the side of a
 * cluster: if two clusters A and B are to be merged, the result can either be A-B or B-A.
 * </li>
 * <li>
 * if useLBSLinkOnly is false: the strength between LBSClusterSide is considered. This is similar
 * to what si done with LegBusSet but the assessment of the strength of the link considers both sides of the
 * cluster.
 * Therefore, with cluster A and B, there are 4 LBSClusterSide A-Right A-Left B-Right and B-Left. The links that
 * are considered are (A-Right, B-Left), (A-Right, B-Right), (B-Right, B-Left), (B-Right, B-Right). When merging,
 * alignment is required (meaning that clusters could be reversed to ensure the connection sides between the
 * 2 clusters are respected : 1st cluster-Right is merged with 2nd cluster-left).
 * </li>
 * </ul>
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */

// WE ASSUME THAT IT IS POSSIBLE TO STACK ALL CELLS AND BE ABLE TO ORGANISE THE VOLTAGELEVEL CONSISTENTLY

public class PositionByClustering implements PositionFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(PositionByClustering.class);

    private boolean useLBSLinkOnly;

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
                .filter(cell -> cell.getType() == Cell.CellType.EXTERN
                        || (cell.getType() == Cell.CellType.INTERN && ((InternCell) cell).isUniLeg()))
                .map(BusCell.class::cast)
                .sorted(Comparator.comparing(Cell::getFullId)) // avoid randomness
                .forEach(cell -> pushNewLBS(legBusSets, nodeToNb, cell, Side.UNDEFINED));

        graph.getCells().stream()
                .filter(cell -> cell.getType() == Cell.CellType.INTERN && !((InternCell) cell).isUniLeg())
                .map(InternCell.class::cast)
                .sorted(Comparator.comparing(cell -> -((InternCell) cell).getBusNodes().size())         // bigger first to identify encompassed InternCell at the end with the smaller one
                        .thenComparing(cell -> ((InternCell) cell).getFullId()))                        // avoid randomness
                .forEach(cell -> pushNonUnilegInternCell(legBusSets, nodeToNb, cell));

        // find orphan busNodes and build their LBS
        List<BusNode> allBusNodes = new ArrayList<>(graph.getNodeBuses());
        allBusNodes.removeAll(legBusSets.stream().
                flatMap(legBusSet -> legBusSet.getBusNodeSet().stream()).collect(Collectors.toList()));
        allBusNodes.stream()
                .sorted(Comparator.comparing(Node::getId))              //avoid randomness
                .forEach(busNode -> legBusSets.add(new LegBusSet(nodeToNb, busNode)));
        legBusSets.forEach(LegBusSet::checkInternCells);
        return legBusSets;
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

    private void pushNonUnilegInternCell(List<LegBusSet> legBusSets, Map<BusNode, Integer> nodeToNb, InternCell internCell) {
        for (LegBusSet lbs : legBusSets) {
            if (lbs.contains(internCell.getBusNodes())) {
                lbs.addEmbededCell(internCell);
                return;
            }
        }
        pushNewLBS(legBusSets, nodeToNb, internCell, Side.LEFT);
        pushNewLBS(legBusSets, nodeToNb, internCell, Side.RIGHT);
    }

    private LBSCluster clusteringByLBSLink(Graph graph, List<LegBusSet> legBusSets) {
        List<LBSCluster> lbsClusters = initLBSCluster(legBusSets);
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
        List<LBSCluster> lbsClusters = initLBSCluster(legBusSets);
        Links<LBSClusterSide> links = new Links<>();
        lbsClusters.forEach(lbsCluster -> {
            links.addClusterConnector(new LBSClusterSide(lbsCluster, Side.LEFT));
            links.addClusterConnector(new LBSClusterSide(lbsCluster, Side.RIGHT));
        });
        while (!links.isEmpty()) {
            Link<LBSClusterSide> link = links.getStrongestLink();
            link.mergeClusters();
            LBSCluster mergedCluster = link.getClusterConnector(0).getCluster();
            links.removeClusterConnector(link.getClusterConnector(0));
            links.removeClusterConnector(link.getClusterConnector(1));
            links.removeClusterConnector(link.getClusterConnector(0).getOtherSameRoot(links.getClusterConnectors()));
            links.removeClusterConnector(link.getClusterConnector(1).getOtherSameRoot(links.getClusterConnectors()));

            links.addClusterConnector(new LBSClusterSide(mergedCluster, Side.LEFT));
            links.addClusterConnector(new LBSClusterSide(mergedCluster, Side.RIGHT));
        }

        lbsClusters.get(0).tetrisHorizontalLanes();
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

    private List<LBSCluster> initLBSCluster(List<LegBusSet> legBusSets) {
        List<LBSCluster> lbsClusters = new ArrayList<>();
        int nb = 0;
        for (LegBusSet lbs : legBusSets) {
            new LBSCluster(lbsClusters, lbs, nb++);
        }
        return lbsClusters;
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
            for (ExternCell busCell : lbs.getEmbeddedCells().stream()
                    .filter(busCell -> busCell.getType() == Cell.CellType.EXTERN)
                    .map(ExternCell.class::cast)
                    .sorted(Comparator.comparingInt(ExternCell::getNumber))
                    .collect(Collectors.toList())) {
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
