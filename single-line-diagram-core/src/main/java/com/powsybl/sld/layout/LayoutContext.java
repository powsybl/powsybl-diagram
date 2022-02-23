/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
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
    private boolean isInternCell = false;
    private boolean isFlat = false;
    private boolean isUnileg = false;

    private LayoutContext(double firstBusY, double lastBusY, double maxInternCellHeight, Direction direction) {
        this.firstBusY = firstBusY;
        this.lastBusY = lastBusY;
        this.maxInternCellHeight = maxInternCellHeight;
        this.direction = direction;
    }

    public static LayoutContext create(double firstBusY, double lastBusY, double maxInternCellHeight, Direction direction) {
        return new LayoutContext(firstBusY, lastBusY, maxInternCellHeight, direction);
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

    public void setInternCell(boolean isInternCell) {
        this.isInternCell = isInternCell;
    }

    public boolean isFlat() {
        return isFlat;
    }

    public void setFlat(boolean isFlat) {
        this.isFlat = isFlat;
    }

    public boolean isUnileg() {
        return isUnileg;
    }

    public void setUnileg(boolean isUnileg) {
        this.isUnileg = isUnileg;
    }
}
