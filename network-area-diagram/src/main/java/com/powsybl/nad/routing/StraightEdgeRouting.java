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

import java.util.List;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class StraightEdgeRouting extends AbstractEdgeRouting {

    @Override
    protected void computeTextEdgeLayoutCoordinates(VoltageLevelNode node1, TextNode node2, TextEdge edge) {
        edge.setPoints(node1.getPosition(), node2.getEdgeConnection());
    }

    @Override
    protected void computeSingleBranchEdgeCoordinates(Graph graph, BranchEdge edge, SvgParameters svgParameters) {
        EdgeStarts edgeStarts = computeEdgeStarts(graph, edge, svgParameters);
        Point middle = Point.createMiddlePoint(edgeStarts.point1(), edgeStarts.point2());
        if (edge.isTransformerEdge()) {
            double radius = svgParameters.getTransformerCircleRadius();
            edge.setPoints1(edgeStarts.point1(), middle.atDistance(1.5 * radius, edgeStarts.direction2()));
            edge.setPoints2(edgeStarts.point2(), middle.atDistance(1.5 * radius, edgeStarts.direction1()));
        } else {
            edge.setPoints1(edgeStarts.point1(), middle);
            edge.setPoints2(edgeStarts.point2(), middle);
        }
    }

    @Override
    protected void computeMultiBranchEdgesCoordinates(Graph graph, List<BranchEdge> edges, SvgParameters svgParameters) {
        BranchEdge firstEdge = edges.iterator().next();
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

        int i = 0;
        for (BranchEdge edge : edges) {
            if (2 * i + 1 == nbForks) { // in the middle, hence alpha = 0
                computeSingleBranchEdgeCoordinates(graph, edge, svgParameters);
            } else {
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
            i++;
        }
    }

    private void computeHalfForkCoordinates(Graph graph, SvgParameters svgParameters, VoltageLevelNode node, BranchEdge edge, Point fork, Point middle, BranchEdge.Side side) {
        Node busNodeA = side == BranchEdge.Side.ONE ? graph.getBusGraphNode1(edge) : graph.getBusGraphNode2(edge);
        Point edgeStart = computeEdgeStart(busNodeA, fork, node, svgParameters);
        Point endFork = edge.isTransformerEdge()
                ? middle.atDistance(1.5 * svgParameters.getTransformerCircleRadius(), fork)
                : middle;
        edge.setPoints(side, edgeStart, fork, endFork);
    }

}
