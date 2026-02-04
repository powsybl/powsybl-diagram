/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.layout.forces.util;

import com.powsybl.diagram.util.layout.geometry.LayoutContext;

/**
 * @author Dissoubray Nathan {@literal <nathan.dissoubray at rte-france.com>}
 */
public record NoOverlapPointSize<V, E>(double pointSizeScale, double pointSizeOffset) {

    public double calculatePointSize(LayoutContext<V, E> layoutContext) {
        return this.pointSizeScale * layoutContext.getAllPoints().size() + this.pointSizeOffset;
    }
}
