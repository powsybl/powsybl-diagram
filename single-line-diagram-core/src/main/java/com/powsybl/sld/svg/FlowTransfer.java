/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import java.util.Optional;

import com.powsybl.sld.svg.DiagramLabelProvider.Direction;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class FlowTransfer {

    private final Direction direction;
    private final String valueLabel;
    private final String customText;

    public FlowTransfer() {
        this(Double.NaN, null);
    }

    public FlowTransfer(double value, String customText) {
        if (Double.isNaN(value)) {
            direction = null;
            valueLabel = null;
        } else {
            direction = value > 0 ? Direction.UP : Direction.DOWN;
            valueLabel = String.valueOf(Math.round(value));
        }
        this.customText = customText;
    }

    public Optional<Direction> getArrowDirection() {
        return Optional.ofNullable(direction);
    }

    public Optional<String> getValueLabel() {
        return Optional.ofNullable(valueLabel);
    }

    public Optional<String> getCustomLabel() {
        return Optional.ofNullable(customText);
    }
}
