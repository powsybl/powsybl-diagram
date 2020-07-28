/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.positionfromextension;

import com.powsybl.sld.layout.HorizontalBusLaneManager;
import com.powsybl.sld.layout.LBSCluster;
import com.powsybl.sld.layout.LegBusSet;
import com.powsybl.sld.layout.PositionFinder;
import com.powsybl.sld.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PositionFromExtension implements PositionFinder {
    private static final Logger LOGGER = LoggerFactory.getLogger(PositionFromExtension.class);
    private static final BusCell.Direction DEFAULTDIRECTION = BusCell.Direction.TOP;
    private static final HorizontalBusLaneManager HBLMANAGER = new HBLaneManagerByExtension();

    /**
     * Builds the layout of the bus nodes, and organises cells (order and directions)
     */

    @Override
    public Map<BusNode, Integer> indexBusPosition(List<BusNode> busNodes) {
        Map<BusNode, Integer> busToNb = new HashMap<>();
        int i = 1;
        for (BusNode busNode : busNodes.stream()
                .sorted((bn1, bn2) -> {
                    Position p1 = bn1.getStructuralPosition();
                    Position p2 = bn2.getStructuralPosition();
                    return p1.getV() == p2.getV() ? p1.getH() - p2.getH() : p1.getV() - p2.getV();
                })
                .collect(Collectors.toList())) {
            busToNb.put(busNode, i++);
        }
        return busToNb;
    }

    @Override
    public LBSCluster organizeLegBusSets(List<LegBusSet> legBusSets) {
        Graph graph = legBusSets.get(0).getBusNodeSet().iterator().next().getGraph();
        gatherLayoutExtensionInformation(graph);

        List<LBSCluster> lbsClusters = LBSCluster.createLBSClusters(
                legBusSets.stream().sorted(sortLBS).collect(Collectors.toList()));

        while (lbsClusters.size() != 1) {
            lbsClusters.get(0).merge(Side.RIGHT, lbsClusters.get(1), Side.LEFT, HBLMANAGER);
            lbsClusters.remove(1);
        }
        return lbsClusters.get(0);
    }

    private void gatherLayoutExtensionInformation(Graph graph) {
        graph.getNodes().stream()
                .filter(node -> node.getType() == Node.NodeType.FEEDER)
                .map(FeederNode.class::cast)
                .forEach(feederNode -> {
                    ExternCell cell = (ExternCell) feederNode.getCell();
                    cell.setDirection(
                            feederNode.getDirection() == BusCell.Direction.UNDEFINED ? DEFAULTDIRECTION : feederNode.getDirection());
                    cell.setOrder(feederNode.getOrder());
                });
        graph.getCells().stream().filter(cell -> cell.getType() == Cell.CellType.EXTERN).map(ExternCell.class::cast)
                .forEach(ExternCell::orderFromFeederOrders);

        List<ExternCell> problematicCells = graph.getCells().stream()
                .filter(cell -> cell.getType().equals(Cell.CellType.EXTERN))
                .map(ExternCell.class::cast)
                .filter(cell -> cell.getOrder() == -1).collect(Collectors.toList());
        if (!problematicCells.isEmpty()) {
            LOGGER.warn("Unable to build the layout only with Extension\nproblematic cells :");
            problematicCells.forEach(cell -> LOGGER
                    .info("Cell Nb : {}, Order : {}, Type : {}",
                            cell.getNumber(),
                            cell.getOrder(),
                            cell.getType()));
        }
    }

    private Comparator<LegBusSet> sortLBS = new Comparator<LegBusSet>() {
        @Override
        public int compare(LegBusSet lbs1, LegBusSet lbs2) {
            int order1 = externCellOrderNb(lbs1);
            int order2 = externCellOrderNb(lbs2);
            if (order1 != -1 && order2 != -1) {
                return order1 - order2;
            }
            for (BusNode busNode : lbs1.getBusNodeSet()) {
                final Position pos1 = busNode.getStructuralPosition();
                Optional<Position> pos2 = lbs2.getBusNodeSet().stream().map(BusNode::getStructuralPosition)
                        .filter(p -> p.getV() == pos1.getV()).findFirst();
                if (pos2.isPresent() && pos2.get().getH() != pos1.getH()) {
                    return pos1.getH() - pos2.get().getH();
                }
            }
            return lbs1.getBusNodeSet().size() - lbs2.getBusNodeSet().size();
        }

        private int externCellOrderNb(LegBusSet lbs) {
            Set<ExternCell> cells = lbs.getExternCells();
            if (cells.isEmpty()) {
                return -1;
            } else {
                return cells.iterator().next().getOrder();
            }
        }
    };
}
