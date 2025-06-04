/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.layout;

import com.powsybl.diagram.util.forcelayout.LayoutAlgorithmRunner;
import com.powsybl.diagram.util.forcelayout.geometry.ForceGraph;
import com.powsybl.diagram.util.forcelayout.geometry.Point;
import com.powsybl.diagram.util.forcelayout.geometry.Vector2D;
import com.powsybl.diagram.util.forcelayout.layouts.parameters.Atlas2Parameters;
import com.powsybl.diagram.util.forcelayout.setup.SetupEnum;
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
    private static final int SCALE = 100;

    private final SetupEnum setupChoice;
    //maybe change the class name to not be confused with NAD LayoutParameters ?
    private final Atlas2Parameters<Node, Edge> atlas2Parameters;

    public Atlas2ForceLayout(SetupEnum setupChoice, Atlas2Parameters<Node, Edge> atlas2Parameters) {
        this.setupChoice = setupChoice;
        this.atlas2Parameters = atlas2Parameters;
    }

    public Atlas2ForceLayout() {
        this (SetupEnum.SIMPLE, new Atlas2Parameters.Builder().build());
    }

    @Override
    protected void nodesLayout(Graph graph, LayoutParameters layoutParameters) {
        ForceGraph<Node, Edge> forceGraph = new ForceGraph<>(graph.getJgraphtGraph(layoutParameters.isTextNodesForceLayout()));
        //this is not ideal as there are two places with the maxStep, ie LayoutParameters, and Atlas2Parameters
        LayoutAlgorithmRunner<Node, Edge> layoutAlgorithmRunner = new LayoutAlgorithmRunner<>(
            this.setupChoice,
            this.atlas2Parameters
        );
        setInitialPositions(graph, forceGraph);
        Set<Node> fixedNodes = getNodesWithFixedPosition().stream()
                .map(graph::getNode)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
        forceGraph.setFixedNodes(fixedNodes);

        layoutAlgorithmRunner.run(forceGraph);

        forceGraph.getSimpleGraph().vertexSet().forEach(node -> {
            Vector2D p = layoutAlgorithmRunner.getStablePosition(node);
            if (node instanceof TextNode texNode) {
                texNode.setPosition(SCALE * p.getX(), SCALE * p.getY() - layoutParameters.getTextNodeEdgeConnectionYShift());
                texNode.setEdgeConnection(new com.powsybl.nad.model.Point(SCALE * p.getX(), SCALE * p.getY()));
            } else {
                node.setPosition(SCALE * p.getX(), SCALE * p.getY());
            }
        });

        if (!layoutParameters.isTextNodesForceLayout()) {
            graph.getTextEdgesMap().values().forEach(nodePair -> fixedTextNodeLayout(nodePair, layoutParameters));
        }
    }

    /// Taken from BasicForceLayout, could be put in parent class but not exactly the same
    /// This would require to change BasicForceLayout to use LayoutAlgorithmRunner instead of ForceLayout
    private void setInitialPositions(Graph graph, ForceGraph<Node, Edge> forceGraph) {
        Map<Node, Point> initialPoints = getInitialNodePositions().entrySet().stream()
                // Only accept positions for nodes in the graph
                .filter(nodePosition -> graph.getNode(nodePosition.getKey()).isPresent())
                .collect(Collectors.toMap(
                        nodePosition -> graph.getNode(nodePosition.getKey()).orElseThrow(),
                        nodePosition -> new com.powsybl.diagram.util.forcelayout.geometry.Point(
                                nodePosition.getValue().getX() / SCALE,
                                nodePosition.getValue().getY() / SCALE)
                ));
        forceGraph.setInitialPoints(initialPoints);
    }
}

