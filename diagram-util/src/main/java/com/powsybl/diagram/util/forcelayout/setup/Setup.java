/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout.setup;

import com.powsybl.diagram.util.forcelayout.geometry.LayoutContext;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public interface Setup<V, E> {
    /**
     * Set up the layout context so the layout algorithm can run on it properly<br>
     * An implementation of a setup is expected to do at least the following:<br>
     * - fill the <code>fixedPoints</code> and <code>movingPoints</code> Map of <code>layoutContext</code> depending on if the vertex are in <code>fixedNodes</code> or not<br>
     * - use <code>initialPoints</code> to give a starting position to all the points, or create a position if no initial position is given<br>
     * Regarding the initial position, take care that the created positions do not overlap too much. Some forces such as repulsion forces will not work if points are at the exact same place<br>
     * It is therefore ill-advised to put all the points on (0, 0) for example.
     * For a simple example, see {@link com.powsybl.diagram.util.forcelayout.setup.SimpleSetup}
     * @param layoutContext the context of the layout, the graph and the position of points
     */
    void run(LayoutContext<V, E> layoutContext);
}
