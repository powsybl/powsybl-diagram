/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.Cell;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PositionVoltageLevelLayout implements VoltageLevelLayout {

    private static final Logger LOGGER = LoggerFactory.getLogger(PositionVoltageLevelLayout.class);

    private final Graph graph;

    public PositionVoltageLevelLayout(Graph graph) {
        this.graph = Objects.requireNonNull(graph);
    }

    /**
     * Calculate real coordinate of busNode and blocks connected to busbar
     */
    @Override
    public void run(LayoutParameters layoutParam) {
        LOGGER.info("Running voltage level layout");
        calculateBusNodeCoord(graph, layoutParam);
        calculateCellCoord(graph, layoutParam);
        graph.getNodes().stream()
                .filter(node -> node.getType() != Node.NodeType.BUS)
                .forEach(Node::finalizeCoord);
    }

    private void calculateBusNodeCoord(Graph graph, LayoutParameters layoutParam) {
        graph.getNodeBuses().forEach(nb -> nb.calculateCoord(layoutParam));
    }

    private void calculateCellCoord(Graph graph, LayoutParameters layoutParam) {
        graph.getCells().stream()
                .filter(cell -> cell.getType() == Cell.CellType.EXTERN
                        || cell.getType() == Cell.CellType.INTERN)
                .forEach(cell -> cell.calculateCoord(layoutParam));
        graph.getCells().stream()
                .filter(cell -> cell.getType() == Cell.CellType.SHUNT)
                .forEach(cell -> cell.calculateCoord(layoutParam));
    }
}
