/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.forcelayout.geometry;

import org.jgrapht.Graph;

import java.util.*;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class ForceGraph<V, E> {
    public static final Point ORIGIN = new Point(0, 0);

    private final Graph<V, E> graph;

    private final Map<V, Point> movingPoints = new LinkedHashMap<>();
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

    public static void setCenter(Vector2D center) {
        ORIGIN.setPosition(center);
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
}
