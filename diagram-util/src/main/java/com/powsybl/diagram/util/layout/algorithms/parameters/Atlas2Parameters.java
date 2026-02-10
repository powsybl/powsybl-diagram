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
public final class Atlas2Parameters {
    private static final int DEFAULT_MAX_STEPS = 6000;
    private static final double DEFAULT_REPULSION_INTENSITY = 4;
    private static final double DEFAULT_EDGE_ATTRACTION_INTENSITY = 1;
    private static final double DEFAULT_ATTRACT_TO_CENTER_INTENSITY = 0.001;
    private static final double DEFAULT_SPEED_FACTOR = 1;
    private static final double DEFAULT_MAX_SPEED_FACTOR = 10;
    private static final double DEFAULT_SWING_TOLERANCE = 1;
    private static final double DEFAULT_MAX_GLOBAL_SPEED_INCREASE_RATIO = 1.5;
    private static final boolean DEFAULT_REPULSION_FROM_FIXED_POINTS_ENABLED = true;
    private static final boolean DEFAULT_ATTRACT_TO_CENTER_ENABLED = true;

    private final int maxSteps;
    private final double repulsionIntensity;
    private final double edgeAttractionIntensity;
    private final double attractToCenterIntensity;
    private final double speedFactor;
    private final double maxSpeedFactor;
    private final double swingTolerance;
    private final double maxGlobalSpeedIncreaseRatio;
    private final boolean repulsionFromFixedPointsEnabled;
    private final boolean attractToCenterEnabled;

    private Atlas2Parameters(
            int maxSteps,
            double repulsionIntensity,
            double edgeAttractionIntensity,
            double attractToCenterIntensity,
            double speedFactor,
            double maxSpeedFactor,
            double swingTolerance,
            double maxGlobalSpeedIncreaseRatio,
            boolean repulsionFromFixedPointsEnabled,
            boolean attractToCenterEnabled
    ) {
        this.maxSteps = maxSteps;
        this.repulsionIntensity = repulsionIntensity;
        this.edgeAttractionIntensity = edgeAttractionIntensity;
        this.attractToCenterIntensity = attractToCenterIntensity;
        this.speedFactor = speedFactor;
        this.maxSpeedFactor = maxSpeedFactor;
        this.swingTolerance = swingTolerance;
        this.maxGlobalSpeedIncreaseRatio = maxGlobalSpeedIncreaseRatio;
        this.repulsionFromFixedPointsEnabled = repulsionFromFixedPointsEnabled;
        this.attractToCenterEnabled = attractToCenterEnabled;
    }

    public static class Builder {
        private int maxSteps = DEFAULT_MAX_STEPS;
        private double repulsionIntensity = DEFAULT_REPULSION_INTENSITY;
        private double edgeAttractionIntensity = DEFAULT_EDGE_ATTRACTION_INTENSITY;
        private double attractToCenterIntensity = DEFAULT_ATTRACT_TO_CENTER_INTENSITY;
        private double speedFactor = DEFAULT_SPEED_FACTOR;
        private double maxSpeedFactor = DEFAULT_MAX_SPEED_FACTOR;
        private double swingTolerance = DEFAULT_SWING_TOLERANCE;
        private double maxGlobalSpeedIncreaseRatio = DEFAULT_MAX_GLOBAL_SPEED_INCREASE_RATIO;
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
         * Coefficient for the attraction force between points that share an edge, increasing this might help with
         * emphasizing clusters of points. It will also tend to make the graph smaller. Default is {@value DEFAULT_EDGE_ATTRACTION_INTENSITY}
         * @param edgeAttractionIntensity the coefficient for the attraction force between points that have an edge together
         * @return the instance of this Builder with the `edgeAttractionIntensity` changed
         */
        public Builder withEdgeAttractionIntensity(double edgeAttractionIntensity) {
            this.edgeAttractionIntensity = edgeAttractionIntensity;
            return this;
        }

        /**
         * Coefficient for the force that attracts all points to the center of the 2D space. Smaller values will lead to a less dense graph.
         * Default is {@value DEFAULT_ATTRACT_TO_CENTER_INTENSITY}
         * @param attractToCenterIntensity coefficient for the center attraction force
         * @return the instance of this Builder with the `attractToCenterIntensity` changed
         */
        public Builder withAttractToCenterIntensity(double attractToCenterIntensity) {
            this.attractToCenterIntensity = attractToCenterIntensity;
            return this;
        }

        /**
         * Coefficient used to calculate individual point speed factor based on the global graph speed.
         * The link between global and local speed is not a simple multiplication by this coefficient, but it is used in the calculation.
         * If this is lower, points will be slower.
         * A lower value might give worse results in terms of convergence speed, but it might help with stability. Default is {@value DEFAULT_SPEED_FACTOR}
         * @param speedFactor coefficient for determining local speed compared to global speed
         * @return the instance of this Builder with the `speedFactor` changed
         */
        public Builder withSpeedFactor(double speedFactor) {
            this.speedFactor = speedFactor;
            return this;
        }

        /**
         * The maximum factor for local speed compared to global speed. Lowering this might mean slower convergence, but it improves
         * stability (ie points might swing less around their stability position). Default is {@value DEFAULT_MAX_SPEED_FACTOR}
         * @param maxSpeedFactor how fast can a point be compared to the global speed of the graph
         * @return the instance of this Builder with the `maxSpeedFactor` changed
         */
        public Builder withMaxSpeedFactor(double maxSpeedFactor) {
            this.maxSpeedFactor = maxSpeedFactor;
            return this;
        }

        /**
         * How much do we accept swing contributing to the global speed of the graph. A lower value means that we accept less swinging.
         * If this value is too low, the global speed of the graph will be low and convergence will slow down. If it's too high, we can
         * observe erratic behaviour in the way the points move. You probably shouldn't change this unless you know what you are doing.
         * Default is {@value DEFAULT_SWING_TOLERANCE}
         * @param swingTolerance how much swing do we accept compared to traction (ie how much we prefer swinging around the stability position compared to going towards it)
         * @return the instance of this Builder with the `swingTolerance` changed
         */
        public Builder withSwingTolerance(double swingTolerance) {
            if (swingTolerance <= 0) {
                throw new IllegalArgumentException("swingTolerance should be strictly positive");
            }
            this.swingTolerance = swingTolerance;
            return this;
        }

        /**
         * How much can the global speed increase between each step. Higher means that the graph will reach a good global speed faster,
         * but it might lead to more erratic behaviour between each step. Default is {@value DEFAULT_MAX_GLOBAL_SPEED_INCREASE_RATIO}
         * @param maxGlobalSpeedIncreaseRatio what is the maximum increase in speed allowed between each step
         * @return the instance of this Builder with the `maxGlobalSpeedIncreaseRatio` changed
         */
        public Builder withMaxGlobalSpeedIncreaseRatio(double maxGlobalSpeedIncreaseRatio) {
            this.maxGlobalSpeedIncreaseRatio = maxGlobalSpeedIncreaseRatio;
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

        public Atlas2Parameters build() {
            return new Atlas2Parameters(
                    maxSteps,
                    repulsionIntensity,
                    edgeAttractionIntensity,
                    attractToCenterIntensity,
                    speedFactor,
                    maxSpeedFactor,
                    swingTolerance,
                    maxGlobalSpeedIncreaseRatio,
                    repulsionFromFixedPointsEnabled,
                    attractToCenterEnabled
            );
        }
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    public double getRepulsionIntensity() {
        return repulsionIntensity;
    }

    public double getEdgeAttractionIntensity() {
        return edgeAttractionIntensity;
    }

    public double getAttractToCenterIntensity() {
        return attractToCenterIntensity;
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

    public boolean isRepulsionFromFixedPointsEnabled() {
        return repulsionFromFixedPointsEnabled;
    }

    public boolean isAttractToCenterEnabled() {
        return attractToCenterEnabled;
    }

}

