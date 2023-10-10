/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.pathfinding;

import java.util.*;

/**
 * @author Thomas Adam <tadam at neverhack.com>
 */
public final class Dijkstra {
    private static final int[] DX = {1, 0, -1, 0};  // Horizontal moves
    private static final int[] DY = {0, 1, 0, -1};  // Vertical moves

    public static List<Point> findShortestPath(Grid grid, int startX, int startY, int endX, int endY) {
        Point start = new Point(startX, startY);
        Point end = new Point(endX, endY);
        Map<Point, Point> parent = new HashMap<>();
        Map<Point, Integer> distance = new HashMap<>();

        PriorityQueue<Point> queue = new PriorityQueue<>(Comparator.comparingInt(distance::get));

        distance.put(start, 0);
        queue.add(start);

        while (!queue.isEmpty()) {
            Point current = queue.poll();
            if (current.equals(end)) {
                return reconstructPath(parent, end);
            }

            for (int i = 0; i < 4; i++) {
                int newX = current.x() + DX[i];
                int newY = current.y() + DY[i];
                Point neighbor = new Point(newX, newY);

                if (grid.isValid(neighbor)) {
                    int newDist = distance.get(current) + 1;
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

    private static List<Point> reconstructPath(Map<Point, Point> parent, Point start) {
        List<Point> path = new ArrayList<>();
        Point current = start;
        while (parent.containsKey(current)) {
            path.add(current);
            current = parent.get(current);
        }
        Collections.reverse(path);
        return simplify(path);
    }

    private static List<Point> simplify(List<Point> points) {
        if (points.size() < 3) {
            return points; // Do not simplify if less of 3 points
        }

        List<Point> simplifiedPoints = new ArrayList<>();
        simplifiedPoints.add(points.get(0)); // Add first point

        for (int i = 1; i < points.size() - 1; i++) {
            Point currentPoint = points.get(i);
            Point lastSimplifiedPoint = simplifiedPoints.get(simplifiedPoints.size() - 1);
            Point nextPoint = points.get(i + 1);

            if (isRightAngle(lastSimplifiedPoint, currentPoint, nextPoint)) {
                simplifiedPoints.add(currentPoint);
            }
        }

        simplifiedPoints.add(points.get(points.size() - 1)); // Add last point

        return simplifiedPoints;
    }

    private static boolean isRightAngle(Point p1,
                                        Point p2,
                                        Point p3) {
        int x1 = p1.x();
        int y1 = p1.y();
        int x2 = p2.x();
        int y2 = p2.y();
        int x3 = p3.x();
        int y3 = p3.y();

        // Check if right angles when scalar products are null
        int dx1 = x2 - x1;
        int dy1 = y2 - y1;
        int dx2 = x3 - x2;
        int dy2 = y3 - y2;

        return dx1 * dx2 + dy1 * dy2 == 0;
    }

    private Dijkstra() {
        // Hide default constructor
    }
}
