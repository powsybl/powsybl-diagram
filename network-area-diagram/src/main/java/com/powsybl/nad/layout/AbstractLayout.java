/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.layout;

import com.powsybl.nad.model.*;
import org.jgrapht.alg.util.Pair;

import java.util.*;
import java.util.stream.Stream;

/**
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public abstract class AbstractLayout implements Layout {

    private Map<String, Point> initialNodePositions = Collections.emptyMap();
    private Set<String> nodesWithFixedPosition = Collections.emptySet();

    @Override
    public void run(Graph graph, LayoutParameters layoutParameters) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(layoutParameters);

        nodesLayout(graph, layoutParameters);
        busNodesLayout(graph);
        edgesLayout(graph, layoutParameters);

        computeSize(graph);
    }

    @Override
    public Map<String, Point> getInitialNodePositions() {
        return initialNodePositions;
    }

    @Override
    public void setInitialNodePositions(Map<String, Point> initialNodePositions) {
        Objects.requireNonNull(initialNodePositions);
        this.initialNodePositions = initialNodePositions;
    }

    @Override
    public void setNodesWithFixedPosition(Set<String> nodesWithFixedPosition) {
        this.nodesWithFixedPosition = nodesWithFixedPosition;
    }

    @Override
    public Set<String> getNodesWithFixedPosition() {
        return nodesWithFixedPosition;
    }

    public void setFixedNodePositions(Map<String, Point> fixedNodePositions) {
        setInitialNodePositions(fixedNodePositions);
        setNodesWithFixedPosition(fixedNodePositions.keySet());
    }

    protected abstract void nodesLayout(Graph graph, LayoutParameters layoutParameters);

    protected void busNodesLayout(Graph graph) {
        Comparator<BusNode> c = Comparator.comparing(bn -> graph.getBusEdges(bn).size());
        graph.getVoltageLevelNodesStream().forEach(n -> {
            n.sortBusNodes(c);
            List<BusNode> sortedNodes = n.getBusNodes();
            for (int i = 0; i < sortedNodes.size(); i++) {
                BusNode busNode = sortedNodes.get(i);
                busNode.setIndex(i);
                busNode.setNbNeighbouringBusNodes(sortedNodes.size() - 1);
                busNode.setPosition(n.getPosition());
            }
        });
    }

    protected void fixedTextNodeLayout(Pair<VoltageLevelNode, TextNode> nodes, LayoutParameters layoutParameters) {
        Point fixedShift = layoutParameters.getTextNodeFixedShift();
        Point textPos = nodes.getFirst().getPosition().shift(fixedShift.getX(), fixedShift.getY());
        nodes.getSecond().setPosition(textPos);
    }

    protected void edgesLayout(Graph graph, LayoutParameters layoutParameters) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(layoutParameters);
        graph.getBranchEdgeStream().forEach(edge -> {
            setEdgeVisibility(graph.getNode1(edge), edge, BranchEdge.Side.ONE);
            setEdgeVisibility(graph.getNode2(edge), edge, BranchEdge.Side.TWO);
        });
    }

    private void setEdgeVisibility(Node node, BranchEdge branchEdge, BranchEdge.Side side) {
        if (node instanceof VoltageLevelNode && !((VoltageLevelNode) node).isVisible()) {
            branchEdge.setVisible(side, false);
        }
    }

    private void computeSize(Graph graph) {
        double[] dims = new double[4];
        // FIXME(Luma) If the bounding box has minX, minY > 0
        //  it will return (0, 0, maxX, maxY) instead of (minX, minY, maxX, maxY)
        //  similar problem if maxX, maxY < 0
        Stream.concat(graph.getTextNodesStream(), graph.getNodesStream()).forEach(node -> {
            dims[0] = Math.min(dims[0], node.getX());
            dims[1] = Math.max(dims[1], node.getX());
            dims[2] = Math.min(dims[2], node.getY());
            dims[3] = Math.max(dims[3], node.getY());
        });
        graph.setDimensions(dims[0], dims[1], dims[2], dims[3]);
    }
}
