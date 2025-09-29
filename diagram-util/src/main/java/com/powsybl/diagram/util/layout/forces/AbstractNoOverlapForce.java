/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.layout.forces;

import com.powsybl.diagram.util.layout.geometry.LayoutContext;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public abstract class AbstractNoOverlapForce<V, E> implements Force<V, E> {
    protected final double pointSizeScale;
    protected final double pointSizeOffset;
    protected double pointSize = 15;

    protected AbstractNoOverlapForce(double pointSizeScale, double pointSizeOffset) {
        this.pointSizeScale = pointSizeScale;
        this.pointSizeOffset = pointSizeOffset;
    }

    @Override
    public void init(LayoutContext<V, E> layoutContext) {
        this.pointSize = pointSizeScale * layoutContext.getAllPoints().size() + pointSizeOffset;
    }
}

