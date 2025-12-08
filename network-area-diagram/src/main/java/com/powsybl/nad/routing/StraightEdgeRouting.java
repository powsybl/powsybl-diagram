/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.routing;

import com.powsybl.nad.model.*;
import com.powsybl.nad.svg.SvgParameters;
import com.powsybl.nad.utils.RadiusUtils;

import java.util.List;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class StraightEdgeRouting extends AbstractEdgeRouting {

    @Override
    protected void computeTextEdgeLayoutCoordinates(VoltageLevelNode voltageLevelNode, TextNode textNode, TextEdge edge, SvgParameters svgParameters) {
        double circleRadius = RadiusUtils.getVoltageLevelCircleRadius(voltageLevelNode, svgParameters);
        Point textNodeConnection = textNode.getEdgeConnection();
        Point vlPoint = voltageLevelNode.getPosition().atDistance(circleRadius, textNodeConnection);
        edge.setPoints(vlPoint, textNodeConnection);
    }

    @Override
    protected void computeSingleBranchEdgeCoordinates(Graph graph, BranchEdge edge, SvgParameters svgParameters) {
        Node node1 = graph.getBusGraphNode1(edge);
        Node node2 = graph.getBusGraphNode2(edge);
        VoltageLevelNode voltageLevelNode1 = graph.getVoltageLevelNode1(edge);
        VoltageLevelNode voltageLevelNode2 = graph.getVoltageLevelNode2(edge);

        Point direction1 = getDirection(node2, voltageLevelNode2);
        Point edgeStart1 = computeEdgeStart(node1, direction1, graph.getVoltageLevelNode1(edge), svgParameters);

        Point direction2 = getDirection(node1, voltageLevelNode1);
        Point edgeStart2 = computeEdgeStart(node2, direction2, graph.getVoltageLevelNode2(edge), svgParameters);

        Point middle = Point.createMiddlePoint(edgeStart1, edgeStart2);
        if (edge.isTransformerEdge()) {
            double radius = svgParameters.getTransformerCircleRadius();
            edge.setPoints1(edgeStart1, middle.atDistance(1.5 * radius, direction2));
            edge.setPoints2(edgeStart2, middle.atDistance(1.5 * radius, direction1));
        } else {
            edge.setPoints1(edgeStart1, middle);
            edge.setPoints2(edgeStart2, middle);
        }

        edge.setArrowPoint1(getArrowCenter(voltageLevelNode1, (BusNode) node1, edge.getPoints1(), svgParameters));
        edge.setArrowPoint2(getArrowCenter(voltageLevelNode2, (BusNode) node2, edge.getPoints2(), svgParameters));
        for (BranchEdge.Side side : BranchEdge.Side.values()) {
            edge.setArrowAngle(side, edge.getEdgeStartAngle(side));
        }
    }

    protected Point getDirection(Node directionBusGraphNode, Node vlNode) {
        if (directionBusGraphNode == BusNode.UNKNOWN) {
            return vlNode.getPosition();
        }
        return directionBusGraphNode.getPosition();
    }

    @Override
    protected void computeMultiBranchEdgesCoordinates(Graph graph, List<BranchEdge> edges, SvgParameters svgParameters) {
        BranchEdge firstEdge = edges.getFirst();
        VoltageLevelNode nodeA = graph.getVoltageLevelNode1(firstEdge);
        VoltageLevelNode nodeB = graph.getVoltageLevelNode2(firstEdge);
        Point pointA = nodeA.getPosition();
        Point pointB = nodeB.getPosition();

        double dx = pointB.getX() - pointA.getX();
        double dy = pointB.getY() - pointA.getY();
        double angle = Math.atan2(dy, dx);

        int nbForks = edges.size();
        double forkAperture = Math.toRadians(svgParameters.getEdgesForkAperture());
        double forkLength = svgParameters.getEdgesForkLength();
        double angleStep = forkAperture / (nbForks - 1);

        for (int i = 0; i < edges.size(); i++) {
            BranchEdge edge = edges.get(i);

            double alpha = -forkAperture / 2 + i * angleStep;
            double angleForkA = angle - alpha;
            double angleForkB = angle + Math.PI + alpha;

            Point forkA = pointA.shift(forkLength * Math.cos(angleForkA), forkLength * Math.sin(angleForkA));
            Point forkB = pointB.shift(forkLength * Math.cos(angleForkB), forkLength * Math.sin(angleForkB));
            Point middle = Point.createMiddlePoint(forkA, forkB);
            BranchEdge.Side sideA = graph.getNode1(edge) == nodeA ? BranchEdge.Side.ONE : BranchEdge.Side.TWO;

            computeHalfForkCoordinates(graph, svgParameters, nodeA, edge, forkA, middle, sideA);
            computeHalfForkCoordinates(graph, svgParameters, nodeB, edge, forkB, middle, sideA.getOpposite());
        }
    }

    private void computeHalfForkCoordinates(Graph graph, SvgParameters svgParameters, VoltageLevelNode node, BranchEdge edge, Point fork, Point middle, BranchEdge.Side side) {
        Node busNodeA = side == BranchEdge.Side.ONE ? graph.getBusGraphNode1(edge) : graph.getBusGraphNode2(edge);
        Point edgeStart = computeEdgeStart(busNodeA, fork, node, svgParameters);
        Point endFork = edge.isTransformerEdge()
                ? middle.atDistance(1.5 * svgParameters.getTransformerCircleRadius(), fork)
                : middle;
        edge.setPoints(side, edgeStart, fork, endFork);
        edge.setArrow(side, fork.atDistance(svgParameters.getArrowShift(), endFork));
        edge.setArrowAngle(side, edge.getEdgeEndAngle(side));
    }
}
