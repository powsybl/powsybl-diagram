/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.position.predefined;

import com.powsybl.sld.layout.position.AbstractPositionFinder;
import com.powsybl.sld.layout.position.BSCluster;
import com.powsybl.sld.layout.position.VerticalBusSet;
import com.powsybl.sld.model.cells.*;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.model.coordinate.Direction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.powsybl.sld.model.cells.Cell.CellType.EXTERN;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 */
public class PositionPredefined extends AbstractPositionFinder {
    private static final Logger LOGGER = LoggerFactory.getLogger(PositionPredefined.class);

    private static final Direction DEFAULTDIRECTION = Direction.TOP;

    private static final Comparator<VerticalBusSet> VBSCOMPARATOR = new Comparator<VerticalBusSet>() {
        @Override
        public int compare(VerticalBusSet vbs1, VerticalBusSet vbs2) {
            for (BusNode busNode : vbs1.getBusNodeSet()) {
                Optional<Integer> optionalSectionIndex2 = vbs2.getBusNodeSet().stream()
                        .filter(busNode2 -> busNode2.getBusbarIndex() == busNode.getBusbarIndex())
                        .findFirst().map(BusNode::getSectionIndex);
                if (optionalSectionIndex2.isPresent() && optionalSectionIndex2.get() != busNode.getSectionIndex()) {
                    return busNode.getSectionIndex() - optionalSectionIndex2.get();
                }
            }

            Optional<Integer> order1 = externCellOrderNb(vbs1);
            Optional<Integer> order2 = externCellOrderNb(vbs2);
            if (order1.isPresent() && order2.isPresent()) {
                return order1.get() - order2.get();
            }

            int h1max = getMaxPos(vbs1.getBusNodeSet(), BusNode::getSectionIndex);
            int h2max = getMaxPos(vbs2.getBusNodeSet(), BusNode::getSectionIndex);
            if (h1max != h2max) {
                return h1max - h2max;
            }

            int v1max = getMaxPos(vbs1.getBusNodeSet(), BusNode::getBusbarIndex);
            int v2max = getMaxPos(vbs2.getBusNodeSet(), BusNode::getBusbarIndex);
            if (v1max != v2max) {
                return v1max - v2max;
            }
            return vbs1.getBusNodeSet().size() - vbs2.getBusNodeSet().size();
        }

        private int getMaxPos(Set<BusNode> busNodes, Function<BusNode, Integer> fun) {
            return busNodes.stream()
                    .map(fun).max(Integer::compareTo).orElse(0);
        }

        private Optional<Integer> externCellOrderNb(VerticalBusSet vbs) {
            return vbs.getExternCells().stream().findFirst().map(exCell -> exCell.getOrder().orElse(-1));
        }

    };

    /**
     * Builds the layout of the bus nodes, and organises cells (order and directions)
     */

    @Override
    public Map<BusNode, Integer> indexBusPosition(List<BusNode> busNodes, List<BusCell> busCells) {
        Map<BusNode, Integer> busToNb = new HashMap<>();
        setMissingPositionIndices(busNodes, busCells);
        List<BusNode> busNodesSorted = busNodes.stream()
                .sorted(Comparator.comparingInt(BusNode::getBusbarIndex).thenComparing(BusNode::getSectionIndex))
                .collect(Collectors.toList());
        int i = 1;
        for (BusNode busNode : busNodesSorted) {
            busToNb.put(busNode, i++);
        }
        return busToNb;
    }

    /**
     * Look for missing/incoherent position indices in given bus nodes, and replace them with an appropriate value.
     * A missing/incoherent position index means a zero or negative value for busbar index and/or section index.
     * @param busNodes all voltageLevelGraph bus nodes
     * @param busCells all voltageLevelGraph bus cells
     */
    private static void setMissingPositionIndices(List<BusNode> busNodes, List<BusCell> busCells) {
        List<BusNode> missingIndicesBusNodes = busNodes.stream()
                .filter(busNode -> busNode.getBusbarIndex() <= 0 || busNode.getSectionIndex() <= 0)
                .collect(Collectors.toList());
        if (!missingIndicesBusNodes.isEmpty()) {
            int maxSectionIndex = busNodes.stream().mapToInt(BusNode::getSectionIndex).max().orElse(0);
            for (BusNode busNode : missingIndicesBusNodes) {
                setMissingPositionIndices(busNode, busNodes, busCells, maxSectionIndex);
                maxSectionIndex = Math.max(maxSectionIndex, busNode.getSectionIndex());
            }
        }
    }

    /**
     * Replace position indices in given bus node with an appropriate value: either with the same section index as the
     * first busNode which shares a BusCell with, or if no such busNode with a new section index.
     * @param busNode bus node with a missing/incoherent position index/indices
     * @param busNodes all voltageLevelGraph bus nodes
     * @param busCells all voltageLevelGraph bus cells
     * @param maxSectionIndex up-to-date max section index
     */
    private static void setMissingPositionIndices(BusNode busNode, List<BusNode> busNodes, List<BusCell> busCells, int maxSectionIndex) {
        int newSectionIndex = maxSectionIndex + 1;
        int newBusbarIndex = 1;
        for (BusCell busCell : busCells) {
            List<BusNode> cellBusNodes = busCell.getBusNodes();
            if (cellBusNodes.contains(busNode)) {
                int section = cellBusNodes.stream().mapToInt(BusNode::getSectionIndex).max().getAsInt();
                if (section > 0) {
                    newSectionIndex = section;
                    newBusbarIndex = 1 + busNodes.stream()
                            .filter(bn -> bn.getSectionIndex() == section)
                            .mapToInt(BusNode::getBusbarIndex)
                            .max().orElse(0);
                    break;
                }
            }
        }

        LOGGER.warn("Incoherent position extension on busbar {} (busbar index: {}, section index: {}): setting busbar index to {} and section index to {}",
                busNode.getId(), busNode.getBusbarIndex(), busNode.getSectionIndex(), newBusbarIndex, newSectionIndex);
        busNode.setBusBarIndexSectionIndex(newBusbarIndex, newSectionIndex);
    }

    @Override
    public BSCluster organizeBusSets(VoltageLevelGraph graph, List<VerticalBusSet> verticalBusSets) {
        gatherLayoutExtensionInformation(graph);

        List<BSCluster> bsClusters = BSCluster.createBSClusters(
                verticalBusSets.stream().sorted(VBSCOMPARATOR).collect(Collectors.toList()));

        BSCluster bsCluster = bsClusters.get(0);

        while (bsClusters.size() != 1) {
            bsCluster.merge(Side.RIGHT, bsClusters.get(1), Side.LEFT, this);
            bsClusters.remove(1);
        }
        bsCluster.sortHblByVPos();
        return bsCluster;
    }

    private static void gatherLayoutExtensionInformation(VoltageLevelGraph graph) {

        graph.getBusCellStream().forEach(bc -> {
            setDirection(bc);
            setOrder(bc);
        });

        List<ExternCell> problematicCells = graph.getExternCellStream()
                .filter(cell -> cell.getOrder().isEmpty()).collect(Collectors.toList());
        if (!problematicCells.isEmpty()) {
            LOGGER.warn("Unable to build the layout only with Extension\nproblematic cells :");
            problematicCells.forEach(cell -> LOGGER
                    .info("Cell Nb : {}, Order : {}, Type : {}",
                            cell.getNumber(),
                            cell.getOrder().orElse(null),
                            cell.getType()));
        }
    }

    private static void setDirection(BusCell bc) {
        List<Direction> listOfDirectionsInsideCell = bc.getNodes().stream().map(Node::getDirection)
                .filter(d -> d != Direction.UNDEFINED).distinct().collect(Collectors.toList());
        int numberOfDirectionsInsideCell = listOfDirectionsInsideCell.size();
        if (numberOfDirectionsInsideCell == 0) {
            if (bc.getType() == EXTERN) {
                bc.setDirection(DEFAULTDIRECTION);
            }
            // The intern cells with undefined direction cannot be forced to a default position, as they are dealt with
            // later to avoid overlap, see BlockPositionner::manageInternCellOverlaps. This cannot be done now, as the
            // flat intern cells haven't been yet identified
        } else {
            bc.setDirection(listOfDirectionsInsideCell.get(0));
            if (numberOfDirectionsInsideCell > 1) {
                LOGGER.warn("Directions inside cell are not consistent: {} directions found instead of 1", numberOfDirectionsInsideCell);
            }
        }
    }

    private static void setOrder(BusCell bc) {
        bc.getNodes().stream().map(Node::getOrder)
                .filter(Optional::isPresent)
                .mapToInt(Optional::get)
                .min()
                .ifPresent(bc::setOrder);
    }

    @Override
    public void mergeHbl(BSCluster leftCluster, BSCluster rightCluster) {
        //for this implementation, the busBar structuralPosition are already defined,
        // we must ensure that structuralPosition vPos when merging left and right HorizontalPosition,
        // and structuralPosition hPos are ordered
        leftCluster.getHorizontalBusLists().forEach(hbl -> {
            BusNode rightNodeOfLeftHbl = hbl.getSideNode(Side.RIGHT);
            rightCluster.getHorizontalBusLists().stream()
                    .filter(hbl2 -> hbl2.getSideNode(Side.LEFT).getBusbarIndex() == rightNodeOfLeftHbl.getBusbarIndex())
                    .findFirst()
                    .ifPresent(rightHbl -> {
                        BusNode leftNodeOfRightHbl = rightHbl.getSideNode(Side.LEFT);
                        if (leftNodeOfRightHbl == rightNodeOfLeftHbl
                                || rightNodeOfLeftHbl.getSectionIndex() < leftNodeOfRightHbl.getSectionIndex()) {
                            hbl.merge(rightHbl);
                            rightCluster.removeHbl(rightHbl);
                        }
                    });
        });
        mergeHblWithNoLink(leftCluster, rightCluster);
    }

}
