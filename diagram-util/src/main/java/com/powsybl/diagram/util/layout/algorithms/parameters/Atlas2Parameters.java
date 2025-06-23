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
    private static final double DEFAULT_BARNES_HUT_THETA = 1.2;

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
    private final double barnesHutTheta;

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
            double iterationNumberIncreasePercent,
            double barnesHutTheta
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
        this.barnesHutTheta = barnesHutTheta;
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
        private double barnesHutTheta = DEFAULT_BARNES_HUT_THETA;

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
         * Coefficient for the attraction force between points that share an edge, increasing this might help with
         * emphasizing clusters of points. It will also tend to make the graph smaller. Default is {@value DEFAULT_EDGE_ATTRACTION}
         * @param edgeAttraction the coefficient for the attraction force between points that have an edge together
         * @return the instance of this Builder with the `edgeAttraction` changed
         */
        public Builder withEdgeAttraction(double edgeAttraction) {
            this.edgeAttraction = edgeAttraction;
            return this;
        }

        /**
         * Coefficient for the force that attracts all points to the center of the 2D space. Smaller values will lead to a less dense graph.
         * Default is {@value DEFAULT_ATTRACT_TO_CENTER}
         * @param attractToCenter coefficient for the center attraction force
         * @return the instance of this Builder with the `attractToCenter` changed
         */
        public Builder withAttractToCenter(double attractToCenter) {
            this.attractToCenter = attractToCenter;
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
        public Builder withActivateAttractToCenterForce(boolean activateAttractToCenterForce) {
            this.activateAttractToCenterForce = activateAttractToCenterForce;
            return this;
        }

        /**
         * By how many iteration (in percent) you want to increase the run once the stopping condition was met.
         * The stopping condition generally stops when the graph is "good enough", but for specific use you might want a graph that looks better.
         * Increasing the number of iterations past the stopping condition will increase the visual quality of the graph, but it will also take longer.
         * This coefficient will also be directly the increase in runtime (e.g. if you use 10%, you will have a 10% longer runtime).
         * Default is {@value DEFAULT_ITERATION_NUMBER_INCREASE_PERCENT}
         * @param iterationNumberIncreasePercent how many more iterations (in percent) you want the run to last
         * @return the instance of this Builder with the `iterationNumberIncreasePercent` changed
         */
        public Builder withIterationNumberIncreasePercent(double iterationNumberIncreasePercent) {
            if (iterationNumberIncreasePercent < 0) {
                throw new IllegalArgumentException("iterationNumberIncreasePercent should be strictly positive, as the number of iterations cannot be reduced to less than the stopping criterion");
            }
            this.iterationNumberIncreasePercent = iterationNumberIncreasePercent;
	    return this;
	}
        public Builder withBarnesHutTheta(double barnesHutTheta) {
            if (barnesHutTheta < 0) {
                throw new IllegalArgumentException("The theta of the Barnes Hut optimization cannot be a negative value");
            }
            this.barnesHutTheta = barnesHutTheta;
            return this;
        }

        public Atlas2Parameters build() {
            return new Atlas2Parameters<>(
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
                    iterationNumberIncreasePercent,
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

    public double getBarnesHutTheta() {
        return barnesHutTheta;
    }
}

