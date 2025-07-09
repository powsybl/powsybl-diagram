/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.forcelayout.forces;

import com.powsybl.diagram.util.forcelayout.geometry.LayoutContext;
import com.powsybl.diagram.util.forcelayout.geometry.Point;
import com.powsybl.diagram.util.forcelayout.geometry.Vector2D;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public interface Force<V, E> {
    default void init(LayoutContext<V, E> layoutContext) {
        // most forces do not need anything to init, default is empty
    }

    /**
     *
     * @param vertex : the vertex to consider for the application of the force
     * @param point : the 2D point corresponding to the vertex parameter
     * @param layoutContext : information about the context of the layout
     * @return the calculated force to be applied to the point in the 2D space
     */
    Vector2D apply(V vertex, Point point, LayoutContext<V, E> layoutContext);
}
