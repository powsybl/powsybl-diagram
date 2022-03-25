/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.coordinate.Direction;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public final class LayoutContext {
    private final double firstBusY;
    private final double lastBusY;
    private final double maxInternCellHeight;
    private final Direction direction;
    private final boolean isInternCell;
    private final boolean isFlat;
    private final boolean isUnileg;

    public LayoutContext(double firstBusY, double lastBusY, double maxInternCellHeight, Direction direction,
                         boolean isInternCell, boolean isFlat, boolean isUnileg) {
        this.firstBusY = firstBusY;
        this.lastBusY = lastBusY;
        this.maxInternCellHeight = maxInternCellHeight;
        this.direction = direction;
        this.isInternCell = isInternCell;
        this.isFlat = isFlat;
        this.isUnileg = isUnileg;
    }

    public LayoutContext(double firstBusY, double lastBusY, double maxInternCellHeight, Direction direction) {
        this(firstBusY, lastBusY, maxInternCellHeight, direction, false, false, false);
    }

    public double getFirstBusY() {
        return firstBusY;
    }

    public double getLastBusY() {
        return lastBusY;
    }

    public double getMaxInternCellHeight() {
        return maxInternCellHeight;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean isInternCell() {
        return isInternCell;
    }

    public boolean isFlat() {
        return isFlat;
    }

    public boolean isUnileg() {
        return isUnileg;
    }
}
