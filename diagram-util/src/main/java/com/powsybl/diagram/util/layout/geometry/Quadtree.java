/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.layout.geometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/// This quadtree implementation is a Java version of a C++ quadtree presented by Nikita Lisitsa on his lisyarus blog
/// The article is called ["Building a quadtree in 22 lines of code"](https://lisyarus.github.io/blog/posts/building-a-quadtree.html)

/**
 * A quadtree is a structure that recursively divides a space in 4 until only a given number of points reside in each subdivided area. In this case,
 * the quadtree will only contain 1 point in each leaf node.
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class Quadtree {
    public static class QuadtreeNode {
        // package private
        short[][] childrenNodeId = {
                {NO_CHILDREN, NO_CHILDREN},
                {NO_CHILDREN, NO_CHILDREN}
        };

        public short[] getChildrenNodeIdFlatten() {
            return new short[] {
                childrenNodeId[0][0],
                childrenNodeId[0][1],
                childrenNodeId[1][0],
                childrenNodeId[1][1]
            };
        }
    }

    /**
     * The bounding box of all the points. This is also the area of the root of the quadtree
     */
    BoundingBox bb;
    /**
     * The index of the root node in the list of nodes
     */
    private final short rootIndex;
    /**
     * The array of all the nodes of the quadtree
     */
    private final QuadtreeNode[] nodes;
    // contains both position and mass (since Point has mass attribute)
    /**
     * The array of barycenters, ie the position and mass of the corresponding node at the same index in the array of nodes.
     * That is to say, for a given index i, barycenters[i] is the barycenter of nodes[i]
     */
    private final Point[] barycenters;
    /**
     * The index corresponding to a node that does not exist (for example if a given node doesn't have a children for an area, usually if it's a leaf node)
     */
    public static final short NO_CHILDREN = -1;
    /**
     * The maximum recursion depth when dividing the space. Prevents problems where two points are at the same position or very close and can't be separated into two nodes
     */
    private static final int MAX_RECURSION_DEPTH = 64;

    /**
     * Build a quadtree with the given points and a function to get the mass of the point
     * @param points the list of points we want a quadtree for
     * @param massGetter a function that associates each point to a mass, used to calculate barycenter. This is used if we want
     *                   to calculate barycenter differently. For example, we might want to give all the points a mass of 1, or we might
     *                   want each point to have a mass equal to its number of edges + 1; this allows flexibility
     */
    public Quadtree(Collection<Point> points, ToDoubleFunction<Point> massGetter) {
        bb = BoundingBox.computeBoundingBox(points);
        List<QuadtreeNode> nodesList = new ArrayList<>();
        List<Point> barycentersList = new ArrayList<>();
        this.rootIndex = buildQuadtree(
                nodesList,
                barycentersList,
                points.toArray(new Point[0]),
                bb,
                (short) 0,
                (short) points.size(),
                massGetter,
                (short) 0,
                (short) 0,
                MAX_RECURSION_DEPTH
        );

        this.nodes = nodesList.toArray(new QuadtreeNode[0]);
        this.barycenters = barycentersList.toArray(new Point[0]);
    }

    /**
     * Recursively build the quadtree
     * @param nodesList the current list of nodes (same as the array of nodes but using a list since we don't know the length yet)
     * @param barycentersList the current list of barycenters (same as the array of barycenters but using a list since we don't know the length yet)
     * @param points all the points that we are building a quadtree on
     * @param boundingBox the bounding box of the current set of points we are doing the recursion on (this is not the bounding box of all the points)
     * @param firstIndex the index of the first point contained in the bounding box (included)
     * @param lastIndex the index of the last point contained in the bounding box (excluded)
     * @param massGetter the function that gives the mass of a point
     * @param previousFirstIndex the previous index of the first point contained in the previous bounding box (included).
     *                           Used for an optimisation to check if two or more points are at the same position (to avoid recursively dividing until the depth limit)
     * @param previousLastIndex the previous index of the last point contained in the previous bounding box (excluded).
     *                           Used for an optimisation to check if two or more points are at the same position (to avoid recursively dividing until the depth limit)
     * @param remainingDepth how many more division of the space can we make for this given bounding box
     * @return the index of the most parent node (if the depth is 0, this is the root of the quadtree, if the depth is 1, this is the index of a child of the root node, etc...)
     */
    private short buildQuadtree(
            List<QuadtreeNode> nodesList,
            List<Point> barycentersList,
            Point[] points,
            BoundingBox boundingBox,
            short firstIndex,
            short lastIndex,
            ToDoubleFunction<Point> massGetter,
            short previousFirstIndex,
            short previousLastIndex,
            int remainingDepth
    ) {
        if (firstIndex == lastIndex) {
            // no children
            return NO_CHILDREN;
        }
        short newNodeIndex = (short) nodesList.size();
        nodesList.add(new QuadtreeNode());
        Point nodeBarycenter = new Point(0, 0);
        barycentersList.add(nodeBarycenter);

        if (firstIndex + 1 == lastIndex) {
            // if there is only a single Point in this node, it's a leaf, barycenter is the point
            setLeafBarycenter(points, nodeBarycenter, firstIndex, lastIndex, massGetter);
            return newNodeIndex;
        }

        // if the size of the points in an area did not diminish, it might be because two or more points are at the same position, or very close
        // use this check to not have to calculate the equality between all points in the range each time
        if (remainingDepth == 0 || firstIndex == previousFirstIndex && lastIndex == previousLastIndex && checkPointPositionEquality(points, firstIndex, lastIndex)) {
            setLeafBarycenter(points, nodeBarycenter, firstIndex, lastIndex, massGetter);
            return newNodeIndex;
        }

        Vector2D boundingBoxCenter = boundingBox.getCenter();
        Predicate<Vector2D> isBottom = v -> v.getY() > boundingBoxCenter.getY(); // bottom is max Y, top is min Y
        Predicate<Vector2D> isLeft = v -> v.getX() < boundingBoxCenter.getX();

        // [firstIndex, ySplitIndex) is at the bottom, [ySplitIndex, lastIndex) is at the top
        short ySplitIndex = partitionPoints(points, firstIndex, lastIndex, isBottom);
        // [firstIndex, xLowerSplitIndex) is bottom left, [xLowerSplitIndex, ySplitIndex) is bottom right
        short xLowerSplitIndex = partitionPoints(points, firstIndex, ySplitIndex, isLeft);
        // [ySplitIndex, xUpperSplitIndex) is top left, [xUpperSplitIndex, lastIndex) is top right
        short xUpperSplitIndex = partitionPoints(points, ySplitIndex, lastIndex, isLeft);

        BoundingBox bottomLeftBb = new BoundingBox(boundingBox.getLeft(), boundingBoxCenter.getY(), boundingBoxCenter.getX(), boundingBox.getBottom());
        BoundingBox bottomRightBb = new BoundingBox(boundingBoxCenter.getX(), boundingBoxCenter.getY(), boundingBox.getRight(), boundingBox.getBottom());
        BoundingBox topLeftBb = new BoundingBox(boundingBox.getLeft(), boundingBox.getTop(), boundingBoxCenter.getX(), boundingBoxCenter.getY());
        BoundingBox topRightBb = new BoundingBox(boundingBoxCenter.getX(), boundingBox.getTop(), boundingBox.getRight(), boundingBoxCenter.getY());

        nodesList.get(newNodeIndex).childrenNodeId[0][0] = buildQuadtree(nodesList, barycentersList, points, bottomLeftBb, firstIndex, xLowerSplitIndex, massGetter, firstIndex, lastIndex, remainingDepth - 1);
        nodesList.get(newNodeIndex).childrenNodeId[0][1] = buildQuadtree(nodesList, barycentersList, points, bottomRightBb, xLowerSplitIndex, ySplitIndex, massGetter, firstIndex, lastIndex, remainingDepth - 1);
        nodesList.get(newNodeIndex).childrenNodeId[1][0] = buildQuadtree(nodesList, barycentersList, points, topLeftBb, ySplitIndex, xUpperSplitIndex, massGetter, firstIndex, lastIndex, remainingDepth - 1);
        nodesList.get(newNodeIndex).childrenNodeId[1][1] = buildQuadtree(nodesList, barycentersList, points, topRightBb, xUpperSplitIndex, lastIndex, massGetter, firstIndex, lastIndex, remainingDepth - 1);
        setNodeBarycenter(barycentersList, nodesList.get(newNodeIndex), nodeBarycenter);

        return newNodeIndex;

    }

    /**
     * Partition the points between start (included) and end (excluded) such as :
     * all points between startIndex (included) and splitIndex (the return value) (excluded) have the splitPredicate as true
     * all points between splitIndex (the return value) (included) and endIndex (excluded) have the splitPredicate as false
     * This will change in place the order of the points inside the array of point, but will not create new objects.
     * This runs in linear time. This function is adapted from the "Possible implementation" section of the
     * C++ reference page for [std::partition](https://en.cppreference.com/w/cpp/algorithm/partition.html)
     * @param points the array of all the points
     * @param startIndex the start index from which we want to partition the array (included)
     * @param endIndex the end index at which we stop the partition of the array (excluded)
     * @param splitPredicate a function that returns either true or false given a Vector,
     *                       it must be consistent ie if two vectors are equals with regards to .equals(),
     *                       then the splitPredicate must return the same boolean for both
     * @return the splitIndex at which the predicate changes from true to false on the sorted array.
     * for all i in [startIndex, splitIndex[, predicate(points[i]) is true
     * for all i in [splitIndex, endIndex[, predicate(points[i]) is false
     * no guarantee is given for points outside the [startIndex, endIndex[ range
     */
    private short partitionPoints(Point[] points, short startIndex, short endIndex, Predicate<Vector2D> splitPredicate) {
        // directly find the index such that the predicate is false, no need to sort the start of the array if it's already ok
        short firstFalseIndex = startIndex;
        while (firstFalseIndex < endIndex) {
            if (splitPredicate.test(points[firstFalseIndex].getPosition())) {
                ++firstFalseIndex;
            } else {
                break;
            }
        }
        if (firstFalseIndex == endIndex) {
            return firstFalseIndex;
        }

        for (int i = firstFalseIndex + 1; i < endIndex; ++i) {
            if (splitPredicate.test(points[i].getPosition())) {
                // swap points
                Point temp = points[i];
                points[i] = points[firstFalseIndex];
                points[firstFalseIndex] = temp;
                ++firstFalseIndex;
            }
        }
        return firstFalseIndex;
    }

    /**
     * Set the mass and position of the barycenter of a node, use this only if it's a leaf node. This function assumes all points in the [startIndex, endIndex[ range have the same position
     * (so either the common case of having a single point, or the rarer case of have multiple points on the same position)
     * @param points the list of all the points
     * @param nodeBarycenter the barycenter
     * @param startIndex the index (included) of the first point contained in the node of which we are setting the barycenter
     * @param endIndex the index (excluded) of the last point contained in the node (the endIndex point is not in the node, the endIndex - 1 is)
     * @param massGetter the function that associates a point to its mass
     */
    private void setLeafBarycenter(Point[] points, Point nodeBarycenter, short startIndex, short endIndex, ToDoubleFunction<Point> massGetter) {
        Point leafPoint = points[startIndex];
        nodeBarycenter.setPosition(leafPoint.getPosition());
        double totalMass = massGetter.applyAsDouble(leafPoint);
        for (int i = startIndex + 1; i < endIndex; ++i) {
            // Springy and Atlas define mass differently for forces
            // for springy it's the literal mass, for Atlas it's the vertex degree + 1, just use a function to accommodate everyone
            totalMass += massGetter.applyAsDouble(points[i]);
        }
        nodeBarycenter.setMass(totalMass);
    }

    /**
     * Set the mass and position of the barycenter of a node that is not a leaf node (ie it has at least one children node, otherwise it would be a leaf node,
     * in which case, use {@link #setLeafBarycenter(Point[], Point, short, short, ToDoubleFunction)}
     * @param barycentersList the list of all the barycenters
     * @param node the node for which we are calculating the barycenter
     * @param nodeBarycenter the barycenter of the node, the one we are setting the mass and position of
     */
    private void setNodeBarycenter(List<Point> barycentersList, QuadtreeNode node, Point nodeBarycenter) {
        short[] barycenterIndex = node.getChildrenNodeIdFlatten();
        Vector2D barycenterPosition = new Vector2D();
        double totalBarycenterMass = 0;
        for (short index : barycenterIndex) {
            // index is only -1 in the case of nothing being there
            if (index != NO_CHILDREN) {
                // get the mass / position of each quadrant and do a weighted sum (quite literally)
                Point quadrantBarycenter = barycentersList.get(index);
                Vector2D quadrantBarycenterPosition = new Vector2D(quadrantBarycenter.getPosition());
                // do not use the massGetter, that's only for leaf nodes, quadrants contain leaf which already have their correct mass set
                double quadrantMass = quadrantBarycenter.getMass();
                quadrantBarycenterPosition.multiplyBy(quadrantMass);
                barycenterPosition.add(quadrantBarycenterPosition);
                totalBarycenterMass += quadrantMass;
            }
        }
        barycenterPosition.divideBy(totalBarycenterMass);
        nodeBarycenter.setPosition(barycenterPosition);
        nodeBarycenter.setMass(totalBarycenterMass);
    }

    /**
     * Check if all the points between startIndex (included) and endIndex (excluded) have the same position
     * @param points all the points
     * @param startIndex the index of the first point we want to test position equality for (included)
     * @param endIndex the index of the last point we want to test position equality for (excluded)
     * @return true if all the points have the same position, false otherwise
     */
    private boolean checkPointPositionEquality(Point[] points, short startIndex, short endIndex) {
        Vector2D firstPosition = points[startIndex].getPosition();
        for (int i = startIndex + 1; i < endIndex; ++i) {
            if (!firstPosition.equals(points[i].getPosition())) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the index of the root of the quadtree, that is the index of the only node that does not have a parent in the nodes array
     */
    public short getRootIndex() {
        return rootIndex;
    }

    /**
     * @return the array of all the nodes of the quadtree
     */
    public QuadtreeNode[] getNodes() {
        return nodes;
    }

    /**
     * @return the array of all the barycenters of the quadtree
     */
    public Point[] getBarycenters() {
        return barycenters;
    }

    /**
     * @return the bounding box of all the points of the quadtree, this is also the area of the root node of the quadtree
     */
    public BoundingBox getBoundingBox() {
        return bb;
    }
}

