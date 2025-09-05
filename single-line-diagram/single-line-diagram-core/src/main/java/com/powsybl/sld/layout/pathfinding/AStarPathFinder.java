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

import static com.powsybl.sld.layout.pathfinding.Headings.isRightAngle;

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
    private static final int TURNING_COST = 100;
    /**
     * The cost of being too close to another wire
     */
    private static final int AROUND_WIRE_COST = 10;
    /**
     * The cost of having a wire cross another wire
     */
    private static final int CROSSING_COST = 200;

    /**
     * The grid that keeps track of which areas are available, using a wire or not
     */
    private AvailabilityGrid availabilityGrid;

    @Override
    public List<Point> findBestPath(AvailabilityGrid availabilityGrid, Point start, Point goal) {
        this.availabilityGrid = availabilityGrid;
        //AvailabilityGridImageDisplayer.makeAvailabilityImage(availabilityGrid, "/tmp/grid_color.png");
        PointInteger startInteger = new PointInteger(start);
        PointInteger goalInteger = new PointInteger(goal);
        Map<PointHeading, PathNode> visitedNodes = new HashMap<>();
        PriorityQueue<PathNode> queue = new PriorityQueue<>(Comparator.comparingDouble(PathNode::getTotalCost));
        queue.add(new PathNode(new PointHeading(startInteger, null), null, 0, startInteger.manhattanDistance(goalInteger)));

        while (!queue.isEmpty()) {
            PathNode current = queue.poll();
            if (current.getPointHeading().getPoint().equals(goalInteger)) {
                List<PointInteger> path = rebuildPath(current);
                // update the grid with the chosen path
                availabilityGrid.makeWirePath(path);
                // make the path smooth (ie only keeping the right angles) and convert to List<Point>
                return convertToPointPath(makeSmoothPath(path));
            }
            for (PointHeading neighbor : generateAvailableNeighbors(current)) {
                int neighborCost = current.getPathCost() + costOfMovement(current, neighbor);
                if (visitedNodes.containsKey(neighbor)) {
                    PathNode neighborNode = visitedNodes.get(neighbor);
                    if (neighborCost < neighborNode.getPathCost()) {
                        // we need to remove and add the node after modification, because there is no way to update the queue directly
                        queue.remove(neighborNode);
                        neighborNode.setPathCost(neighborCost);
                        neighborNode.setParentNode(current);
                        neighborNode.setTotalCost(neighborCost + neighbor.getPoint().manhattanDistance(goalInteger));
                        queue.add(neighborNode);
                    }
                } else {
                    PathNode neighborPath = new PathNode(neighbor, current, neighborCost, neighborCost + neighbor.getPoint().manhattanDistance(goalInteger));
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

    /**
     * Starting from the last node in the path search, go back from parent to parent until backtracking to the start. This path is the shortest path
     * @param lastNode rebuild the path starting from the last node (generally the goal node)
     * @return the list of all the points starting from the start to the goal, which is the path with the lowest cost in terms of the distance metric defined (here we add virtual distance for right angle and wire superposition)
     */
    private List<PointInteger> rebuildPath(PathNode lastNode) {
        PathNode current = lastNode;
        List<PointInteger> path = new ArrayList<>();
        while (current != null) {
            path.add(current.getPointHeading().getPoint());
            current = current.getParentNode();
        }
        Collections.reverse(path);
        return path;
    }

    /**
     * Transform a path to only its changes in direction (since between those changes, it's only direct lines, so the information would be redundant otherwise)
     * @param notSmoothPath a path that has all its point
     * @return virtually the same path, but with only the changes in direction
     */
    private List<PointInteger> makeSmoothPath(List<PointInteger> notSmoothPath) {
        List<PointInteger> smoothPath = new ArrayList<>();
        smoothPath.add(notSmoothPath.get(0));
        // start from the second point, stop at the point before the last
        for (int i = 1; i < notSmoothPath.size() - 1; ++i) {
            PointInteger currentPoint = notSmoothPath.get(i);
            PointInteger previousPoint = notSmoothPath.get(i - 1);
            PointInteger nextPoint = notSmoothPath.get(i + 1);
            if (isRightAngle(previousPoint, currentPoint, nextPoint)) {
                smoothPath.add(currentPoint);
            }
        }
        smoothPath.add(notSmoothPath.get(notSmoothPath.size() - 1)); //getLast is only after JDK 21, this code is written on JDK 17
        return smoothPath;
    }

    /**
     * Convert a {@code List<PointInteger>} to a {@code List<Point>}.
     * Yes that's it, nothing more
     * @param path the path using point integer
     * @return same path but using point
     */
    private List<Point> convertToPointPath(List<PointInteger> path) {
        List<Point> pointPath = new ArrayList<>();
        for (PointInteger pointInteger : path) {
            pointPath.add(new Point(pointInteger));
        }
        return pointPath;
    }

    /**
     * Make the list of all the neighbors of a given node with the direction to go that neighbor, considering were we came from (no backtracking),
     * the borders of the grid, and which places are available or not
     * @param pathNode the node which we want to generate its neighbors for
     * @return the list of all neighbors that verify availability conditions as given in the description of this function, with the associated direction
     */
    private List<PointHeading> generateAvailableNeighbors(PathNode pathNode) {
        List<PointHeading> availableNeighbors = new ArrayList<>();
        PointInteger currentPoint = pathNode.getPointHeading().getPoint();
        for (PointInteger movement : Headings.ALL_HEADINGS) {
            // check that we do not go backwards
            // we can use == instead of .equals because we use static reference everywhere
            if (Headings.getOppositeHeading(pathNode.getPointHeading().getHeading()) == movement) {
                continue;
            }
            PointInteger candidate = currentPoint.getShiftedPoint(movement);
            int candidateX = candidate.getX();
            int candidateY = candidate.getY();
            //check that the candidate position is inside the bounds of the array and that it is available
            if (
                    availabilityGrid.isInBounds(candidateX, candidateY)
                    && !availabilityGrid.isNotAvailable(candidateX, candidateY)
            ) {
                PointHeading pointHeading = new PointHeading(candidate, movement);
                availableNeighbors.add(pointHeading);
            }
        }
        return availableNeighbors;
    }

    /**
     * Calculate the cost of the movement we are about to do. The default cost of a movement is 1, then we add cost if we are making a turn or crossing another wire
     * @param currentNode the node we are currently at
     * @param neighbor the point we are going to next
     * @return the cost of the movement from currentNode to neighbor
     */
    private int costOfMovement(PathNode currentNode, PointHeading neighbor) {
        int cost = 1; // default cost of movement
        // if the direction is different, then it means we are turning (since the neighbors can't do backtracking)
        if (currentNode.getPointHeading().getHeading() != neighbor.getHeading()) {
            cost += TURNING_COST;
        }
        if (availabilityGrid.isWire(neighbor.getPoint())) {
            cost += CROSSING_COST;
        }
        if (availabilityGrid.isAroundWire(neighbor.getPoint())) {
            cost += AROUND_WIRE_COST;
        }
        return cost;
    }
}

