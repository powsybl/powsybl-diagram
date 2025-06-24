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
    private static final double DEFAULT_ATTRACTION = 1;
    private static final double DEFAULT_GRAVITY = 0.001;
    private static final double DEFAULT_SPEED_FACTOR = 1;
    private static final double DEFAULT_MAX_SPEED_FACTOR = 10;
    private static final double DEFAULT_SWING_TOLERANCE = 1;
    private static final double DEFAULT_MAX_GLOBAL_SPEED_INCREASE_RATIO = 1.5;
    private static final boolean DEFAULT_REPULSION_FROM_FIXED_POINTS = true;
    private static final boolean DEFAULT_ATTRACT_TO_CENTER = true;
    private static final double DEFAULT_BARNES_HUT_THETA = 1.2;

    private final int maxSteps;
    private final double repulsion;
    private final double attraction;
    private final double gravity;
    private final double speedFactor;
    private final double maxSpeedFactor;
    private final double swingTolerance;
    private final double maxGlobalSpeedIncreaseRatio;
    private final boolean repulsionForceFromFixedPoints;
    private final boolean attractToCenterForce;
    private final double barnesHutTheta;

    private Atlas2Parameters(
            int maxSteps,
            double repulsion,
            double attraction,
            double gravity,
            double speedFactor,
            double maxSpeedFactor,
            double swingTolerance,
            double maxGlobalSpeedIncreaseRatio,
            boolean repulsionForceFromFixedPoints,
            boolean attractToCenterForce,
            double barnesHutTheta
    ) {
        this.maxSteps = maxSteps;
        this.repulsion = repulsion;
        this.attraction = attraction;
        this.gravity = gravity;
        this.speedFactor = speedFactor;
        this.maxSpeedFactor = maxSpeedFactor;
        this.swingTolerance = swingTolerance;
        this.maxGlobalSpeedIncreaseRatio = maxGlobalSpeedIncreaseRatio;
        this.repulsionForceFromFixedPoints = repulsionForceFromFixedPoints;
        this.attractToCenterForce = attractToCenterForce;
        this.barnesHutTheta = barnesHutTheta;
    }

    public static class Builder {
        private int maxSteps = DEFAULT_MAX_STEPS;
        private double repulsion = DEFAULT_REPULSION;
        private double attraction = DEFAULT_ATTRACTION;
        private double gravity = DEFAULT_GRAVITY;
        private double speedFactor = DEFAULT_SPEED_FACTOR;
        private double maxSpeedFactor = DEFAULT_MAX_SPEED_FACTOR;
        private double swingTolerance = DEFAULT_SWING_TOLERANCE;
        private double maxGlobalSpeedIncreaseRatio = DEFAULT_MAX_GLOBAL_SPEED_INCREASE_RATIO;
        private boolean repulsionForceFromFixedPoints = DEFAULT_REPULSION_FROM_FIXED_POINTS;
        private boolean attractToCenterForce = DEFAULT_ATTRACT_TO_CENTER;
        private double barnesHutTheta = DEFAULT_BARNES_HUT_THETA;

        public Builder withMaxSteps(int maxSteps) {
            this.maxSteps = maxSteps;
            return this;
        }

        public Builder withRepulsion(double repulsion) {
            this.repulsion = repulsion;
            return this;
        }

        public Builder withAttraction(double attraction) {
            this.attraction = attraction;
            return this;
        }

        public Builder withGravity(double gravity) {
            this.gravity = gravity;
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

        public Builder withRepulsionForceFromFixedPoints(boolean repulsionForceFromFixedPoints) {
            this.repulsionForceFromFixedPoints = repulsionForceFromFixedPoints;
            return this;
        }

        public Builder withAttractToCenterForce(boolean attractToCenterForce) {
            this.attractToCenterForce = attractToCenterForce;
            return this;
        }

        public Builder withBarnesHutTheta(double barnesHutTheta) {
            if (barnesHutTheta < 0) {
                throw new IllegalArgumentException("The theta of the Barnes Hut optimization cannot be a negative value");
            }
            this.barnesHutTheta = barnesHutTheta;
            return this;
        }

        /// By default, the Barnes-Hut optimisation is activated with theta = 1.2
        /// If your network has a small number of nodes (less than 500), Atlas might finish faster by deactivating Barnes-Hut
        /// You can do so by using withBarnesHutTheta(0)
        /// This might be interesting to do especially if you need to run Atlas on a lot of small networks
	
        public Atlas2Parameters build() {
            return new Atlas2Parameters<>(
                    maxSteps,
                    repulsion,
                    attraction,
                    gravity,
                    speedFactor,
                    maxSpeedFactor,
                    swingTolerance,
                    maxGlobalSpeedIncreaseRatio,
                    repulsionForceFromFixedPoints,
                    attractToCenterForce,
                    barnesHutTheta
            );
        }
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    public double getRepulsion() {
        return repulsion;
    }

    public double getAttraction() {
        return attraction;
    }

    public double getGravity() {
        return gravity;
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

    public boolean isRepulsionForceFromFixedPoints() {
        return repulsionForceFromFixedPoints;
    }

    public boolean isAttractToCenterForce() {
        return attractToCenterForce;
    }

    public double getBarnesHutTheta() {
        return barnesHutTheta;
    }
}

