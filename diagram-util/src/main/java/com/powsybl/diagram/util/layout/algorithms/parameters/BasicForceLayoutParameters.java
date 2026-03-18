/**
 * Copyright (c) 2025-2026, RTE (http://www.rte-france.com)
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
    private static final int DEFAULT_MAX_DURATION_SECONDS = 15;
    private static final double DEFAULT_MIN_ENERGY_THRESHOLD = 0.001;
    private static final double DEFAULT_DELTA_TIME = 0.1;
    private static final double DEFAULT_REPULSION_INTENSITY = 800.0;
    private static final double DEFAULT_FRICTION_INTENSITY = 500;
    private static final double DEFAULT_MAX_SPEED = 100;
    private static final boolean DEFAULT_REPULSION_FROM_FIXED_POINTS_ENABLED = true;
    private static final boolean DEFAULT_ATTRACT_TO_CENTER_ENABLED = true;

    private final int maxSteps;
    private final double timeoutSeconds;
    private final double minEnergyThreshold;
    private final double deltaTime;
    private final double repulsionIntensity;
    private final double frictionIntensity;
    private final double maxSpeed;
    private final boolean repulsionFromFixedPointsEnabled;
    private final boolean attractToCenterEnabled;

    private BasicForceLayoutParameters(
            int maxSteps,
            double timeoutSeconds,
            double minEnergyThreshold,
            double deltaTime,
            double repulsionIntensity,
            double frictionIntensity,
            double maxSpeed,
            boolean repulsionFromFixedPointsEnabled,
            boolean attractToCenterEnabled
    ) {
        this.maxSteps = maxSteps;
        this.timeoutSeconds = timeoutSeconds;
        this.minEnergyThreshold = minEnergyThreshold;
        this.deltaTime = deltaTime;
        this.repulsionIntensity = repulsionIntensity;
        this.frictionIntensity = frictionIntensity;
        this.maxSpeed = maxSpeed;
        this.repulsionFromFixedPointsEnabled = repulsionFromFixedPointsEnabled;
        this.attractToCenterEnabled = attractToCenterEnabled;
    }

    public static class Builder {
        private int maxSteps = DEFAULT_MAX_STEPS;
        private double timeoutSeconds = DEFAULT_MAX_DURATION_SECONDS;
        private double minEnergyThreshold = DEFAULT_MIN_ENERGY_THRESHOLD;
        private double deltaTime = DEFAULT_DELTA_TIME;
        private double repulsionIntensity = DEFAULT_REPULSION_INTENSITY;
        private double frictionIntensity = DEFAULT_FRICTION_INTENSITY;
        private double maxSpeed = DEFAULT_MAX_SPEED;
        private boolean repulsionFromFixedPointsEnabled = DEFAULT_REPULSION_FROM_FIXED_POINTS_ENABLED;
        private boolean attractToCenterEnabled = DEFAULT_ATTRACT_TO_CENTER_ENABLED;

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

        public Builder withTimeoutSeconds(double timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
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
         * Coefficient for the repulsionIntensity force between nodes. Increasing this will make the network more sparse (ie nodes will be further apart),
         * default is {@value DEFAULT_REPULSION_INTENSITY}
         * @param repulsionIntensity the repulsionIntensity coefficient you want
         * @return the instance of this Builder with the `repulsionIntensity` changed
         */
        public Builder withRepulsionIntensity(double repulsionIntensity) {
            this.repulsionIntensity = repulsionIntensity;
            return this;
        }

        /**
         * Coefficient for the slowdown of the points, the higher the coefficient, the slower a point will be,
         * default is {@value DEFAULT_FRICTION_INTENSITY}
         * @param frictionIntensity the value of the frictionIntensity
         * @return the instance of this Builder with the `frictionIntensity` changed
         */
        public Builder withFrictionIntensity(double frictionIntensity) {
            this.frictionIntensity = frictionIntensity;
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
         * default is {@value DEFAULT_REPULSION_FROM_FIXED_POINTS_ENABLED}
         * @param repulsionFromFixedPointsEnabled whether you want to activate repulsion from fixed points or not
         * @return the instance of this Builder with the `repulsionFromFixedPointsEnabled` changed
         */
        public Builder withRepulsionFromFixedPointsEnabled(boolean repulsionFromFixedPointsEnabled) {
            this.repulsionFromFixedPointsEnabled = repulsionFromFixedPointsEnabled;
            return this;
        }

        /**
         * Activate or deactivate the force that attracts points to the center of the graph. It is used to prevent non-connected points
         * from drifting away, default is {@value DEFAULT_ATTRACT_TO_CENTER_ENABLED}
         * @param attractToCenterEnabled activate or deactivate the center attraction force
         * @return the instance of this Builder with the `attractToCenterEnabled` changed
         */
        public Builder withAttractToCenterEnabled(boolean attractToCenterEnabled) {
            this.attractToCenterEnabled = attractToCenterEnabled;
            return this;
        }

        public BasicForceLayoutParameters build() {
            return new BasicForceLayoutParameters(
                    maxSteps,
                    timeoutSeconds,
                    minEnergyThreshold,
                    deltaTime,
                    repulsionIntensity,
                    frictionIntensity,
                    maxSpeed,
                    repulsionFromFixedPointsEnabled,
                    attractToCenterEnabled
            );
        }
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    public double getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public double getMinEnergyThreshold() {
        return minEnergyThreshold;
    }

    public double getDeltaTime() {
        return deltaTime;
    }

    public double getRepulsionIntensity() {
        return repulsionIntensity;
    }

    public double getFrictionIntensity() {
        return frictionIntensity;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public boolean isRepulsionFromFixedPointsEnabled() {
        return repulsionFromFixedPointsEnabled;
    }

    public boolean isAttractToCenterEnabled() {
        return attractToCenterEnabled;
    }

}
