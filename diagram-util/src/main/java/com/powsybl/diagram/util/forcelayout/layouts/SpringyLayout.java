/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.forcelayout.layouts;

import com.powsybl.diagram.util.forcelayout.forces.AbstractForce;
import com.powsybl.diagram.util.forcelayout.forces.CoulombForce;
import com.powsybl.diagram.util.forcelayout.forces.GravityForceLinear;
import com.powsybl.diagram.util.forcelayout.forces.SpringForce;
import com.powsybl.diagram.util.forcelayout.forces.forceparameter.*;
import com.powsybl.diagram.util.forcelayout.geometry.ForceGraph;
import com.powsybl.diagram.util.forcelayout.geometry.Point;
import com.powsybl.diagram.util.forcelayout.geometry.Vector2D;
import com.powsybl.diagram.util.forcelayout.layouts.layoutsparameters.SpringyParameters;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class SpringyLayout<V, E> extends AbstractLayoutAlgorithm<V, E> {
    private static final double DEFAULT_STIFFNESS = 100.0;
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringyLayout.class);

    private final SpringyParameters<V, E> layoutParameters;

    public SpringyLayout(SpringyParameters<V, E> layoutParameters) {
        super();
        this.forces.add(new CoulombForce<>(
                new IntensityEffectFromFixedNodesParameters(
                        layoutParameters.getRepulsion(),
                        layoutParameters.isRepulsionForceFromFixedPoints()
                )
        ));
        if (layoutParameters.isAttractToCenterForce()) {
            this.forces.add(new GravityForceLinear<>(
                    new IntensityParameter(
                            layoutParameters.getRepulsion() / 200
                    )
            ));
        }
        this.layoutParameters = layoutParameters;
    }

    public static <V, E> SpringContainer<DefaultEdge> initializeSprings(ForceGraph<V, E> forceGraph) {
        Map<DefaultEdge, SpringParameter> springs = new HashMap<>();
        SimpleGraph<V, DefaultEdge> simpleGraph = forceGraph.getSimpleGraph();
        for (DefaultEdge edge : simpleGraph.edgeSet()) {
            V edgeSource = simpleGraph.getEdgeSource(edge);
            V edgeTarget = simpleGraph.getEdgeTarget(edge);
            if (forceGraph.getFixedPoints().containsKey(edgeSource) && forceGraph.getFixedPoints().containsKey(edgeTarget)) {
                continue;
            }
            Point pointSource = Objects.requireNonNullElseGet(forceGraph.getMovingPoints().get(edgeSource), () -> forceGraph.getInitialPoints().get(edgeSource));
            Point pointTarget = Objects.requireNonNullElseGet(forceGraph.getMovingPoints().get(edgeTarget), () -> forceGraph.getInitialPoints().get(edgeTarget));
            if (pointSource != pointTarget) { // no use in force layout to add loops
                springs.put(edge, new SpringParameter(DEFAULT_STIFFNESS, simpleGraph.getEdgeWeight(edge)));
            }
        }
        return new SpringContainer<>(springs);
    }

    @Override
    public void calculateLayout(ForceGraph<V, E> forceGraph) {
        // it would be better if this was created with all the other forces but we need the graph to init the springs
        // TODO that could cause an issue if we launch the runner on multiple different graph, we would keep adding spring force
        this.forces.add(new SpringForce<>(initializeSprings(forceGraph)));

        // do the loop on the nodes and forces
        int i;
        for (i = 0; i < layoutParameters.getMaxSteps(); ++i) {
            for (Map.Entry<V, Point> entry : forceGraph.getMovingPoints().entrySet()) {
                Point point = entry.getValue();
                for (AbstractForce<V, E, ? extends ForceParameter> force : forces) {
                    Vector2D resultingVector = force.calculateForce(entry.getKey(), point, forceGraph);
                    point.applyForce(resultingVector);
                }
            }
            updateVelocity(forceGraph);
            updatePosition(forceGraph);

            if (isStable(forceGraph)) {
                break;
            }
        }
        LOGGER.info("Calculating the layout took {} steps", i);
    }

    private void updateVelocity(ForceGraph<V, E> forceGraph) {
        for (Point point : forceGraph.getMovingPoints().values()) {
            Vector2D newVelocity = new Vector2D(point.getForces());
            newVelocity.multiply((1 - Math.exp(-layoutParameters.getDeltaTime() * layoutParameters.getFriction() / point.getMass())) / layoutParameters.getFriction());
            point.setVelocity(newVelocity);

            if (newVelocity.magnitude() > layoutParameters.getMaxSpeed()) {
                newVelocity.normalize();
                newVelocity.multiply(layoutParameters.getMaxSpeed());
            }

            point.resetForces();
        }
    }

    private void updatePosition(ForceGraph<V, E> forceGraph) {
        // Here we only update the position for the nodes that do not have fixed positions
        for (Point point : forceGraph.getMovingPoints().values()) {
            Vector2D speed = new Vector2D(point.getVelocity());
            speed.multiply(layoutParameters.getDeltaTime());
            point.getPosition().add(speed);
        }
    }

    private boolean isStable(ForceGraph<V, E> forceGraph) {
        return forceGraph.getMovingPoints().values().stream().allMatch(p -> p.getEnergy() < layoutParameters.getMinEnergyThreshold());
    }
}
