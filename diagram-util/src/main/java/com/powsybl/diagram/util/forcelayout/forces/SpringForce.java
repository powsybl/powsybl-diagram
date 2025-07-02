/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.forcelayout.forces;

import com.powsybl.diagram.util.forcelayout.forces.parameters.SpringContainer;
import com.powsybl.diagram.util.forcelayout.forces.parameters.SpringParameter;
import com.powsybl.diagram.util.forcelayout.geometry.ForceGraph;
import com.powsybl.diagram.util.forcelayout.geometry.Point;
import com.powsybl.diagram.util.forcelayout.geometry.Vector2D;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class SpringForce<V, E> implements Force<V, E> {
    private final SpringContainer<DefaultEdge> forceParameter;

    public SpringForce(SpringContainer<DefaultEdge> forceParameter) {
        this.forceParameter = forceParameter;
    }

    /// This is Hooke's Law
    @Override
    public Vector2D apply(V forThisVertex, Point correspondingPoint, ForceGraph<V, E> forceGraph) {
        Vector2D resultingForce = new Vector2D(0, 0);
        for (DefaultEdge edge : forceGraph.getSimpleGraph().edgesOf(forThisVertex)) {
            // this is basically what is done in Graphs.neighborSet, but we need the edge to get the corresponding spring
            V otherVertex = Graphs.getOppositeVertex(forceGraph.getSimpleGraph(), edge, forThisVertex);
            Point otherPoint = forceGraph.getMovingPoints().get(otherVertex);
            if (otherPoint == null) {
                otherPoint = forceGraph.getFixedPoints().get(otherVertex);
            }
            if (otherPoint == null) {
                throw new NullPointerException(String.format("No such point corresponding to the given vertex in either moving or non-moving points: Vertex %s", otherVertex));
            }

            SpringParameter spring = forceParameter.getSprings().get(edge);

            Vector2D force = Vector2D.calculateVectorBetweenPoints(correspondingPoint, otherPoint);
            double displacement = force.magnitude() - spring.getLength();
            force.normalize();

            // multiply by 0.5 because each vertex will move half of the distance, assuming both are free
            // should this be different if the other point is not moving ?
            force.multiplyBy(spring.getStiffness() * displacement * 0.5);
            // might be good to have a method to do this in place instead of creating new Vector2D each time
            resultingForce.add(force);
        }
        return resultingForce;
    }
}
