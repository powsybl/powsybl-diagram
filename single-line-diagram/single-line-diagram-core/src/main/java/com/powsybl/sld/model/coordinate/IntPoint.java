/**
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.model.coordinate;

/**
 * @author Dissoubray Nathan {@literal <nathan.dissoubray at rte-france.com>}
 */
public record IntPoint(int x, int y) {
    public static int dx(IntPoint from, IntPoint towards) {
        return Integer.compare(towards.x, from.x);
    }

    public static int dy(IntPoint from, IntPoint towards) {
        return Integer.compare(towards.y, from.y);
    }

    public IntPoint(Point other) {
        this((int) other.getX(), (int) other.getY());
    }

    public IntPoint(IntPoint other) {
        this(other.x, other.y);
    }

    public IntPoint getSegmentDirection(IntPoint towards) {
        return new IntPoint(towards.x - x, towards.y - y);
    }

    public IntPoint getUnitSegmentDirection(IntPoint towards) {
        return new IntPoint(dx(this, towards), dy(this, towards));
    }

    public IntPoint getShiftedPoint(IntPoint shift) {
        return new IntPoint(x + shift.x, y + shift.y);
    }
}
