/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.layout.algorithms.quadtreeupdateschedule;

/// See "It Pays to Be Lazy: Reusing Force Approximations to Compute Better Graph Layouts Faster"
/// By Robert Gove, Two Six Labs, for an explanation

/**
 * A schedule that says it's time to update every X time step
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class ConstantSchedule {

    private final int increment;
    private int nextUpdate = 0;

    /**
     * Build a constant update schedule
     * @param increment update the quadtree every increment step
     */
    public ConstantSchedule(int increment) {
        if (increment <= 0) {
            throw new IllegalArgumentException("The increment for the constant schedule has to be strictly positive");
        } else {
            this.increment = increment;
        }
    }

    /**
     * Check if it's time to update
     * @param index the time step we are at
     * @return true if the index is one we need to update the quadtree at, false otherwise
     */
    public boolean isTimeToUpdate(int index) {
        if (index == nextUpdate) {
            nextUpdate += increment;
            return true;
        } else {
            return false;
        }
    }
}
