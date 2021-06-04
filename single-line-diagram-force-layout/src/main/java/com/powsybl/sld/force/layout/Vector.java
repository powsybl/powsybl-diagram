/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.force.layout;

import java.util.Objects;

/**
 * @author Mathilde Grapin <mathilde.grapin at rte-france.com>
 */
public class Vector {
    private double x;
    private double y;

    public Vector() { }

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

    public Vector multiply(double scalar) {
        return new Vector(x * scalar, y * scalar);
    }

    public Vector divide(double scalar) {
        return new Vector(x / scalar, y / scalar);
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y);
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

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Vector vector = (Vector) o;
        return x == vector.x && y == vector.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
