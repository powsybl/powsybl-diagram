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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

/**
 * @author Mathilde Grapin {@literal <mathilde.grapin at rte-france.com>}
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public abstract class AbstractForceLayout<V, E> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractForceLayout.class);

    /** Deterministic randomness */
    private final Random random = new Random(3L);

    private static final int DEFAULT_MAX_STEPS = 1000;
    private static final double DEFAULT_MIN_ENERGY_THRESHOLD = 0.001;
    private static final double DEFAULT_DELTA_TIME = 1;
    private int maxSteps;
    private double minEnergyThreshold;
    private double deltaTime;
    /** Initial location for some nodes */
    private Map<V, Point> initialPoints = Collections.emptyMap();
    /** The location of these nodes should not be modified by the layout */
    private Set<V> fixedNodes = Collections.emptySet();

    private final Graph<V, E> graph;
    private final Map<V, Point> points = new LinkedHashMap<>();
    private final Set<Spring> springs = new LinkedHashSet<>();

    private boolean hasBeenExecuted = false;

    public abstract void execute();

    protected AbstractForceLayout(Graph<V, E> graph) {
        this.maxSteps = DEFAULT_MAX_STEPS;
        this.minEnergyThreshold = DEFAULT_MIN_ENERGY_THRESHOLD;
        this.deltaTime = DEFAULT_DELTA_TIME;
        this.graph = Objects.requireNonNull(graph);
    }

    public AbstractForceLayout<V, E> setMaxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
        return this;
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    public AbstractForceLayout<V, E> setMinEnergyThreshold(double minEnergyThreshold) {
        this.minEnergyThreshold = minEnergyThreshold;
        return this;
    }

    public double getDeltaTime() {
        return deltaTime;
    }

    public AbstractForceLayout<V, E> setDeltaTime(double deltaTime) {
        this.deltaTime = deltaTime;
        return this;
    }

    public AbstractForceLayout<V, E> setInitialPoints(Map<V, Point> initialPoints) {
        this.initialPoints = Objects.requireNonNull(initialPoints);
        return this;
    }

    public AbstractForceLayout<V, E> setFixedPoints(Map<V, Point> fixedPoints) {
        this.initialPoints = Objects.requireNonNull(fixedPoints);
        setFixedNodes(fixedPoints.keySet());
        return this;
    }

    public AbstractForceLayout<V, E> setFixedNodes(Set<V> fixedNodes) {
        this.fixedNodes = Objects.requireNonNull(fixedNodes);
        return this;
    }

    public void setHasBeenExecuted(boolean hasBeenExecuted) {
        this.hasBeenExecuted = hasBeenExecuted;
    }

    void initializePoints() {
        for (V vertex : graph.vertexSet()) {
            Point p;
            if (initialPoints.containsKey(vertex)) {
                Point pInitial = initialPoints.get(vertex);
                p = new Point(pInitial.getPosition().getX(), pInitial.getPosition().getY(), graph.degreeOf(vertex));
            } else {
                p = new Point(random.nextDouble(), random.nextDouble(), graph.degreeOf(vertex));
            }
            points.put(vertex, p);
        }
    }

    void initializeSprings() {
        for (E e : graph.edgeSet()) {
            Point pointSource = points.get(graph.getEdgeSource(e));
            Point pointTarget = points.get(graph.getEdgeTarget(e));
            if (pointSource != pointTarget) { // no use in force layout to add loops
                springs.add(new Spring(pointSource, pointTarget, graph.getEdgeWeight(e)));
            }
        }
    }

    void updatePosition() {
        // Optimisation hint: do not compute forces or update velocities for fixed nodes
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

    boolean isStable() {
        return points.values().stream().allMatch(p -> p.getEnergy() < minEnergyThreshold);
    }

    public Vector getStablePosition(V vertex) {
        if (!hasBeenExecuted) {
            LOGGER.warn("Force layout has not been executed yet");
        }
        return points.getOrDefault(vertex, new Point(-1, -1)).getPosition();
    }

    public Map<V, Point> getPoints() {
        return points;
    }

    public Set<Spring> getSprings() {
        return springs;
    }

    Set<V> getFixedNodes() {
        return fixedNodes;
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
