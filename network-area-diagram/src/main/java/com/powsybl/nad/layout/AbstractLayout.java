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

    private Map<String, Point> movableNodesInitialPositions = Collections.emptyMap();
    private Map<String, Point> fixedNodesPositions = Collections.emptyMap();
    private final Map<String, TextPosition> textNodesWithFixedPosition = new HashMap<>();

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
    public Map<String, Point> getMovableNodesInitialPositions() {
        return movableNodesInitialPositions;
    }

    @Override
    public void setMovableNodesInitialPositions(Map<String, Point> initialNodePositions) {
        Objects.requireNonNull(initialNodePositions);
        this.movableNodesInitialPositions = initialNodePositions;
    }

    @Override
    public void setFixedNodesPositions(Map<String, Point> nodesWithFixedPosition) {
        this.fixedNodesPositions = nodesWithFixedPosition;
    }

    @Override
    public Map<String, Point> getFixedNodesPositions() {
        return fixedNodesPositions;
    }

    @Override
    public void setTextNodeFixedPosition(String voltageLevelId, Point topLeft, Point edgeConnection) {
        Objects.requireNonNull(voltageLevelId);
        Objects.requireNonNull(topLeft);
        Objects.requireNonNull(edgeConnection);
        textNodesWithFixedPosition.put(voltageLevelId, new TextPosition(topLeft, edgeConnection));
    }

    protected abstract void nodesLayout(Graph graph, LayoutParameters layoutParameters);

    protected void busNodesLayout(Graph graph) {
        Comparator<BusNode> c = Comparator.comparing(bn -> graph.getBusEdges(bn).size());
        graph.getVoltageLevelNodesStream().forEach(n -> {
            // We store the original position of each bus, to build the correct CSS class later
            List<BusNode> nodes = n.getBusNodes();
            for (int i = 0; i < nodes.size(); i++) {
                nodes.get(i).setBusIndex(i);
            }
            // We sort the buses to draw them later from less connections (center) to more connections (outer annulus)
            n.sortBusNodes(c);
            List<BusNode> sortedNodes = n.getBusNodes();
            for (int i = 0; i < sortedNodes.size(); i++) {
                BusNode busNode = sortedNodes.get(i);
                busNode.setRingIndex(i);
                busNode.setNbNeighbouringBusNodes(sortedNodes.size() - 1);
                busNode.setPosition(n.getPosition());
            }
        });
    }

    protected void fixedTextNodeLayout(Pair<VoltageLevelNode, TextNode> nodes, LayoutParameters layoutParameters) {
        TextPosition fixedTextPosition = textNodesWithFixedPosition.get(nodes.getFirst().getEquipmentId());
        Point textShift = fixedTextPosition != null ? fixedTextPosition.topLeftPosition() : layoutParameters.getTextNodeFixedShift();
        Point textPosition = nodes.getFirst().getPosition().shift(textShift.x(), textShift.y());
        Point connectionShift = fixedTextPosition != null ? fixedTextPosition.edgeConnection() :
                new Point(layoutParameters.getTextNodeFixedShift().x(), layoutParameters.getTextNodeFixedShift().y() + layoutParameters.getTextNodeEdgeConnectionYShift());
        Point edgeConnection = nodes.getFirst().getPosition().shift(connectionShift.x(), connectionShift.y());
        nodes.getSecond().setPosition(textPosition);
        nodes.getSecond().setEdgeConnection(edgeConnection);
    }

    protected void edgesLayout(Graph graph, LayoutParameters layoutParameters) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(layoutParameters);
        graph.getBranchEdgeStream().forEach(edge -> {
            VoltageLevelNode node1 = graph.getVoltageLevelNode(edge, BranchEdge.Side.ONE);
            VoltageLevelNode node2 = graph.getVoltageLevelNode(edge, BranchEdge.Side.TWO);
            setEdgeVisibility(node1, node2, edge, layoutParameters.isEdgesToInvisibleNodesDisplayed());
        });
    }

    private void setEdgeVisibility(VoltageLevelNode node1, VoltageLevelNode node2, BranchEdge branchEdge, boolean isEdgesToInvisibleNodesDisplayed) {
        boolean side1Visible = node1.isVisible() && (isEdgesToInvisibleNodesDisplayed || node2.isVisible());
        boolean side2Visible = node2.isVisible() && (isEdgesToInvisibleNodesDisplayed || node1.isVisible());
        branchEdge.setVisible(BranchEdge.Side.ONE, side1Visible);
        branchEdge.setVisible(BranchEdge.Side.TWO, side2Visible);
    }

    private void computeSize(Graph graph) {
        double[] dims = new double[] {Double.MAX_VALUE, -Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE};
        Stream.concat(graph.getTextNodesStream(), graph.getNodesStream()).forEach(node -> {
            dims[0] = Math.min(dims[0], node.getX());
            dims[1] = Math.max(dims[1], node.getX());
            dims[2] = Math.min(dims[2], node.getY());
            dims[3] = Math.max(dims[3], node.getY());
        });
        graph.setDimensions(dims[0], dims[1], dims[2], dims[3]);
    }
}
