/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.layout.forces;

import com.powsybl.diagram.util.layout.geometry.LayoutContext;
import com.powsybl.diagram.util.layout.geometry.Point;

import java.util.Map;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public abstract class AbstractByEdgeNumberForce<V, E> implements Force<V, E> {
    @Override
    public void init(LayoutContext<V, E> layoutContext) {
        for (Map.Entry<V, Point> entry : layoutContext.getAllPoints().entrySet()) {
            entry.getValue().setPointVertexDegree(layoutContext.getSimpleGraph().degreeOf(entry.getKey()));
        }
    }
}

