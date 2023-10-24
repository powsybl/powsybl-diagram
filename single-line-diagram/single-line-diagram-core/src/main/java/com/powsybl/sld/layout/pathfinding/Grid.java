/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.pathfinding;

import java.io.*;
import java.util.*;

/**
 * @author Thomas Adam <tadam at neverhack.com>
 */
public class Grid {

    static class Node {
        private final Point point;
        private int cost;

        public Node(int x, int y) {
            point = new Point(x, y);
            cost = WALKABLE;
        }

        public int x() {
            return point.x();
        }

        public int y() {
            return point.x();
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
                   cost == node.cost;
        }

        @Override
        public int hashCode() {
            return Objects.hash(point, cost);
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
                nodes[x][y] = new Node(x, y);
            }
        }
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
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

    public boolean isValid(Point point) {
        return point.x() >= 0 && point.x() < width && point.y() >= 0 && point.y() < height && nodes[point.x()][point.y()].cost != -1;
    }

    public void dumpToFile(String filename) {
        Objects.requireNonNull(filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            // Transpose from row / col to x / y
            int[][] toDump = transposeCost(nodes);

            for (int[] ints : toDump) {
                for (int y = 0; y < toDump[0].length; y++) {
                    String str = ints[y] == 0 ? " " : "X";
                    str += "";
                    writer.write(str);
                }
                writer.write("\n");
                writer.flush();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static int[][] transposeCost(Node[][] mat) {
        int[][] result = new int[mat[0].length][mat.length];
        for (int i = 0; i < mat.length; ++i) {
            for (int j = 0; j < mat[0].length; ++j) {
                result[j][i] = mat[i][j].cost;
            }
        }
        return result;
    }
}
