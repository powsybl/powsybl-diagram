/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.forcelayout.layouts;

import com.powsybl.diagram.util.forcelayout.forces.AbstractForce;
import com.powsybl.diagram.util.forcelayout.forces.forceparameter.ForceParameter;
import com.powsybl.diagram.util.forcelayout.geometry.ForceGraph;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public abstract class AbstractLayoutAlgorithm<V, E> {
    protected List<AbstractForce<V, E, ? extends ForceParameter>> forces;

    public abstract void calculateLayout(ForceGraph<V, E> forceGraph);

    AbstractLayoutAlgorithm(List<AbstractForce<V, E, ? extends ForceParameter>> forces) {
        this.forces = forces;
    }

    AbstractLayoutAlgorithm() {
        this.forces = new ArrayList<>();
    }
}
