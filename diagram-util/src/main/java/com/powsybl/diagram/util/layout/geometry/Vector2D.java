/**
 * Copyright (c) 2021-2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.layout.geometry;

/**
 * @author Mathilde Grapin {@literal <mathilde.grapin at rte-france.com>}
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class Vector2D {
    private double x;
    private double y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D() {
        new Vector2D(0, 0);
    }

    public Vector2D(Vector2D otherVector) {
        this.x = otherVector.x;
        this.y = otherVector.y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void add(Vector2D otherVector) {
        this.x += otherVector.x;
        this.y += otherVector.y;
    }

    public void subtract(Vector2D otherVector) {
        this.x -= otherVector.x;
        this.y -= otherVector.y;
    }

    public void multiplyBy(double scalar) {
        this.x *= scalar;
        this.y *= scalar;
    }

    public void divideBy(double scalar) {
        this.x /= scalar;
        this.y /= scalar;
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public double magnitudeSquare() {
        return x * x + y * y;
    }

    public void normalize() {
        this.divideBy(this.magnitude());
    }

    public static Vector2D calculateVectorBetweenPoints(Point from, Point towards) {
        Vector2D direction = new Vector2D(towards.getPosition());
        direction.subtract(from.getPosition());
        return direction;
    }

    /**
     * Calculate the unit vector that goes from `from`, pointing in the direction of `towards`
      */
    public static Vector2D calculateUnitVector(Point from, Point towards) {
        if (from != towards) {
            Vector2D normalizedVector = calculateVectorBetweenPoints(from, towards);
            normalizedVector.normalize();
            return normalizedVector;
        } else {
            return new Vector2D(0, 0);
        }

    }
}
