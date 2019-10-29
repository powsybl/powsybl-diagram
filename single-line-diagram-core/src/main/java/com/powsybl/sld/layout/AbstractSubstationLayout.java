/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.model.BusCell;
import com.powsybl.sld.model.BusCell.Direction;
import com.powsybl.sld.model.Coord;
import com.powsybl.sld.model.Edge;
import com.powsybl.sld.model.ExternCell;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.model.Side;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.model.TwtEdge;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public abstract class AbstractSubstationLayout implements SubstationLayout {

    protected SubstationGraph graph;
    protected VoltageLevelLayoutFactory vLayoutFactory;

    public AbstractSubstationLayout(SubstationGraph graph, VoltageLevelLayoutFactory vLayoutFactory) {
        this.graph = Objects.requireNonNull(graph);
        this.vLayoutFactory = Objects.requireNonNull(vLayoutFactory);
    }

    @Override
    public void run(LayoutParameters layoutParameters) {
        double graphX = layoutParameters.getHorizontalSubstationPadding();
        double graphY = layoutParameters.getVerticalSubstationPadding();

        for (Graph vlGraph : graph.getNodes()) {
            vlGraph.setX(graphX);
            vlGraph.setY(graphY);

            // Calculate the objects coordinates inside the voltageLevel graph
            VoltageLevelLayout vLayout = vLayoutFactory.create(vlGraph);
            vLayout.run(layoutParameters);

            // Calculate the coordinate of the voltageLevel graph inside the substation graph
            Coord posVLGraph = calculateCoordVoltageLevel(layoutParameters, vlGraph);

            graphX += posVLGraph.getX() + getHorizontalSubstationPadding(layoutParameters);
            graphY += posVLGraph.getY() + getVerticalSubstationPadding(layoutParameters);
        }

        // Determine points of the snakeLine
        Map<BusCell.Direction, Integer> nbSnakeLinesTopBottom = EnumSet.allOf(BusCell.Direction.class).stream().collect(Collectors.toMap(Function.identity(), v -> 0));
        Map<String, Integer> nbSnakeLinesBetween = graph.getNodes().stream().collect(Collectors.toMap(g -> g.getVoltageLevelId(), v -> 0));
        Map<Side, Integer> nbSnakeLinesLeftRight = EnumSet.allOf(Side.class).stream().collect(Collectors.toMap(Function.identity(), v -> 0));
        Map<String, Integer> nbSnakeLinesBottomVL = graph.getNodes().stream().collect(Collectors.toMap(g -> g.getVoltageLevelId(), v -> 0));
        Map<String, Integer> nbSnakeLinesTopVL = graph.getNodes().stream().collect(Collectors.toMap(g -> g.getVoltageLevelId(), v -> 0));

        for (TwtEdge edge : graph.getEdges()) {
            List<Double> pol = calculatePolylineSnakeLine(layoutParameters,
                    edge,
                    nbSnakeLinesTopBottom,
                    nbSnakeLinesLeftRight,
                    nbSnakeLinesBetween,
                    nbSnakeLinesBottomVL,
                    nbSnakeLinesTopVL);
            edge.setSnakeLine(pol);
        }
    }

    protected abstract Coord calculateCoordVoltageLevel(LayoutParameters layoutParameters, Graph vlGraph);

    protected abstract double getHorizontalSubstationPadding(LayoutParameters layoutParameters);

    protected abstract double getVerticalSubstationPadding(LayoutParameters layoutParameters);

    protected abstract List<Double> calculatePolylineSnakeLine(LayoutParameters layoutParameters, Edge edge,
            Map<Direction, Integer> nbSnakeLinesTopBottom, Map<Side, Integer> nbSnakeLinesLeftRight,
            Map<String, Integer> nbSnakeLinesBetween, Map<String, Integer> nbSnakeLinesBottomVL,
            Map<String, Integer> nbSnakeLinesTopVL);

    protected BusCell.Direction getNodeDirection(Node node, int nb) {
        if (node.getType() != Node.NodeType.FEEDER) {
            throw new PowsyblException("Node " + nb + " is not a feeder node");
        }
        BusCell.Direction dNode = node.getCell() != null ? ((ExternCell) node.getCell()).getDirection() : BusCell.Direction.TOP;
        if (dNode != BusCell.Direction.TOP && dNode != BusCell.Direction.BOTTOM) {
            throw new PowsyblException("Node " + nb + " cell direction not TOP or BOTTOM");
        }
        return dNode;
    }

}
