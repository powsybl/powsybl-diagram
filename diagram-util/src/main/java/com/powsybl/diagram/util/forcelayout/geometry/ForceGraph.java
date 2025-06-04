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
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
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

    private final SimpleGraph<V, DefaultEdge> simpleGraph;

    private final Map<V, Point> movingPoints = new LinkedHashMap<>();
    // this will be filled by the Setup function using fixedNodes and initialPoints
    private final Map<V, Point> fixedPoints = new LinkedHashMap<>();

    private Map<V, Point> initialPoints = Collections.emptyMap();
    private Set<V> fixedNodes = Collections.emptySet();

    public ForceGraph(Graph<V, E> graph) {
        SimpleGraph<V, DefaultEdge> locSimpleGraph = new SimpleGraph<>(DefaultEdge.class);
        for (V vertex : graph.vertexSet()) {
            locSimpleGraph.addVertex(vertex);
        }
        for (E edge : graph.edgeSet()) {
            V source = graph.getEdgeSource(edge);
            V target = graph.getEdgeTarget(edge);
            if (source != target) {
                locSimpleGraph.addEdge(graph.getEdgeSource(edge), graph.getEdgeTarget(edge));
            }
        }
        this.simpleGraph = locSimpleGraph;
    }

    public SimpleGraph<V, DefaultEdge> getSimpleGraph() {
        return simpleGraph;
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
        Objects.requireNonNull(fixedNodes);
        Set<V> intersection = new HashSet<>(fixedNodes);
        // only put fixed nodes that are actually in the graph
        // no need to have a fixed vertex if the vertex doesn't even exist in the graph
        intersection.retainAll(this.simpleGraph.vertexSet());
        if (!intersection.equals(fixedNodes)) {
            LOGGER.warn("Some nodes of the given fixedNodes were not nodes of the graph, those nodes have been ignored");
        }
        this.fixedNodes = intersection;
        return this;
    }

    public ForceGraph<V, E> setFixedPoints(Map<V, Point> fixedPoints) {
        Objects.requireNonNull(fixedPoints);
        Map<V, Point> intersection = new HashMap<>(fixedPoints);
        intersection.keySet().retainAll(this.simpleGraph.vertexSet());
        if (!intersection.keySet().equals(fixedPoints.keySet())) {
            LOGGER.warn("Some (vertex, point) of the given fixedPoints were not vertex of the graph, those (vertex, point) have been ignored");
        }
        this.initialPoints = intersection;
        setFixedNodes(intersection.keySet());
        return this;
    }

    public void toSVG(Function<V, String> tooltip, Writer writer) {
        BoundingBox boundingBoxMovingPoints = BoundingBox.computeBoundingBox(movingPoints.values());
        BoundingBox boundingBoxFixedPoints = BoundingBox.computeBoundingBox(fixedPoints.values());
        BoundingBox boundingBox = BoundingBox.addBoundingBoxes(boundingBoxMovingPoints, boundingBoxFixedPoints);
        Canvas canvas = new Canvas(boundingBox, 1000, 60);

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

        // use graph and not simple graph, because we want to represent multiple edges, in case of multiple lines between stations
        for (DefaultEdge edge : simpleGraph.edgeSet()) {
            V firstVertex = simpleGraph.getEdgeSource(edge);
            V secondVertex = simpleGraph.getEdgeTarget(edge);
            Optional<Point> point1Opt = getPointWithVertex(firstVertex);
            Optional<Point> point2Opt = getPointWithVertex(secondVertex);
            if (point1Opt.isEmpty() || point2Opt.isEmpty()) {
                LOGGER.error("No point found for edge, trying to continue with other vertex: {}", edge);
                continue;
            }
            Point point1 = point1Opt.get();
            Point point2 = point2Opt.get();
            // this seems incorrect (reversed point1 and point2), but this is a refactor, so I just copied what was in the Spring class
            Vector2D screenPosition1 = canvas.toScreen(point2.getPosition());
            Vector2D screenPosition2 = canvas.toScreen(point1.getPosition());
            printWriter.printf(
                    Locale.US,
                    "<line x1=\"%.2f\" y1=\"%.2f\" x2=\"%.2f\" y2=\"%.2f\"/>%n",
                    screenPosition1.getX(),
                    screenPosition1.getY(),
                    screenPosition2.getX(),
                    screenPosition2.getY()
            );
        }

        printWriter.println("</svg>");

        printWriter.close();
    }

    private Optional<Point> getPointWithVertex(V vertex) {
        Point point = movingPoints.get(vertex);
        if (point != null) {
            return Optional.of(point);
        } else {
            return Optional.ofNullable(fixedPoints.get(vertex));
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
