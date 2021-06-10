/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.force.layout;

import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.function.Function;

/**
 *
 * The following algorithm is a force layout algorithm.
 * It seeks to place the nodes of a graph in such a way that the nodes are well spaced and that there are no crossings.
 * The algorithm uses an analogy with physics where the nodes of the graph are particles with mass and the edges are springs.
 * Force calculations are used to place the nodes.
 *
 * The algorithm is taken from: https://github.com/dhotson/springy
 */

/**
 * @author Mathilde Grapin <mathilde.grapin at rte-france.com>
 */
public class ForceLayout<V, E> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ForceLayout.class);

    private final Random random = new Random();

    private static final int DEFAULT_MAX_STEPS = 2000;
    private static final double DEFAULT_MIN_ENERGY_THRESHOLD = 0.01;
    private static final double DEFAULT_DELTA_TIME = 0.05;
    private static final double DEFAULT_REPULSION = 400.0;
    private static final double DEFAULT_DAMPING = 0.5;
    private static final double DEFAULT_MAX_SPEED = Double.POSITIVE_INFINITY;

    private int maxSteps;
    private double minEnergyThreshold;
    private double deltaTime;
    private double repulsion;
    private double damping;
    private double maxSpeed;

    private Graph<V, E> graph;
    private Map<V, Point> points = new LinkedHashMap<>();
    private Set<Spring> springs = new LinkedHashSet<>();

    private boolean hasBeenExecuted = false;

    public ForceLayout(Graph<V, E> graph) {
        this.maxSteps = DEFAULT_MAX_STEPS;
        this.minEnergyThreshold = DEFAULT_MIN_ENERGY_THRESHOLD;
        this.deltaTime = DEFAULT_DELTA_TIME;
        this.repulsion = DEFAULT_REPULSION;
        this.damping = DEFAULT_DAMPING;
        this.maxSpeed = DEFAULT_MAX_SPEED;

        this.graph = graph;
    }

    public ForceLayout<V, E> setMaxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
        return this;
    }

    public ForceLayout<V, E> setMinEnergyThreshold(double minEnergyThreshold) {
        this.minEnergyThreshold = minEnergyThreshold;
        return this;
    }

    public ForceLayout<V, E> setDeltaTime(double deltaTime) {
        this.deltaTime = deltaTime;
        return this;
    }

    public ForceLayout<V, E> setRepulsion(double repulsion) {
        this.repulsion = repulsion;
        return this;
    }

    public ForceLayout<V, E> setDamping(double damping) {
        this.damping = damping;
        return this;
    }

    public ForceLayout<V, E> setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
        return this;
    }

    public ForceLayout<V, E> setInitialisationSeed(long seed) {
        random.setSeed(seed);
        return this;
    }

    private void initializePoints() {
        for (V vertex : graph.vertexSet()) {
            points.put(vertex, new Point(random.nextDouble(), random.nextDouble()));
        }
    }

    private void initializeSprings() {
        for (E e : graph.edgeSet()) {
            Point pointSource = points.get(graph.getEdgeSource(e));
            Point pointTarget = points.get(graph.getEdgeTarget(e));
            springs.add(new Spring(pointSource, pointTarget));
        }
    }

    public void execute() {
        long start = System.nanoTime();

        initializePoints();
        initializeSprings();

        int i;
        for (i = 0; i < maxSteps; i++) {
            applyCoulombsLaw();
            applyHookesLaw();
            attractToCenter();
            updateVelocity();
            updatePosition();

            if (isStable()) {
                break;
            }
        }

        hasBeenExecuted = true;

        long elapsedTime = System.nanoTime() - start;

        LOGGER.info("Number of steps: {}", i);
        LOGGER.info("Elapsed time: {}", (double) elapsedTime / 1e9);
    }

    private void applyCoulombsLaw() {
        Collection<Point> points = this.points.values();
        for (Point point : points) {
            for (Point otherPoint : points) {
                if (!point.equals(otherPoint)) {
                    Vector distance = point.getPosition().subtract(otherPoint.getPosition());
                    double magnitude = distance.magnitude() + 0.1;
                    Vector direction = distance.normalize();

                    Vector force = direction.multiply(repulsion).divide(magnitude * magnitude * 0.5);
                    point.applyForce(force);
                    otherPoint.applyForce(force.multiply(-1));
                }
            }
        }
    }

    private void applyHookesLaw() {
        for (Spring spring : springs) {
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
        for (Point point : points.values()) {
            Vector direction = point.getPosition().multiply(-1);

            point.applyForce(direction.multiply(repulsion / 50.0));
        }
    }

    private void updateVelocity() {
        for (Point point : points.values()) {
            Vector velocity = point.getVelocity().add(point.getAcceleration().multiply(deltaTime)).multiply(damping);
            point.setVelocity(velocity);

            if (point.getVelocity().magnitude() > maxSpeed) {
                velocity = point.getVelocity().normalize().multiply(maxSpeed);
                point.setVelocity(velocity);
            }

            point.setAcceleration(new Vector(0, 0));
        }
    }

    private void updatePosition() {
        for (Point point : points.values()) {
            Vector position = point.getPosition().add(point.getVelocity().multiply(deltaTime));
            point.setPosition(position);
        }
    }

    private boolean isStable() {
        return points.values().stream().allMatch(p -> p.getEnergy() < minEnergyThreshold);
    }

    public Vector getStablePosition(V vertex) {
        if (!hasBeenExecuted) {
            LOGGER.warn("Force layout has not been executed yet");
        }
        return points.getOrDefault(vertex, new Point(-1, -1)).getPosition();
    }

    public Set<Spring> getSprings() {
        return springs;
    }

    public void toSVG(Function<V, String> tooltip) throws IOException {
        if (!hasBeenExecuted) {
            LOGGER.warn("Force layout has not been executed yet");
            return;
        }

        BoundingBox boundingBox = computeBoundingBox();
        Canvas canvas = new Canvas((int) Math.ceil(boundingBox.getWidth() * 600 / boundingBox.getHeight()), 600);

        File tmpFile = File.createTempFile("springy", ".svg");

        FileWriter fileWriter = new FileWriter(tmpFile);
        PrintWriter printWriter = new PrintWriter(fileWriter);

        printWriter.printf("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
        printWriter.printf("<svg width=\"%d\" height=\"%d\" xmlns=\"http://www.w3.org/2000/svg\">%n", canvas.getWidth(), canvas.getHeight());
        printWriter.println("<style>");
        printWriter.println("<![CDATA[");
        printWriter.printf("circle {fill: %s;}%n", "purple");
        printWriter.printf("line {stroke: %s;}%n", "purple");
        printWriter.println("]]>");
        printWriter.println("</style>");

        points.forEach((vertex, point) -> {
            point.toSVG(printWriter, canvas, boundingBox, tooltip, vertex);
        });

        for (Spring spring : springs) {
            spring.toSVG(printWriter, canvas, boundingBox);
        }

        printWriter.println("</svg>");

        printWriter.close();
        fileWriter.close();
    }

    private BoundingBox computeBoundingBox() {
        Vector topRight = new Vector(2, 2);
        Vector bottomLeft = new Vector(-2, -2);

        for (Point node : points.values()) {
            Vector position = node.getPosition();
            if (position.getX() < bottomLeft.getX())  {
                bottomLeft.setX(position.getX());
            }
            if (position.getY() < bottomLeft.getY()) {
                bottomLeft.setY(position.getY());
            }
            if (position.getX() > topRight.getX()) {
                topRight.setX(position.getX());
            }
            if (position.getY() > topRight.getY()) {
                topRight.setY(position.getY());
            }
        }

        Vector padding = topRight.subtract(bottomLeft).multiply(0.07); // padding

        return new BoundingBox(topRight.add(padding), bottomLeft.subtract(padding));
    }
}
