/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.layout.algorithms.parameters;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public final class BasicForceLayoutParameters {
    private static final int DEFAULT_MAX_STEPS = 400;
    private static final double DEFAULT_MIN_ENERGY_THRESHOLD = 0.001;
    private static final double DEFAULT_DELTA_TIME = 0.1;
    private static final double DEFAULT_REPULSION = 800.0;
    private static final double DEFAULT_FRICTION = 500;
    private static final double DEFAULT_MAX_SPEED = 100;
    private static final boolean DEFAULT_ACTIVATE_REPULSION_FORCE_FROM_FIXED_POINTS = true;
    private static final boolean DEFAULT_ACTIVATE_ATTRACT_TO_CENTER_FORCE = true;

    private final int maxSteps;
    private final double minEnergyThreshold;
    private final double deltaTime;
    private final double repulsion;
    private final double friction;
    private final double maxSpeed;
    private final boolean activateRepulsionForceFromFixedPoints;
    private final boolean activateAttractToCenterForce;

    private BasicForceLayoutParameters(
            int maxSteps,
            double minEnergyThreshold,
            double deltaTime,
            double repulsion,
            double friction,
            double maxSpeed,
            boolean activateRepulsionForceFromFixedPoints,
            boolean activateAttractToCenterForce
    ) {
        this.maxSteps = maxSteps;
        this.minEnergyThreshold = minEnergyThreshold;
        this.deltaTime = deltaTime;
        this.repulsion = repulsion;
        this.friction = friction;
        this.maxSpeed = maxSpeed;
        this.activateRepulsionForceFromFixedPoints = activateRepulsionForceFromFixedPoints;
        this.activateAttractToCenterForce = activateAttractToCenterForce;
    }

    public static class Builder {
        private int maxSteps = DEFAULT_MAX_STEPS;
        private double minEnergyThreshold = DEFAULT_MIN_ENERGY_THRESHOLD;
        private double deltaTime = DEFAULT_DELTA_TIME;
        private double repulsion = DEFAULT_REPULSION;
        private double friction = DEFAULT_FRICTION;
        private double maxSpeed = DEFAULT_MAX_SPEED;
        private boolean activateRepulsionForceFromFixedPoints = DEFAULT_ACTIVATE_REPULSION_FORCE_FROM_FIXED_POINTS;
        private boolean activateAttractToCenterForce = DEFAULT_ACTIVATE_ATTRACT_TO_CENTER_FORCE;

        /**
         * Change the maximum number of iteration the algorithm is allowed to run,
         * default is {@value DEFAULT_MAX_STEPS}
         * @param maxSteps the maximum number of iteration
         * @return the instance of this Builder with the `maxSteps` changed
         */
        public Builder withMaxSteps(int maxSteps) {
            this.maxSteps = maxSteps;
            return this;
        }

        /**
         * Control the energy that each point has to reach before stopping the algorithm.
         * The lower the value, the longer the run, but it might provide better results visually,
         * default is {@value DEFAULT_MIN_ENERGY_THRESHOLD}
         * @param minEnergyThreshold the value of the energy
         * @return the instance of this Builder with the `minEnergyThreshold` changed
         */
        public Builder withMinEnergyThreshold(double minEnergyThreshold) {
            this.minEnergyThreshold = minEnergyThreshold;
            return this;
        }

        /**
         * The time interval between each simulation step. A lower number means the force is more precise, but that runtime might be longer,
         * default is {@value DEFAULT_DELTA_TIME}
         * @param deltaTime the time between two steps of the force graph simulation
         * @return the instance of this Builder with the `deltaTime` changed
         */
        public Builder withDeltaTime(double deltaTime) {
            this.deltaTime = deltaTime;
            return this;
        }

        /**
         * Coefficient for the repulsion force between nodes. Increasing this will make the network more sparse (ie nodes will be further apart),
         * default is {@value DEFAULT_REPULSION}
         * @param repulsion the repulsion coefficient you want
         * @return the instance of this Builder with the `repulsion` changed
         */
        public Builder withRepulsion(double repulsion) {
            this.repulsion = repulsion;
            return this;
        }

        /**
         * Coefficient for the slowdown of the points, the higher the coefficient, the slower a point will be,
         * default is {@value DEFAULT_FRICTION}
         * @param friction the value of the friction
         * @return the instance of this Builder with the `friction` changed
         */
        public Builder withFriction(double friction) {
            this.friction = friction;
            return this;
        }

        /**
         * The maximum speed a point can have, higher values might allow faster convergence, but if set too high could start having the opposite effect,
         * with points starting to swing around their equilibrium position, default is {@value DEFAULT_MAX_SPEED}
         * @param maxSpeed the maximum speed of a point
         * @return the instance of this Builder with the `maxSpeed` changed
         */
        public Builder withMaxSpeed(double maxSpeed) {
            this.maxSpeed = maxSpeed;
            return this;
        }

        /**
         * If set to true, other points will get a repulsion effect from unmovable points (fixed points),
         * default is {@value DEFAULT_ACTIVATE_REPULSION_FORCE_FROM_FIXED_POINTS}
         * @param activateRepulsionForceFromFixedPoints whether you want to activate repulsion from fixed points or not
         * @return the instance of this Builder with the `activateRepulsionForceFromFixedPoints` changed
         */
        public Builder withActivateRepulsionForceFromFixedPoints(boolean activateRepulsionForceFromFixedPoints) {
            this.activateRepulsionForceFromFixedPoints = activateRepulsionForceFromFixedPoints;
            return this;
        }

        /**
         * Activate or deactivate the force that attracts points to the center of the graph. It is used to prevent non-connected points
         * from drifting away, default is {@value DEFAULT_ACTIVATE_ATTRACT_TO_CENTER_FORCE}
         * @param activateAttractToCenterForce activate or deactivate the center attraction force
         * @return the instance of this Builder with the `activateAttractToCenterForce` changed
         */
        public Builder withAttractToCenterForce(boolean activateAttractToCenterForce) {
            this.activateAttractToCenterForce = activateAttractToCenterForce;
            return this;
        }

        public BasicForceLayoutParameters build() {
            return new BasicForceLayoutParameters(
                    maxSteps,
                    minEnergyThreshold,
                    deltaTime,
                    repulsion,
                    friction,
                    maxSpeed,
                    activateRepulsionForceFromFixedPoints,
                    activateAttractToCenterForce
            );
        }
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    public double getMinEnergyThreshold() {
        return minEnergyThreshold;
    }

    public double getDeltaTime() {
        return deltaTime;
    }

    public double getRepulsion() {
        return repulsion;
    }

    public double getFriction() {
        return friction;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public boolean isActivateRepulsionForceFromFixedPoints() {
        return activateRepulsionForceFromFixedPoints;
    }

    public boolean isActivateAttractToCenterForce() {
        return activateAttractToCenterForce;
    }

}
