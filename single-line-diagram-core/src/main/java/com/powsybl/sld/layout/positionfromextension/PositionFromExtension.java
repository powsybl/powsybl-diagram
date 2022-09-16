/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.positionfromextension;

import com.powsybl.sld.layout.*;
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
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public class PositionFromExtension implements PositionFinder {
    private static final Logger LOGGER = LoggerFactory.getLogger(PositionFromExtension.class);
    private static final Direction DEFAULTDIRECTION = Direction.TOP;
    private static final HorizontalBusLaneManager HBLMANAGER = new HBLaneManagerByExtension();

    /**
     * Builds the layout of the bus nodes, and organises cells (order and directions)
     */

    @Override
    public Map<BusNode, Integer> indexBusPosition(List<BusNode> busNodes) {
        Map<BusNode, Integer> busToNb = new HashMap<>();
        setMissingIndices(busNodes);
        List<BusNode> busNodesSorted = busNodes.stream()
                .sorted(Comparator.comparingInt(BusNode::getBusbarIndex).thenComparing(BusNode::getSectionIndex))
                .collect(Collectors.toList());
        int i = 1;
        for (BusNode busNode : busNodesSorted) {
            busToNb.put(busNode, i++);
        }
        return busToNb;
    }

    private static void setMissingIndices(List<BusNode> busNodes) {
        List<BusNode> missingIndicesBusNodes = busNodes.stream()
                .filter(busNode -> busNode.getBusbarIndex() <= 0 || busNode.getSectionIndex() <= 0)
                .collect(Collectors.toList());
        if (!missingIndicesBusNodes.isEmpty()) {
            int maxSectionIndex = busNodes.stream().mapToInt(BusNode::getSectionIndex).max().orElse(0);
            for (BusNode busNode : missingIndicesBusNodes) {
                LOGGER.warn("Incoherent position extension on busbar {} (busbar index: {}, section index: {}): setting busbar index to 1 and section index to {}",
                        busNode.getId(), busNode.getBusbarIndex(), busNode.getSectionIndex(), maxSectionIndex + 1);
                busNode.setBusBarIndexSectionIndex(1, ++maxSectionIndex);
            }
        }
    }

    @Override
    public LBSCluster organizeLegBusSets(VoltageLevelGraph graph, List<LegBusSet> legBusSets) {
        gatherLayoutExtensionInformation(graph);

        List<LBSCluster> lbsClusters = LBSCluster.createLBSClusters(
                legBusSets.stream().sorted(sortLBS).collect(Collectors.toList()));

        LBSCluster lbsCluster = lbsClusters.get(0);

        while (lbsClusters.size() != 1) {
            lbsCluster.merge(Side.RIGHT, lbsClusters.get(1), Side.LEFT, HBLMANAGER);
            lbsClusters.remove(1);
        }
        lbsCluster.sortHorizontalBusLanesByVPos();
        return lbsCluster;
    }

    private void gatherLayoutExtensionInformation(VoltageLevelGraph graph) {
        graph.getNodes().stream().filter(node -> node.getDirection() != Direction.UNDEFINED && graph.getCell(node).isPresent()).forEach(node -> {
            BusCell cell = (BusCell) graph.getCell(node).get();
            cell.setDirection(node.getDirection());
            node.getOrder().ifPresent(cell::setOrder);
        });
        graph.getBusCellStream().forEach(bc -> {
            bc.getNodes().stream().map(Node::getOrder)
                    .filter(Optional::isPresent)
                    .mapToInt(Optional::get)
                    .average()
                    .ifPresent(a -> bc.setOrder((int) Math.floor(a)));
            if (bc.getDirection() == Direction.UNDEFINED && bc.getType() == EXTERN) {
                bc.setDirection(DEFAULTDIRECTION);
            }
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

    private Comparator<LegBusSet> sortLBS = new Comparator<LegBusSet>() {
        @Override
        public int compare(LegBusSet lbs1, LegBusSet lbs2) {
            for (BusNode busNode : lbs1.getBusNodeSet()) {
                Optional<Integer> optionalSectionIndex2 = lbs2.getBusNodeSet().stream()
                        .filter(busNode2 -> busNode2.getBusbarIndex() == busNode.getBusbarIndex())
                        .findFirst().map(BusNode::getSectionIndex);
                if (optionalSectionIndex2.isPresent() && optionalSectionIndex2.get() != busNode.getSectionIndex()) {
                    return busNode.getSectionIndex() - optionalSectionIndex2.get();
                }
            }

            Optional<Integer> order1 = externCellOrderNb(lbs1);
            Optional<Integer> order2 = externCellOrderNb(lbs2);
            if (order1.isPresent() && order2.isPresent()) {
                return order1.get() - order2.get();
            }

            int h1max = getMaxPos(lbs1.getBusNodeSet(), BusNode::getSectionIndex);
            int h2max = getMaxPos(lbs2.getBusNodeSet(), BusNode::getSectionIndex);
            if (h1max != h2max) {
                return h1max - h2max;
            }

            int v1max = getMaxPos(lbs1.getBusNodeSet(), BusNode::getBusbarIndex);
            int v2max = getMaxPos(lbs2.getBusNodeSet(), BusNode::getBusbarIndex);
            if (v1max != v2max) {
                return v1max - v2max;
            }
            return lbs1.getBusNodeSet().size() - lbs2.getBusNodeSet().size();
        }

        private int getMaxPos(Set<BusNode> busNodes, Function<BusNode, Integer> fun) {
            return busNodes.stream()
                    .map(fun).max(Integer::compareTo).orElse(0);
        }

        private Optional<Integer> externCellOrderNb(LegBusSet lbs) {
            return lbs.getExternCells().stream().findAny().map(exCell -> exCell.getOrder().orElse(-1));
        }

    };
}
