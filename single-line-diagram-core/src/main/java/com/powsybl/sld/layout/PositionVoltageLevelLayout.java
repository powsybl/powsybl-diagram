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
        if (layoutParam.isShiftFeedersPosition()) {
            graph.shiftFeedersPosition(layoutParam.getScaleShiftFeedersPosition());
        }
        calculateSize(graph, layoutParam);
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

    private void calculateSize(Graph graph, LayoutParameters layoutParam) {
        int maxH = graph.getNodeBuses().stream()
                .mapToInt(nodeBus -> nodeBus.getPosition().getH() + nodeBus.getPosition().getHSpan()).max().orElse(0);

        // TODO is this what we want ? for a simple diagram with 1 bus
        // and 1 cell with the default parameters, we get:
        // 0..20 20 px left margin
        // 20..70 50 px cell
        // 70..150 80 px right margin
        // It feels weird that the margin is not symmetrical (but right now it helps
        // because
        // the names of the feeders are often bigger dans the cells and overflowing on
        // the right side)
        double width = layoutParam.getInitialXBus() + (maxH + 2) * layoutParam.getCellWidth();

        int maxV = graph.getNodeBuses().stream()
                .mapToInt(nodeBus -> nodeBus.getPosition().getV() + nodeBus.getPosition().getVSpan()).max().orElse(0);

        // TODO this crops the feeder name
        double height = layoutParam.getInitialYBus() + layoutParam.getStackHeight() + layoutParam.getExternCellHeight()
                + layoutParam.getVerticalSpaceBus() * (maxV + 2);

        graph.setWidth(width);
        graph.setHeigth(height);
    }
}
