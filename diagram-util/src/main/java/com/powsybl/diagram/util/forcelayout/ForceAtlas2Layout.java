/**
 Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout;

import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * The following algorithm is an implementation of the ForceAtlas2 layout algorithm.
 * The implementation is based on the paper <a href="https://doi.org/10.1371/journal.pone.0098679">ForceAtlas2, a Continuous Graph Layout Algorithm for Handy Network Visualization Designed for the Gephi Software</a>
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class ForceAtlas2Layout<V, E> extends AbstractForceLayout<V, E> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ForceAtlas2Layout.class);
    private static final double DEFAULT_K_REPULSION = 10.0;
    private static final int DEFAULT_REPULSION_MODEL = -1;
    private static final int DEFAULT_EDGE_WEIGHT_INFLUENCE = 1;
    private static final int DEFAULT_ATTRACTION_MODEL = 1;
    private static final double DEFAULT_K_GRAVITY = 1.0;
    private static final double DEFAULT_K_SPEED = 0.1;
    private static final double DEFAULT_K_MAX_SPEED = 10.0;
    private static final double DEFAULT_GLOBAL_SPEED_RATIO = 1.0;
    private static final double DEFAULT_GLOBAL_SPEED_INCREMENT_FACTOR = 1.5;
    private static final boolean DEFAULT_STRONG_GRAVITY_MODE = false;

    private double kRepulsion;
    private int repulsionModel;
    private int edgeWeightInfluence;
    private int attractionModel;
    private double kGravity;
    private double kSpeed;
    private double kMaxSpeed;
    private double globalSpeedRatio;
    private double globalSpeedIncrementFactor;
    private boolean strongGravityMode;
    private double globalSpeed = Double.NaN;

    public ForceAtlas2Layout(Graph<V, E> graph) {
        super(graph);
        this.kRepulsion = DEFAULT_K_REPULSION;
        this.repulsionModel = DEFAULT_REPULSION_MODEL;
        this.edgeWeightInfluence = DEFAULT_EDGE_WEIGHT_INFLUENCE;
        this.attractionModel = DEFAULT_ATTRACTION_MODEL;
        this.kGravity = DEFAULT_K_GRAVITY;
        this.kSpeed = DEFAULT_K_SPEED;
        this.kMaxSpeed = DEFAULT_K_MAX_SPEED;
        this.globalSpeedRatio = DEFAULT_GLOBAL_SPEED_RATIO;
        this.globalSpeedIncrementFactor = DEFAULT_GLOBAL_SPEED_INCREMENT_FACTOR;
        this.strongGravityMode = DEFAULT_STRONG_GRAVITY_MODE;
    }

    public double getkRepulsion() {
        return kRepulsion;
    }

    public void setkRepulsion(double kRepulsion) {
        this.kRepulsion = kRepulsion;
    }

    public int getRepulsionModel() {
        return repulsionModel;
    }

    public void setRepulsionModel(int repulsionModel) {
        this.repulsionModel = repulsionModel;
    }

    public int getEdgeWeightInfluence() {
        return edgeWeightInfluence;
    }

    public void setEdgeWeightInfluence(int edgeWeightInfluence) {
        this.edgeWeightInfluence = edgeWeightInfluence;
    }

    public int getAttractionModel() {
        return attractionModel;
    }

    public void setAttractionModel(int attractionModel) {
        this.attractionModel = attractionModel;
    }

    public double getkGravity() {
        return kGravity;
    }

    public void setkGravity(double kGravity) {
        this.kGravity = kGravity;
    }

    public double getkSpeed() {
        return kSpeed;
    }

    public void setkSpeed(double kSpeed) {
        this.kSpeed = kSpeed;
    }

    public double getkMaxSpeed() {
        return kMaxSpeed;
    }

    public void setkMaxSpeed(double kMaxSpeed) {
        this.kMaxSpeed = kMaxSpeed;
    }

    public double getGlobalSpeedRatio() {
        return globalSpeedRatio;
    }

    public void setGlobalSpeedRatio(double globalSpeedRatio) {
        this.globalSpeedRatio = globalSpeedRatio;
    }

    public double getGlobalSpeedIncrementFactor() {
        return globalSpeedIncrementFactor;
    }

    public void setGlobalSpeedIncrementFactor(double globalSpeedIncrementFactor) {
        this.globalSpeedIncrementFactor = globalSpeedIncrementFactor;
    }

    public boolean isStrongGravityMode() {
        return strongGravityMode;
    }

    public void setStrongGravityMode(boolean strongGravityMode) {
        this.strongGravityMode = strongGravityMode;
    }

    public double getGlobalSpeed() {
        return globalSpeed;
    }

    public void setGlobalSpeed(double globalSpeed) {
        this.globalSpeed = globalSpeed;
    }

    @Override
    public void execute() {
        long start = System.nanoTime();

        initializePoints();
        initializeSprings();

        int i;
        for (i = 0; i < getMaxSteps(); i++) {
            // kRepulsion = scalingRatio
            applyPointsRepulsionForces(kRepulsion, repulsionModel);
            applySpringsAttractionForces(edgeWeightInfluence, attractionModel);

            if (strongGravityMode) {
                strongGravity(kGravity);
            } else {
                gravity(kGravity);
            }

            updateNodalAndGlobalVelocity(kSpeed, kMaxSpeed, globalSpeedRatio, globalSpeedIncrementFactor);
            updatePosition();

            if (isStable()) {
                break;
            }
        }

        setHasBeenExecuted(true);

        long elapsedTime = System.nanoTime() - start;

        LOGGER.info("Number of steps: {}", i);
        LOGGER.info("Elapsed time: {}", elapsedTime / 1e9);
    }

    private void applyPointsRepulsionForces(double kRepulsion, int repulsionModel) {
        for (Point point : getPoints().values()) {
            Vector p = point.getPosition();
            int degree = point.getDegree();
            for (Point otherPoint : getPoints().values()) {
                if (!point.equals(otherPoint)) {
                    point.applyForce(repulsionForce(p, degree, otherPoint.getPosition(), otherPoint.getDegree(), repulsionModel, kRepulsion));
                }
            }
        }
    }

    private Vector repulsionForce(Vector p1, int degree1, Vector p2, int degree2, int repulsionModel, double repulsion) {
        Vector distance = p1.subtract(p2);
        Vector direction = distance.normalize();
        return direction.multiply(repulsion * (degree1 + 1) * (degree2 + 1)).multiply(Math.pow(distance.magnitude(), repulsionModel));
    }

    private void applySpringsAttractionForces(int edgeWeightInfluence, int attractionModel) {
        for (Spring spring : getSprings()) {
            Point point1 = spring.getNode1();
            Point point2 = spring.getNode2();

            Vector distance = point2.getPosition().subtract(point1.getPosition());
            double displacement = distance.magnitude();
            Vector direction = distance.normalize();

            Vector force = direction.multiply(Math.pow(spring.getLength(), edgeWeightInfluence) * Math.pow(displacement, attractionModel));

            point1.applyForce(force.multiply(1));
            point2.applyForce(force.multiply(-1));
        }
    }

    private void gravity(double kGravity) {
        Point point2 = new Point(0, 0);
        for (Point point : getPoints().values()) {
            Vector distance = point2.getPosition().subtract(point.getPosition());
            Vector direction = distance.normalize();

            point.applyForce(direction.multiply(kGravity * (point.getDegree() + 1)));
        }
    }

    private void strongGravity(double kGravity) {
        Point point2 = new Point(0, 0);
        for (Point point : getPoints().values()) {
            Vector distance = point2.getPosition().subtract(point.getPosition());
            double displacement = distance.magnitude();
            Vector direction = distance.normalize();

            point.applyForce(direction.multiply(kGravity * (point.getDegree() + 1) * displacement));
        }
    }

    private void updateNodalAndGlobalVelocity(double kSpeed, double kMaxSpeed, double globalSpeedRatio, double globalSpeedIncrementFactor) {
        double nextGlobalSpeed = calculateGlobalSpeed(globalSpeedRatio, globalSpeedIncrementFactor);
        for (Point point : getPoints().values()) {
            double nodalSpeed = calculateNodalSpeed(point, kSpeed, nextGlobalSpeed);
            Vector newVelocity = point.getForces().multiply(nodalSpeed);
            if (newVelocity.magnitude() >= kMaxSpeed) {
                newVelocity = newVelocity.normalize().multiply(kMaxSpeed);
            }
            point.setVelocity(newVelocity);
            point.setPreviousForces();
            point.resetForces();
        }
        this.globalSpeed = nextGlobalSpeed;
    }

    private double calculateGlobalSwinging() {
        double globalSwinging = 0;
        for (Map.Entry<V, Point> vertexPoint : getPoints().entrySet()) {
            if (getFixedNodes().contains(vertexPoint.getKey())) {
                continue;
            }
            Point point = vertexPoint.getValue();
            globalSwinging += calculateNodalSwinging(point);
        }
        // Avoid potential division by zero
        return globalSwinging == 0.0 ? Double.MIN_VALUE : globalSwinging;
    }

    private static double calculateNodalSwinging(Point point) {
        return (point.getDegree() + 1) * point.getForces().subtract(point.getPreviousForces()).magnitude();
    }

    private double calculateGlobalTraction() {
        double globalTraction = 0.0;
        for (Map.Entry<V, Point> vertexPoint : getPoints().entrySet()) {
            if (getFixedNodes().contains(vertexPoint.getKey())) {
                continue;
            }
            Point point = vertexPoint.getValue();
            globalTraction += (point.getDegree() + 1) * point.getForces().add(point.getPreviousForces()).magnitude() * 0.5;
        }
        return globalTraction;
    }

    private double calculateGlobalSpeed(double globalSpeedRatio, double globalSpeedIncrementFactor) {
        double nextGlobalSpeed = globalSpeedRatio * calculateGlobalTraction() / calculateGlobalSwinging();
        if (!Double.isNaN(this.globalSpeed) && nextGlobalSpeed > globalSpeedIncrementFactor * this.globalSpeed) {
            nextGlobalSpeed = globalSpeedIncrementFactor * this.globalSpeed;
        }
        return nextGlobalSpeed;
    }

    private static double calculateNodalSpeed(Point point, double kSpeed, double globalSpeed) {
        return kSpeed * globalSpeed / (1 + globalSpeed * Math.sqrt(calculateNodalSwinging(point)));
    }
}
