/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.layout;

import com.powsybl.diagram.util.forcelayout.ForceLayout;
import com.powsybl.diagram.util.forcelayout.Vector;
import com.powsybl.nad.model.Edge;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.Node;
import com.powsybl.nad.model.Point;
import com.powsybl.nad.model.TextNode;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class BasicForceLayout extends AbstractLayout {

    private static final int SCALE = 100;

    private final boolean repulsionForceFromFixedPoints;
    private final boolean attractToCenterForce;

    public BasicForceLayout() {
        this(true, true);
    }

    BasicForceLayout(boolean repulsionForceFromFixedPoints, boolean attractToCenterForce) {
        this.repulsionForceFromFixedPoints = repulsionForceFromFixedPoints;
        this.attractToCenterForce = attractToCenterForce;
    }

    @Override
    protected void nodesLayout(Graph graph, LayoutParameters layoutParameters) {
        org.jgrapht.Graph<Node, Edge> jgraphtGraph = graph.getJgraphtGraph(layoutParameters.isTextNodesForceLayout());
        ForceLayout<Node, Edge> forceLayout = new ForceLayout<>(jgraphtGraph)
                .setAttractToCenterForce(attractToCenterForce)
                .setRepulsionForceFromFixedPoints(repulsionForceFromFixedPoints);
        forceLayout.setSpringRepulsionFactor(layoutParameters.getSpringRepulsionFactorForceLayout());
        forceLayout.setMaxSteps(layoutParameters.getMaxSteps());

        setInitialPositions(forceLayout, graph);
        Set<Node> fixedNodes = getNodesWithFixedPosition().stream()
                .map(graph::getNode)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
        forceLayout.setFixedNodes(fixedNodes);

        forceLayout.execute();

        jgraphtGraph.vertexSet().forEach(node -> {
            Vector p = forceLayout.getStablePosition(node);
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

    private void setInitialPositions(ForceLayout<Node, Edge> forceLayout, Graph graph) {
        Map<Node, com.powsybl.diagram.util.forcelayout.Point> initialPoints = getInitialNodePositions().entrySet().stream()
                // Only accept positions for nodes in the graph
                .filter(nodePosition -> graph.getNode(nodePosition.getKey()).isPresent())
                .collect(Collectors.toMap(
                    nodePosition -> graph.getNode(nodePosition.getKey()).orElseThrow(),
                    nodePosition -> new com.powsybl.diagram.util.forcelayout.Point(
                            nodePosition.getValue().getX() / SCALE,
                            nodePosition.getValue().getY() / SCALE)
                ));
        forceLayout.setInitialPoints(initialPoints);
    }
}
