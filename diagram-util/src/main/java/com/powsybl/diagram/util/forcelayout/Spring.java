/**
 * Copyright (c) 2021-2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout;

import com.powsybl.diagram.util.layout.Canvas;
import com.powsybl.diagram.util.layout.geometry.Point;
import com.powsybl.diagram.util.layout.geometry.Vector2D;

import java.io.PrintWriter;
import java.util.Locale;
import java.util.Objects;

/**
 * @author Mathilde Grapin {@literal <mathilde.grapin at rte-france.com>}
 * @deprecated this class is not used anymore for the description of the springs of the ForceLayout, see forces.SpringForce instead
 */
@Deprecated(since = "5.0.0", forRemoval = true)
public class Spring {
    private static final double DEFAULT_LENGTH = 1.0;
    private static final double DEFAULT_STIFFNESS = 100.0;

    private final double length;
    private final double stiffness;
    private final Point point1;
    private final Point point2;

    public Spring(Point point1, Point point2, double length, double stiffness) {
        this.length = length;
        this.stiffness = stiffness;
        this.point1 = Objects.requireNonNull(point1);
        this.point2 = Objects.requireNonNull(point2);
    }

    public Spring(Point point1, Point point2, double length) {
        this(point1, point2, length, DEFAULT_STIFFNESS);
    }

    public Spring(Point point1, Point point2) {
        this(point1, point2, DEFAULT_LENGTH);
    }

    public Point getNode1() {
        return point1;
    }

    public Point getNode2() {
        return point2;
    }

    public double getLength() {
        return length;
    }

    public double getStiffness() {
        return stiffness;
    }

    public void toSVG(PrintWriter printWriter, Canvas canvas) {
        Vector2D screenPosition1 = canvas.toScreen(point2.getPosition());
        Vector2D screenPosition2 = canvas.toScreen(point1.getPosition());
        printWriter.printf(Locale.US, "<line x1=\"%.2f\" y1=\"%.2f\" x2=\"%.2f\" y2=\"%.2f\"/>%n",
            screenPosition1.getX(), screenPosition1.getY(), screenPosition2.getX(), screenPosition2.getY());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Spring spring = (Spring) o;
        return point1 == spring.point1 && point2 == spring.point2
                || point1 == spring.point2 && point2 == spring.point1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(point1) + Objects.hash(point2);
    }
}
