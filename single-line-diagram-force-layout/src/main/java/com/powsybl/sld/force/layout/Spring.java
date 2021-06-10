/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.force.layout;

import java.io.PrintWriter;

/**
 * @author Mathilde Grapin <mathilde.grapin at rte-france.com>
 */
public class Spring {
    private static final double DEFAULT_LENGTH = 1.0;
    private static final double DEFAULT_STIFFNESS = 400.0;

    private final double length;
    private final double stiffness;
    private final Point source;
    private final Point target;

    public Spring(Point source, Point target, double length, double stiffness) {
        this.length = length;
        this.stiffness = stiffness;
        this.source = source;
        this.target = target;
    }

    public Spring(Point source, Point target, double stiffness) {
        this(source, target, DEFAULT_LENGTH, stiffness);
    }

    public Spring(Point source, Point target) {
        this(source, target, DEFAULT_LENGTH, DEFAULT_STIFFNESS);
    }

    public Point getNode1() {
        return source;
    }

    public Point getNode2() {
        return target;
    }

    public double getLength() {
        return length;
    }

    public double getStiffness() {
        return stiffness;
    }

    public void toSVG(PrintWriter printWriter, Canvas canvas, BoundingBox boundingBox) {
        Vector position1 = target.getPosition();
        Vector position2 = source.getPosition();

        Vector screenPosition1 = canvas.toScreen(boundingBox, position1);
        Vector screenPosition2 = canvas.toScreen(boundingBox, position2);

        printWriter.println("<g>");
        printWriter.printf("<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke-width=\"%d\"/>%n",
                (int) Math.round(screenPosition1.getX()),
                (int) Math.round(screenPosition1.getY()),
                (int) Math.round(screenPosition2.getX()),
                (int) Math.round(screenPosition2.getY()),
                2);
        printWriter.println("</g>");
    }
}
