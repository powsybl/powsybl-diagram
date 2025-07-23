/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.layout.algorithms;

import com.powsybl.diagram.util.layout.forces.Force;
import com.powsybl.diagram.util.layout.forces.AttractToCenterForceByEdgeNumberLinear;
import com.powsybl.diagram.util.layout.forces.EdgeAttractionForceLinear;
import com.powsybl.diagram.util.layout.forces.RepulsionForceByEdgeNumberLinear;
import com.powsybl.diagram.util.layout.geometry.LayoutContext;
import com.powsybl.diagram.util.layout.geometry.Point;
import com.powsybl.diagram.util.layout.geometry.Vector2D;
import com.powsybl.diagram.util.layout.algorithms.parameters.Atlas2Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// The code in this class is the implementation of the paper:
/// Jacomy M, Venturini T, Heymann S, Bastian M (2014)
/// ForceAtlas2, a Continuous Graph Layout Algorithm for Handy Network Visualization Designed for
/// the Gephi Software. PLoS ONE 9(6): e98679. doi:10.1371/journal.pone.0098679
/// The paper requires its usage to credit the original author and the source, DO NOT REMOVE THE ABOVE REFERENCE
/// Some parts of the code do not directly come from the paper and are instead found through experimenting, to find something that works best
/// The parts that are not inside the original paper are:
/// - the choice of a starting global speed for the graph
/// - a maximum decrease ratio in global speed between each step
/// - a "quick to calculate" stopping condition

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class Atlas2Layout<V, E> implements LayoutAlgorithm<V, E> {
    private final Atlas2Parameters layoutParameters;
    private final List<Force<V, E>> forces = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(Atlas2Layout.class);

    // The magic numbers
    // totally empirical, and not present in the original Atlas2 paper
    // This was found by launching the algorithm on a lot of graphs and checking after how many steps the graph looked stable
    // We then check the corresponding global graph speed, and made a regression between the number of nodes and the global graph speed at each step
    // this gave a curve y = a * x^b with a = NORMALIZED_STOPPING_VALUE and b = NORMALIZATION_POWER
    // it means that we can know the graph speed at which the graph is stable, no matter the speed of the graph
    // The speed is globally decreasing (even though it goes up sometimes). On all the tests that were ran, the global speed
    // reaches this stopping value for the first time when it is stable (meaning it doesn't become stable when it reaches it for the 2nd, 3rd try)
    // meaning we can be confident to stop once we reach this value for the first time
    private static final double NORMALIZED_STOPPING_VALUE = 1.06944;
    private static final double NORMALIZATION_POWER = -0.107886;
    // This is not part of Atlas2's paper, used to control the global graph speed decrease and start value
    private static final double STARTING_SPEED_RATIO = 1;
    private static final double MAX_SPEED_DECREASE_RATIO = 0.7;

    public Atlas2Layout(Atlas2Parameters layoutParameters) {
        this.forces.add(new RepulsionForceByEdgeNumberLinear<>(
                layoutParameters.getRepulsion(),
                layoutParameters.isRepulsionForceFromFixedPoints()));
        this.forces.add(new EdgeAttractionForceLinear<>(layoutParameters.getAttraction()));
        if (layoutParameters.isAttractToCenterForce()) {
            // Atlas2 talks about both a unit gravity force and a linear gravity force
            // Both can work, but for your visualization purpose, a linear gravity force which tends to make the graph more compact worked better
            this.forces.add(new AttractToCenterForceByEdgeNumberLinear<>(layoutParameters.getGravity()));
        }
        this.layoutParameters = layoutParameters;
    }

    // To be moved later if needed by other algorithms
    private void initAllForces(List<Force<V, E>> forces, LayoutContext<V, E> layoutContext) {
        for (Force<V, E> force : forces) {
            force.init(layoutContext);
        }
    }

    /// Use Atlas2 layout with default parameters
    public Atlas2Layout() {
        this(new Atlas2Parameters.Builder().build());
    }

    ///  Note : the mass of the points doesn't have an impact on the graph as it doesn't appear in Atlas2
    /// We could have the impact be in the position update, by dividing the displacement by the mass of the point
    @Override
    public void run(LayoutContext<V, E> layoutContext) {
        initAllForces(forces, layoutContext);

        Map<Point, Vector2D> previousForces = new HashMap<>();
        Map<Point, Double> swingMap = new HashMap<>();
        int graphSize = layoutContext.getSimpleGraph().vertexSet().size();
        // starting speed proportional to the size of the network, not part of Atlas2's paper
        double previousGraphSpeed = STARTING_SPEED_RATIO * graphSize;

        // to know by how much to normalize the global graph speed, used for the stopping condition$
        // normalization is used so that networks of all size have the same stopping condition
        // this is not part of Atlas2's paper
        final double stoppingGlobalGraphSpeed = NORMALIZED_STOPPING_VALUE * Math.pow(graphSize, NORMALIZATION_POWER);

        for (Point point : layoutContext.getMovingPoints().values()) {
            previousForces.put(point, new Vector2D());
            swingMap.put(point, 0.);
        }
        int i;
        for (i = 0; i < layoutParameters.getMaxSteps(); ++i) {
            double graphSwing = 0.;
            double newGraphSpeed;
            double graphTraction = 0.;
            //calculate forces
            for (Map.Entry<V, Point> entry : layoutContext.getMovingPoints().entrySet()) {
                Point point = entry.getValue();
                for (Force<V, E> force : forces) {
                    Vector2D resultingForce = force.apply(entry.getKey(), point, layoutContext);
                    point.applyForce(resultingForce);
                }
                // calculate swg(n) for each node the swing of the node
                // at the same time calculate tra(n) the traction of the node
                // we can also calculate swg(G) and tra(G) the swing and traction of the graph
                int vertexDegreePlusOne = layoutContext.getSimpleGraph().degreeOf(entry.getKey()) + 1;
                Vector2D previousPointForce = previousForces.get(point);
                double pointSwing = calculatePointSwing(point, previousPointForce);
                swingMap.put(point, pointSwing);
                graphSwing += pointSwing * vertexDegreePlusOne;
                graphTraction += calculatePointTraction(point, previousPointForce) * vertexDegreePlusOne;
            }
            if (graphSwing == 0) {
                // that means that all the points are not moving anymore, or are diverging very fast
                break;
            }
            // calculate s(G) the global speed of the graph
            // this speed should not be less than a certain amount of the previous graph speed
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
            updatePosition(layoutContext, newGraphSpeed, swingMap, previousForces);
            if (isStable(newGraphSpeed, stoppingGlobalGraphSpeed)) {
                break;
            }
            previousGraphSpeed = newGraphSpeed;
        }
        LOGGER.info("Finished in {} steps", i);
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

    private void updatePosition(LayoutContext<V, E> layoutContext, double graphSpeed, Map<Point, Double> swingMap, Map<Point, Vector2D> previousForces) {
        for (Point point : layoutContext.getMovingPoints().values()) {
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

    private boolean isStable(double newGraphSpeed, double stoppingGlobalGraphSpeed) {
        // this stability condition is handmade and not mentioned in Atlas2's paper
        // a stop condition is given in that paper, but it involves calculating the distance of all the edges and vertex of the graph
        // which is expensive to do for big graphs (quadratic complexity with the number of vertex)
        // instead we use the global graph speed, which could be seen as a measurement of how much the graph is moving
        // graphs that become stable have less global energy / a lower graph speed
        return newGraphSpeed <= stoppingGlobalGraphSpeed;
    }
}

