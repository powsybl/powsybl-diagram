/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout.layouts;

import com.powsybl.diagram.util.forcelayout.forces.Force;
import com.powsybl.diagram.util.forcelayout.forces.GravityForceByDegreeLinear;
import com.powsybl.diagram.util.forcelayout.forces.LinearEdgeAttractionForce;
import com.powsybl.diagram.util.forcelayout.forces.LinearRepulsionForceByDegree;
import com.powsybl.diagram.util.forcelayout.forces.parameters.IntensityEffectFromFixedNodesParameters;
import com.powsybl.diagram.util.forcelayout.forces.parameters.IntensityParameter;
import com.powsybl.diagram.util.forcelayout.geometry.ForceGraph;
import com.powsybl.diagram.util.forcelayout.geometry.Point;
import com.powsybl.diagram.util.forcelayout.geometry.Vector2D;
import com.powsybl.diagram.util.forcelayout.layouts.parameters.Atlas2Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class Atlas2Layout<V, E> implements LayoutAlgorithm<V, E> {
    private final Atlas2Parameters<V, E> layoutParameters;
    private final List<Force<V, E>> forces = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(Atlas2Layout.class);

    // The magic number (found with a regression on the number of steps it takes for a graph to look like it's finished)
    // totally empirical
    private static final double NORMALIZED_STOPPING_VALUE = 1.06944;
    private static final double NORMALIZATION_POWER = -0.107886;
    private static final double STARTING_SPEED_RATIO = 1;
    private static final double MAX_SPEED_DECREASE_RATIO = 0.7;

    public Atlas2Layout(Atlas2Parameters<V, E> layoutParameters) {
        this.forces.add(new LinearEdgeAttractionForce<>(
                        new IntensityParameter(
                                layoutParameters.getAttraction()
                        )
        ));
        if (layoutParameters.isAttractToCenterForce()) {
            this.forces.add(new GravityForceByDegreeLinear<>(
                    new IntensityParameter(
                            layoutParameters.getGravity()
                    )
            ));
        }
        this.layoutParameters = layoutParameters;
    }

    /// Use Atlas2 layout with default parameters
    public Atlas2Layout() {
        this(new Atlas2Parameters.Builder().build());
    }

    ///  Note : the mass of the points doesn't have an impact on the graph as it doesn't appear in Atlas2
    /// We could have the impact be in the position update, by dividing the displacement by the mass of the point
    @Override
    public void calculateLayout(ForceGraph<V, E> forceGraph) {
        IntensityEffectFromFixedNodesParameters repulsionForceParameters = buildRepulsionForceParameters(forceGraph);
        this.forces.add(new LinearRepulsionForceByDegree<>(
               repulsionForceParameters
        ));

        Map<Point, Vector2D> previousForces = new HashMap<>();
        Map<Point, Double> swingMap = new HashMap<>();
        int graphSize = forceGraph.getSimpleGraph().vertexSet().size();
        double previousGraphSpeed = STARTING_SPEED_RATIO * graphSize;

        double normalizationFactor = Math.pow(graphSize, NORMALIZATION_POWER);

        for (Point point : forceGraph.getMovingPoints().values()) {
            previousForces.put(point, new Vector2D());
            swingMap.put(point, 0.);
        }
        int i;
        for (i = 0; i < layoutParameters.getMaxSteps(); ++i) {
            double graphSwing = 0.;
            double newGraphSpeed;
            double graphTraction = 0.;
            //calculate forces
            for (Map.Entry<V, Point> entry : forceGraph.getMovingPoints().entrySet()) {
                Point point = entry.getValue();
                for (Force<V, E> force : forces) {
                    Vector2D resultingForce = force.calculateForce(entry.getKey(), point, forceGraph);
                    point.applyForce(resultingForce);
                }
                // calculate swg(n) for each node the swing of the node
                // at the same time calculate tra(n) the traction of the node
                // we can also calculate swg(G) and tra(G) the swing and traction of the graph
                int vertexDegreePlusOne = forceGraph.getSimpleGraph().degreeOf(entry.getKey()) + 1;
                Vector2D previousPointForce = previousForces.get(point);
                double pointSwing = calculatePointSwing(point, previousPointForce);
                swingMap.put(point, pointSwing);
                graphSwing += pointSwing * vertexDegreePlusOne;
                graphTraction += calculatePointTraction(point, previousPointForce) * vertexDegreePlusOne;
            }
            if (graphSwing == 0) {
                // that means that all the points are not moving anymore
                break;
            }
            // calculate s(G) the speed of the graph
            // the graph speed should not be less than a certain amount of the previous graph speed
            // the graph speed should not be more than a certain amount of the previous graph speed
            // calculate given the swing tolerance and check it's being between those bounds
            newGraphSpeed = Math.max(
                MAX_SPEED_DECREASE_RATIO * previousGraphSpeed,
                Math.min(
                    layoutParameters.getSwingTolerance() * graphTraction / graphSwing,
                    layoutParameters.getMaxGlobalSpeedIncreaseRatio() * previousGraphSpeed
            ));
            // calculate s(n) the speed of each node n
            // store the forces on each node into the map of forces
            // calculate D(n) the displacement of each node n
            // reset forces on all points (we create a new vector2D so it won't affect forces in the map of forces)
            updatePosition(forceGraph, newGraphSpeed, swingMap, previousForces);
            if (isStable(newGraphSpeed, normalizationFactor)) {
                break;
            }
            previousGraphSpeed = newGraphSpeed;
        }
        LOGGER.info("Finished in {} steps", i);
        this.forces.remove(2); // remove the LinearRepulsionForce as it depends on the graph
    }

    private IntensityEffectFromFixedNodesParameters buildRepulsionForceParameters(ForceGraph<V, E> forceGraph) {
        for (Map.Entry<V, Point> entry : forceGraph.getMovingPoints().entrySet()) {
            entry.getValue().setPointVertexDegree(forceGraph.getSimpleGraph().degreeOf(entry.getKey()));
        }
        for (Map.Entry<V, Point> entry : forceGraph.getFixedPoints().entrySet()) {
            entry.getValue().setPointVertexDegree(forceGraph.getSimpleGraph().degreeOf(entry.getKey()));
        }
        return new IntensityEffectFromFixedNodesParameters(
                layoutParameters.getRepulsion(),
                layoutParameters.isRepulsionForceFromFixedPoints()
        );
    }

    private double calculatePointSwing(Point point, Vector2D previousForce) {
        Vector2D swingVector = new Vector2D(point.getForces());
        swingVector.subtract(previousForce);
        return swingVector.magnitude();
    }

    private double calculatePointTraction(Point point, Vector2D previousForce) {
        Vector2D tractionVector = new Vector2D(point.getForces());
        tractionVector.add(previousForce);
        return tractionVector.magnitude() / 2.;
    }

    private void updatePosition(ForceGraph<V, E> forceGraph, double graphSpeed, Map<Point, Double> swingMap, Map<Point, Vector2D> previousForces) {
        for (Point point : forceGraph.getMovingPoints().values()) {
            double speedFactor = layoutParameters.getSpeedFactor()
                    * graphSpeed
                    / (1 + graphSpeed * Math.sqrt(swingMap.get(point)));
            Vector2D pointForceDisplacement = new Vector2D(point.getForces());
            double forceMagnitude = pointForceDisplacement.magnitude();
            if (forceMagnitude != 0) {
                speedFactor = Math.min(speedFactor, layoutParameters.getMaxSpeedFactor() / forceMagnitude);
            }
            // Displacement = speed * force (this is not physically correct but this is what Atlas2 does)
            pointForceDisplacement.multiplyBy(speedFactor);
            point.getPosition().add(pointForceDisplacement);
            // store the force in this loop for the next iteration
            previousForces.put(point, point.getForces());
            point.resetForces();
        }
    }

    private boolean isStable(double newGraphSpeed, double normalizationFactor) {
        // this stability condition is handmade and not mentioned in Atlas2's paper
        // a stop condition is given in that paper, but it involves calculating the distance of all the edges and vertex of the graph
        // which is expensive to do for big graphs (quadratic complexity with the number of vertex)
        // instead we use the global graph speed, which could be seen as a measurement of how much the graph is moving
        // graphs that become stable have less global energy / a lower graph speed
        return newGraphSpeed / normalizationFactor <= NORMALIZED_STOPPING_VALUE;
    }
}

