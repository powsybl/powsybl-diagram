/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.layout;

import com.powsybl.diagram.util.layout.Layout;
import com.powsybl.diagram.util.layout.algorithms.Atlas2ForceLayoutAlgorithm;
import com.powsybl.diagram.util.layout.geometry.LayoutContext;
import com.powsybl.diagram.util.layout.geometry.Point;
import com.powsybl.diagram.util.layout.geometry.Vector2D;
import com.powsybl.diagram.util.layout.algorithms.parameters.Atlas2Parameters;
import com.powsybl.diagram.util.layout.setup.Setup;
import com.powsybl.diagram.util.layout.setup.SquareRandomSetup;
import com.powsybl.nad.model.Edge;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.Node;
import com.powsybl.nad.model.TextNode;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class Atlas2ForceLayout extends AbstractLayout {
    private static final double SCALE_COEFFICIENT = 190.757;
    private static final double SCALE_EXPONENT = -0.458;

    private final Setup<Node, Edge> setup;
    //maybe change the class name to not be confused with NAD LayoutParameters ?
    private final Atlas2Parameters atlas2Parameters;

    public Atlas2ForceLayout(Setup<Node, Edge> setup, Atlas2Parameters atlas2Parameters) {
        this.setup = setup;
        this.atlas2Parameters = atlas2Parameters;
    }

    public Atlas2ForceLayout() {
        this (new SquareRandomSetup<>(), new Atlas2Parameters.Builder().build());
    }

    @Override
    protected void nodesLayout(Graph graph, LayoutParameters layoutParameters) {
        LayoutContext<Node, Edge> layoutContext = new LayoutContext<>(graph.getJgraphtGraph(layoutParameters.isTextNodesForceLayout()));
        double scale = SCALE_COEFFICIENT * Math.pow(layoutContext.getSimpleGraph().vertexSet().size(), SCALE_EXPONENT);
        //TODO should we use the layoutParameters maxSteps to set Atlas2Parameters maxSteps ?
        Layout<Node, Edge> layoutAlgorithmRunner = new Layout<>(
            this.setup,
            new Atlas2ForceLayoutAlgorithm<>(this.atlas2Parameters)
        );
        setInitialPositions(graph, layoutContext, scale);
        Set<Node> fixedNodes = getNodesWithFixedPosition().stream()
                .map(graph::getNode)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
        layoutContext.setFixedNodes(fixedNodes);

        layoutAlgorithmRunner.run(layoutContext);

        layoutContext.getSimpleGraph().vertexSet().forEach(node -> {
            Vector2D p = layoutContext.getStablePosition(node);
            if (node instanceof TextNode texNode) {
                texNode.setPosition(scale * p.getX(), scale * p.getY() - layoutParameters.getTextNodeEdgeConnectionYShift());
                texNode.setEdgeConnection(new com.powsybl.nad.model.Point(scale * p.getX(), scale * p.getY()));
            } else {
                node.setPosition(scale * p.getX(), scale * p.getY());
            }
        });

        if (!layoutParameters.isTextNodesForceLayout()) {
            graph.getTextEdgesMap().values().forEach(nodePair -> fixedTextNodeLayout(nodePair, layoutParameters));
        }
    }

    /// Taken from BasicForceLayout, could be put in parent class but not exactly the same
    /// This would require to change BasicForceLayout to use LayoutAlgorithmRunner instead of ForceLayout
    private void setInitialPositions(Graph graph, LayoutContext<Node, Edge> layoutContext, double scale) {
        Map<Node, Point> initialPoints = getInitialNodePositions().entrySet().stream()
                // Only accept positions for nodes in the graph
                .filter(nodePosition -> graph.getNode(nodePosition.getKey()).isPresent())
                .collect(Collectors.toMap(
                        nodePosition -> graph.getNode(nodePosition.getKey()).orElseThrow(),
                        nodePosition -> new Point(
                                nodePosition.getValue().getX() / scale,
                                nodePosition.getValue().getY() / scale)
                ));
        layoutContext.setInitialPoints(initialPoints);
    }
}

