/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout.pathfinding;

import java.util.*;

/**
 * @author Thomas Adam <tadam at neverhack.com>
 */
public final class DijkstraPathFinder extends AbstractPathFinder {

    private static final int[] DX = {1, 0, -1, 0};  // Horizontal moves
    private static final int[] DY = {0, 1, 0, -1};  // Vertical moves

    public DijkstraPathFinder() {
        // Nothing to do
    }

    @Override
    public List<Point> findShortestPath(Grid grid, int startX, int startY, int endX, int endY, boolean setSnakeLineAsObstacle) {
        Point start = new Point(startX, startY);
        Point end = new Point(endX, endY);
        Map<Point, Point> parent = new HashMap<>();
        Map<Point, Integer> distance = new HashMap<>();

        PriorityQueue<Point> queue = new PriorityQueue<>(Comparator.comparingInt(distance::get));

        distance.put(start, start.manhattanDistance(end));
        queue.add(start);

        while (!queue.isEmpty()) {
            Point current = queue.poll();
            if (current.equals(end)) {
                List<Point> path = reconstructPath(parent, end);
                // Make path not available
                grid.setAvailability(path, false);

                return smoothPath(path);
            }

            for (int i = 0; i < 4; i++) {
                int newX = current.x() + DX[i];
                int newY = current.y() + DY[i];
                Point neighbor = new Point(newX, newY);

                if (grid.isValid(neighbor)) {
                    int newDist = neighbor.manhattanDistance(end);

                    if (!distance.containsKey(neighbor) || newDist < distance.get(neighbor)) {
                        distance.put(neighbor, newDist);
                        parent.put(neighbor, current);
                        queue.add(neighbor);
                    }
                }
            }
        }
        return new ArrayList<>();  // No path found
    }

    private List<Point> reconstructPath(Map<Point, Point> parent, Point end) {
        List<Point> path = new ArrayList<>();
        Point current = end;
        while (parent.containsKey(current)) {
            path.add(current);
            current = parent.get(current);
        }
        Collections.reverse(path);
        return path;
    }

    public static List<Point> smoothPath(List<Point> path) {
        if (path.size() < 3) {
            return path;
        }

        List<Point> smoothedPath = new ArrayList<>();
        smoothedPath.add(path.get(0));

        for (int i = 1; i < path.size() - 1; i++) {
            Point prev = smoothedPath.get(smoothedPath.size() - 1);
            Point current = path.get(i);
            Point next = path.get(i + 1);

            if (isRightAngle(prev, current, next)) {
                smoothedPath.add(current);
            }
        }

        smoothedPath.add(path.get(path.size() - 1));

        return smoothedPath;
    }

    private static boolean isRightAngle(Point p1,
                                        Point p2,
                                        Point p3) {
        // Check if right angles when scalar products are null
        int dx1 = p2.x() - p1.x();
        int dy1 = p2.y() - p1.y();
        int dx2 = p3.x() - p2.x();
        int dy2 = p3.y() - p2.y();

        return dx1 * dx2 + dy1 * dy2 == 0;
    }
}
