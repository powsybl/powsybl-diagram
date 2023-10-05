/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.algo;

import java.util.*;

/**
 * A 2D point on the grid
 */
public record Point(int x, int y) {
    public boolean equals(Point point) {
        Objects.requireNonNull(point);

        // Return true if the fields match:
        return x == point.x && y == point.y;
    }

    @Override
    public String toString() {
        return "Point = {" + x + ", " + y + '}';
    }
}
