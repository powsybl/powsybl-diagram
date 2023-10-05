/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.algo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Class used to find the best path from A to B.
 */
public final class PathFinding {

    private PathFinding() {
        // Utility class
    }

    /**
     * Method you should use to get path allowing 4 directional movement
     * @param grid grid to search in.
     * @param startPos starting position.
     * @param targetPos ending position.
     * @param allowDiagonals Pass true if you want 8 directional pathfinding, false for 4 direcitonal
     * @return List of Point's with found path.
     */
    public static List<Point> findPath(Grid grid, Point startPos, Point targetPos, boolean allowDiagonals) {
        // Find path
        List<Node> pathInNodes = findPathNodes(grid, startPos, targetPos, allowDiagonals);

        // Convert to a list of points and return
        List<Point> pathInPoints = new ArrayList<>();

        for (Node node : pathInNodes) {
            pathInPoints.add(new Point(node.x, node.y));
        }

        return pathInPoints;
    }

    /**
     * Helper method for findPath8Directions()
     * @param grid Gird instance containing information about tiles
     * @param startPos Starting position.
     * @param targetPos Targeted position.
     * @param allowDiagonals Pass true if you want 8 directional pathfinding, false for 4 direcitonal
     * @return List of Node's with found path.
     */
    private static List<Node> findPathNodes(Grid grid, Point startPos, Point targetPos, boolean allowDiagonals) {
        Node startNode = grid.nodes[startPos.x()][startPos.y()];
        Node targetNode = grid.nodes[targetPos.x()][targetPos.y()];

        List<Node> openSet = new ArrayList<>();
        HashSet<Node> closedSet = new HashSet<>();
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node currentNode = openSet.get(0);

            for (int k = 1; k < openSet.size(); k++) {
                Node open = openSet.get(k);

                if (open.getFCost() < currentNode.getFCost() ||
                        open.getFCost() == currentNode.getFCost() &&
                                open.hCost < currentNode.hCost) {
                    currentNode = open;
                }
            }

            openSet.remove(currentNode);
            closedSet.add(currentNode);

            if (currentNode == targetNode) {
                return retracePath(startNode, targetNode);
            }

            List<Node> neighbours;
            if (allowDiagonals) {
                neighbours = grid.get8Neighbours(currentNode);
            } else {
                neighbours = grid.get4Neighbours(currentNode);
            }

            for (Node neighbour : neighbours) {
                if (!neighbour.walkable || closedSet.contains(neighbour)) {
                    continue;
                }

                int newMovementCostToNeighbour = currentNode.gCost + getDistance(currentNode, neighbour) * (int) (10.0f * neighbour.price);
                if (newMovementCostToNeighbour < neighbour.gCost || !openSet.contains(neighbour)) {
                    neighbour.gCost = newMovementCostToNeighbour;
                    neighbour.hCost = getDistance(neighbour, targetNode);
                    neighbour.parent = currentNode;

                    if (!openSet.contains(neighbour)) {
                        openSet.add(neighbour);
                    }
                }
            }
        }

        return Collections.emptyList();
    }

    private static List<Node> retracePath(Node startNode, Node endNode) {
        List<Node> path = new ArrayList<>();
        Node currentNode = endNode;

        while (currentNode != startNode) {
            path.add(currentNode);
            currentNode = currentNode.parent;
        }

        Collections.reverse(path);
        return path;
    }

    private static int getDistance(Node nodeA, Node nodeB) {
        int distanceX = Math.abs(nodeA.x - nodeB.x);
        int distanceY = Math.abs(nodeA.y - nodeB.y);

        if (distanceX > distanceY) {
            return 14 * distanceY + 10 * (distanceX - distanceY);
        }
        return 14 * distanceX + 10 * (distanceY - distanceX);
    }

    public static List<Point> simplify(List<Point> points, double tolerance) {
        if (points.size() < 3) {
            return points; // Ne pas simplifier si moins de 3 points
        }

        List<Point> simplifiedPoints = new ArrayList<>();
        simplifiedPoints.add(points.get(0)); // Ajouter le premier point

        for (int i = 1; i < points.size() - 1; i++) {
            Point currentPoint = points.get(i);
            Point lastSimplifiedPoint = simplifiedPoints.get(simplifiedPoints.size() - 1);
            Point nextPoint = points.get(i + 1);

            double distance = pointToLineDistance(currentPoint, lastSimplifiedPoint, nextPoint);

            if (distance > tolerance) {
                simplifiedPoints.add(currentPoint);
            }
        }

        simplifiedPoints.add(points.get(points.size() - 1)); // Ajouter le dernier point

        return simplifiedPoints;
    }

    private static double pointToLineDistance(Point point, Point lineStart, Point lineEnd) {
        int x1 = lineStart.x();
        int y1 = lineStart.y();
        int x2 = lineEnd.x();
        int y2 = lineEnd.y();
        int x0 = point.x();
        int y0 = point.y();

        int dx = x2 - x1;
        int dy = y2 - y1;

        if (dx == 0 && dy == 0) {
            // Les deux points sont identiques, donc la distance est nulle.
            return Math.sqrt((x0 - x1) * (x0 - x1) + (y0 - y1) * (y0 - y1));
        }

        double t = ((x0 - x1) * dx + (y0 - y1) * dy) / (dx * dx + dy * dy);

        if (t < 0) {
            // Le point est en dehors du segment, la distance est la distance entre le point et le début du segment.
            return Math.sqrt((x0 - x1) * (x0 - x1) + (y0 - y1) * (y0 - y1));
        }

        if (t > 1) {
            // Le point est en dehors du segment, la distance est la distance entre le point et la fin du segment.
            return Math.sqrt((x0 - x2) * (x0 - x2) + (y0 - y2) * (y0 - y2));
        }

        // Le point est sur le segment, la distance est la distance perpendiculaire au segment.
        int perpendicularX = (int) (x1 + t * dx);
        int perpendicularY = (int) (y1 + t * dy);
        return Math.sqrt((x0 - perpendicularX) * (x0 - perpendicularX) + (y0 - perpendicularY) * (y0 - perpendicularY));
    }
}
