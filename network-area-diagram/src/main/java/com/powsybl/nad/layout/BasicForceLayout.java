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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class BasicForceLayout extends AbstractLayout {

    public static final int SCALE = 100;
    private static final int PREFERRED_NODE_SIZE = 2 * 30; // svg parameters voltageLevelCircleRadius

    @Override
    protected void nodesLayout(Graph graph, LayoutParameters layoutParameters) {
        org.jgrapht.Graph<Node, Edge> jgraphtGraph = graph.getJgraphtGraph(layoutParameters.isTextNodesForceLayout());
        ForceLayout<Node, Edge> forceLayout = new ForceLayout<>(jgraphtGraph);
        forceLayout.setSpringRepulsionFactor(layoutParameters.getSpringRepulsionFactorForceLayout());

        setInitialPositions(forceLayout, graph);
        Set<Node> fixedNodes = getNodesWithFixedPosition().stream()
                .map(graph::getNode)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
        forceLayout.setFixedNodes(fixedNodes);

        forceLayout.execute();

        if (layoutParameters.xxxIsFixedScale()) {
            jgraphtGraph.vertexSet().forEach(node -> {
                Vector p = forceLayout.getStablePosition(node);
                node.setPosition(SCALE * p.getX(), SCALE * p.getY());
            });
        } else {
            Collection<Node> laidOutNodes = jgraphtGraph.vertexSet();
            updatePositions(forceLayout, laidOutNodes);
            scaleToPreferedNodeSize(laidOutNodes);
        }

        if (!layoutParameters.isTextNodesForceLayout()) {
            graph.getTextEdgesMap().values().forEach(nodePair -> fixedTextNodeLayout(nodePair, layoutParameters));
        }
    }

    private void updatePositions(ForceLayout<Node, Edge> forceLayout, Collection<Node> nodes) {
        nodes.forEach(node -> {
            Vector p = forceLayout.getStablePosition(node);
            node.setPosition(p.getX(), p.getY());
        });
    }

    private static final class Dimensions {
        final double width;
        final double height;

        private Dimensions(double w, double h) {
            this.width = w;
            this.height = h;
        }

        private static double min(double v0, double v1) {
            return Double.isNaN(v0) ? v1 : Math.min(v0, v1);
        }

        private static double max(double v0, double v1) {
            return Double.isNaN(v0) ? v1 : Math.max(v0, v1);
        }

        static Dimensions ofBoundingBox(Collection<Node> nodes) {
            Double[] m = {Double.NaN, Double.NaN, Double.NaN, Double.NaN};
            nodes.forEach(node -> {
                m[0] = min(m[0], node.getX());
                m[1] = max(m[1], node.getX());
                m[2] = min(m[2], node.getY());
                m[3] = max(m[3], node.getY());
            });
            return new Dimensions(m[1] - m[0], m[3] - m[2]);
        }
    }

    private void scaleToPreferedNodeSize(Collection<Node> nodes) {
        // Node size is 20% of the space reserved for each node
        double preferredNodeSpace = 5.0 * PREFERRED_NODE_SIZE;

        Dimensions dimensions = Dimensions.ofBoundingBox(nodes);
        double w = dimensions.width;
        double h = dimensions.height;
        long nodeCount = nodes.size();
        double nodeCountY = Math.sqrt(nodeCount * h / w);
        double nodeCountX = w / h * nodeCountY;

        LOG.info("scale to preferred node size");
        LOG.info("inputs:");
        LOG.info("  nodeSize  = {}", PREFERRED_NODE_SIZE);
        LOG.info("  nodeSpace = {}", preferredNodeSpace);
        LOG.info("  nodeCount = {}", nodeCount);
        LOG.info("  w, h      = {}, {}", w, h);
        LOG.info("calculations:");
        LOG.info("  Nx, Ny    = {}, {}", nodeCountX, nodeCountY);
        LOG.info("  layout nodeSpace");
        double sx = w / Math.sqrt(nodeCountX);
        double sy = h / Math.sqrt(nodeCountY);
        LOG.info("            = {}, {}", sx, sy);
        LOG.info("results:");
        double scalex = preferredNodeSpace * nodeCountX / w;
        double scaley = preferredNodeSpace * nodeCountY / h;
        LOG.info("  scaling f = {} == {}", scalex, scaley);
        LOG.info("  w', h'    = {}, {}", w * scalex, h * scaley);

        double f = preferredNodeSpace * nodeCountX / w;
        nodes.forEach(node -> {
            Point p = node.getPosition();
            node.setPosition(f * p.getX(), f * p.getY());
        });
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

    private static final Logger LOG = LoggerFactory.getLogger(BasicForceLayout.class);
}
