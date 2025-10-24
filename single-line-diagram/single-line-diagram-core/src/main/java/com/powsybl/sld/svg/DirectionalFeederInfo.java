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
 * Class used to specify a directional element
 * (see FeederInfo and AbstractFeederInfo)
 *
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public class DirectionalFeederInfo extends AbstractFeederInfo {

    private final LabelProvider.LabelDirection arrowDirection;

    private final double value;

    public DirectionalFeederInfo(String componentType, LabelProvider.LabelDirection arrowDirection, String leftLabel, String rightLabel) {
        this(componentType, arrowDirection, leftLabel, rightLabel, null);
    }

    public DirectionalFeederInfo(String componentType, LabelProvider.LabelDirection arrowDirection, String leftLabel, String rightLabel, String userDefinedId) {
        super(componentType, leftLabel, rightLabel, userDefinedId);
        this.arrowDirection = Objects.requireNonNull(arrowDirection);
        this.value = Double.NaN;
    }

    public DirectionalFeederInfo(String componentType, double value, DoubleFunction<String> formatter) {
        this(componentType, value, formatter, null);
    }

    public DirectionalFeederInfo(String componentType, double value, DoubleFunction<String> formatter, String userDefinedId) {
        super(componentType, null, formatter.apply(value), userDefinedId);
        this.arrowDirection = Objects.requireNonNull(getArrowDirection(value));
        this.value = value;
    }

    public DirectionalFeederInfo(String componentType, double value, String unit, BiFunction<Double, String, String> formatter, String userDefinedId) {
        super(componentType, null, formatter.apply(value, unit), userDefinedId);
        this.arrowDirection = Objects.requireNonNull(getArrowDirection(value));
        this.value = value;
    }

    public DirectionalFeederInfo(String componentType, double value, String unit, BiFunction<Double, String, String> formatter) {
        this(componentType, value, unit, formatter, (String) null);
    }

    public DirectionalFeederInfo(String componentType, double value, String unit, BiFunction<Double, String, String> formatter, LabelProvider.LabelDirection arrowDirection, String userDefinedId) {
        super(componentType, null, formatter.apply(value, unit), userDefinedId);
        this.arrowDirection = arrowDirection;
        this.value = value;
    }

    public DirectionalFeederInfo(String componentType, double value, String unit, BiFunction<Double, String, String> formatter, LabelProvider.LabelDirection arrowDirection) {
        this(componentType, value, unit, formatter, arrowDirection, null);
    }

    private static LabelProvider.LabelDirection getArrowDirection(double value) {
        return value > 0 ? LabelProvider.LabelDirection.OUT : LabelProvider.LabelDirection.IN;
    }

    public LabelProvider.LabelDirection getDirection() {
        return arrowDirection;
    }

    public double getValue() {
        return value;
    }
}
