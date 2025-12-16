/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout.pathfinding;

import com.powsybl.sld.model.coordinate.*;

import java.util.*;

/**
 * @author Thomas Adam {@literal <tadam at neverhack.com>}
 */
public final class DijkstraPathFinder implements PathFinder {

    @Override
    public List<Point> findShortestPath(Grid grid, Point start, Point goal) {
        Set<Point> visited = new HashSet<>();
        PriorityQueue<Grid.Node> queue = new PriorityQueue<>(Comparator.comparingDouble(node -> node.getCost() + node.getDistance()));
        queue.add(new Grid.Node(start, true, start.distance(goal)));

        while (!queue.isEmpty()) {
            Grid.Node current = queue.poll();
            Point currentPoint = current.getPoint();
            Grid.Node currentParent = current.getParent();
            // Path can be rebuilt only when goal point is reached
            if (currentPoint.equals(goal)) {
                List<Point> path = rebuildPath(current);
                // Make path not available
                grid.setAvailability(path, false);
                return path;
            }
            // Store node already visited
            visited.add(currentPoint);
            // Loop on available neighbors
            for (Grid.Node neighbor : grid.getNeighbors(currentPoint)) {
                Point neighborPoint = neighbor.getPoint();
                // Avoid to visit previous visited point
                if (!visited.contains(neighborPoint)) {
                    int cost = 1;
                    if (currentParent != null && Grid.isRightAngle(currentParent.getPoint(), currentPoint, neighborPoint)) {
                        // Assuming right angle are useless
                        cost++;
                    }
                    // Update Node parameters
                    grid.updateNode(neighborPoint,
                            current.getCost() + cost,
                            neighborPoint.distance(goal),
                            current);
                    // Adding next path node
                    queue.add(neighbor);
                }
            }
        }
        // No path found
        return new ArrayList<>();
    }

    private List<Point> rebuildPath(Grid.Node goal) {
        Grid.Node current = goal;
        // Reconstruct path
        List<Point> path = new ArrayList<>();
        while (current != null) {
            path.add(current.getPoint());
            current = current.getParent();
        }
        Collections.reverse(path);
        return smoothPath(path);
    }

    // Only keep directions changes points
    public static List<Point> smoothPath(List<Point> path) {
        if (path.size() < 3) {
            return path;
        }

        List<Point> smoothedPath = new ArrayList<>();
        smoothedPath.add(path.getFirst());

        for (int i = 1; i < path.size() - 1; i++) {
            Point prev = smoothedPath.getLast();
            Point current = path.get(i);
            Point next = path.get(i + 1);

            if (Grid.isRightAngle(prev, current, next)) {
                smoothedPath.add(current);
            }
        }

        smoothedPath.add(path.getLast());

        return smoothedPath;
    }
}
