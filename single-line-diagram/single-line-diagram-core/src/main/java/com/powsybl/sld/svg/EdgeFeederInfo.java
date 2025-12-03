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
public class EdgeFeederInfo extends AbstractFeederInfo {

    private final boolean directional;
    private final LabelProvider.LabelDirection labelDirection;

    private final double value;

    public EdgeFeederInfo(String componentType, LabelProvider.LabelDirection labelDirection, String leftLabel, String rightLabel) {
        this(componentType, labelDirection, leftLabel, rightLabel, null);
    }

    public EdgeFeederInfo(String componentType, LabelProvider.LabelDirection labelDirection, String leftLabel, String rightLabel, String userDefinedId) {
        super(componentType, leftLabel, rightLabel, userDefinedId);
        this.directional = true;
        this.labelDirection = Objects.requireNonNull(labelDirection);
        this.value = Double.NaN;
    }

    public EdgeFeederInfo(String componentType, boolean directional, double value, DoubleFunction<String> formatter) {
        this(componentType, directional, value, formatter, null);
    }

    public EdgeFeederInfo(String componentType, double value, DoubleFunction<String> formatter) {
        this(componentType, value, formatter, null);
    }

    public EdgeFeederInfo(String componentType, boolean directional, double value, DoubleFunction<String> formatter, String userDefinedId) {
        super(componentType, null, formatter.apply(value), userDefinedId);
        this.directional = directional;
        this.labelDirection = Objects.requireNonNull(getLabelDirection(value));
        this.value = value;
    }

    public EdgeFeederInfo(String componentType, double value, DoubleFunction<String> formatter, String userDefinedId) {
        super(componentType, null, formatter.apply(value), userDefinedId);
        this.directional = true;
        this.labelDirection = Objects.requireNonNull(getLabelDirection(value));
        this.value = value;
    }

    public EdgeFeederInfo(String componentType, boolean directional, double value, String unit, BiFunction<Double, String, String> formatter, String userDefinedId) {
        super(componentType, null, formatter.apply(value, unit), userDefinedId);
        this.directional = directional;
        this.labelDirection = Objects.requireNonNull(getLabelDirection(value));
        this.value = value;
    }

    public EdgeFeederInfo(String componentType, boolean directional, double value, String unit, BiFunction<Double, String, String> formatter) {
        this(componentType, directional, value, unit, formatter, null);
    }

    public boolean isDirectional() {
        return directional;
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
