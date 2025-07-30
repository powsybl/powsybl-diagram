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
public final class Atlas2Parameters {
    private static final int DEFAULT_MAX_STEPS = 6000;
    private static final double DEFAULT_REPULSION = 4;
    private static final double DEFAULT_EDGE_ATTRACTION = 1;
    private static final double DEFAULT_ATTRACT_TO_CENTER = 0.001;
    private static final double DEFAULT_SPEED_FACTOR = 1;
    private static final double DEFAULT_MAX_SPEED_FACTOR = 10;
    private static final double DEFAULT_SWING_TOLERANCE = 1;
    private static final double DEFAULT_MAX_GLOBAL_SPEED_INCREASE_RATIO = 1.5;
    private static final boolean DEFAULT_ACTIVATE_REPULSION_FORCE_FROM_FIXED_POINTS = true;
    private static final boolean DEFAULT_ACTIVATE_ATTRACT_TO_CENTER_FORCE = true;
    private static final double DEFAULT_ITERATION_NUMBER_INCREASE_PERCENT = 0;

    private final int maxSteps;
    private final double repulsion;
    private final double edgeAttraction;
    private final double attractToCenter;
    private final double speedFactor;
    private final double maxSpeedFactor;
    private final double swingTolerance;
    private final double maxGlobalSpeedIncreaseRatio;
    private final boolean activateRepulsionForceFromFixedPoints;
    private final boolean activateAttractToCenterForce;
    private final double iterationNumberIncreasePercent;

    private Atlas2Parameters(
            int maxSteps,
            double repulsion,
            double edgeAttraction,
            double attractToCenter,
            double speedFactor,
            double maxSpeedFactor,
            double swingTolerance,
            double maxGlobalSpeedIncreaseRatio,
            boolean activateRepulsionForceFromFixedPoints,
            boolean activateAttractToCenterForce,
            double iterationNumberIncreasePercent
    ) {
        this.maxSteps = maxSteps;
        this.repulsion = repulsion;
        this.edgeAttraction = edgeAttraction;
        this.attractToCenter = attractToCenter;
        this.speedFactor = speedFactor;
        this.maxSpeedFactor = maxSpeedFactor;
        this.swingTolerance = swingTolerance;
        this.maxGlobalSpeedIncreaseRatio = maxGlobalSpeedIncreaseRatio;
        this.activateRepulsionForceFromFixedPoints = activateRepulsionForceFromFixedPoints;
        this.activateAttractToCenterForce = activateAttractToCenterForce;
        this.iterationNumberIncreasePercent = iterationNumberIncreasePercent;
    }

    public static class Builder {
        private int maxSteps = DEFAULT_MAX_STEPS;
        private double repulsion = DEFAULT_REPULSION;
        private double edgeAttraction = DEFAULT_EDGE_ATTRACTION;
        private double attractToCenter = DEFAULT_ATTRACT_TO_CENTER;
        private double speedFactor = DEFAULT_SPEED_FACTOR;
        private double maxSpeedFactor = DEFAULT_MAX_SPEED_FACTOR;
        private double swingTolerance = DEFAULT_SWING_TOLERANCE;
        private double maxGlobalSpeedIncreaseRatio = DEFAULT_MAX_GLOBAL_SPEED_INCREASE_RATIO;
        private boolean activateRepulsionForceFromFixedPoints = DEFAULT_ACTIVATE_REPULSION_FORCE_FROM_FIXED_POINTS;
        private boolean activateAttractToCenterForce = DEFAULT_ACTIVATE_ATTRACT_TO_CENTER_FORCE;
        private double iterationNumberIncreasePercent = DEFAULT_ITERATION_NUMBER_INCREASE_PERCENT;

        public Builder withMaxSteps(int maxSteps) {
            this.maxSteps = maxSteps;
            return this;
        }

        public Builder withRepulsion(double repulsion) {
            this.repulsion = repulsion;
            return this;
        }

        public Builder withEdgeAttraction(double attraction) {
            this.edgeAttraction = attraction;
            return this;
        }

        public Builder withAttractToCenter(double attractToCenter) {
            this.attractToCenter = attractToCenter;
            return this;
        }

        public Builder withSpeedFactor(double speedFactor) {
            this.speedFactor = speedFactor;
            return this;
        }

        public Builder withMaxSpeedFactor(double maxSpeedFactor) {
            this.maxSpeedFactor = maxSpeedFactor;
            return this;
        }

        public Builder withSwingTolerance(double swingTolerance) {
            this.swingTolerance = swingTolerance;
            return this;
        }

        public Builder withMaxGlobalSpeedIncreaseRatio(double maxGlobalSpeedIncreaseRatio) {
            this.maxGlobalSpeedIncreaseRatio = maxGlobalSpeedIncreaseRatio;
            return this;
        }

        public Builder withActivateRepulsionForceFromFixedPoints(boolean activateRepulsionForceFromFixedPoints) {
            this.activateRepulsionForceFromFixedPoints = activateRepulsionForceFromFixedPoints;
            return this;
        }

        public Builder withActivateAttractToCenterForce(boolean activateAttractToCenterForce) {
            this.activateAttractToCenterForce = activateAttractToCenterForce;
            return this;
        }

        public Builder withIterationNumberIncreasePercent(double iterationNumberIncreasePercent) {
            if (iterationNumberIncreasePercent < 0) {
                throw new IllegalArgumentException("iterationNumberIncreasePercent should be strictly positive, as the number of iterations cannot be reduced to less than the stopping criterion");
            }
            this.iterationNumberIncreasePercent = iterationNumberIncreasePercent;
            return this;
        }

        public Atlas2Parameters build() {
            return new Atlas2Parameters(
                    maxSteps,
                    repulsion,
                    edgeAttraction,
                    attractToCenter,
                    speedFactor,
                    maxSpeedFactor,
                    swingTolerance,
                    maxGlobalSpeedIncreaseRatio,
                    activateRepulsionForceFromFixedPoints,
                    activateAttractToCenterForce,
                    iterationNumberIncreasePercent
            );
        }
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    public double getRepulsion() {
        return repulsion;
    }

    public double getEdgeAttraction() {
        return edgeAttraction;
    }

    public double getAttractToCenter() {
        return attractToCenter;
    }

    public double getSpeedFactor() {
        return speedFactor;
    }

    public double getMaxSpeedFactor() {
        return maxSpeedFactor;
    }

    public double getSwingTolerance() {
        return swingTolerance;
    }

    public double getMaxGlobalSpeedIncreaseRatio() {
        return maxGlobalSpeedIncreaseRatio;
    }

    public boolean isActivateRepulsionForceFromFixedPoints() {
        return activateRepulsionForceFromFixedPoints;
    }

    public boolean isActivateAttractToCenterForce() {
        return activateAttractToCenterForce;
    }

    public double getIterationNumberIncreasePercent() {
        return iterationNumberIncreasePercent;
    }
}

