/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.layout.forces.util;

/**
 * @author Dissoubray Nathan {@literal <nathan.dissoubray at rte-france.com>}
 */
public class NoOverlapPointSize {
    private final double pointSizeScale;
    private final double pointSizeOffset;
    private double pointSize = 15;

    public NoOverlapPointSize(double pointSizeScale, double pointSizeOffset) {
        this.pointSizeScale = pointSizeScale;
        this.pointSizeOffset = pointSizeOffset;
    }

    public void calculatePointSize(double graphSize) {
        this.pointSize = this.pointSizeScale * graphSize + this.pointSizeOffset;
    }

    public double getPointSize() {
        return pointSize;
    }
}
