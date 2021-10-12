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
public class FeederMeasure {

    private final String componentType;
    private final Direction arrowDirection;
    private final String leftLabel;
    private final String rightLabel;

    public FeederMeasure(String componentType) {
        this(componentType, null, null, null);
    }

    public FeederMeasure(String componentType, Direction arrowDirection, String leftLabel, String rightLabel) {
        this.componentType = Objects.requireNonNull(componentType);
        this.arrowDirection = arrowDirection;
        this.leftLabel = leftLabel;
        this.rightLabel = rightLabel;
    }

    public FeederMeasure(String componentType, double value, String leftLabel) {
        this(componentType, value > 0 ? Direction.UP : Direction.DOWN, leftLabel, String.valueOf(Math.round(value)));
    }

    public FeederMeasure(String componentType, double value) {
        this(componentType, value, null);
    }

    public boolean isEmpty() {
        return getDirection().isEmpty() && getLeftLabel().isEmpty() || getRightLabel().isEmpty();
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
