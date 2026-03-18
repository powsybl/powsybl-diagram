/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.DoubleFunction;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public class ValueFeederInfo extends AbstractFeederInfo {

    private final LabelProvider.LabelDirection labelDirection;
    private final double value;

    public ValueFeederInfo(String componentType, LabelProvider.LabelDirection labelDirection, String leftLabel, String rightLabel) {
        this(componentType, labelDirection, leftLabel, rightLabel, null);
    }

    public ValueFeederInfo(String componentType, LabelProvider.LabelDirection labelDirection, String leftLabel, String rightLabel, String userDefinedId) {
        super(componentType, leftLabel, rightLabel, userDefinedId);
        this.labelDirection = Objects.requireNonNull(labelDirection);
        this.value = Double.NaN;
    }

    public ValueFeederInfo(String componentType, double value, DoubleFunction<String> formatter) {
        this(componentType, value, formatter, null);
    }

    public ValueFeederInfo(String componentType, LabelProvider.LabelDirection labelDirection, double value, DoubleFunction<String> formatter) {
        this(componentType, labelDirection, value, formatter, null);
    }

    public ValueFeederInfo(String componentType, double value, DoubleFunction<String> formatter, String userDefinedId) {
        super(componentType, null, formatter.apply(value), userDefinedId);
        this.labelDirection = Objects.requireNonNull(getLabelDirection(value));
        this.value = value;
    }

    public ValueFeederInfo(String componentType, LabelProvider.LabelDirection labelDirection, double value, DoubleFunction<String> formatter, String userDefinedId) {
        super(componentType, null, formatter.apply(value), userDefinedId);
        this.labelDirection = Objects.requireNonNull(labelDirection);
        this.value = value;
    }

    public ValueFeederInfo(String componentType, double value, String unit, BiFunction<Double, String, String> formatter, String userDefinedId) {
        super(componentType, null, formatter.apply(value, unit), userDefinedId);
        this.labelDirection = Objects.requireNonNull(getLabelDirection(value));
        this.value = value;
    }

    public ValueFeederInfo(String componentType, LabelProvider.LabelDirection labelDirection, double value, String unit, BiFunction<Double, String, String> formatter, String userDefinedId) {
        super(componentType, null, formatter.apply(value, unit), userDefinedId);
        this.labelDirection = Objects.requireNonNull(labelDirection);
        this.value = value;
    }

    public ValueFeederInfo(String componentType, double value, String unit, BiFunction<Double, String, String> formatter) {
        this(componentType, value, unit, formatter, null);
    }

    public ValueFeederInfo(String componentType, LabelProvider.LabelDirection labelDirection, double value, String unit, BiFunction<Double, String, String> formatter) {
        this(componentType, labelDirection, value, unit, formatter, null);
    }

    private static LabelProvider.LabelDirection getLabelDirection(double value) {
        return value > 0 ? LabelProvider.LabelDirection.OUT : LabelProvider.LabelDirection.IN;
    }

    public LabelProvider.LabelDirection getDirection() {
        return labelDirection;
    }

    public double getValue() {
        return value;
    }
}
