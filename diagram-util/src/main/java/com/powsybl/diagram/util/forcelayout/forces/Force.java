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
     * Return the force vector which is the sum of the influence of all other points of the graph on this point<br>
     * Some forces use all other points to calculate this, such as repulsion force. Other only need to consider points that have an edge with
     *  this point (such as edge attraction forces), and other don't even need the other points of the graph (forces that attract a point to the center)
     * @param vertex : the vertex to consider for the application of the force
     * @param point : the 2D point corresponding to the vertex parameter
     * @param layoutContext : information about the context of the layout
     * @return the calculated force to be applied to the point in the 2D space, sum of the influence of each other point in the graph on this point (this influence can be 0 for some points, depending on the force)
     */
    Vector2D apply(V vertex, Point point, LayoutContext<V, E> layoutContext);
}
