/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.layout.postprocessing;

import com.powsybl.diagram.util.layout.geometry.LayoutContext;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public interface PostProcessing<V, E> {
    /**
     * Make some final changes to the layout, this generally makes local changes, as global changes are made by {@link com.powsybl.diagram.util.layout.algorithms.LayoutAlgorithm LayoutAlgorithm}
     * @param layoutContext the context of the layout, the position of the points and the graph
     */
    void run(LayoutContext<V, E> layoutContext);
}

