/**
 * Copyright (c) 2025-2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.layout.algorithms;

import com.powsybl.commons.ref.RefObj;
import com.powsybl.diagram.util.layout.algorithms.quadtreeupdateschedule.ConstantSchedule;
import com.powsybl.diagram.util.layout.forces.*;
import com.powsybl.diagram.util.layout.geometry.*;
import com.powsybl.diagram.util.layout.algorithms.parameters.Atlas2Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * <p>
 * The code in this class is the implementation of the paper:<br>
 * Jacomy M, Venturini T, Heymann S, Bastian M (2014) ForceAtlas2,
 * a Continuous Graph Layout Algorithm for Handy Network Visualization Designed for
 * the Gephi Software. PLoS ONE 9(6): e98679. doi:10.1371/journal.pone.0098679<br>
 * The paper requires its usage to credit the original author and the source, DO NOT REMOVE THE ABOVE REFERENCE <br>
 * Some parts of the code do not directly come from the paper and were instead developed through experimenting, to find something that works best.
 * The parts that are not inside the original paper are:
 * <ul>
 *     <li>the choice of a starting global speed for the graph</li>
 *     <li>a maximum decrease ratio in global speed between each step</li>
 *     <li>a "quick to calculate" stopping condition</li>
 * </ul>
 * </p>
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class Atlas2ForceLayoutAlgorithm<V, E> implements LayoutAlgorithm<V, E> {
    private final Atlas2Parameters layoutParameters;
    private final List<Force<V, E>> forces = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(Atlas2ForceLayoutAlgorithm.class);
    private final RefObj<Quadtree> quadtreeContainer = new RefObj<>(null);

    // The magic numbers
    // totally empirical, and not present in the original Atlas2 paper
    // This was found by launching the algorithm on a lot of graphs and checking after how many steps the graph looked stable
    // We then check the corresponding global graph speed, and made a regression between the number of nodes and the global graph speed at each step
    // this gave a curve y = a * x^b with a = NORMALIZED_STOPPING_VALUE and b = NORMALIZATION_POWER
    // It means that we can know the graph speed at which the graph is stable, no matter the speed of the graph
    // The speed is globally decreasing (even though it goes up sometimes). On all the tests that were ran, the global speed
    // reaches this stopping value for the first time when it is stable (meaning it doesn't become stable when it reaches it for the 2nd, 3rd try)
    // meaning we can be confident to stop once we reach this value for the first time
    private static final double NORMALIZED_STOPPING_VALUE = 1.06944;
    private static final double NORMALIZATION_POWER = -0.107886;
    // This is not part of Atlas2's paper, used to control the global graph speed decrease and start value
    /**
     * The ratio of the global speed at which to start compared to the number of nodes of the graph. The starting speed is this ratio times the number of nodes
     */
    private static final double STARTING_SPEED_RATIO = 1;
    /**
     * How much can the global speed decrease between each time step
     */
    private static final double MAX_SPEED_DECREASE_RATIO = 0.7;

    public Atlas2ForceLayoutAlgorithm(Atlas2Parameters layoutParameters) {
        this.layoutParameters = layoutParameters;
        addRepulsionForce(layoutParameters);
        this.forces.add(new EdgeAttractionForceLinear<>(layoutParameters.getEdgeAttractionIntensity()));
        if (layoutParameters.isAttractToCenterEnabled()) {
            // Atlas2 talks about both a unit gravity force and a linear gravity force
            // Both can work, but for your visualization purpose, a linear gravity force which tends to make the graph more compact worked better
            this.forces.add(new AttractToCenterForceDegreeBasedLinear<>(layoutParameters.getAttractToCenterIntensity()));
        }
    }

    /**
     * Use Atlas2 layout with default parameters
     */
    public Atlas2ForceLayoutAlgorithm() {
        this(new Atlas2Parameters.Builder().build());
    }

    //  Note : the mass of the points doesn't have an impact on the graph as it doesn't appear in Atlas2
    // We could have the impact be in the position update, by dividing the displacement by the mass of the point
    @Override
    public void run(LayoutContext<V, E> layoutContext) {
        Objects.requireNonNull(layoutContext);
        forces.forEach(f -> f.init(layoutContext));

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
        }
        int i = 0;
        int stoppingStep = layoutParameters.getMaxSteps();
        boolean graphSwingIsZero = false;

        ConstantSchedule quadtreeUpdateSchedule = new ConstantSchedule(layoutParameters.getQuadtreeCalculationIncrement());

        while (i < stoppingStep && !graphSwingIsZero) {
            if (layoutParameters.isBarnesHutEnabled() && quadtreeUpdateSchedule.isTimeToUpdate(i)) {
                Collection<Point> interactingPoints = getInteractingPoints(layoutContext);
                this.quadtreeContainer.set(new Quadtree(interactingPoints, (Point point) -> point.getPointVertexDegree() + 1));
            }
            GraphDataValues graphDataValues = calculateForces(layoutContext, previousForces, swingMap);
            graphSwingIsZero = graphDataValues.graphSwing() == 0;
            // calculate s(G) the global speed of the graph
            // this speed should not be less than a certain amount of the previous graph speed
            // the graph speed should not be more than a certain amount of the previous graph speed
            // calculate given the swing tolerance and check it's being between those bounds
            if (!graphSwingIsZero) {
                double newGraphSpeed = Math.clamp(
                        layoutParameters.getSwingTolerance() * graphDataValues.graphTraction() / graphDataValues.graphSwing(),
                        MAX_SPEED_DECREASE_RATIO * previousGraphSpeed,
                        layoutParameters.getMaxGlobalSpeedIncreaseRatio() * previousGraphSpeed
                );
                // calculate s(n) the speed of each node n
                // store the forces on each node into the map of forces
                // calculate D(n) the displacement of each node n
                // reset forces on all points (we create a new vector2D so it won't affect forces in the map of forces)
                updateAllPositions(layoutContext, newGraphSpeed, swingMap, previousForces);
                if (isStable(newGraphSpeed, stoppingGlobalGraphSpeed)) {
                    //TODO check impact of not increasing the stopping step by barnesHutTheta / 8, maybe change the stopping condition
                    break;
                }
                previousGraphSpeed = newGraphSpeed;
                ++i;
            }
        }
        LOGGER.info("Finished in {} steps", i);
    }

    /**
     * Calculate the forces and updates the swingMap with the calculated values. Returns the graphSwing and the graphTraction
     * @param layoutContext the information about the graph and the positon of the points
     * @param previousForces the forces used on the previous iteration turn. Used to calculate the swing of each point (and the graph swing as well as the graph traction)
     * @param swingMap a map containing the swing of each point. Note that we don't have the same for the traction, as we only need the graph traction later on, not the traction of each point
     * @return the swing of the graph and the traction of the graph, also updates <code>previousForces</code> and <code>swingMap</code> as a side effect
     */
    private Atlas2ForceLayoutAlgorithm.GraphDataValues calculateForces(LayoutContext<V, E> layoutContext, Map<Point, Vector2D> previousForces, Map<Point, Double> swingMap) {
        double graphSwing = 0;
        double graphTraction = 0;
        for (Map.Entry<V, Point> entry : layoutContext.getMovingPoints().entrySet()) {
            Point point = entry.getValue();
            for (Force<V, E> force : forces) {
                Vector2D resultingForce = force.apply(entry.getKey(), point, layoutContext);
                point.applyForce(resultingForce);
            }
            // calculate swg(n) for each node the swing of the node
            // at the same time calculate tra(n) the traction of the node
            // we can also calculate swg(G) and tra(G) the swing and traction of the graph
            int weight = layoutContext.getSimpleGraph().degreeOf(entry.getKey()) + 1;
            Vector2D previousPointForce = previousForces.get(point);
            double pointSwing = calculatePointSwing(point, previousPointForce);
            swingMap.put(point, pointSwing);
            graphSwing += pointSwing * weight;
            graphTraction += calculatePointTraction(point, previousPointForce) * weight;
        }
        return new GraphDataValues(graphSwing, graphTraction);
    }

    private record GraphDataValues(double graphSwing, double graphTraction) {
    }

    /**
     * Choose whether to add a repulsion force using barnes-hut or not
     */
    private void addRepulsionForce(Atlas2Parameters parameters) {
        if (parameters.isBarnesHutEnabled()) {
            this.forces.add(new RepulsionForceDegreeBasedLinearBarnesHut<>(
                    parameters.getRepulsionIntensity(),
                    parameters.isRepulsionFromFixedPointsEnabled(),
                    parameters.getBarnesHutTheta(),
                    this.quadtreeContainer
            ));
        } else {
            this.forces.add(new RepulsionForceDegreeBasedLinear<>(
                    parameters.getRepulsionIntensity(),
                    parameters.isRepulsionFromFixedPointsEnabled()
            ));
        }
    }

    /**
     * Choose which points to interact with, either all the points or just the moving points, depending on if repulsion force for fixed points is activated
     * @param layoutContext the context of the layout (points, graph of the points)
     * @return all the points if fixed points have a repulsion force, just the moving points otherwise
     */
    private Collection<Point> getInteractingPoints(LayoutContext<V, E> layoutContext) {
        if (layoutParameters.isRepulsionFromFixedPointsEnabled()) {
            return layoutContext.getAllPoints().values();
        } else {
            return layoutContext.getMovingPoints().values();
        }
    }

    /**
     * Calculate the swing of the point, as the magnitude of the difference between the previous and the current force applied to the point
     * @param point the point the force is applied to
     * @param previousForce the force that was previously applied on the point
     * @return the swing of the point
     */
    private double calculatePointSwing(Point point, Vector2D previousForce) {
        Vector2D swingVector = new Vector2D(point.getForces());
        swingVector.subtract(previousForce);
        return swingVector.magnitude();
    }

    /**
     * Calculate the traction of the point, as the magnitude of the average of the previous and the current force applied to the point
     * @param point the point the force is applied to
     * @param previousForce the force that was previously applied on the point
     * @return the traction of the point
     */
    private double calculatePointTraction(Point point, Vector2D previousForce) {
        Vector2D tractionVector = new Vector2D(point.getForces());
        tractionVector.add(previousForce);
        return tractionVector.magnitude() / 2.;
    }

    /**
     * Update the position of all the points of the layout
     * @param layoutContext the context of the layout (points, graph of the points)
     * @param graphSpeed the global speed of the entire graph
     * @param swingMap the stored swing of each point, we already calculated it earlier so we store it to not repeat calculations
     * @param previousForces the map that keeps track of the force applied to each point on the previous time step of the simulation
     */
    private void updateAllPositions(LayoutContext<V, E> layoutContext, double graphSpeed, Map<Point, Double> swingMap, Map<Point, Vector2D> previousForces) {
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

    /**
     * Check if the current global graph speed is lower than the stopping speed
     * @param newGraphSpeed the current global graph speed
     * @param stoppingGlobalGraphSpeed the value of the graph speed we should stop at, or under
     * @return true if the graph speed is lower than the stopping speed, false otherwise
     */
    private boolean isStable(double newGraphSpeed, double stoppingGlobalGraphSpeed) {
        // this stability condition is handmade and not mentioned in Atlas2's paper
        // a stop condition is given in that paper, but it involves calculating the distance of all the edges and vertex of the graph
        // which is expensive to do for big graphs (quadratic complexity with the number of vertex)
        // instead we use the global graph speed, which could be seen as a measurement of how much the graph is moving
        // graphs that become stable have less global energy / a lower graph speed
        return newGraphSpeed <= stoppingGlobalGraphSpeed;
    }
}

