/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout.layouts.quadtreeupdateschedule;

/// See "It Pays to Be Lazy: Reusing Force Approximations to Compute Better Graph Layouts Faster"
/// By Robert Gove, Two Six Labs, for an explanation

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class ConstantSchedule {

    private final int increment;
    private int nextUpdate = 0;

    public ConstantSchedule(int increment) {
        if (increment <= 0) {
            throw new IllegalArgumentException("The increment for the constant schedule has to be strictly positive");
        } else {
            this.increment = increment;
        }
    }

    /// Know if it's time to recalculate the quadtree
    public boolean isTimeToUpdate(int index) {
        if (index == nextUpdate) {
            nextUpdate += increment;
            return true;
        } else {
            return false;
        }
    }
}
