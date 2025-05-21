/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout.layouts;

import com.powsybl.diagram.util.forcelayout.forces.AbstractForce;
import com.powsybl.diagram.util.forcelayout.forces.GravityForceByEdgeNumber;
import com.powsybl.diagram.util.forcelayout.forces.LinearEdgeAttractionForce;
import com.powsybl.diagram.util.forcelayout.forces.LinearRepulsionForceByDegree;
import com.powsybl.diagram.util.forcelayout.forces.forceparameter.IntensityEffectFromFixedNodesParameters;
import com.powsybl.diagram.util.forcelayout.forces.forceparameter.IntensityParameter;
import com.powsybl.diagram.util.forcelayout.geometry.ForceGraph;
import com.powsybl.diagram.util.forcelayout.geometry.Point;
import com.powsybl.diagram.util.forcelayout.geometry.Vector2D;
import com.powsybl.diagram.util.forcelayout.layouts.layoutsparameters.Atlas2Parameters;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class Atlas2Layout<V, E> extends AbstractLayoutAlgorithm<V, E> {
    private final Atlas2Parameters<V, E> layoutParameters;

    public Atlas2Layout(Atlas2Parameters<V, E> layoutParameters) {
        super();
        this.forces.add(new LinearEdgeAttractionForce<>(
                        new IntensityParameter(
                                layoutParameters.getAttraction()
                        )
        ));
        this.forces.add(new LinearRepulsionForceByDegree<>(
                new IntensityEffectFromFixedNodesParameters(
                        layoutParameters.getRepulsion(),
                        layoutParameters.isRepulsionForceFromFixedPoints()
                )
        ));
        if (layoutParameters.isAttractToCenterForce()) {
            this.forces.add(new GravityForceByEdgeNumber<>(
                    new IntensityParameter(
                            layoutParameters.getGravity()
                    )
            ));
        }
        this.layoutParameters = layoutParameters;
    }

    ///  Note : the mass of the points doesn't have an impact on the graph as it doesn't appear in Atlas2
    /// We could have the impact be in the position update, by dividing the displacement by the mass of the point
    @Override
    public void calculateLayout(ForceGraph<V, E> forceGraph) {
        Map<Point, Vector2D> previousForces = new HashMap<>();
        Map<Point, Double> swingMap = new HashMap<>();
        double previousGraphSpeed = 0.;
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
                for (AbstractForce<V, E> force : forces) {
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
            newGraphSpeed = layoutParameters.getSwingTolerance() * graphTraction / graphSwing;
            if (i != 0) {
                newGraphSpeed = Math.min(
                        newGraphSpeed,
                        layoutParameters.getMaxGlobalSpeedIncreaseRatio() * previousGraphSpeed
                );
            }
            // calculate s(n) the speed of each node n
            // store the forces on each node into the map of forces
            // calculate D(n) the displacement of each node n
            // reset forces on all points (we create a new vector2D so it won't affect forces in the map of forces)
            updatePosition(forceGraph, newGraphSpeed, swingMap, previousForces);
            if (isStable(previousGraphSpeed, newGraphSpeed)) {
                break;
            }
            previousGraphSpeed = newGraphSpeed;
        }
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

    private boolean isStable(double previousGraphSpeed, double newGraphSpeed) {
        // this stability condition is handmade and not mentioned in Atlas2's paper
        // a stop condition is given in that paper, but it involves calculating the distance of all the edges and vertex of the graph
        // which is expensive to do for big graphs (quadratic complexity with the number of vertex)
        if (newGraphSpeed == 0) {
            return true;
        } else {
            // check the graph speed is low, and check that the graph speed doesn't change much between steps
            // the limit values could be chosen by the user, but for now we keep it like that
            return newGraphSpeed < 0.1 && Math.abs(1 - previousGraphSpeed / newGraphSpeed) < 0.2;
        }
    }
}

