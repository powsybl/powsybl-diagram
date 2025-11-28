/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import java.util.Optional;
import java.util.function.DoubleFunction;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class EdgeInfo {
    public static final String ACTIVE_POWER = "ActivePower";
    public static final String REACTIVE_POWER = "ReactivePower";
    public static final String CURRENT = "Current";
    public static final String NAME = "Name";
    public static final String LOAD_PERCENTAGE = "Load percentage";
    public static final String EMPTY = "Empty";

    private final String infoType1;
    private final String infoType2;
    private final Direction arrowDirection;
    private final String label1;
    private final String label2;

    public EdgeInfo(String infoType1, String infoType2, Direction arrowDirection, String label1, String label2) {
        this.infoType2 = infoType2;
        this.infoType1 = infoType1;
        this.arrowDirection = arrowDirection;
        this.label1 = label1;
        this.label2 = label2;
    }

    public EdgeInfo(String infoType1, String infoType2, double referenceValue, String label1, String label2) {
        this(infoType1, infoType2, getArrowDirection(referenceValue), label1, label2);
    }

    public EdgeInfo(String infoType, Direction arrowDirection, String label1, String label2) {
        this(infoType, infoType, arrowDirection, label1, label2);
    }

    public EdgeInfo(String infoType, double internalValue, double externalValue, DoubleFunction<String> formatter) {
        this(infoType, getArrowDirection(externalValue), formatter.apply(internalValue), formatter.apply(externalValue));
    }

    public EdgeInfo(String infoType, double value, DoubleFunction<String> formatter) {
        this(infoType, getArrowDirection(value), null, formatter.apply(value));
    }

    public EdgeInfo(String infoType, double value, String label2) {
        this(infoType, getArrowDirection(value), null, label2);
    }

    public EdgeInfo() {
        this(EdgeInfo.ACTIVE_POWER, null, null, null, null);
    }

    private static Direction getArrowDirection(double value) {
        if (Double.isNaN(value)) {
            return null;
        }
        return value < 0 ? Direction.IN : Direction.OUT;
    }

    /**
     * @deprecated since 5.1.0, use {@link #getInfoType2()} instead.
     */
    @Deprecated(since = "5.1.0")
    public String getInfoType() {
        return getInfoType2();
    }

    public String getInfoType2() {
        return infoType2;
    }

    public String getInfoType1() {
        return infoType1;
    }

    public Optional<Direction> getDirection() {
        return Optional.ofNullable(arrowDirection);
    }

    public Optional<String> getLabel1() {
        return Optional.ofNullable(label1);
    }

    public Optional<String> getLabel2() {
        return Optional.ofNullable(label2);
    }

    public enum Direction {
        IN, OUT
    }
}
