/**
 * Copyright (c) 2021-2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.layout;

import com.powsybl.diagram.util.layout.Layout;
import com.powsybl.diagram.util.layout.geometry.LayoutContext;
import com.powsybl.diagram.util.layout.geometry.Vector2D;
import com.powsybl.diagram.util.layout.algorithms.BasicForceLayoutAlgorithm;
import com.powsybl.diagram.util.layout.algorithms.parameters.BasicForceLayoutParameters;
import com.powsybl.diagram.util.layout.postprocessing.PostProcessing;
import com.powsybl.diagram.util.layout.setup.SquareRandomBarycenterSetup;
import com.powsybl.nad.model.Edge;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.Node;
import com.powsybl.nad.model.Point;
import com.powsybl.nad.model.TextNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class BasicForceLayout extends AbstractLayout {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicForceLayout.class);

    private static final int SCALE = 100;

    private boolean repulsionForceFromFixedPoints = true;
    private boolean attractToCenterForce = true;
    private BasicForceLayoutParameters parameters = null;

    public BasicForceLayout() {
        //TODO replace this by a call to new BasicForceLayoutParameters.Builder().build() once both booleans are removed and maxSteps / timeout are not in the layoutParameters / BasicForceLayoutParamters
    }

    /**
     * @deprecated use {@link BasicForceLayout#BasicForceLayout(BasicForceLayoutParameters)} instead
     */
    @Deprecated(since = "5.3.0", forRemoval = true)
    BasicForceLayout(boolean repulsionForceFromFixedPoints, boolean attractToCenterForce) {
        this.repulsionForceFromFixedPoints = repulsionForceFromFixedPoints;
        this.attractToCenterForce = attractToCenterForce;
    }

    BasicForceLayout(BasicForceLayoutParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    protected void nodesLayout(Graph graph, LayoutParameters layoutParameters) {
        org.jgrapht.Graph<Node, Edge> jgraphtGraph = graph.getJgraphtGraph(layoutParameters.isTextNodesForceLayout());
        if (parameters == null) {
            parameters = new BasicForceLayoutParameters.Builder()
                    .withAttractToCenterEnabled(attractToCenterForce)
                    .withRepulsionFromFixedPointsEnabled(repulsionForceFromFixedPoints)
                    .withMaxSteps(layoutParameters.getMaxSteps())
                    .withTimeoutSeconds(layoutParameters.getTimeoutSeconds())
                    .build();
        } else {
            //TODO remove this once maxSteps and timeout are properly passed
            if (parameters.getMaxSteps() != layoutParameters.getMaxSteps()) {
                LOGGER.warn("The max steps of layoutParameters and BasicForceLayoutParameters are different, ignoring layoutParameters");
            }
            if (parameters.getTimeoutSeconds() != layoutParameters.getTimeoutSeconds()) {
                LOGGER.warn("The timeout of layoutParameters and BasicForceLayoutParameters are different, ignoring layoutParameters");
            }
        }
        Layout<Node, Edge> layout = new Layout<>(
                new SquareRandomBarycenterSetup<>(),
                new BasicForceLayoutAlgorithm<>(
                        parameters
                ),
                PostProcessing.noOp()
        );

        LayoutContext<Node, Edge> layoutContext = new LayoutContext<>(jgraphtGraph);

        setInitialPositions(layoutContext, graph);
        Set<Node> fixedNodes = getNodesWithFixedPosition().stream()
                .map(graph::getNode)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
        layoutContext.setFixedNodes(fixedNodes);

        layout.run(layoutContext);

        jgraphtGraph.vertexSet().forEach(node -> {
            Vector2D p = layoutContext.getStablePosition(node);
            if (node instanceof TextNode texNode) {
                texNode.setPosition(SCALE * p.getX(), SCALE * p.getY() - layoutParameters.getTextNodeEdgeConnectionYShift());
                texNode.setEdgeConnection(new Point(SCALE * p.getX(), SCALE * p.getY()));
            } else {
                node.setPosition(SCALE * p.getX(), SCALE * p.getY());
            }
        });

        if (!layoutParameters.isTextNodesForceLayout()) {
            graph.getTextEdgesMap().values().forEach(nodePair -> fixedTextNodeLayout(nodePair, layoutParameters));
        }
    }

    private void setInitialPositions(LayoutContext<Node, Edge> layoutContext, Graph graph) {
        Map<Node, com.powsybl.diagram.util.layout.geometry.Point> initialPoints = getInitialNodePositions().entrySet().stream()
                // Only accept positions for nodes in the graph
                .filter(nodePosition -> graph.getNode(nodePosition.getKey()).isPresent())
                .collect(Collectors.toMap(
                    nodePosition -> graph.getNode(nodePosition.getKey()).orElseThrow(),
                    nodePosition -> new com.powsybl.diagram.util.layout.geometry.Point(
                            nodePosition.getValue().x() / SCALE,
                            nodePosition.getValue().y() / SCALE)
                ));
        layoutContext.setInitialPoints(initialPoints);
    }
}
