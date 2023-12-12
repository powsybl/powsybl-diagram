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
public class Grid {

    static class Node {
        private final Point point;
        private int cost;
        private int distance;
        private Node parent;

        public Node(Point p, int cost, int distance) {
            this.point = p;
            this.cost = cost;
            this.distance = distance;
            this.parent = null;
        }

        public Node(int x, int y, int cost, int distance) {
            this(new Point(x, y), cost, distance);
        }

        public Point getPoint() {
            return point;
        }

        public int getCost() {
            return cost;
        }

        public int getDistance() {
            return distance;
        }

        public Node getParent() {
            return parent;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Node node = (Node) o;
            return point == node.point &&
                   cost == node.cost &&
                   distance == node.distance;
        }

        @Override
        public int hashCode() {
            return Objects.hash(point, cost, distance);
        }
    }

    private static final int NOT_WALKABLE = -1;
    private static final int WALKABLE = 0;

    private final Node[][] nodes;
    private final int width;
    private final int height;

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        this.nodes = new Node[width][height];
        for (int x = 0; x < nodes.length; ++x) {
            for (int y = 0; y < nodes[0].length; ++y) {
                nodes[x][y] = new Node(x, y, NOT_WALKABLE, 0);
            }
        }
    }

    public void updateNode(Point point, int cost, int distance, Node parent) {
        Node node = getNode(point);
        node.cost = cost;
        node.distance = distance;
        node.parent = parent;
    }

    private Node getNode(Point point) {
        return getNode(point.x(), point.y());
    }

    private Node getNode(int x, int y) {
        // Make sure we are not out of bounds
        int nodeX = Math.max(0, Math.min(x, width - 1));
        int nodeY = Math.max(0, Math.min(y, height - 1));
        return nodes[nodeX][nodeY];
    }

    public void setAvailability(int x, int y, boolean available) {
        getNode(x, y).cost = available ? WALKABLE : NOT_WALKABLE;
    }

    public void setAvailability(Point point, boolean available) {
        setAvailability(point.x(), point.y(), available);
    }

    public void setAvailability(List<com.powsybl.sld.layout.pathfinding.Point> path, boolean available) {
        path.forEach(p -> setAvailability(p, available));
    }

    public boolean isAvailable(Node n) {
        return isAvailable(n.point);
    }

    public boolean isAvailable(Point point) {
        return point.x() >= 0 && point.x() < width && point.y() >= 0 && point.y() < height && nodes[point.x()][point.y()].cost != -1;
    }

    public List<Node> getNeighbors(Point point) {
        // Considering only adjacent points
        List<Node> neighbors = new ArrayList<>();
        Node right = getNode(point.x() + 1, point.y());
        Node left = getNode(point.x() - 1, point.y());
        Node up = getNode(point.x(), point.y() + 1);
        Node down = getNode(point.x(), point.y() - 1);
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
}
