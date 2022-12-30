/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.diagram.util.forcelayout;

/**
 * @author Mathilde Grapin <mathilde.grapin at rte-france.com>
 */
public class Vector {
    private final double x;
    private final double y;

    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector add(Vector otherVector) {
        return new Vector(x + otherVector.getX(), y + otherVector.getY());
    }

    public Vector subtract(Vector otherVector) {
        return new Vector(x - otherVector.getX(), y - otherVector.getY());
    }

    public double dot(Vector otherVector) {
        return x * otherVector.getX() + y * otherVector.getY();
    }

    public Vector multiply(double scalar) {
        return new Vector(x * scalar, y * scalar);
    }

    public Vector divide(double scalar) {
        return new Vector(x / scalar, y / scalar);
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public double magnitudeSquare() {
        return x * x + y * y;
    }

    public Vector normalize() {
        return this.divide(this.magnitude());
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Vector projectionOnSegment(Vector q1, Vector q2) {
        // points q along the segment defined by points q1, q2
        // can be obtained using escalar parameter t as: q = q1 + t(q2 - q1)
        // A point q that is the projection of p over the line extended from segment q2, q1
        // (p - q) . (q2 - q1) = 0
        // the vector (q - p) is normal to the vector (q2 - q1)
        // In this equation we express q using the parametric equation:
        // [p - (q1 + t(q2 - q1)] . (q2 - q1) = 0
        // (p - q1) . (q2 - q1) - t (q2 - q1) . (q2 - q1) = 0
        // t = [(p - q1) . (q2 - q1)] / |q2 - q1|^2
        double t = this.subtract(q1).dot(q2.subtract(q1)) / q2.subtract(q1).magnitudeSquare();
        // Only if the projection is inside the segment
        // If it is outside, we rely on the repulsion between nodes (point p will be repelled by q1 and q2)
        if (t > 0 && t < 1) {
            return q1.add(q2.subtract(q1).multiply(t));
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("(%10.4f, %10.4f)", x, y);
    }
}
