package com.powsybl.sld.force.layout;

import java.io.PrintWriter;

public class Point {
    private static final double DEFAULT_MASS = 1.0;

    private Vector position;
    private Vector velocity;
    private Vector acceleration;
    private double mass;
    private String label;

    public Point(double x, double y) {
        this.position = new Vector(x, y);
        this.velocity = new Vector();
        this.acceleration = new Vector();
        this.mass = DEFAULT_MASS;
    }

    public Point(double x, double y, String label) {
        this(x, y);
        this.label = label;
    }

    public void setMass(double mass) {
        this.mass = mass;
    }

    public void applyForce(Vector force) {
        this.acceleration = this.acceleration.add(force.divide(this.mass));
    }

    public Vector getPosition() {
        return position;
    }

    public Vector getVelocity() {
        return velocity;
    }

    public Vector getAcceleration() {
        return acceleration;
    }

    public double getMass() {
        return mass;
    }

    public void setPosition(Vector position) {
        this.position = position;
    }

    public void setVelocity(Vector velocity) {
        this.velocity = velocity;
    }

    public void setAcceleration(Vector acceleration) {
        this.acceleration = acceleration;
    }

    public double getEnergy() {
        double speed = velocity.magnitude();
        return 0.5 * mass * speed * speed;
    }

    public void printSVG(PrintWriter printWriter, Canvas canvas, BoundingBox boundingBox) {
        Vector position = this.getPosition();
        Vector screenPosition = canvas.toScreen(boundingBox, position);

        int screenPositionX = (int) Math.round(screenPosition.getX());
        int screenPositionY = (int) Math.round(screenPosition.getY());

        printWriter.println("<g>");
        printWriter.printf("<circle cx=\"%d\" cy=\"%d\" r=\"20\" fill=\"purple\"/>%n",
                screenPositionX,
                screenPositionY);

        if (this.label != null && !this.label.isEmpty()) {
            printWriter.printf("<text x=\"%d\" y=\"%d\" text-anchor=\"middle\" fill=\"purple\">%n",
                    screenPositionX,
                    screenPositionY - 25);
            printWriter.println(this.label);
            printWriter.println("</text>");
        }

        printWriter.println("</g>");
    }
}
