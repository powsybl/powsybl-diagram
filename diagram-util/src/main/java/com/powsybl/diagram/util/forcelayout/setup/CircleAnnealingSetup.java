/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout.setup;

import com.powsybl.diagram.util.forcelayout.geometry.ForceGraph;
import com.powsybl.diagram.util.forcelayout.geometry.Point;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.*;

/// This is the implementation of the method described in the paper:
/// Simulated Annealing as a Pre-Processing Step for Force-Directed Graph Drawing
/// Farshad Ghassemi Toosi, Nikola S. Nikolov, Malachy Eaton
/// July 2016

/// Modifications compared to the paper:
/// Make the size of the starting circle depending on the number of nodes in the graph, to prevent a density that is too high
/// Treat points that are not connected separately by putting those on another circle, larger than the first one

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class CircleAnnealingSetup<V, E> implements Setup<V, E> {
    @Override
    public void setup(ForceGraph<V, E> forceGraph, Random random) {

    }

    private SetupListData getPointWithNoEdgeAndPointPairWithLengthTwo(ForceGraph<V, E> forceGraph) {
        List<Set<V>> neighborSetPerVertex = new ArrayList<>();
        SimpleGraph<V, DefaultEdge> graph = forceGraph.getSimpleGraph();
        List<V> vertexWithNoEdge = new ArrayList<>();
        List<VertexPair<V>> vertexWithDistanceTwo = new ArrayList<>();
        @SuppressWarnings("unchecked")
        V[] allVertex = (V[]) graph.vertexSet().toArray();
        for (V vertex : allVertex) {
            neighborSetPerVertex.add(Graphs.neighborSetOf(graph, vertex));
        }
        for (int i = 0; i < neighborSetPerVertex.size(); ++i) {
            Set<V> setOfNeighbors = neighborSetPerVertex.get(i);
            if (setOfNeighbors.isEmpty()) {
                vertexWithNoEdge.add(allVertex[i]);
            } else {
                for (int k = i + 1; k < neighborSetPerVertex.size(); ++k) {
                    // if two vertex are not next to each other, and they share a common neighbor, it means they are at a distance of 2
                    if (
                            !setOfNeighbors.contains(allVertex[k])
                                    && !Collections.disjoint(setOfNeighbors, neighborSetPerVertex.get(k))
                    ) {
                        vertexWithDistanceTwo.add(new VertexPair<>(allVertex[i], allVertex[k]));
                    }
                }
            }
        }
        // Now convert from vertex to point and return
        List<Point> pointWithNoEdge = new ArrayList<>();
        List<PointPair> pointsWithDistanceTwo = new ArrayList<>();
        for (V vertex : vertexWithNoEdge) {
            pointWithNoEdge.add(Objects.requireNonNull(
                    forceGraph.getAllPoints().get(vertex),
                    String.format("No point corresponding to the given vertex %s", vertex.toString())
            ));
        }
        for (VertexPair<V> vertexPair : vertexWithDistanceTwo) {
            pointsWithDistanceTwo.add(new PointPair(
                    Objects.requireNonNull(
                            forceGraph.getAllPoints().get(vertexPair.first),
                            String.format("No point corresponding to the given vertex %s", vertexPair.first.toString())
                            ),
                    Objects.requireNonNull(
                            forceGraph.getAllPoints().get(vertexPair.second),
                            String.format("No point corresponding to the given vertex %s", vertexPair.second.toString())
                            )
                )
            );
        }
        return new SetupListData(
                pointWithNoEdge.toArray(new Point[0]),
                pointsWithDistanceTwo.toArray(new PointPair[0])
        );
    }

    private record PointPair(Point first, Point second) { }

    private record VertexPair<V>(V first, V second) { }

    private record SetupListData(Point[] pointWithNoEdge, PointPair[] pointsWithDistanceTwo) { }
}

