/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout.forces.parameters;

import com.powsybl.diagram.util.forcelayout.geometry.Quadtree;
import com.powsybl.diagram.util.forcelayout.geometry.QuadtreeContainer;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class IntensityEffectFromFIxedNodesBarnesHutParameters extends IntensityEffectFromFixedNodesParameters {
    private final double barnesHutTheta;
    private final QuadtreeContainer quadtreeContainer;

    public IntensityEffectFromFIxedNodesBarnesHutParameters(double forceIntensity, boolean effectFromFixedNodes, double barnesHutTheta, QuadtreeContainer quadtreeContainer) {
        super(forceIntensity, effectFromFixedNodes);
        this.barnesHutTheta = barnesHutTheta;
        this.quadtreeContainer = quadtreeContainer;
    }

    public double getBarnesHutTheta() {
        return barnesHutTheta;
    }

    public Quadtree getQuadtree() {
        return quadtreeContainer.getQuadtree();
    }
}

