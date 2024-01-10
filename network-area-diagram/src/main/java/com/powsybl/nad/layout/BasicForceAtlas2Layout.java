/**
 Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.layout;

import com.powsybl.diagram.util.forcelayout.ForceAtlas2Layout;
import com.powsybl.diagram.util.forcelayout.Vector;
import com.powsybl.nad.model.Edge;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.Node;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class BasicForceAtlas2Layout extends AbstractLayout {

    private static final int SCALE = 20;

    @Override
    protected void nodesLayout(Graph graph, LayoutParameters layoutParameters) {
        org.jgrapht.Graph<Node, Edge> jgraphtGraph = graph.getJgraphtGraph(layoutParameters.isTextNodesForceLayout());
        ForceAtlas2Layout<Node, Edge> forceLayout = new ForceAtlas2Layout<>(jgraphtGraph);
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
            node.setPosition(SCALE * p.getX(), SCALE * p.getY());
        });

        if (!layoutParameters.isTextNodesForceLayout()) {
            graph.getTextEdgesMap().values().forEach(nodePair -> fixedTextNodeLayout(nodePair, layoutParameters));
        }
    }

    private void setInitialPositions(ForceAtlas2Layout<Node, Edge> forceLayout, Graph graph) {
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
