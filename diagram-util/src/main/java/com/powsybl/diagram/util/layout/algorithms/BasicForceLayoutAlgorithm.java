/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.layout.algorithms;

import com.powsybl.diagram.util.layout.forces.Force;
import com.powsybl.diagram.util.layout.forces.CoulombForce;
import com.powsybl.diagram.util.layout.forces.AttractToCenterForceLinear;
import com.powsybl.diagram.util.layout.forces.SpringForce;
import com.powsybl.diagram.util.layout.geometry.LayoutContext;
import com.powsybl.diagram.util.layout.geometry.Point;
import com.powsybl.diagram.util.layout.geometry.Vector2D;
import com.powsybl.diagram.util.layout.algorithms.parameters.BasicForceLayoutParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * The following algorithm is a force layout algorithm.
 * It seeks to place the nodes of a graph in such a way that the nodes are well spaced and that there are no unnecessary crossings.
 * The algorithm uses an analogy with physics where the nodes of the graph are particles with mass and the edges are springs.
 * Force calculations are used to place the nodes.
 * The algorithm is inspired from: https://github.com/dhotson/springy
 *
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class BasicForceLayoutAlgorithm<V, E> implements LayoutAlgorithm<V, E> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicForceLayoutAlgorithm.class);

    private final BasicForceLayoutParameters layoutParameters;
    private final List<Force<V, E>> forces = new ArrayList<>();

    public BasicForceLayoutAlgorithm(BasicForceLayoutParameters layoutParameters) {
        Objects.requireNonNull(layoutParameters);
        this.forces.add(new SpringForce<>());
        this.forces.add(new CoulombForce<>(
            layoutParameters.getRepulsion(),
            layoutParameters.isRepulsionForceFromFixedPoints()
        ));
        if (layoutParameters.isAttractToCenterForce()) {
            this.forces.add(new AttractToCenterForceLinear<>(layoutParameters.getRepulsion() / 200));
        }
        this.layoutParameters = layoutParameters;
    }

    @Override
    public void run(LayoutContext<V, E> layoutContext) {
        Objects.requireNonNull(layoutContext);
        //Initialize Spring force
        initAll(forces, layoutContext);

        // do the loop on the nodes and forces
        int i;
        for (i = 0; i < layoutParameters.getMaxSteps(); ++i) {
            for (Map.Entry<V, Point> entry : layoutContext.getMovingPoints().entrySet()) {
                Point point = entry.getValue();
                for (Force<V, E> force : forces) {
                    Vector2D resultingVector = force.apply(entry.getKey(), point, layoutContext);
                    point.applyForce(resultingVector);
                }
            }
            updateVelocity(layoutContext);
            updatePosition(layoutContext);

            if (isStable(layoutContext)) {
                break;
            }
        }
        LOGGER.info("Calculating the layout took {} steps", i);
    }

    private void updateVelocity(LayoutContext<V, E> layoutContext) {
        for (Point point : layoutContext.getMovingPoints().values()) {
            Vector2D newVelocity = new Vector2D(point.getForces());
            newVelocity.multiplyBy((1 - Math.exp(-layoutParameters.getDeltaTime() * layoutParameters.getFriction() / point.getMass())) / layoutParameters.getFriction());
            point.setVelocity(newVelocity);

            if (newVelocity.magnitude() > layoutParameters.getMaxSpeed()) {
                newVelocity.normalize();
                newVelocity.multiplyBy(layoutParameters.getMaxSpeed());
            }

            point.resetForces();
        }
    }

    private void updatePosition(LayoutContext<V, E> layoutContext) {
        // Here we only update the position for the nodes that do not have fixed positions
        for (Point point : layoutContext.getMovingPoints().values()) {
            Vector2D speed = new Vector2D(point.getVelocity());
            speed.multiplyBy(layoutParameters.getDeltaTime());
            point.getPosition().add(speed);
        }
    }

    private boolean isStable(LayoutContext<V, E> layoutContext) {
        return layoutContext.getMovingPoints().values().stream().allMatch(p -> p.getEnergy() < layoutParameters.getMinEnergyThreshold());
    }
}
