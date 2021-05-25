package com.powsybl.sld.force.layout;

public class Point {
    private final static double DEFAULT_MASS = 1.0;

    private String id;
    private Vector position;
    private double mass;
    private Vector velocity;
    private Vector acceleration;

    public Point(String id, double x, double y) {
        this.id = id;
        this.position = new Vector(x, y);
        this.mass = DEFAULT_MASS;
        this.velocity = new Vector();
        this.acceleration = new Vector();
    }

    public String getId() {
        return id;
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
}
