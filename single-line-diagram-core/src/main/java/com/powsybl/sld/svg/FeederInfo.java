/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import java.util.Objects;
import java.util.Optional;

import com.powsybl.sld.svg.DiagramLabelProvider.Direction;

/**
 * Class used to describe an information element which is displayed below feeders, which contains one or more of the following:
 * <ul>
 * <li>an arrow whose direction is specified</li>
 * <li>a string on its right</li>
 * <li>a string on its left</li>
 * </ul>
 * Each of these three element part is optional
 *
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class FeederInfo {

    private final String id;
    private final String componentType;
    private final Direction arrowDirection;
    private final String leftLabel;
    private final String rightLabel;

    public FeederInfo(String id, String componentType) {
        this(id, componentType, null, null, null);
    }

    public FeederInfo(String id, String componentType, Direction arrowDirection, String leftLabel, String rightLabel) {
        this.id = id;
        this.componentType = Objects.requireNonNull(componentType);
        this.arrowDirection = arrowDirection;
        this.leftLabel = leftLabel;
        this.rightLabel = rightLabel;
    }

    public FeederInfo(String id, String componentType, double value) {
        this(id, componentType, value > 0 ? Direction.OUT : Direction.IN, null, String.valueOf(Math.round(value)));
    }

    public boolean isEmpty() {
        return getDirection().isEmpty() && getLeftLabel().isEmpty() || getRightLabel().isEmpty();
    }

    public String getId() {
        return id;
    }

    public String getComponentType() {
        return componentType;
    }

    public Optional<Direction> getDirection() {
        return Optional.ofNullable(arrowDirection);
    }

    public Optional<String> getLeftLabel() {
        return Optional.ofNullable(leftLabel);
    }

    public Optional<String> getRightLabel() {
        return Optional.ofNullable(rightLabel);
    }
}
