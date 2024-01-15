/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

import java.util.Objects;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class Point {

    private final double x;
    private final double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point() {
        this(0, 0);
    }

    public static Point createMiddlePoint(Point point1, Point point2) {
        Objects.requireNonNull(point1);
        Objects.requireNonNull(point2);
        return new Point(0.5 * (point1.x + point2.x), 0.5 * (point1.y + point2.y));
    }

    public double distanceSquare(Point other) {
        Objects.requireNonNull(other);
        double dx = other.x - x;
        double dy = other.y - y;
        return dx * dx + dy * dy;
    }

    public double distance(Point other) {
        Objects.requireNonNull(other);
        return Math.sqrt(distanceSquare(other));
    }

    public Point shiftRhoTheta(double rho, double theta) {
        return shift(rho * Math.cos(theta), rho * Math.sin(theta));
    }

    public Point shift(double shiftX, double shiftY) {
        return new Point(x + shiftX, y + shiftY);
    }

    public double getY() {
        return y;
    }

    public double getX() {
        return x;
    }

    public Point atDistance(double dist, Point direction) {
        double r = dist / distance(direction);
        return new Point(x + r * (direction.x - x),
                y + r * (direction.y - y));
    }

    public Point atDistance(double dist, double angle) {
        return new Point(x + dist * Math.cos(angle),
                y + dist * Math.sin(angle));
    }

    public double getAngle(Point other) {
        return Math.atan2(other.y - y, other.x - x);
    }
}
