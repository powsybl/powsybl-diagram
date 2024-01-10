/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.diagram.util.forcelayout;

import java.io.PrintWriter;
import java.util.Locale;
import java.util.function.Function;

/**
 * @author Mathilde Grapin {@literal <mathilde.grapin at rte-france.com>}
 */
public class Point {
    private static final double DEFAULT_MASS = 1.0;

    private Vector position;
    private Vector velocity;
    private Vector forces;
    private final double mass;
    private Vector previousForces;
    private final int degree;

    public Point(double x, double y) {
        this(x, y, DEFAULT_MASS, 0);
    }

    public Point(double x, double y, int degree) {
        this(x, y, DEFAULT_MASS, degree);
    }

    public Point(double x, double y, double mass) {
        this(x, y, mass, 0);
    }

    public Point(double x, double y, double mass, int degree) {
        this.position = new Vector(x, y);
        this.velocity = new Vector(0, 0);
        this.previousForces = new Vector(0, 0);
        this.forces = new Vector(0, 0);
        this.mass = mass;
        this.degree = degree;
    }

    public void applyForce(Vector force) {
        forces = forces.add(force);
    }

    public Vector getPosition() {
        return position;
    }

    public Vector getVelocity() {
        return velocity;
    }

    public Vector getPreviousForces() {
        return previousForces;
    }

    public Vector getForces() {
        return forces;
    }

    public void setPosition(Vector position) {
        this.position = position;
    }

    public void setVelocity(Vector velocity) {
        this.velocity = velocity;
    }

    public double getEnergy() {
        return 0.5 * mass * velocity.magnitudeSquare();
    }

    public double getMass() {
        return this.mass;
    }

    public int getDegree() {
        return this.degree;
    }

    public void resetForces() {
        this.forces = new Vector(0, 0);
    }

    public void setPreviousForces() {
        this.previousForces = new Vector(forces.getX(), forces.getY());
    }

    public <V> void toSVG(PrintWriter printWriter, Canvas canvas, Function<V, String> tooltip, V vertex) {
        printWriter.println("<g>");

        printWriter.printf("<title>%s</title>%n", tooltip.apply(vertex));

        Vector screenPosition = canvas.toScreen(getPosition());
        printWriter.printf(Locale.US, "<circle cx=\"%.2f\" cy=\"%.2f\" r=\"10\"/>%n",
            screenPosition.getX(), screenPosition.getY()
        );

        printWriter.println("</g>");
    }
}
