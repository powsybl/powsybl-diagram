/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.position.clustering;

import com.powsybl.sld.layout.position.*;
import com.powsybl.sld.model.cells.BusCell;
import com.powsybl.sld.model.cells.Cell;
import com.powsybl.sld.model.cells.ExternCell;
import com.powsybl.sld.model.cells.InternCell;
import com.powsybl.sld.model.cells.ShuntCell;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.BusNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.powsybl.sld.model.coordinate.Direction.BOTTOM;
import static com.powsybl.sld.model.coordinate.Direction.TOP;
import static com.powsybl.sld.model.coordinate.Side.LEFT;
import static com.powsybl.sld.model.coordinate.Side.RIGHT;

/**
 * PositionByClustering finds adequate positions for the busBars with the following principles:
 * All the connections to the BusBar of the leg of an ExternCell, or each leg of an InternCell shall be stackable
 * (ie could be displayed with disconnectors to busbar vertically aligned).
 * This implies that the busBars of a leg shall be spread in different vertical structuralPosition,
 * (and in many case having the same horizontal structuralPosition).
 * The first step consists in building LegBusSets that contains busBars that shall be vertically aligned (considering
 * they have legs of cell that impose it).
 * Then BSClusters are initiated by building one BSCluster per LegBusSet.
 * The BSClusters are then merged 2 by 2 starting by BSClusters that have the strongest Link.
 * We differentiate the side of a BSCluster using BSClusterSide is considered. This is similar
 * to what si done with LegBusSet but the assessment of the strength of the link considers both sides of the
 * cluster.
 * Therefore, with cluster A and B, there are 4 BSClusterSide A-Right A-Left B-Right and B-Left. The links that
 * are considered are (A-Right, B-Left), (A-Right, B-Right), (B-Right, B-Left), (B-Right, B-Right). When merging,
 * alignment is required (meaning that clusters could be reversed to ensure the connection sides between the
 * 2 clusters are respected : 1st cluster-Right is merged with 2nd cluster-left).
 * </li>
 * </ul>
 *
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 */

// WE ASSUME THAT IT IS POSSIBLE TO STACK ALL CELLS AND BE ABLE TO ORGANIZE THE VOLTAGELEVEL ACCORDINGLY

public class PositionByClustering extends AbstractPositionFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(PositionByClustering.class);

    @Override
    public Map<BusNode, Integer> indexBusPosition(List<BusNode> busNodes, List<BusCell> busCells) {
        Map<BusNode, Integer> busToNb = new LinkedHashMap<>();
        int i = 1;
        for (BusNode n : busNodes.stream()
                .sorted(Comparator.comparing(BusNode::getId))
                .collect(Collectors.toList())) {
            busToNb.put(n, i);
            n.setBusBarIndexSectionIndex(0, 0);
            i++;
        }
        return busToNb;
    }

    public BSCluster organizeBusSets(VoltageLevelGraph graph, List<VerticalBusSet> verticalBusSets) {
        List<BSCluster> bsClusters = BSCluster.createBSClusters(verticalBusSets);
        Links links = new Links(bsClusters);
        while (!links.isEmpty()) {
            links.mergeLink(links.getStrongestLink());
        }
        BSCluster bsCluster = links.getFinalBsCluster();

        tetrisHorizontalBusLists(bsCluster);
        bsCluster.getHorizontalBusLists().forEach(hl -> LOGGER.info(hl.toString()));
        bsCluster.establishBusNodePosition();
        establishFeederPositions(bsCluster);

        return bsCluster;
    }

    private void tetrisHorizontalBusLists(BSCluster bsCluster) {
        List<HorizontalBusList> horizontalBusLists = bsCluster.getHorizontalBusLists();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("{}", horizontalBusLists);
        }
        List<HorizontalBusList> sortedHbl = horizontalBusLists.stream()
                .sorted(Comparator.comparingInt(HorizontalBusList::getStartingIndex)
                        .thenComparing(hl -> hl.getBusNodes().get(0).getId())) // cope with randomness
                .collect(Collectors.toList());
        int clusterLength = sortedHbl.stream()
                .mapToInt(HorizontalBusList::getEndingIndex)
                .max().orElse(0);
        int i = 0;
        while (i < sortedHbl.size()) {
            HorizontalBusList lane = sortedHbl.get(i);
            int actualMaxIndex = lane.getEndingIndex();
            while (actualMaxIndex < clusterLength) {
                int finalActualMax = actualMaxIndex;
                HorizontalBusList hblToAdd = sortedHbl.stream()
                        .filter(l -> l.getStartingIndex() >= finalActualMax)
                        .findFirst().orElse(null);
                if (hblToAdd != null) {
                    lane.merge(hblToAdd);
                    sortedHbl.remove(hblToAdd);
                    horizontalBusLists.remove(hblToAdd);
                    actualMaxIndex = lane.getEndingIndex();
                } else {
                    i++;
                    break;
                }
            }
            i++;
        }
    }

    private void establishFeederPositions(BSCluster bsCluster) {
        int cellOrder = 0;
        for (VerticalBusSet vbs : bsCluster.getVerticalBusSets()) {
            for (ExternCell cell : vbs.getExternCells()) {
                cell.setOrder(cellOrder++);
            }
        }
    }

    @Override
    public void organizeDirections(VoltageLevelGraph graph, List<Subsection> subsections) {

        int cellPos = 0;
        int cellShuntShift = 0;

        for (Subsection ss : subsections) {
            for (ExternCell externCell : ss.getExternCells()) {
                if (externCell.getShuntCells().stream().anyMatch(sc -> sc.getSideCell(RIGHT) == externCell)) {
                    cellShuntShift++; // an ExternCell on the right of a Shunt does not take its turn for flipping direction
                } else {
                    Direction direction = (cellPos + cellShuntShift) % 2 == 0 ? TOP : BOTTOM;
                    externCell.setDirection(direction);
                }
                cellPos++;
            }
        }

        Set<ShuntCell> visitedShuntCells = new HashSet<>();
        graph.getShuntCellStream().forEach(shuntCell -> {
            // starting from each shunt, find the shunt-connected set of extern cells to set the same direction for all of them
            List<ExternCell> externCells = new ArrayList<>();
            shuntTraversal(shuntCell, visitedShuntCells, externCells);
            externCells.stream().map(Cell::getDirection).filter(d -> d != Direction.UNDEFINED).findFirst()
                    .ifPresent(d -> externCells.forEach(externCell -> externCell.setDirection(d)));
        });
    }

    private void shuntTraversal(ShuntCell shuntCell, Set<ShuntCell> visitedShuntCells, List<ExternCell> externCells) {
        if (visitedShuntCells.contains(shuntCell)) {
            return;
        }

        visitedShuntCells.add(shuntCell);

        ExternCell leftCell = shuntCell.getSideCell(LEFT);
        ExternCell rightCell = shuntCell.getSideCell(RIGHT);
        externCells.add(leftCell);
        externCells.add(rightCell);

        Stream.of(leftCell, rightCell)
                .map(ExternCell::getShuntCells)
                .flatMap(Collection::stream)
                .filter(sc -> sc != shuntCell)
                .forEach(sc -> shuntTraversal(sc, visitedShuntCells, externCells));
    }

    public static void mergeHorizontalBusLists(BSCluster leftCluster, BSCluster rightCluster) {
        List<HorizontalBusList> availableHblToMerge = new ArrayList<>(leftCluster.getHorizontalBusLists());
        mergeHblWithCommonBusNode(leftCluster, rightCluster, availableHblToMerge);
        mergeHblWithFlatCell(leftCluster, rightCluster, availableHblToMerge);
        mergeHblWithNoLink(leftCluster, rightCluster);
    }

    private static void mergeHblWithCommonBusNode(BSCluster leftCluster, BSCluster rightCluster, List<HorizontalBusList> availableHblToMerge) {
        List<BusNode> commonNodes = new ArrayList<>(leftCluster.hblSideBuses(Side.RIGHT));
        commonNodes.retainAll(rightCluster.hblSideBuses(Side.LEFT));
        commonNodes.forEach(busNode ->
                finalizeHblBuilding(leftCluster, rightCluster, busNode, busNode, availableHblToMerge));
    }

    private static void mergeHblWithFlatCell(BSCluster leftCluster, BSCluster rightCluster,
                                        List<HorizontalBusList> availableHblToMerge) {
        List<BusNode> myAvailableRightBuses = BSCluster.hblSideBuses(Side.RIGHT, availableHblToMerge);
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
                                new BSClusterSide(leftCluster, Side.RIGHT),
                                new BSClusterSide(rightCluster, Side.LEFT)))
                        .thenComparing(InternCell::getFullId))
                .forEachOrdered(internCell -> {
                    Optional<BusNode> myNode = internCellNodeInHblSide(leftCluster, Side.RIGHT, internCell);
                    Optional<BusNode> otherNode = internCellNodeInHblSide(rightCluster, Side.LEFT, internCell);
                    if (myNode.isPresent() && otherNode.isPresent()) {
                        internCell.setFlat();
                        finalizeHblBuilding(leftCluster, rightCluster, myNode.get(), otherNode.get(), availableHblToMerge);
                    }
                });
    }

    private static Optional<BusNode> internCellNodeInHblSide(BSCluster bsCluster, Side side, InternCell cell) {
        List<BusNode> hblBuses = bsCluster.hblSideBuses(side);
        hblBuses.retainAll(cell.getBusNodes());
        return hblBuses.stream().findFirst();
    }

    private static void finalizeHblBuilding(BSCluster leftCluster, BSCluster rightCluster,
                                      BusNode myNode, BusNode otherBus, List<HorizontalBusList> availableHblToMerge) {
        Optional<HorizontalBusList> myHbl = leftCluster.getHblFromSideBus(myNode, Side.RIGHT);
        Optional<HorizontalBusList> otherHbl = rightCluster.getHblFromSideBus(otherBus, Side.LEFT);
        if (otherHbl.isPresent() && myHbl.isPresent()) {
            myHbl.get().merge(otherHbl.get());
            rightCluster.removeHbl(otherHbl.get());
            availableHblToMerge.remove(myHbl.get());
        }
    }
}
