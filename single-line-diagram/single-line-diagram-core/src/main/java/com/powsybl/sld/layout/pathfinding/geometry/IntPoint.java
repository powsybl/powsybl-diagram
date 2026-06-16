/**
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout.pathfinding.geometry;

/**
 * @author Dissoubray Nathan {@literal <nathan.dissoubray at rte-france.com>}
 */
public record IntPoint(int x, int y) {
    public static int dx(IntPoint first, IntPoint second) {
        return Integer.compare(second.x, first.x);
    }

    public static int dy(IntPoint first, IntPoint second) {
        return Integer.compare(second.y, first.y);
    }
}
