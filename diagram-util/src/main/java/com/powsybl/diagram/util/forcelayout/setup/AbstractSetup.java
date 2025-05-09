/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout.setup;

import com.powsybl.diagram.util.forcelayout.geometry.ForceGraph;

import java.util.Random;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public abstract class AbstractSetup<V, E> {
    // Suppress the warning about possible unsafe Random, because we use this for simulation and not cryptography
    @java.lang.SuppressWarnings("java:S2245")
    protected final Random random = new Random(3L);

    public abstract void setup(ForceGraph<V, E> forceGraph);
}
