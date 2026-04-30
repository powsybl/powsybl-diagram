/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.layout.forces.util;

import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * Util class used to get perfectly overlapping points unstuck by providing a small random force
 * @author Dissoubray Nathan {@literal <nathan.dissoubray at rte-france.com>}
 */
public final class RandomForce {

    private final RandomGenerator randomGenerator = new Random(45L);

    public RandomGenerator getRandomGenerator() {
        return randomGenerator;
    }
}
