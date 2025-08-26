/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout.pathfinding;

import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.coordinate.PointInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.powsybl.sld.layout.pathfinding.AvailabilityGrid.WIRE;
import static com.powsybl.sld.layout.pathfinding.AvailabilityGrid.isRightAngle;

/**
 * Implementation of the A* algorithm for finding the shortest path between two points. This also adds some constraints,
 * such as an elevated cost for turning or crossing another wire of the grid.
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public final class AStarPathFinder implements PathFinder {
    private static final Logger LOGGER = LoggerFactory.getLogger(AStarPathFinder.class);
    /**
     * The cost of turning, ie making a 90° turn
     */
    private static final int TURNING_COST = 20;
    /**
     * The cost of having a wire cross another wire
     */
    private static final int CROSSING_COST = 100;

    /**
     * The grid that keeps track of which areas are available, using a wire or not
     */
    private final AvailabilityGrid availabilityGrid;

    /**
     * The movements we can do, using points instead of vectors because in the end, it's two double
     */
    private static final List<PointInteger> ALLOWED_MOVEMENTS = List.of(
        new PointInteger(1, 0),
        new PointInteger(-1, 0),
        new PointInteger(0, 1),
        new PointInteger(0, -1)
    );

    public AStarPathFinder(AvailabilityGrid availabilityGrid) {
        this.availabilityGrid = availabilityGrid;
    }

    @Override
    public List<Point> findShortestPath(Point start, Point goal) {
        PointInteger startInteger = new PointInteger(start);
        PointInteger goalInteger = new PointInteger(goal);
        Map<PointInteger, PathNode> visitedNodes = new HashMap<>();
        PriorityQueue<PathNode> queue = new PriorityQueue<>(Comparator.comparingDouble(PathNode::getTotalCost));
        queue.add(new PathNode(startInteger, null, 0, startInteger.manhattanDistance(goalInteger)));

        while (!queue.isEmpty()) {
            PathNode current = queue.poll();
            if (current.getPointInteger().equals(goalInteger)) {
                List<PointInteger> path = rebuildPath(current);
                // update the grid with the chosen path
                availabilityGrid.makeWire(path);
                // convert to List<Point>
                return convertToPointPath(path);
            }
            for (PointInteger neighbor : generateAvailableNeighbors(current)) {
                int neighborCost = current.getPathCost() + costOfMovement(current, neighbor);
                if (visitedNodes.containsKey(neighbor)) {
                    PathNode neighborNode = visitedNodes.get(neighbor);
                    if (neighborCost < neighborNode.getPathCost()) {
                        // we need to remove and add the node after modification, because there is no way to update the queue directly
                        queue.remove(neighborNode);
                        neighborNode.setPathCost(neighborCost);
                        neighborNode.setParent(current);
                        neighborNode.setTotalCost(neighborCost + neighbor.manhattanDistance(goalInteger));
                        queue.add(neighborNode);
                    }
                } else {
                    PathNode neighborPath = new PathNode(neighbor, current, neighborCost, neighborCost + neighbor.manhattanDistance(goalInteger));
                    visitedNodes.put(neighbor, neighborPath);
                    // add the neighbor as a new node to check
                    queue.add(neighborPath);
                }
            }
        }
        // No path was found, return an empty list
        LOGGER.error("No path was found between {} and {}", start, goal);
        return List.of();
    }

    @Override
    public List<Point> findShortestPath(Grid grid, Point start, Point goal) {
        return List.of();
    }

    private List<PointInteger> rebuildPath(PathNode lastNode) {
        PathNode current = lastNode;
        List<PointInteger> path = new ArrayList<>();
        while (current != null) {
            path.add(new PointInteger(current.getPointInteger().getX(), current.getPointInteger().getY()));
            current = current.getParent();
        }
        Collections.reverse(path);
        return path;
    }

    private List<Point> convertToPointPath(List<PointInteger> path) {
        List<Point> pointPath = new ArrayList<>();
        for (PointInteger pointInteger : path) {
            pointPath.add(new Point(pointInteger));
        }
        return pointPath;
    }

    private List<PointInteger> generateAvailableNeighbors(PathNode pathNode) {
        List<PointInteger> availableNeighbors = new ArrayList<>();
        PointInteger currentPoint = pathNode.getPointInteger();
        for (PointInteger movement : ALLOWED_MOVEMENTS) {
            PointInteger candidate = currentPoint.getShiftedPoint(movement);

            if (
                    !candidate.equals(pathNode.getParent().getPointInteger()) // check that we do not go backwards
                    && availabilityGrid.getGrid()[candidate.getY()][candidate.getX()] != AvailabilityGrid.NOT_AVAILABLE
            ) {
                availableNeighbors.add(candidate);
            }
        }
        return availableNeighbors;
    }

    private int costOfMovement(PathNode currentNode, PointInteger neighbor) {
        int cost = 1; // default cost of movement
        if (isRightAngle(currentNode.getParent().getPointInteger(), currentNode.getPointInteger(), neighbor)) {
            cost += TURNING_COST;
        }
        if (availabilityGrid.getGrid()[neighbor.getY()][neighbor.getX()] == WIRE) {
            cost += CROSSING_COST;
        }
        return cost;
    }
}

