/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.diagram.util.forcelayout;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * @author Mathilde Grapin <mathilde.grapin at rte-france.com>
 */
public class Point {
    private static final double DEFAULT_MASS = 1.0;

    private Vector position;
    private Vector velocity;
    private Vector previousForces;
    private Vector forces;
    private final double mass;
    private final int degree;
    private LastStepEnergy lastStepEnergy;

    private List<Vector> positionHistory;
    private List<Vector> velocityHistory;
    private List<Vector> forcesHistory;

    public Point(double x, double y) {
        this(x, y, 0, DEFAULT_MASS);
    }

    public Point(double x, double y, int degree) {
        this(x, y, degree, DEFAULT_MASS);
    }

    public Point(double x, double y, int degree, double mass) {
        this.position = new Vector(x, y);
        this.velocity = new Vector(0, 0);
        this.previousForces = new Vector(0, 0);
        this.forces = new Vector(0, 0);
        this.degree = degree;
        this.mass = mass;

        this.positionHistory = new ArrayList<>();
        this.velocityHistory = new ArrayList<>();
        this.forcesHistory = new ArrayList<>();
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

    public int getDegree() {
        return this.degree;
    }

    public double getMass() {
        return this.mass;
    }

    public void setPreviousForces() {
        this.previousForces = new Vector(forces.getX(), forces.getY());
    }

    public void resetForces() {
        this.forces = new Vector(0, 0);
    }

    public <V> void toSVG(PrintWriter printWriter, Canvas canvas, Function<V, String> tooltip, V vertex) {
        printWriter.println("<g>");

        printWriter.printf("<title>%s</title>%n", tooltip.apply(vertex));

        Vector screenPosition = canvas.toScreen(getPosition());
        // XXX(Luma) Debug labels {
        printWriter.printf(Locale.US, "<text x=\"%.2f\" y=\"%.2f\">%s</text>%n",
                screenPosition.getX() + 10, screenPosition.getY() - 10,
                tooltip.apply(vertex));
        // XXX(Luma) }
        printWriter.printf(Locale.US, "<circle cx=\"%.2f\" cy=\"%.2f\" r=\"10\"/>%n",
            screenPosition.getX(), screenPosition.getY()
        );

        printWriter.println("</g>");
    }

    public void setLastStepEnergy(int lastStep, double lastEnergy) {
        this.lastStepEnergy = new LastStepEnergy(lastStep, lastEnergy);
    }

    public LastStepEnergy getLastStepEnergy() {
        return lastStepEnergy;
    }

    public void recordHistory() {
        positionHistory.add(position);
        forcesHistory.add(forces);
        velocityHistory.add(velocity);
    }

    public void history(String id) {
        // All the lists have the same number of entries
        if (positionHistory.isEmpty()) {
            return;
        }

        System.err.printf("History of Point %s %n", id);
        int i;
        for (i = 0; i < positionHistory.size(); i++) {
            System.err.printf("Step %04d Position %10.4f %10.4f Forces %16.4f %16.4f velocity %10.4f %10.4f %n", i,
                    positionHistory.get(i).getX(), positionHistory.get(i).getY(),
                    forcesHistory.get(i).getX(), forcesHistory.get(i).getY(),
                    velocityHistory.get(i).getX(), velocityHistory.get(i).getY());
        }
        System.err.printf("History end ---> %n");
    }

    public static final class LastStepEnergy {
        private final int step;
        private final double energy;

        private LastStepEnergy(int step, double energy) {
            this.step = step;
            this.energy = energy;
        }

        public int getStep() {
            return step;
        }

        public double getEnergy() {
            return energy;
        }
    }
}
