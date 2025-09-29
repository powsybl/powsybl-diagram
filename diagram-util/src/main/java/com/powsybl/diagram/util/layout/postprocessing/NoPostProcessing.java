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
 * Used when no post-processing is required
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class NoPostProcessing<V, E> implements PostProcessing<V, E> {
    @Override
    public void run(LayoutContext<V, E> layoutContext) {
        //empty because we don't need to do anything
    }
}

