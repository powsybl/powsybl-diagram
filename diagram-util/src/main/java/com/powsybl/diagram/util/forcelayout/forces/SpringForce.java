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
import com.powsybl.diagram.util.forcelayout.geometry.LayoutContext;
import com.powsybl.diagram.util.forcelayout.geometry.Point;
import com.powsybl.diagram.util.forcelayout.geometry.Vector2D;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class SpringForce<V, E> implements Force<V, E> {
    private final SpringContainer<DefaultEdge> forceParameter;
    private static final double DEFAULT_STIFFNESS = 100.0;

    public SpringForce(SpringContainer<DefaultEdge> forceParameter) {
        this.forceParameter = forceParameter;
    }

    @Override
    public void init(LayoutContext<V, E> layoutContext) {
        Map<DefaultEdge, SpringParameter> springs = new HashMap<>();
        SimpleGraph<V, DefaultEdge> simpleGraph = layoutContext.getSimpleGraph();
        for (DefaultEdge edge : simpleGraph.edgeSet()) {
            V edgeSource = simpleGraph.getEdgeSource(edge);
            V edgeTarget = simpleGraph.getEdgeTarget(edge);
            if (layoutContext.getFixedPoints().containsKey(edgeSource) && layoutContext.getFixedPoints().containsKey(edgeTarget)) {
                continue;
            }
            Point pointSource = Objects.requireNonNullElseGet(layoutContext.getMovingPoints().get(edgeSource), () -> layoutContext.getInitialPoints().get(edgeSource));
            Point pointTarget = Objects.requireNonNullElseGet(layoutContext.getMovingPoints().get(edgeTarget), () -> layoutContext.getInitialPoints().get(edgeTarget));
            if (pointSource != pointTarget) { // no use in force layout to add loops
                springs.put(edge, new SpringParameter(DEFAULT_STIFFNESS, simpleGraph.getEdgeWeight(edge)));
            }
        }
        forceParameter.setSprings(springs);
    }

    /**
     * This is Hooke's Law
     */
    @Override
    public Vector2D apply(V vertex, Point point, LayoutContext<V, E> layoutContext) {
        Vector2D resultingForce = new Vector2D(0, 0);
        for (DefaultEdge edge : layoutContext.getSimpleGraph().edgesOf(vertex)) {
            // this is basically what is done in Graphs.neighborSet, but we need the edge to get the corresponding spring
            V otherVertex = Graphs.getOppositeVertex(layoutContext.getSimpleGraph(), edge, vertex);
            Point otherPoint = layoutContext.getMovingPoints().get(otherVertex);
            if (otherPoint == null) {
                otherPoint = layoutContext.getFixedPoints().get(otherVertex);
            }
            if (otherPoint == null) {
                throw new NullPointerException(String.format("No such point corresponding to the given vertex in either moving or non-moving points: Vertex %s", otherVertex));
            }

            SpringParameter spring = forceParameter.getSprings().get(edge);

            Vector2D force = Vector2D.calculateVectorBetweenPoints(point, otherPoint);
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
