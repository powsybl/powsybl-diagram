/**
 * Copyright (c) 2025-2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.layout.forces;

import com.powsybl.diagram.util.layout.forces.util.NoOverlapPointSize;
import com.powsybl.diagram.util.layout.geometry.LayoutContext;
import com.powsybl.diagram.util.layout.geometry.Point;
import com.powsybl.diagram.util.layout.geometry.Vector2D;

import java.util.Map;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class RepulsionForceDegreeBasedNoOverlapLinear<V, E> implements Force<V, E> {
    private final double forceIntensityNoOverlap;
    private final double forceIntensityWithOverlap;
    private final double repulsionZoneRatio;
    private double repulsionZoneRadius;
    private final NoOverlapPointSize pointSizeRecord;

    /**
     * Build a repulsion force to prevent overlap of points that have a given pointSize, only consider points closer than pointSize * repulsionZoneRatio for the repulsion interaction
     * @param forceIntensityNoOverlap the intensity of the force when points are not overlapping, this should be at least an order of magnitude lower than forceIntensityWithOverlap
     * @param forceIntensityWithOverlap the intensity of the force when points are overlapping, this should be at least an order of magnitude bigger than forceIntensityNoOverlap
     * @param pointSizeScale scaling coefficient for the size of the point given the number of nodes of the graph, get the size of a point via scale * graph size + offset
     * @param pointSizeOffset offset for the size of the point, get the size of a point via scale * graph size + offset
     * @param repulsionZoneRatio the zone in which to consider the interaction with other points is a disc of radius repulsionZoneRatio * pointSize
     */
    public RepulsionForceDegreeBasedNoOverlapLinear(double forceIntensityNoOverlap, double forceIntensityWithOverlap, double pointSizeScale, double pointSizeOffset, double repulsionZoneRatio) {
        this.forceIntensityNoOverlap = forceIntensityNoOverlap;
        this.forceIntensityWithOverlap = forceIntensityWithOverlap;
        this.repulsionZoneRatio = repulsionZoneRatio;
        // using default value, will change later
        this.pointSizeRecord = new NoOverlapPointSize(pointSizeScale, pointSizeOffset);
        this.repulsionZoneRadius = pointSizeRecord.getPointSize() * repulsionZoneRatio;
    }

    @Override
    public void init(LayoutContext<V, E> layoutContext) {
        // init point size
        pointSizeRecord.calculatePointSize(layoutContext.getAllPoints().size());
        layoutContext.cacheDegree();
        this.repulsionZoneRadius = this.repulsionZoneRatio * this.pointSizeRecord.getPointSize();
    }

    @Override
    public Vector2D apply(V vertex, Point point, LayoutContext<V, E> layoutContext) {
        Vector2D resultingForce = new Vector2D();
        int thisVertexDegree = point.getPointVertexDegree();
        for (Map.Entry<V, Point> otherVertexPoint : layoutContext.getAllPoints().entrySet()) {
            if (otherVertexPoint.getValue() != point) {
                linearRepulsionBetweenPoints(
                        resultingForce,
                        thisVertexDegree,
                        point,
                        otherVertexPoint.getValue()
                );
            }
        }
        return resultingForce;
    }

    private void linearRepulsionBetweenPoints(
            Vector2D resultingForce,
            int vertexDegree,
            Point point,
            Point otherPoint
    ) {
        Vector2D force = Vector2D.calculateVectorBetweenPoints(otherPoint, point);
        double magnitude = force.magnitude();
        if (magnitude < repulsionZoneRadius) {
            //check distance against 2 * pointSize, imagine that the two points are touching edge to edge,
            // the distance between centers will be 2 * pointSize
            // we want to check against that limit to know if points are too close to each other
            double forceIntensity = magnitude <= 2 * pointSizeRecord.getPointSize() ? forceIntensityWithOverlap : forceIntensityNoOverlap / magnitude;

            double intensity = forceIntensity
                    * (vertexDegree + 1)
                    * (otherPoint.getPointVertexDegree() + 1)
                    / magnitude;

            force.multiplyBy(intensity);
            resultingForce.add(force);
        }
    }
}

