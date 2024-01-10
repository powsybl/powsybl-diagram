/**
 * Java transcription of Springy v2.8.0
 *
 * Copyright (c) 2010-2018 Dennis Hotson
 * Copyright (c) 2021 RTE (https://www.rte-france.com)
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package com.powsybl.diagram.util.forcelayout;

import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The following algorithm is a force layout algorithm.
 * It seeks to place the nodes of a graph in such a way that the nodes are well spaced and that there are no unnecessary crossings.
 * The algorithm uses an analogy with physics where the nodes of the graph are particles with mass and the edges are springs.
 * Force calculations are used to place the nodes.
 *
 * The algorithm is taken from: https://github.com/dhotson/springy
 *
 * @author Mathilde Grapin {@literal <mathilde.grapin at rte-france.com>}
 */
public class ForceLayout<V, E> extends AbstractForceLayout<V, E> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ForceLayout.class);
    private static final double DEFAULT_REPULSION = 800.0;
    private static final double DEFAULT_FRICTION = 500;
    private static final double DEFAULT_MAX_SPEED = 100;
    /** Spring repulsion is disabled by default */
    private static final double DEFAULT_SPRING_REPULSION_FACTOR = 0.0;
    private double repulsion;
    private double friction;
    private double maxSpeed;
    private double springRepulsionFactor;

    public ForceLayout(Graph<V, E> graph) {
        super(graph);
        this.friction = DEFAULT_FRICTION;
        this.maxSpeed = DEFAULT_MAX_SPEED;
        this.repulsion = DEFAULT_REPULSION;
        this.springRepulsionFactor = DEFAULT_SPRING_REPULSION_FACTOR;
    }

    public ForceLayout<V, E> setRepulsion(double repulsion) {
        this.repulsion = repulsion;
        return this;
    }

    public ForceLayout<V, E> setFriction(double friction) {
        this.friction = friction;
        return this;
    }

    public ForceLayout<V, E> setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
        return this;
    }

    public ForceLayout<V, E> setSpringRepulsionFactor(double springRepulsionFactor) {
        this.springRepulsionFactor = springRepulsionFactor;
        return this;
    }

    public void execute() {
        long start = System.nanoTime();

        initializePoints();
        initializeSprings();

        int i;
        for (i = 0; i < getMaxSteps(); i++) {
            applyCoulombsLawToPoints();
            if (springRepulsionFactor != 0.0) {
                applyCoulombsLawToSprings();
            }
            applyHookesLaw();
            attractToCenter();
            updateVelocity();
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

    private Vector coulombsForce(Vector p1, Vector p2, double repulsion) {
        Vector distance = p1.subtract(p2);
        Vector direction = distance.normalize();
        return direction.multiply(repulsion).divide(distance.magnitudeSquare() * 0.5 + 0.1);
    }

    private void applyCoulombsLawToPoints() {
        for (Point point : getPoints().values()) {
            Vector p = point.getPosition();
            for (Point otherPoint : getPoints().values()) {
                if (!point.equals(otherPoint)) {
                    point.applyForce(coulombsForce(p, otherPoint.getPosition(), repulsion));
                }
            }
        }
    }

    private void applyCoulombsLawToSprings() {
        for (Point point : getPoints().values()) {
            Vector p = point.getPosition();
            for (Spring spring : getSprings()) {
                Point n1 = spring.getNode1();
                Point n2 = spring.getNode2();
                if (!n1.equals(point) && !n2.equals(point)) {
                    Vector q1 = spring.getNode1().getPosition();
                    Vector q2 = spring.getNode2().getPosition();
                    Vector center = q1.add(q2.subtract(q1).multiply(0.5));
                    Vector force = coulombsForce(p, center, repulsion * springRepulsionFactor);
                    point.applyForce(force);
                    n1.applyForce(force.multiply(-0.5));
                    n2.applyForce(force.multiply(-0.5));
                }
            }
        }
        for (Spring spring : getSprings()) {
            Point n1 = spring.getNode1();
            Point n2 = spring.getNode2();
            Vector p1 = spring.getNode1().getPosition();
            Vector p2 = spring.getNode2().getPosition();
            Vector center = p1.add(p2.subtract(p1).multiply(0.5));
            for (Spring otherSpring : getSprings()) {
                if (!spring.equals(otherSpring)) {
                    // Compute the repulsion force between centers of the springs
                    Vector op1 = otherSpring.getNode1().getPosition();
                    Vector op2 = otherSpring.getNode2().getPosition();
                    Vector otherCenter = op1.add(op2.subtract(op1).multiply(0.5));
                    Vector force = coulombsForce(center, otherCenter, repulsion * springRepulsionFactor);

                    // And apply it to both points of the spring
                    n1.applyForce(force);
                    n2.applyForce(force);
                }
            }
        }
    }

    private void applyHookesLaw() {
        for (Spring spring : getSprings()) {
            Point point1 = spring.getNode1();
            Point point2 = spring.getNode2();

            Vector distance = point2.getPosition().subtract(point1.getPosition());
            double displacement = spring.getLength() - distance.magnitude();
            Vector direction = distance.normalize();

            Vector force = direction.multiply(spring.getStiffness() * displacement * 0.5);
            point1.applyForce(force.multiply(-1));
            point2.applyForce(force);
        }
    }

    private void attractToCenter() {
        for (Point point : getPoints().values()) {
            Vector direction = point.getPosition().multiply(-1);

            point.applyForce(direction.multiply(repulsion / 200.0));
        }
    }

    private void updateVelocity() {
        for (Point point : getPoints().values()) {
            Vector newVelocity = point.getForces().multiply((1 - Math.exp(-getDeltaTime() * friction / point.getMass())) / friction);
            point.setVelocity(newVelocity);

            if (point.getVelocity().magnitude() > maxSpeed) {
                Vector velocity = point.getVelocity().normalize().multiply(maxSpeed);
                point.setVelocity(velocity);
            }

            point.resetForces();
        }
    }
}
