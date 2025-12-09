/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import java.util.Optional;

/**
 * Edge information container
 *
 * <p>{@code infoType1} and {@code label1} correspond to the data on the <b>internal</b> side for an EdgeInfo on a side of an
 * edge, and to the data on <b>side 1</b> for the EdgeInfo in the middle of an edge.</p>
 * <p>{@code infoType2} and {@code label2} correspond to the data on the <b>external</b> side for an EdgeInfo on a side of an
 * edge, and to the data on <b>side 2</b> for the EdgeInfo in the middle of an edge.</p>
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class EdgeInfo {
    public static final String ACTIVE_POWER = "ActivePower";
    public static final String REACTIVE_POWER = "ReactivePower";
    public static final String CURRENT = "Current";
    public static final String NAME = "Name";
    public static final String VALUE_PERMANENT_LIMIT_PERCENTAGE = "PermanentLimitPercentage";

    private final String infoTypeA;
    private final String infoTypeB;
    private final Direction arrowDirection;
    private final String labelA;
    private final String labelB;

    public EdgeInfo(String infoTypeA, String infoTypeB, Direction arrowDirection, String labelA, String labelB) {
        this.infoTypeB = infoTypeB;
        this.infoTypeA = infoTypeA;
        this.arrowDirection = arrowDirection;
        this.labelA = labelA;
        this.labelB = labelB;
    }

    public EdgeInfo(String infoTypeA, String infoTypeB, double referenceValue, String labelA, String labelB) {
        this(infoTypeA, infoTypeB, getArrowDirection(referenceValue), labelA, labelB);
    }

    private static Direction getArrowDirection(double value) {
        if (Double.isNaN(value)) {
            return null;
        }
        return value < 0 ? Direction.IN : Direction.OUT;
    }

    /**
     * @deprecated since 5.2.0, use {@link #getInfoTypeB()} instead.
     */
    @Deprecated(since = "5.2.0")
    public String getInfoType() {
        return getInfoTypeB();
    }

    public String getInfoTypeB() {
        return infoTypeB;
    }

    public String getInfoTypeA() {
        return infoTypeA;
    }

    public Optional<Direction> getDirection() {
        return Optional.ofNullable(arrowDirection);
    }

    public Optional<String> getLabelA() {
        return Optional.ofNullable(labelA);
    }

    public Optional<String> getLabelB() {
        return Optional.ofNullable(labelB);
    }

    /**
     * Returns the main info type.
     * @return the main info type. By default, the info type of the side 2.
     */
    public String getMainInfoType() {
        return infoTypeB != null ? infoTypeB : infoTypeA;
    }

    public enum Direction {
        IN, OUT
    }
}
