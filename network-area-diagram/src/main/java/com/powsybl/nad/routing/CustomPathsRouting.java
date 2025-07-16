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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class CustomPathsRouting extends StraightEdgeRouting {
    private final Map<String, List<Point>> customEdgePaths;
    private final Map<String, List<Point>> customTextPaths;

    public CustomPathsRouting(Map<String, List<Point>> customEdgePaths, Map<String, List<Point>> customTextPaths) {
        this.customEdgePaths = customEdgePaths;
        this.customTextPaths = customTextPaths;
    }

    @Override
    protected void computeSingleBranchEdgeCoordinates(Graph graph, BranchEdge edge, SvgParameters svgParameters) {
        List<Point> customPoints = customEdgePaths.getOrDefault(edge.getEquipmentId(), List.of());
        if (customPoints.isEmpty()) {
            super.computeSingleBranchEdgeCoordinates(graph, edge, svgParameters);
            return;
        }

        EdgeStarts edgeStarts = computeEdgeStarts(graph, edge, svgParameters);
        List<Point> allPoints = new ArrayList<>();
        allPoints.add(edgeStarts.point1());
        allPoints.addAll(customPoints);
        allPoints.add(edgeStarts.point2());

        double[] cumulatedDistance = computeCumulatedDistances(allPoints);
        int iStartMiddlePath = computeIndexMiddlePath(cumulatedDistance);

        double totalDistance = cumulatedDistance[cumulatedDistance.length - 1];
        double distToMiddle = totalDistance / 2 - cumulatedDistance[iStartMiddlePath];
        Point middle = allPoints.get(iStartMiddlePath).atDistance(distToMiddle, allPoints.get(iStartMiddlePath + 1));

        Point[] points1 = new Point[iStartMiddlePath + 2];
        for (int i = 0; i < iStartMiddlePath + 1; i++) {
            points1[i] = allPoints.get(i);
        }
        points1[iStartMiddlePath + 1] = middle;
        edge.setPoints1(points1);

        Point[] points2 = new Point[allPoints.size() - iStartMiddlePath];
        for (int i = 0; i < allPoints.size() - iStartMiddlePath + 1; i++) {
            points2[i] = allPoints.get(allPoints.size() - 1 - i);
        }
        points2[allPoints.size() - iStartMiddlePath + 1] = middle;
        edge.setPoints2(points2);
    }

    private int computeIndexMiddlePath(double[] cumulatedDistance) {
        double totalDistance = cumulatedDistance[cumulatedDistance.length - 1];
        double middleDistance = totalDistance / 2;
        for (int i = 0; i < cumulatedDistance.length; i++) {
            if (cumulatedDistance[i] > middleDistance) {
                return i - 1;
            }
        }
        return cumulatedDistance.length - 1;
    }

    private static double[] computeCumulatedDistances(List<Point> allPoints) {
        double[] cumulatedDistance = new double[allPoints.size()];
        cumulatedDistance[0] = 0;
        double sum = 0;
        for (int i = 0; i < allPoints.size() - 1; i++) {
            sum += allPoints.get(i).distance(allPoints.get(i + 1));
            cumulatedDistance[i + 1] = sum;
        }
        return cumulatedDistance;
    }

    @Override
    protected void computeTextEdgeLayoutCoordinates(VoltageLevelNode voltageLevelNode, TextNode textNode, TextEdge edge) {
        List<Point> customPoints = customTextPaths.getOrDefault(edge.getEquipmentId(), List.of());
        if (customPoints.isEmpty()) {
            super.computeTextEdgeLayoutCoordinates(voltageLevelNode, textNode, edge);
            return;
        }

        textNode.setEdgeConnection(customPoints.get(customPoints.size() - 1));

        List<Point> allPoints = new ArrayList<>();
        allPoints.add(voltageLevelNode.getPosition());
        allPoints.addAll(customPoints);
        edge.setPoints(allPoints.toArray(new Point[allPoints.size() + 1]));
    }
}
