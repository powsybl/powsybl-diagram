/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.coordinate.Direction;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 */
public record LayoutContext(double firstBusY, double lastBusY, double maxInternCellHeight, Direction direction,
                            boolean isInternCell, boolean isFlat, boolean isUnileg) {

    public LayoutContext(double firstBusY, double lastBusY, double maxInternCellHeight, Direction direction) {
        this(firstBusY, lastBusY, maxInternCellHeight, direction, false, false, false);
    }
}
