/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.forcelayout.forces.forceparameter;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class IntensityParameter implements ForceParameter {
    private double forceIntensity;

    public IntensityParameter(double forceIntensity) {
        this.forceIntensity = forceIntensity;
    }

    public IntensityParameter() {
        this(1);
    }

    public double getForceIntensity() {
        return forceIntensity;
    }

    public void setForceIntensity(double forceIntensity) {
        this.forceIntensity = forceIntensity;
    }
}
