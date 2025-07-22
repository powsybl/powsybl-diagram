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

    private final int maxSteps;
    private final double minEnergyThreshold;
    private final double deltaTime;
    private final double repulsion;
    private final double friction;
    private final double maxSpeed;
    private final boolean repulsionForceFromFixedPoints;
    private final boolean attractToCenterForce;

    private BasicForceLayoutParameters(
            int maxSteps,
            double minEnergyThreshold,
            double deltaTime,
            double repulsion,
            double friction,
            double maxSpeed,
            boolean repulsionForceFromFixedPoints,
            boolean attractToCenterForce
    ) {
        this.maxSteps = maxSteps;
        this.minEnergyThreshold = minEnergyThreshold;
        this.deltaTime = deltaTime;
        this.repulsion = repulsion;
        this.friction = friction;
        this.maxSpeed = maxSpeed;
        this.repulsionForceFromFixedPoints = repulsionForceFromFixedPoints;
        this.attractToCenterForce = attractToCenterForce;
    }

    public static class Builder {
        private int maxSteps = DEFAULT_MAX_STEPS;
        private double minEnergyThreshold = DEFAULT_MIN_ENERGY_THRESHOLD;
        private double deltaTime = DEFAULT_DELTA_TIME;
        private double repulsion = DEFAULT_REPULSION;
        private double friction = DEFAULT_FRICTION;
        private double maxSpeed = DEFAULT_MAX_SPEED;
        private boolean repulsionForceFromFixedPoints = true;
        private boolean attractToCenterForce = true;

        public Builder withMaxSteps(int maxSteps) {
            this.maxSteps = maxSteps;
            return this;
        }

        public Builder withMinEnergyThreshold(double minEnergyThreshold) {
            this.minEnergyThreshold = minEnergyThreshold;
            return this;
        }

        public Builder withDeltaTime(double deltaTime) {
            this.deltaTime = deltaTime;
            return this;
        }

        public Builder withRepulsion(double repulsion) {
            this.repulsion = repulsion;
            return this;
        }

        public Builder withFriction(double friction) {
            this.friction = friction;
            return this;
        }

        public Builder withMaxSpeed(double maxSpeed) {
            this.maxSpeed = maxSpeed;
            return this;
        }

        public Builder withRepulsionForceFromFixedPoints(boolean repulsionForceFromFixedPoints) {
            this.repulsionForceFromFixedPoints = repulsionForceFromFixedPoints;
            return this;
        }

        public Builder withAttractToCenterForce(boolean attractToCenterForce) {
            this.attractToCenterForce = attractToCenterForce;
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
                    repulsionForceFromFixedPoints,
                    attractToCenterForce
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

    public boolean isRepulsionForceFromFixedPoints() {
        return repulsionForceFromFixedPoints;
    }

    public boolean isAttractToCenterForce() {
        return attractToCenterForce;
    }

}
