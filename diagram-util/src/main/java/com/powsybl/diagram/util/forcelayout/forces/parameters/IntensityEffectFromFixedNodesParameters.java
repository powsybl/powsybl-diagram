/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.forcelayout.forces.parameters;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class IntensityEffectFromFixedNodesParameters extends IntensityParameter {
    private boolean effectFromFixedNodes = true;

    public IntensityEffectFromFixedNodesParameters(double forceIntensity, boolean effectFromFixedNodes) {
        super(forceIntensity);
        this.effectFromFixedNodes = effectFromFixedNodes;
    }

    public IntensityEffectFromFixedNodesParameters() {
        super();
        // effectFromFixedNodes is already set to true by default
    }

    public boolean isEffectFromFixedNodes() {
        return effectFromFixedNodes;
    }

    public void setEffectFromFixedNodes(boolean effectFromFixedNodes) {
        this.effectFromFixedNodes = effectFromFixedNodes;
    }
}
