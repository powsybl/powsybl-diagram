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
import java.util.stream.Stream;

/**
 * A quadtree is a structure that recursively divides a space in 4 until only a given number of points reside in each subdivided area. In this case,
 * the quadtree will only contain 1 point in each leaf node.
 * <p>This quadtree implementation is a Java version of a C++ quadtree presented by Nikita Lisitsa on his lisyarus blog.
 * The article is called "<a href="https://lisyarus.github.io/blog/posts/building-a-quadtree.html">Building a quadtree in 22 lines of code</a>"</p>
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class Quadtree {
    public static class QuadtreeNode {
        // package private
        int[][] childrenNodeIndex = {
                {NO_CHILDREN, NO_CHILDREN},
                {NO_CHILDREN, NO_CHILDREN}
        };
        /**
         * The barycenter of this node, ie the barycenter of all the points that are in the area of this node
         */
        Point nodeBarycenter;

        QuadtreeNode(Point nodeBarycenter) {
            this.nodeBarycenter = nodeBarycenter;
        }

        /**
         * @return all the index of children that actually exists (ie all quadrants that have at least a point in them)
         */
        public int[] getRealChildrenNodeIndex() {
            return Stream.of(
                childrenNodeIndex[0][0],
                childrenNodeIndex[0][1],
                childrenNodeIndex[1][0],
                childrenNodeIndex[1][1]
            ).filter(id -> id != NO_CHILDREN)
            .mapToInt(Integer::intValue)
            .toArray();
        }

        public Point getNodeBarycenter() {
            return nodeBarycenter;
        }
    }

    /**
     * The bounding box of all the points. This is also the area of the root of the quadtree
     */
    BoundingBox bb;
    /**
     * The index of the root node in the list of nodes
     */
    private final int rootIndex;
    /**
     * The array of all the nodes of the quadtree
     */
    private final QuadtreeNode[] nodes;
    /**
     * The index corresponding to a node that does not exist (for example if a given node doesn't have a children for an area, usually if it's a leaf node)
     */
    public static final int NO_CHILDREN = -1;
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
        List<QuadtreeNode> nodesList = new ArrayList<>(points.size());
        this.rootIndex = buildQuadtree(
                nodesList,
                new ArrayList<>(points),
                bb,
                massGetter,
                points.size(),
                MAX_RECURSION_DEPTH
        );

        this.nodes = nodesList.toArray(new QuadtreeNode[0]);
    }

    /**
     * Recursively build the quadtree
     * @param nodesList the current list of nodes (same as the array of nodes but using a list since we don't know the length yet)
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
    private int buildQuadtree(
            List<QuadtreeNode> nodesList,
            List<Point> points,
            BoundingBox boundingBox,
            ToDoubleFunction<Point> massGetter,
            int previousSize,
            int remainingDepth
    ) {
        if (points.isEmpty()) {
            // no children
            return NO_CHILDREN;
        }

        if (
            // if there is only a single Point in this node, it's a leaf, barycenter is the point
                points.size() == 1
                || remainingDepth == 0
            // if the size of the points in an area did not diminish, it might be because two or more points are at the same position, or very close
            // use the first two index checks to not have to calculate the equality between all points in the range each time
                || points.size() == previousSize && checkPointPositionEquality(points)
        ) {
            int newNodeIndex = nodesList.size();
            Point nodeBarycenter = new Point(0, 0);
            nodesList.add(new QuadtreeNode(nodeBarycenter));
            setLeafBarycenter(points, nodeBarycenter, massGetter);
            return newNodeIndex;
        }

        Vector2D boundingBoxCenter = boundingBox.getCenter();
        Predicate<Vector2D> isBottom = v -> v.getY() > boundingBoxCenter.getY(); // bottom is max Y, top is min Y
        Predicate<Vector2D> isLeft = v -> v.getX() < boundingBoxCenter.getX();

        // [firstIndex, ySplitIndex) is at the bottom, [ySplitIndex, lastIndex) is at the top
        int ySplitIndex = partitionPoints(points, isBottom);
        // [firstIndex, xLowerSplitIndex) is bottom left, [xLowerSplitIndex, ySplitIndex) is bottom right
        int xLowerSplitIndex = partitionPoints(points.subList(0, ySplitIndex), isLeft);
        // [ySplitIndex, xUpperSplitIndex) is top left, [xUpperSplitIndex, lastIndex) is top right
        int xUpperSplitIndex = ySplitIndex + partitionPoints(points.subList(ySplitIndex, points.size()), isLeft);

        BoundingBox bottomLeftBb = new BoundingBox(boundingBox.left(), boundingBoxCenter.getY(), boundingBoxCenter.getX(), boundingBox.bottom());
        BoundingBox bottomRightBb = new BoundingBox(boundingBoxCenter.getX(), boundingBoxCenter.getY(), boundingBox.right(), boundingBox.bottom());
        BoundingBox topLeftBb = new BoundingBox(boundingBox.left(), boundingBox.top(), boundingBoxCenter.getX(), boundingBoxCenter.getY());
        BoundingBox topRightBb = new BoundingBox(boundingBoxCenter.getX(), boundingBox.top(), boundingBox.right(), boundingBoxCenter.getY());

        int newNodeIndex = nodesList.size();
        //need to add it first here even though we modify it just after, because buildQuadtree is recursive. Need to conserve order
        Point nodeBarycenter = new Point(0, 0);
        nodesList.add(new QuadtreeNode(nodeBarycenter));

        nodesList.get(newNodeIndex).childrenNodeIndex[0][0] = buildQuadtree(nodesList, points.subList(0, xLowerSplitIndex), bottomLeftBb, massGetter, points.size(), remainingDepth - 1);
        nodesList.get(newNodeIndex).childrenNodeIndex[0][1] = buildQuadtree(nodesList, points.subList(xLowerSplitIndex, ySplitIndex), bottomRightBb, massGetter, points.size(), remainingDepth - 1);
        nodesList.get(newNodeIndex).childrenNodeIndex[1][0] = buildQuadtree(nodesList, points.subList(ySplitIndex, xUpperSplitIndex), topLeftBb, massGetter, points.size(), remainingDepth - 1);
        nodesList.get(newNodeIndex).childrenNodeIndex[1][1] = buildQuadtree(nodesList, points.subList(xUpperSplitIndex, points.size()), topRightBb, massGetter, points.size(), remainingDepth - 1);
        setNodeBarycenter(nodesList, nodesList.get(newNodeIndex), nodeBarycenter);

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
    private int partitionPoints(List<Point> points, Predicate<Vector2D> splitPredicate) {
        // directly find the index such that the predicate is false, no need to sort the start of the array if it's already ok
        int firstFalseIndex = 0;
        while (firstFalseIndex < points.size() && splitPredicate.test(points.get(firstFalseIndex).getPosition())) {
            ++firstFalseIndex;
        }

        for (int i = firstFalseIndex + 1; i < points.size(); ++i) {
            if (splitPredicate.test(points.get(i).getPosition())) {
                // swap points
                Point temp = points.get(i);
                points.set(i, points.get(firstFalseIndex));
                points.set(firstFalseIndex, temp);
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
    private void setLeafBarycenter(List<Point> points, Point nodeBarycenter, ToDoubleFunction<Point> massGetter) {
        Point leafPoint = points.getFirst();
        nodeBarycenter.setPosition(leafPoint.getPosition());
        double totalMass = massGetter.applyAsDouble(leafPoint);
        for (int i = 1; i < points.size(); ++i) {
            // Springy and Atlas define mass differently for forces
            // for springy it's the literal mass, for Atlas it's the vertex degree + 1, just use a function to accommodate everyone
            totalMass += massGetter.applyAsDouble(points.get(i));
        }
        nodeBarycenter.setMass(totalMass);
    }

    /**
     * Set the mass and position of the barycenter of a node that is not a leaf node (ie it has at least one children node, otherwise it would be a leaf node,
     * in which case, use {@link #setLeafBarycenter(Point[], Point, int, int, ToDoubleFunction)}
     * @param nodeList all the nodes
     * @param node the node for which we are calculating the barycenter
     * @param nodeBarycenter the barycenter of the node, the one we are setting the mass and position of
     */
    private void setNodeBarycenter(List<QuadtreeNode> nodeList, QuadtreeNode node, Point nodeBarycenter) {
        int[] barycenterIndex = node.getRealChildrenNodeIndex();
        Vector2D barycenterPosition = new Vector2D();
        double totalBarycenterMass = 0;
        for (int index : barycenterIndex) {
            // get the mass / position of each quadrant and do a weighted sum (quite literally)
            Point quadrantBarycenter = nodeList.get(index).nodeBarycenter;
            // do not use the massGetter, that's only for leaf nodes, quadrants contain leaf which already have their correct mass set
            double quadrantMass = quadrantBarycenter.getMass();
            barycenterPosition.addScaled(quadrantBarycenter.getPosition(), quadrantMass);
            totalBarycenterMass += quadrantMass;
        }
        barycenterPosition.divideBy(totalBarycenterMass);
        nodeBarycenter.setPosition(barycenterPosition);
        nodeBarycenter.setMass(totalBarycenterMass);
    }

    /**
     * Check if all the points between startIndex (included) and endIndex (excluded) have the same position
     * @param points all the points
     * @return true if all the points have the same position, false otherwise
     */
    private boolean checkPointPositionEquality(List<Point> points) {
        Vector2D firstPosition = points.getFirst().getPosition();
        for (int i = 1; i < points.size(); ++i) {
            if (!firstPosition.equals(points.get(i).getPosition())) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the index of the root of the quadtree, that is the index of the only node that does not have a parent in the nodes array
     */
    public int getRootIndex() {
        return rootIndex;
    }

    /**
     * @return the array of all the nodes of the quadtree
     */
    public QuadtreeNode[] getNodes() {
        return nodes;
    }

    /**
     * @return the bounding box of all the points of the quadtree, this is also the area of the root node of the quadtree
     */
    public BoundingBox getBoundingBox() {
        return bb;
    }
}

