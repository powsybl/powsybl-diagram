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
public class HiddenComponentFeederInfo extends AbstractFeederInfo {

    private final double value;

    public HiddenComponentFeederInfo(String componentType, String leftLabel, String rightLabel) {
        this(componentType, leftLabel, rightLabel, null);
    }

    public HiddenComponentFeederInfo(String componentType, String leftLabel, String rightLabel, String userDefinedId) {
        super(componentType, leftLabel, rightLabel, userDefinedId);
        this.value = Double.NaN;
    }

    public HiddenComponentFeederInfo(String componentType, double value, DoubleFunction<String> formatter) {
        this(componentType, value, formatter, null);
    }

    public HiddenComponentFeederInfo(String componentType, double value, DoubleFunction<String> formatter, String userDefinedId) {
        super(componentType, null, formatter.apply(value), userDefinedId);
        this.value = value;
    }

    public HiddenComponentFeederInfo(String componentType, double value, String unit, BiFunction<Double, String, String> formatter, String userDefinedId) {
        super(componentType, null, formatter.apply(value, unit), userDefinedId);
        this.value = value;
    }

    public HiddenComponentFeederInfo(String componentType, double value, String unit, BiFunction<Double, String, String> formatter) {
        this(componentType, value, unit, formatter, (String) null);
    }

    public double getValue() {
        return value;
    }
}
