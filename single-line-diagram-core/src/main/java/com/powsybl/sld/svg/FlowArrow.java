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
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class FlowArrow {

    private final String componentType;
    private final Direction direction;
    private final String leftLabel;
    private final String rightText;

    public FlowArrow(String componentType) {
        this(componentType, null, null, null);
    }

    public FlowArrow(String componentType, Direction direction, String leftLabel, String rightText) {
        this.componentType = Objects.requireNonNull(componentType);
        this.direction = direction;
        this.leftLabel = leftLabel;
        this.rightText = rightText;
    }

    public FlowArrow(String componentType, double value, String rightText) {
        this(componentType, value > 0 ? Direction.UP : Direction.DOWN, String.valueOf(Math.round(value)), rightText);
    }

    public FlowArrow(String componentType, double value) {
        this(componentType, value, null);
    }

    public String getComponentType() {
        return componentType;
    }

    public Optional<Direction> getDirection() {
        return Optional.ofNullable(direction);
    }

    public Optional<String> getLeftLabel() {
        return Optional.ofNullable(leftLabel);
    }

    public Optional<String> getRightLabel() {
        return Optional.ofNullable(rightText);
    }
}
