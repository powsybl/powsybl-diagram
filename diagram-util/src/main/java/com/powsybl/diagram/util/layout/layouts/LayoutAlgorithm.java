/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.layout.layouts;

import com.powsybl.diagram.util.layout.forces.Force;
import com.powsybl.diagram.util.layout.geometry.LayoutContext;

import java.util.List;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public interface LayoutAlgorithm<V, E> {
    /**
     * Place the <code>movingPoints</code> of <code>layoutContext</code> with the goal of it looking visually pretty
     * This can expect all fields of <code>layoutContext</code> to be correctly set up by the {@link com.powsybl.diagram.util.layout.setup.Setup#run(LayoutContext) Setup.run} method
     * @param layoutContext the context of the layout, the graph and the position of points
     */
    void run(LayoutContext<V, E> layoutContext);

    default void initAll(List<Force<V, E>> forces, LayoutContext<V, E> layoutContext) {
        for (Force<V, E> force : forces) {
            force.init(layoutContext);
        }
    }
}
