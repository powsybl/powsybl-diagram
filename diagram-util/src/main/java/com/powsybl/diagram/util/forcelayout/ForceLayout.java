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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

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
public class ForceLayout<V, E> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ForceLayout.class);

    /** Deterministic randomness */
    private final Random random = new Random(3L);

    private static final int DEFAULT_MAX_STEPS = 400;
    private static final double DEFAULT_MIN_ENERGY_THRESHOLD = 0.001;
    private static final double DEFAULT_DELTA_TIME = 0.1;
    private static final double DEFAULT_REPULSION = 800.0;
    private static final double DEFAULT_FRICTION = 500;
    private static final double DEFAULT_MAX_SPEED = 100;
    /** Spring repulsion is disabled by default */
    private static final double DEFAULT_SPRING_REPULSION_FACTOR = 0.0;

    private int maxSteps;
    private double minEnergyThreshold;
    private double deltaTime;
    private double repulsion;
    private double friction;
    private double maxSpeed;
    private double springRepulsionFactor;
    /** Initial location for some nodes */
    private Map<V, Point> initialPoints = Collections.emptyMap();
    /** The location of these nodes should not be modified by the layout */
    private Set<V> fixedNodes = Collections.emptySet();

    private final Graph<V, E> graph;
    private final Map<V, Point> points = new LinkedHashMap<>();
    private final Set<Spring> springs = new LinkedHashSet<>();

    private boolean hasBeenExecuted = false;

    public ForceLayout(Graph<V, E> graph) {
        this.maxSteps = DEFAULT_MAX_STEPS;
        this.minEnergyThreshold = DEFAULT_MIN_ENERGY_THRESHOLD;
        this.deltaTime = DEFAULT_DELTA_TIME;
        this.repulsion = DEFAULT_REPULSION;
        this.friction = DEFAULT_FRICTION;
        this.maxSpeed = DEFAULT_MAX_SPEED;
        this.springRepulsionFactor = DEFAULT_SPRING_REPULSION_FACTOR;
        this.graph = Objects.requireNonNull(graph);
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

    public ForceLayout<V, E> setInitialPoints(Map<V, Point> initialPoints) {
        this.initialPoints = Objects.requireNonNull(initialPoints);
        return this;
    }

    public ForceLayout<V, E> setFixedPoints(Map<V, Point> fixedPoints) {
        this.initialPoints = Objects.requireNonNull(fixedPoints);
        setFixedNodes(fixedPoints.keySet());
        return this;
    }

    public ForceLayout<V, E> setFixedNodes(Set<V> fixedNodes) {
        this.fixedNodes = Objects.requireNonNull(fixedNodes);
        return this;
    }

    private void initializePoints() {
        for (V vertex : graph.vertexSet()) {
            Point p;
            if (initialPoints.containsKey(vertex)) {
                p = initialPoints.get(vertex);
            } else {
                p = new Point(random.nextDouble(), random.nextDouble());
            }
            points.put(vertex, p);
        }
    }

    private void initializeSprings() {
        for (E e : graph.edgeSet()) {
            Point pointSource = points.get(graph.getEdgeSource(e));
            Point pointTarget = points.get(graph.getEdgeTarget(e));
            if (pointSource != pointTarget) { // no use in force layout to add loops
                springs.add(new Spring(pointSource, pointTarget, graph.getEdgeWeight(e)));
            }
        }
    }

    public void execute() {
        long start = System.nanoTime();

        initializePoints();
        initializeSprings();

        int i;
        for (i = 0; i < maxSteps; i++) {
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

        hasBeenExecuted = true;

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
        for (Point point : points.values()) {
            Vector p = point.getPosition();
            for (Point otherPoint : points.values()) {
                if (!point.equals(otherPoint)) {
                    point.applyForce(coulombsForce(p, otherPoint.getPosition(), repulsion));
                }
            }
        }
    }

    private void applyCoulombsLawToSprings() {
        for (Point point : points.values()) {
            Vector p = point.getPosition();
            for (Spring spring : springs) {
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
        for (Spring spring : springs) {
            Point n1 = spring.getNode1();
            Point n2 = spring.getNode2();
            Vector p1 = spring.getNode1().getPosition();
            Vector p2 = spring.getNode2().getPosition();
            Vector center = p1.add(p2.subtract(p1).multiply(0.5));
            for (Spring otherSpring : springs) {
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

            point.applyForce(direction.multiply(repulsion / 200.0));
        }
    }

    private void updateVelocity() {
        for (Point point : points.values()) {
            Vector newVelocity = point.getForces().multiply((1 - Math.exp(-deltaTime * friction / point.getMass())) / friction);
            point.setVelocity(newVelocity);

            if (point.getVelocity().magnitude() > maxSpeed) {
                Vector velocity = point.getVelocity().normalize().multiply(maxSpeed);
                point.setVelocity(velocity);
            }

            point.resetForces();
        }
    }

    private void updatePosition() {
        // TODO do not compute forces or update velocities for fixed nodes
        // We have computed forces and velocities for all nodes, even for the fixed ones
        // We can optimize calculations by ignoring fixed nodes in those calculations
        // Here we only update the position for the nodes that do not have fixed positions
        for (Map.Entry<V, Point> vertexPoint : points.entrySet()) {
            if (fixedNodes.contains(vertexPoint.getKey())) {
                continue;
            }
            Point point = vertexPoint.getValue();
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

    public void toSVG(Function<V, String> tooltip, Path path) throws IOException {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            toSVG(tooltip, writer);
        }
    }

    public void toSVG(Function<V, String> tooltip, Writer writer) {
        if (!hasBeenExecuted) {
            LOGGER.warn("Force layout has not been executed yet");
            return;
        }

        BoundingBox boundingBox = BoundingBox.computeBoundingBox(points.values());
        Canvas canvas = new Canvas(boundingBox, 600, 10);

        PrintWriter printWriter = new PrintWriter(writer);

        printWriter.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
        printWriter.printf(Locale.US, "<svg width=\"%.2f\" height=\"%.2f\" xmlns=\"http://www.w3.org/2000/svg\">%n", canvas.getWidth(), canvas.getHeight());
        printWriter.println("<style>");
        printWriter.println("<![CDATA[");
        printWriter.printf("circle {fill: %s;}%n", "purple");
        printWriter.printf("line {stroke: %s; stroke-width: 2}%n", "purple");
        printWriter.println("]]>");
        printWriter.println("</style>");

        points.forEach((vertex, point) -> point.toSVG(printWriter, canvas, tooltip, vertex));

        for (Spring spring : springs) {
            spring.toSVG(printWriter, canvas);
        }

        printWriter.println("</svg>");

        printWriter.close();
    }

}
