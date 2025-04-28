/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.forcelayout.geometry;

import java.util.*;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class Graph {
    private final HashMap<Point, Edge[]> points;
    private final Edge[] edges;

    public Graph(Collection<Edge> edges) {
        HashMap<Point, List<Edge>> tempPoints = new HashMap<>();
        this.edges = new HashSet<>(edges).toArray(new Edge[0]);

        for (Edge edge : this.edges) {
            tempPoints.computeIfAbsent(edge.first(), p -> new ArrayList<>()).add(edge);
            tempPoints.computeIfAbsent(edge.second(), p -> new ArrayList<>()).add(edge);
        }

        this.points = new HashMap<>();
        for (Map.Entry<Point, List<Edge>> entry : tempPoints.entrySet()) {
            this.points.put(entry.getKey(), entry.getValue().toArray(new Edge[0]));
        }
    }

    public Map<Point, Edge[]> getPoints() {
        return points;
    }

    public Edge[] getEdges() {
        return edges;
    }
}
