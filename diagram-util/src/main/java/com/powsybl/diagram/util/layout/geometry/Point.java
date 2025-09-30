/**
 * Copyright (c) 2021-2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.layout.geometry;

import com.powsybl.diagram.util.layout.Canvas;

import java.io.PrintWriter;
import java.util.Locale;
import java.util.function.Function;

/**
 * @author Mathilde Grapin {@literal <mathilde.grapin at rte-france.com>}
 */
public class Point {
    public static final double DEFAULT_MASS = 1.0;

    private Vector2D position;
    private Vector2D velocity;
    /**
     * The sum of all the forces currently applied to this point
     */
    private Vector2D forces;
    private double mass;
    /**
     * The degree of the vertex corresponding to this point, e.g. the number of edges of that vertex, used by some forces for calculations
     * We store it here so we don't have to query JGraphT multiple times for the value (because JGraphT does not store it, so caching the value is faster)
     */
    private int pointVertexDegree;

    public Point(double x, double y) {
        this(x, y, DEFAULT_MASS);
    }

    public Point(double x, double y, double mass) throws IllegalStateException {
        if (mass < 0) {
            throw new IllegalStateException("Point with a negative mass is not allowed");
        } else {
            this.mass = mass;
        }
        this.position = new Vector2D(x, y);
        this.velocity = new Vector2D(0, 0);
        this.forces = new Vector2D(0, 0);
    }

    public void applyForce(Vector2D force) {
        forces.add(force);
    }

    public Vector2D getPosition() {
        return position;
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    public Vector2D getForces() {
        return forces;
    }

    public void setPosition(Vector2D position) {
        this.position = position;
    }

    public void setVelocity(Vector2D velocity) {
        this.velocity = velocity;
    }

    /**
     * @return the kinetic energy
     */
    public double getEnergy() {
        return 0.5 * mass * velocity.magnitudeSquare();
    }

    public double getMass() {
        return this.mass;
    }

    public void setMass(double mass) {
        this.mass = mass;
    }

    public void resetForces() {
        this.forces = new Vector2D(0, 0);
    }

    public int getPointVertexDegree() {
        return pointVertexDegree;
    }

    public void setPointVertexDegree(int pointVertexDegree) {
        this.pointVertexDegree = pointVertexDegree;
    }

    public <V> void toSVG(PrintWriter printWriter, Canvas canvas, Function<V, String> tooltip, V vertex) {
        printWriter.println("<g>");

        printWriter.printf("<title>%s</title>%n", tooltip.apply(vertex));

        Vector2D screenPosition = canvas.toScreen(getPosition());
        printWriter.printf(Locale.US, "<circle cx=\"%.2f\" cy=\"%.2f\" r=\"10\"/>%n",
                screenPosition.getX(), screenPosition.getY()
        );

        printWriter.println("</g>");
    }

    /**
     * @param other the other point we want to calculate the distance to
     * @return the Euclidean distance from This point to `other`
     */
    public double distanceTo(Point other) {
        // we could do this.position.subtract(other.getPosition()).magnitude(); but that involves creating a new position, maybe not great for the GC
        double deltaX = this.position.getX() - other.getPosition().getX();
        double deltaY = this.position.getY() - other.getPosition().getY();
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }
}
