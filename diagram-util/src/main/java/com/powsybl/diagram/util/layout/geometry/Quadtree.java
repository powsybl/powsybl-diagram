/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout.geometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/// This quadtree implementation is a Java version of a C++ quadtree presented by Nikita Lisitsa on his lisyarus blog
/// The article is called ["Building a quadtree in 22 lines of code"](https://lisyarus.github.io/blog/posts/building-a-quadtree.html)

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class Quadtree {
    public static class QuadtreeNode {
        // package private
        short[][] childrenNodeId = {
                {NO_CHILDREN, NO_CHILDREN},
                {NO_CHILDREN, NO_CHILDREN}
        };

        public short[][] getChildrenNodeId() {
            return childrenNodeId;
        }

        public short[] getChildrenNodeIdFlatten() {
            return new short[] {
                childrenNodeId[0][0],
                childrenNodeId[0][1],
                childrenNodeId[1][0],
                childrenNodeId[1][1]
            };
        }
    }

    BoundingBox bb;
    private final short rootIndex;
    private final ArrayList<QuadtreeNode> nodes = new ArrayList<>();
    // contains both position and mass (since Point has mass attribute)
    private final ArrayList<Point> barycenters = new ArrayList<>();
    public static final short NO_CHILDREN = -1;

    public Quadtree(Collection<Point> points, ToDoubleFunction<Point> massGetter) {
        bb = BoundingBox.computeBoundingBox(points);
        this.rootIndex = buildQuadtree(points.toArray(new Point[0]), bb, (short) 0, (short) points.size(), massGetter);
    }

    private short buildQuadtree(
            Point[] points,
            BoundingBox boundingBox,
            short firstIndex,
            short lastIndex,
            ToDoubleFunction<Point> massGetter
    ) {
        if (firstIndex == lastIndex) {
            // no children
            return NO_CHILDREN;
        }
        short newNodeIndex = (short) nodes.size();
        nodes.add(new QuadtreeNode());
        Point nodeBarycenter = new Point(0, 0);
        barycenters.add(nodeBarycenter);

        if (firstIndex + 1 == lastIndex) {
            // if there is only a single Point in this node, it's a leaf, barycenter is the point
            Point leafPoint = points[firstIndex];
            nodeBarycenter.setPosition(leafPoint.getPosition());
            // Springy and Atlas define mass differently for forces
            // for springy it's the literal mass, for Atlas it's the vertex degree + 1, just use a function to accommodate everyone
            nodeBarycenter.setMass(massGetter.applyAsDouble(leafPoint));
            return newNodeIndex;
        }

        Vector2D boundingBoxCenter = boundingBox.getCenter();
        Predicate<Vector2D> isBottom = v -> v.getY() < boundingBoxCenter.getY();
        Predicate<Vector2D> isLeft = v -> v.getX() < boundingBoxCenter.getX();

        // [firstIndex, ySplitIndex) is at the bottom, [ySplitIndex, lastIndex) is at the top
        short ySplitIndex = partitionPoints(points, firstIndex, lastIndex, isBottom);
        // [firstIndex, xLowerSplitIndex) is bottom left, [xLowerSplitIndex, ySplitIndex) is bottom right
        short xLowerSplitIndex = partitionPoints(points, firstIndex, ySplitIndex, isLeft);
        // [ySplitIndex, xUpperSplitIndex) os top left, [xUpperSplitIndex, lastIndex) is top right
        short xUpperSplitIndex = partitionPoints(points, ySplitIndex, lastIndex, isLeft);

        BoundingBox bottomLeftBb = new BoundingBox(boundingBox.getLeft(), boundingBoxCenter.getY(), boundingBoxCenter.getX(), boundingBox.getBottom());
        BoundingBox bottomRightBb = new BoundingBox(boundingBoxCenter.getX(), boundingBoxCenter.getY(), boundingBox.getRight(), boundingBox.getBottom());
        BoundingBox topLeftBb = new BoundingBox(boundingBox.getLeft(), boundingBox.getTop(), boundingBoxCenter.getX(), boundingBoxCenter.getY());
        BoundingBox topRightBb = new BoundingBox(boundingBoxCenter.getX(), boundingBox.getTop(), boundingBox.getRight(), boundingBoxCenter.getY());

        nodes.get(newNodeIndex).childrenNodeId[0][0] = buildQuadtree(points, bottomLeftBb, firstIndex, xLowerSplitIndex, massGetter);
        nodes.get(newNodeIndex).childrenNodeId[0][1] = buildQuadtree(points, bottomRightBb, xLowerSplitIndex, ySplitIndex, massGetter);
        nodes.get(newNodeIndex).childrenNodeId[1][0] = buildQuadtree(points, topLeftBb, ySplitIndex, xUpperSplitIndex, massGetter);
        nodes.get(newNodeIndex).childrenNodeId[1][1] = buildQuadtree(points, topRightBb, xUpperSplitIndex, lastIndex, massGetter);
        setNodeBarycenter(nodes.get(newNodeIndex), nodeBarycenter);

        return newNodeIndex;

    }

    /// This function partitions in place and in linear time the points between start and end index, given the splitPredicate
    /// It will return splitIndex such that:
    /// All points from startIndex (included) until splitIndex (excluded) will be such that splitPredicate is true
    /// All points from splitIndex (included) until endIndex (excluded) will be such that splitPredicate is false
    /// This function is adapted from the "Possible implementation" section of the
    /// C++ reference page for [std::partition](https://en.cppreference.com/w/cpp/algorithm/partition.html)
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

    private void setNodeBarycenter(QuadtreeNode node, Point point) {
        short[] barycenterIndex = node.getChildrenNodeIdFlatten();
        Vector2D barycenterPosition = new Vector2D();
        double totalBarycenterMass = 0;
        for (short index : barycenterIndex) {
            // index is only -1 in the case of nothing being there
            if (index != NO_CHILDREN) {
                // get the mass / position of each quadrant and do a weighted sum (quite literally)
                Point quadrantBarycenter = barycenters.get(index);
                Vector2D quadrantBarycenterPosition = new Vector2D(quadrantBarycenter.getPosition());
                // do not use the massGetter, that's only for leaf nodes, quadrants contain leaf which already have their correct mass set
                double quadrantMass = quadrantBarycenter.getMass();
                quadrantBarycenterPosition.multiplyBy(quadrantMass);
                barycenterPosition.add(quadrantBarycenterPosition);
                totalBarycenterMass += quadrantMass;
            }
        }
        barycenterPosition.divideBy(totalBarycenterMass);
        point.setPosition(barycenterPosition);
        point.setMass(totalBarycenterMass);
    }

    public short getRootIndex() {
        return rootIndex;
    }

    public List<QuadtreeNode> getNodes() {
        return nodes;
    }

    public List<Point> getBarycenters() {
        return barycenters;
    }

    public BoundingBox getBoundingBox() {
        return bb;
    }
}

