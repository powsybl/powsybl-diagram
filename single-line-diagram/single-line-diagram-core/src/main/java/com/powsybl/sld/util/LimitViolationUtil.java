/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import com.powsybl.iidm.network.BusbarSection;

/**
 * @author jamal kheyyad {@literal <jamal.kheyyad at rte-international.com>}
 */
public final class LimitViolationUtil {
    private LimitViolationUtil() {
    }

    // this code is an adaptation of the code from the OpenLoadFlow project
    // see the original code at
    // LimitViolationManager.java
    public static boolean detectBusViolations(BusbarSection bus) {
        double nominalV = bus.getTerminal().getVoltageLevel().getNominalV();
        double busV = bus.getV();
        double highVoltageLimit = bus.getTerminal().getVoltageLevel().getHighVoltageLimit() / nominalV;
        double lowVoltageLimit = bus.getTerminal().getVoltageLevel().getLowVoltageLimit() / nominalV;
        if (!Double.isNaN(highVoltageLimit) && busV > highVoltageLimit) {
            return true;
        }
        return !Double.isNaN(lowVoltageLimit) && busV < lowVoltageLimit;
    }
}
