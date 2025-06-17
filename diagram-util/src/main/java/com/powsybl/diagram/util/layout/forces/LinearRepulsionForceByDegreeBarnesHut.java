/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout.forces;

import com.powsybl.diagram.util.forcelayout.forces.parameters.IntensityEffectFromFIxedNodesBarnesHutParameters;
import com.powsybl.diagram.util.forcelayout.geometry.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
// TODO make in interface to mutualize code with LinearRepulsionForceByDegree, waiting for the merge of Atlas2
public class LinearRepulsionForceByDegreeBarnesHut<V, E> implements Force<V, E> {
    private final IntensityEffectFromFIxedNodesBarnesHutParameters forceParameter;

    public LinearRepulsionForceByDegreeBarnesHut(IntensityEffectFromFIxedNodesBarnesHutParameters forceParameter) {
        this.forceParameter = forceParameter;
    }

    @Override
    public Vector2D calculateForce(V forThisVertex, Point correspondingPoint, ForceGraph<V, E> forceGraph) {
        Vector2D resultingForce = new Vector2D();
        int thisVertexDegree = correspondingPoint.getPointVertexDegree();
        BoundingBox rootBb = forceParameter.getQuadtree().getBoundingBox();
        // bounding box might not be square, this will work best for shapes that are not too long
        // could also test by using the diagonal width (using square root), might be faster as it will be tighter (but longer to calculate too)
        double width = Math.max(rootBb.getWidth(), rootBb.getHeight());
        // Assume the quadtree is built based on isEffectFromFixedNodes (ie with the fixed points in it or not)
        List<Point> pointInteractionList = generatePointInteractionList(
                forceParameter.getQuadtree().getRootIndex(),
                correspondingPoint,
                width
        );
        for (Point otherPoint : pointInteractionList) {
            if (otherPoint.getPosition() != correspondingPoint.getPosition()) {
                linearRepulsionBetweenPoints(
                        forceParameter.getForceIntensity(),
                        resultingForce,
                        thisVertexDegree,
                        correspondingPoint,
                        otherPoint
                );
            }
        }
        return resultingForce;
    }

    private void linearRepulsionBetweenPoints(
            double forceIntensity,
            Vector2D resultingForce,
            int thisVertexDegree,
            Point correspondingPoint,
            Point otherPoint
    ) {
        // The force goes from the otherPoint to the correspondingPoint (repulsion)
        Vector2D force = Vector2D.calculateVectorBetweenPoints(otherPoint, correspondingPoint);
        // divide by magnitude^2 because the force multiplies the unit vector by something/magnitude
        // the unit vector is Vector/magnitude, thus the force is Vector/magnitude * something/magnitude, thus Vector/magnitude^2
        // if we just use the vector and not the unit vector, points that are further away will have the same influence as points that are close
        // this is easy to explain as the formula is Vector * k * deg(n1) * deg(n2)/distance
        // which would be UnitVector * k * deg(n1) * deg(n2)
        // all UnitVector will have the same magnitude of 1, giving only the direction, thus the force becomes dependant only on the degree of the nodes
        // the name "linear" is a bit misleading, as its technically inverse linear (1 / distance)
        double intensity = forceIntensity
                * (thisVertexDegree + 1)
                * (otherPoint.getMass())
                / force.magnitudeSquare();
        force.multiplyBy(intensity);
        resultingForce.add(force);
    }

    private List<Point> generatePointInteractionList(short parentNodeIndex, Point forThisPoint, double nodeWidth) {
        Point barycenter = forceParameter.getQuadtree().getBarycenters().get(parentNodeIndex);
        if (nodeWidth / forThisPoint.distanceTo(barycenter) < forceParameter.getBarnesHutTheta()) {
            return List.of(barycenter);
        } else {
            List<Point> pointsToInteractWith = new ArrayList<>();
            Quadtree.QuadtreeNode thisNode = forceParameter.getQuadtree().getNodes().get(parentNodeIndex);
            double childNodeWidth = nodeWidth / 2;
            for (short index : thisNode.getChildrenNodeIdFlatten()) {
                if (index != Quadtree.NO_CHILDREN) {
                    pointsToInteractWith.addAll(generatePointInteractionList(index, forThisPoint, childNodeWidth));
                }
            }
            // if the list is empty, it means we are a leaf node, need to add self to the list and return
            if (pointsToInteractWith.isEmpty()) {
                pointsToInteractWith.add(barycenter);
            }
            return pointsToInteractWith;
        }
    }
}

