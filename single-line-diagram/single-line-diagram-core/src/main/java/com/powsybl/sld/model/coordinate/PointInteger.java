/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.model.coordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Same as pointInteger but with int coordinates. Used for grids that need integer coordinates, to prevent rounding errors with double
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class PointInteger {

    private int x;
    private int y;

    public PointInteger(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public PointInteger(PointInteger pointInteger) {
        this.x = pointInteger.x;
        this.y = pointInteger.y;
    }

    public PointInteger(Point point) {
        this.x = (int) point.getX();
        this.y = (int) point.getY();
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void shift(PointInteger shiftPoint) {
        x += shiftPoint.x;
        y += shiftPoint.y;
    }

    public PointInteger getShiftedPoint(PointInteger shift) {
        PointInteger res = new PointInteger(this);
        res.shift(shift);
        return res;
    }

    public PointInteger getOpposite() {
        return new PointInteger(-this.x, -this.y);
    }

    /**
     * Get the vector from this point to towards, this is not the unit vector
     * @param towards the points towards which we go
     * @return a point integer that represents the vector From -> Towards
     */
    public PointInteger getDirection(PointInteger towards) {
        return new PointInteger(towards.x - this.x, towards.y - this.y);
    }

    /**
     * Rotates the point of 90° in the trigonometric direction
     * Doesn't really make sense for a point, but makes sense if the point is used as a representation for a vector
     */
    public void rotate() {
        int oldX = this.x;
        this.x = -y;
        //noinspection SuspiciousNameCombination
        this.y = oldX;
    }

    public int manhattanDistance(PointInteger other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }

    @Override
    public String toString() {
        return "{" + x + ',' + y + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PointInteger pointInteger = (PointInteger) o;
        return x == pointInteger.x && y == pointInteger.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}

