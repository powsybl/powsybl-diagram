/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.components;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class ComponentTypeName {
    public static final String BATTERY = "BATTERY";
    public static final String CAPACITOR = "CAPACITOR";
    public static final String INDUCTOR = "INDUCTOR";
    public static final String STATIC_VAR_COMPENSATOR = "STATIC_VAR_COMPENSATOR";
    public static final String LOAD = "LOAD";
    public static final String UNKNOWN_COMPONENT = "UNKNOWN_COMPONENT";

    protected ComponentTypeName() {
        throw new AssertionError();
    }
}
