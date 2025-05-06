/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.forcelayout.layouts.layoutsparameters;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class SpringyParameters extends AbstractLayoutParameters {
    private static final int DEFAULT_MAX_STEPS = 400;
    private static final double DEFAULT_MIN_ENERGY_THRESHOLD = 0.001;
    private static final double DEFAULT_DELTA_TIME = 0.1;
    private static final double DEFAULT_REPULSION = 800.0;
    private static final double DEFAULT_FRICTION = 500;
    private static final double DEFAULT_MAX_SPEED = 100;

    private int maxSteps;
    private double minEnergyThreshold;
    private double deltaTime;
    private double repulsion;
    private double friction;
    private double maxSpeed;

    public SpringyParameters() {
        super();
        this.parameterType = LayoutEnum.SPRINGY;

        this.maxSteps = DEFAULT_MAX_STEPS;
        this.minEnergyThreshold = DEFAULT_MIN_ENERGY_THRESHOLD;
        this.deltaTime = DEFAULT_DELTA_TIME;
        this.repulsion = DEFAULT_REPULSION;
        this.friction = DEFAULT_FRICTION;
        this.maxSpeed = DEFAULT_MAX_SPEED;
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    public void setMaxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
    }

    public double getMinEnergyThreshold() {
        return minEnergyThreshold;
    }

    public void setMinEnergyThreshold(double minEnergyThreshold) {
        this.minEnergyThreshold = minEnergyThreshold;
    }

    public double getDeltaTime() {
        return deltaTime;
    }

    public void setDeltaTime(double deltaTime) {
        this.deltaTime = deltaTime;
    }

    public double getRepulsion() {
        return repulsion;
    }

    public void setRepulsion(double repulsion) {
        this.repulsion = repulsion;
    }

    public double getFriction() {
        return friction;
    }

    public void setFriction(double friction) {
        this.friction = friction;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }
}
