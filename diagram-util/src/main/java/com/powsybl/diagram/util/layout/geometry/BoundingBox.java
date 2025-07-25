/**
 * Copyright (c) 2021-2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.layout.geometry;

import java.util.Collection;

/**
 * @author Mathilde Grapin {@literal <mathilde.grapin at rte-france.com>}
 */
public final class BoundingBox {

    private final double left;
    private final double bottom;
    private final double right;
    private final double top;

    public BoundingBox(double left, double top, double right, double bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        // top is the smallest y, bottom the biggest y
        // axes are:
        // (0, 0) ---> x
        // |
        // v y
        if (left > right || top > bottom) {
            throw new IllegalStateException("Bounding box with negative width or height");
        }
    }

    public static BoundingBox computeBoundingBox(Collection<Point> points) {
        // using Double.MAX_VALUE this way the box for no points is the identity element for box fusion
        double left = points.stream().mapToDouble(p -> p.getPosition().getX()).min().orElse(0);
        double bottom = points.stream().mapToDouble(p -> p.getPosition().getY()).max().orElse(0);
        double right = points.stream().mapToDouble(p -> p.getPosition().getX()).max().orElse(0);
        double top = points.stream().mapToDouble(p -> p.getPosition().getY()).min().orElse(0);
        return new BoundingBox(left, top, right, bottom);
    }

    public static BoundingBox addBoundingBoxes(BoundingBox first, BoundingBox second) {
        double left = Math.min(first.left, second.left);
        double bottom = Math.max(first.bottom, second.bottom);
        double right = Math.max(first.right, second.right);
        double top = Math.min(first.top, second.top);
        return new BoundingBox(left, top, right, bottom);
    }

    public double getHeight() {
        return bottom - top;
    }

    public double getWidth() {
        return right - left;
    }

    public double getLeft() {
        return left;
    }

    public double getTop() {
        return top;
    }

    public double getBottom() {
        return bottom;
    }

    public double getRight() {
        return right;
    }

    public Vector2D getCenter() {
        return new Vector2D((left + right) / 2, (top + bottom) / 2);
    }
}
