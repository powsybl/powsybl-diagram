/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.forcelayout.geometry;

import com.powsybl.diagram.util.forcelayout.Canvas;
import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class ForceGraph<V, E> {
    private final Point origin = new Point(0, 0);
    private static final Logger LOGGER = LoggerFactory.getLogger(ForceGraph.class);

    private final Graph<V, E> graph;

    private final Map<V, Point> movingPoints = new LinkedHashMap<>();
    // this will be filled by the Setup function using fixedNodes and initialPoints
    private final Map<V, Point> fixedPoints = new LinkedHashMap<>();

    private Map<V, Point> initialPoints = Collections.emptyMap();
    private Set<V> fixedNodes = Collections.emptySet();

    public ForceGraph(Graph<V, E> graph) {
        this.graph = graph;
    }

    public Graph<V, E> getGraph() {
        return graph;
    }

    public Map<V, Point> getMovingPoints() {
        return movingPoints;
    }

    public Map<V, Point> getFixedPoints() {
        return fixedPoints;
    }

    public Map<V, Point> getInitialPoints() {
        return initialPoints;
    }

    public Set<V> getFixedNodes() {
        return fixedNodes;
    }

    public void setCenter(Vector2D center) {
        origin.setPosition(center);
    }

    public Point getOrigin() {
        return origin;
    }

    public ForceGraph<V, E> setInitialPoints(Map<V, Point> initialPoints) {
        this.initialPoints = Objects.requireNonNull(initialPoints);
        return this;
    }

    public ForceGraph<V, E> setFixedNodes(Set<V> fixedNodes) {
        this.fixedNodes = Objects.requireNonNull(fixedNodes);
        return this;
    }

    public ForceGraph<V, E> setFixedPoints(Map<V, Point> fixedPoints) {
        this.initialPoints = Objects.requireNonNull(fixedPoints);
        setFixedNodes(fixedPoints.keySet());
        return this;
    }

    public void toSVG(Function<V, String> tooltip, Writer writer) {
        BoundingBox boundingBoxMovingPoints = BoundingBox.computeBoundingBox(movingPoints.values());
        BoundingBox boundingBoxFixedPoints = BoundingBox.computeBoundingBox(fixedPoints.values());
        BoundingBox boundingBox = BoundingBox.addBoundingBoxes(boundingBoxMovingPoints, boundingBoxFixedPoints);
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

        Stream.concat(
                movingPoints.entrySet().stream(),
                fixedPoints.entrySet().stream()
        ).forEach(entry -> entry.getValue().toSVG(printWriter, canvas, tooltip, entry.getKey()));

        for (E edge : graph.edgeSet()) {
            V firstVertex = graph.getEdgeSource(edge);
            V secondVertex = graph.getEdgeTarget(edge);
            Point point1;
            Point point2;
            try {
                point1 = getPointWithVertex(firstVertex);
                point2 = getPointWithVertex(secondVertex);
            } catch (NoSuchElementException e) {
                LOGGER.error("No vertex found, trying to continue with other vertex: %s", e);
                continue;
            }
            // this seems incorrect (reversed point1 and point2), but this is a refactor, so I just copied what was in the Spring class
            Vector2D screenPosition1 = canvas.toScreen(point2.getPosition());
            Vector2D screenPosition2 = canvas.toScreen(point1.getPosition());
            printWriter.printf(
                    Locale.US,
                    "<line x1=\"%.2f\" y1=\"%.2f\" x2=\"%.2f\" y2=\"%.2f\"/>%n",
                    screenPosition1.x(),
                    screenPosition1.y(),
                    screenPosition2.x(),
                    screenPosition2.y()
            );
        }

        printWriter.println("</svg>");

        printWriter.close();
    }

    private Point getPointWithVertex(V vertex) throws NoSuchElementException {
        Point point = movingPoints.get(vertex);
        if (point != null) {
            return point;
        } else {
            point = fixedPoints.get(vertex);
            if (point != null) {
                return point;
            } else {
                throw new NoSuchElementException("There is no point corresponding to this vertex");
            }
        }
    }

    public Vector2D getStablePosition(V vertex, boolean hasBeenExecuted) {
        Point fixedPoint = fixedPoints.get(vertex);
        if (fixedPoint != null) {
            return fixedPoint.getPosition();
        } else {
            if (!hasBeenExecuted) {
                LOGGER.warn("Vertex {} position was not fixed and force layout has not been executed yet", vertex);
            }
            return movingPoints.getOrDefault(vertex, new Point(-1, -1)).getPosition();
        }
    }
}
