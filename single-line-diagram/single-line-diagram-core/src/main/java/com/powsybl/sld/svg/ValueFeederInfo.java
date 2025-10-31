/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import java.util.function.BiFunction;
import java.util.function.DoubleFunction;

/**
 * @author Caroline Jeandat <caroline.jeandat at rte-france.com>
 */
public class ValueFeederInfo extends AbstractFeederInfo {

    public ValueFeederInfo(String componentType, double value, DoubleFunction<String> formatter) {
        super(componentType, null, formatter.apply(value), null);
    }

    public ValueFeederInfo(String componentType, double value, String unit, BiFunction<Double, String, String> formatter) {
        super(componentType, null, formatter.apply(value, unit), null);
    }
}
