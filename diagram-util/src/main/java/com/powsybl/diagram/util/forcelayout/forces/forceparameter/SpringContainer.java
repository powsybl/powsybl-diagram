/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.forcelayout.forces.forceparameter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class SpringContainer<E> implements ForceParameter {
    private final Map<E, SpringParameter> springs;

    public SpringContainer() {
        this.springs = new HashMap<>();
    }

    public SpringContainer(Map<E, SpringParameter> springs) {
        this.springs = springs;
    }

    public Map<E, SpringParameter> getSprings() {
        return springs;
    }
}
