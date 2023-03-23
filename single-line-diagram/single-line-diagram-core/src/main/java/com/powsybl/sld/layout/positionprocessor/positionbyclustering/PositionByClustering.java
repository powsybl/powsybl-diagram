/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.positionprocessor.positionbyclustering;

import com.powsybl.sld.layout.*;
import com.powsybl.sld.layout.positionprocessor.AbstractPositionFinder;
import com.powsybl.sld.layout.positionprocessor.BSCluster;
import com.powsybl.sld.layout.positionprocessor.HorizontalBusSet;
import com.powsybl.sld.layout.positionprocessor.Subsection;
import com.powsybl.sld.layout.positionprocessor.VerticalBusSet;
import com.powsybl.sld.model.cells.BusCell;
import com.powsybl.sld.model.cells.Cell;
import com.powsybl.sld.model.cells.ExternCell;
import com.powsybl.sld.model.cells.ShuntCell;
import com.powsybl.sld.model.coordinate.Direction;
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
 * Then VBSClusters are initiated by building one VBSCluster per LegBusSet.
 * The VBSClusters are then merged 2 by 2 starting by VBSclusters that have the strongest Link.
 * Two strategies of strength assessment of the links between clusters are implemented:
 * <ul>
 * <li>
 * if useVbsLinkOnly is true: the strength between LegBusSets is considered: this means that the strength of
 * the link between two clusters is the one of the strongest link between two LegBusSets (one per cluster). This
 * is a simple implementation that is limited as it it does not consider the difference between the side of a
 * cluster: if two clusters A and B are to be merged, the result can either be A-B or B-A.
 * </li>
 * <li>
 * if useVbsLinkOnly is false: the strength between VbsClusterSide is considered. This is similar
 * to what si done with LegBusSet but the assessment of the strength of the link considers both sides of the
 * cluster.
 * Therefore, with cluster A and B, there are 4 VbsClusterSide A-Right A-Left B-Right and B-Left. The links that
 * are considered are (A-Right, B-Left), (A-Right, B-Right), (B-Right, B-Left), (B-Right, B-Right). When merging,
 * alignment is required (meaning that clusters could be reversed to ensure the connection sides between the
 * 2 clusters are respected : 1st cluster-Right is merged with 2nd cluster-left).
 * </li>
 * </ul>
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */

// WE ASSUME THAT IT IS POSSIBLE TO STACK ALL CELLS AND BE ABLE TO ORGANIZE THE VOLTAGELEVEL ACCORDINGLY

public class PositionByClustering extends AbstractPositionFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(PositionByClustering.class);
    private static final HBSManagerByClustering HBSMANAGER = new HBSManagerByClustering();

    @Override
    public Map<BusNode, Integer> indexBusPosition(List<BusNode> busNodes, List<BusCell> busCells) {
        Map<BusNode, Integer> busToNb = new LinkedHashMap<>();
        int i = 1;
        for (BusNode n : busNodes.stream()
                .sorted(Comparator.comparing(BusNode::getId))
                .collect(Collectors.toList())) {
            busToNb.put(n, i);
            i++;
        }
        return busToNb;
    }

    public BSCluster organizeBusSets(VoltageLevelGraph graph, List<VerticalBusSet> verticalBusSets) {
        List<BSCluster> bsClusters = BSCluster.createBSClusters(verticalBusSets);
        Links links = Links.create(bsClusters, HBSMANAGER);
        while (!links.isEmpty()) {
            links.mergeLink(links.getStrongestLink());
        }
        BSCluster bsCluster = links.getFinalBsCluster();

        tetrisHorizontalBusSets(bsCluster);
        bsCluster.getHorizontalBusSets().forEach(hl -> LOGGER.info(hl.toString()));
        bsCluster.establishBusNodePosition();
        establishFeederPositions(bsCluster);

        return bsCluster;
    }

    private void tetrisHorizontalBusSets(BSCluster bsCluster) {
        List<HorizontalBusSet> horizontalBusSets = bsCluster.getHorizontalBusSets();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("{}", horizontalBusSets);
        }
        List<HorizontalBusSet> sortedHbs = horizontalBusSets.stream()
                .sorted(Comparator.comparingInt(HorizontalBusSet::getStartingIndex)
                        .thenComparing(hl -> hl.getBusNodes().get(0).getId())) // cope with randomness
                .collect(Collectors.toList());
        int clusterLength = sortedHbs.stream()
                .mapToInt(HorizontalBusSet::getEndingIndex)
                .max().orElse(0);
        int i = 0;
        while (i < sortedHbs.size()) {
            HorizontalBusSet lane = sortedHbs.get(i);
            int actualMaxIndex = lane.getEndingIndex();
            while (actualMaxIndex < clusterLength) {
                int finalActualMax = actualMaxIndex;
                HorizontalBusSet hbsToAdd = sortedHbs.stream()
                        .filter(l -> l.getStartingIndex() >= finalActualMax)
                        .findFirst().orElse(null);
                if (hbsToAdd != null) {
                    lane.merge(hbsToAdd);
                    sortedHbs.remove(hbsToAdd);
                    horizontalBusSets.remove(hbsToAdd);
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
        for (VerticalBusSet vbs : bsCluster.getVerticalBusSetList()) {
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

}
