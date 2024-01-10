/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.layout;

import com.powsybl.diagram.util.forcelayout.AbstractForceLayout;
import com.powsybl.diagram.util.forcelayout.Vector;
import com.powsybl.nad.model.Edge;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.Node;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public abstract class AbstractBasicForceLayout extends AbstractLayout {

    protected abstract int getScale();

    protected abstract AbstractForceLayout<Node, Edge> getForceLayoutAlgorithm(org.jgrapht.Graph<Node, Edge> jgraphtGraph, LayoutParameters layoutParameters);

    @Override
    protected void nodesLayout(Graph graph, LayoutParameters layoutParameters) {
        org.jgrapht.Graph<Node, Edge> jgraphtGraph = graph.getJgraphtGraph(layoutParameters.isTextNodesForceLayout());
        AbstractForceLayout<Node, Edge> forceLayout = getForceLayoutAlgorithm(jgraphtGraph, layoutParameters);
        forceLayout.setMaxSteps(layoutParameters.getMaxSteps());

        setInitialPositions(forceLayout, graph);
        Set<Node> fixedNodes = getNodesWithFixedPosition().stream()
                .map(graph::getNode)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
        forceLayout.setFixedNodes(fixedNodes);

        forceLayout.computePositions();

        int scale = getScale();
        jgraphtGraph.vertexSet().forEach(node -> {
            Vector p = forceLayout.getStablePosition(node);
            node.setPosition(scale * p.getX(), scale * p.getY());
        });

        if (!layoutParameters.isTextNodesForceLayout()) {
            graph.getTextEdgesMap().values().forEach(nodePair -> fixedTextNodeLayout(nodePair, layoutParameters));
        }
    }

    private void setInitialPositions(AbstractForceLayout<Node, Edge> forceLayout, Graph graph) {
        int scale = getScale();
        Map<Node, com.powsybl.diagram.util.forcelayout.Point> initialPoints = getInitialNodePositions().entrySet().stream()
                // Only accept positions for nodes in the graph
                .filter(nodePosition -> graph.getNode(nodePosition.getKey()).isPresent())
                .collect(Collectors.toMap(
                    nodePosition -> graph.getNode(nodePosition.getKey()).orElseThrow(),
                    nodePosition -> new com.powsybl.diagram.util.forcelayout.Point(
                            nodePosition.getValue().getX() / scale,
                            nodePosition.getValue().getY() / scale)
                ));
        forceLayout.setInitialPoints(initialPoints);
    }
}
