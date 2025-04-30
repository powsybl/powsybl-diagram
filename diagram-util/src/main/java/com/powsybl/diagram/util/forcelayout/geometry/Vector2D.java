/*
 * Copyright (c) 2021-2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout.geometry;

/**
 * @author Mathilde Grapin {@literal <mathilde.grapin at rte-france.com>}
 */
public record Vector2D(double x, double y) {

    public Vector2D add(Vector2D otherVector) {
        return new Vector2D(x + otherVector.x(), y + otherVector.y());
    }

    public Vector2D subtract(Vector2D otherVector2D) {
        return new Vector2D(x - otherVector2D.x(), y - otherVector2D.y());
    }

    public Vector2D multiply(double scalar) {
        return new Vector2D(x * scalar, y * scalar);
    }

    public Vector2D divide(double scalar) {
        return new Vector2D(x / scalar, y / scalar);
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public double magnitudeSquare() {
        return x * x + y * y;
    }

    public Vector2D normalize() {
        return this.divide(this.magnitude());
    }

    /// Calculate the unit vector that goes from This point, pointing in the direction of towards
    public static Vector2D calculateUnitVector(Point from, Point towards) {
        Vector2D deltaVector = towards.getPosition().subtract(from.getPosition());
        return deltaVector.normalize();
    }

}
