/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.layout.forces.util;

import com.powsybl.diagram.util.layout.geometry.Vector2D;

import java.util.Random;

/**
 * Util class used to get perfectly overlapping points unstuck by providing a small random force
 * @author Dissoubray Nathan {@literal <nathan.dissoubray at rte-france.com>}
 */
public final class RandomForce {
    private static Random random;

    private RandomForce() {
        //util class, no constructor
    }

    /**
     * Use this on points that have equal positions, to get them separated
     * @return a small random force that will be different for each call of this function
     */
    public static Vector2D getRandomForce() {
        if (random == null) {
            random = new Random(45L);
        }
        return new Vector2D(random.nextDouble(1, 2), random.nextDouble(1, 2));
    }
}
