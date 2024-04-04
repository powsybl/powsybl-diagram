/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout.pathfinding;

import com.powsybl.sld.model.coordinate.*;

import java.io.*;
import java.util.*;

/**
 * @author Thomas Adam {@literal <tadam at neverhack.com>}
 */
public class Grid {

    static class Node {
        private final Point point;
        private boolean available;
        private int cost;
        private double distance;
        private Node parent;

        public Node(Point p, boolean available, double distance) {
            this.point = p;
            this.available = available;
            this.distance = distance;
            this.parent = null;
        }

        public Point getPoint() {
            return point;
        }

        public int getCost() {
            return cost;
        }

        public double getDistance() {
            return distance;
        }

        public Node getParent() {
            return parent;
        }
    }

    private final Node[][] nodes;
    private final int width;
    private final int height;

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        this.nodes = new Node[width][height];
        for (int x = 0; x < nodes.length; ++x) {
            for (int y = 0; y < nodes[0].length; ++y) {
                nodes[x][y] = new Node(new Point(x, y), false, 0);
            }
        }
    }

    public void updateNode(Point point, int cost, double distance, Node parent) {
        Node node = getNode(point);
        node.cost = cost;
        node.distance = distance;
        node.parent = parent;
    }

    private Node getNode(Point point) {
        return getNode(point.getX(), point.getY());
    }

    private Node getNode(double x, double y) {
        // Make sure we are not out of bounds
        double nodeX = Math.max(0, Math.min(x, width - 1.0));
        double nodeY = Math.max(0, Math.min(y, height - 1.0));
        return nodes[(int) nodeX][(int) nodeY];
    }

    public void setAvailability(double x, double y, boolean available) {
        getNode(x, y).available = available;
    }

    public void setAvailability(Point point, boolean available) {
        setAvailability(point.getX(), point.getY(), available);
    }

    public void setAvailability(List<Point> path, boolean available) {
        path.forEach(p -> setAvailability(p, available));
    }

    public boolean isAvailable(Node n) {
        return isAvailable(n.point);
    }

    public boolean isAvailable(Point point) {
        return point.getX() >= 0 && point.getX() < width && point.getY() >= 0 && point.getY() < height && nodes[(int) point.getX()][(int) point.getY()].available;
    }

    protected List<Node> getNeighbors(Point point) {
        // Considering only adjacent points
        List<Node> neighbors = new ArrayList<>();
        Node right = getNode(point.getX() + 1.0, point.getY());
        Node left = getNode(point.getX() - 1.0, point.getY());
        Node up = getNode(point.getX(), point.getY() + 1.0);
        Node down = getNode(point.getX(), point.getY() - 1.0);
        if (isAvailable(right)) {
            neighbors.add(right);
        }
        if (isAvailable(left)) {
            neighbors.add(left);
        }
        if (isAvailable(up)) {
            neighbors.add(up);
        }
        if (isAvailable(down)) {
            neighbors.add(down);
        }
        return neighbors;
    }

    public static boolean isRightAngle(Point previous, Point current, Point next) {
        // Check if the angle is a right angle using dot product
        double vectorABx = current.getX() - previous.getX();
        double vectorABy = current.getY() - previous.getY();
        double vectorBCx = next.getX() - current.getX();
        double vectorBCy = next.getY() - current.getY();

        // Dot product of vectors AB and BC
        double dotProduct = vectorABx * vectorBCx + vectorABy * vectorBCy;

        // Check if the dot product is zero (cosine of 90 degrees)
        return dotProduct == 0.0;
    }

    public static List<Point> getPointsAlongSnakeline(List<Point> points) {
        List<Point> pointsList = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);

            List<Point> pointsAlongSegment = getPointsAlongLineSegment(p1, p2);
            pointsList.addAll(pointsAlongSegment);
        }
        // Adding last point
        pointsList.add(points.get(points.size() - 1));
        return pointsList;
    }

    private static List<Point> getPointsAlongLineSegment(Point p1, Point p2) {
        List<Point> points = new ArrayList<>();
        int x1 = (int) p1.getX();
        int y1 = (int) p1.getY();
        int x2 = (int) p2.getX();
        int y2 = (int) p2.getY();
        // Compute coordinates differences
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        // Find out incrementation directions for x and y
        int sx = (x1 < x2) ? 1 : -1;
        int sy = (y1 < y2) ? 1 : -1;
        // Initialize error values
        int err = dx - dy;
        // Bresenham main loop
        while (true) {
            // Adding first point
            points.add(new Point(x1, y1));

            // Check if final destination is reached
            if (x1 == x2 && y1 == y2) {
                break;
            }
            // Compute next error value
            int e2 = 2 * err;
            // If error is bigger than difference in x, move in y
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            // If error is smaller than difference in y, move in x
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
        return points;
    }
}
