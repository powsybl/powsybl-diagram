/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout.geometry;

import java.util.Objects;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public record Edge(Point first, Point second) {
    public double length() {
        return first.distanceTo(second);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Edge e)) {
            return false;
        }
        return this.first.equals(e.first()) && this.second.equals(e.second()) || this.first.equals(e.second()) && this.second.equals(e.first());
    }

    @Override
    public int hashCode() {
        // using min will concentrate the values in a hash towards lower values which might not be good for spreading evenly the key, value pairs,
        // but we want to ensure two edges that just have their first and second reversed still have the same hash, to do the same as equals
        return Math.min(Objects.hash(first, second), Objects.hash(second, first));
    }
}
