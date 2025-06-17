/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout.geometry;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
class QuadtreeTest {

    @Test
    void checkTreeStructure() {
        List<Point> points = List.of(
                new Point(0.5, 0.5),
                new Point(0, 0),
                new Point(0.22, 0.22),
                new Point(-1.29, 1.18),
                new Point(-0.77, -0.73),
                new Point(-1.38, -0.7),
                new Point(1.14, -0.92),
                new Point(0.87, 0.93),
                new Point(0.25, 0.34),
                new Point(0.63, 1.01)
        );
        Quadtree quadtree = new Quadtree(points, Point::getMass);
        short rootIndex = quadtree.getRootIndex();
        List<Quadtree.QuadtreeNode> nodes = quadtree.getNodes();
        List<Point> barycenters = quadtree.getBarycenters();
        List<Point> expectedBarycenters = List.of(
                new Point(0.017, 0.183, 10),
                new Point(-1.075, -0.715, 2),
                new Point(-1.075, -0.715, 2),
                new Point(-1.38, -0.7, 1),
                new Point(-0.77, -0.73, 1),
                new Point(0.57, -0.46, 2),
                new Point(1.14, -0.92, 1),
                new Point(0, 0, 1),
                new Point(-1.29, 1.18, 1),
                new Point(0.494, 0.6, 5),
                new Point(0.323, 0.353, 3),
                new Point(0.235, 0.28, 2),
                new Point(0.22, 0.22, 1),
                new Point(0.25, 0.34, 1),
                new Point(0.5, 0.5, 1),
                new Point(0.75, 0.97, 2),
                new Point(0.63, 1.01, 1),
                new Point(0.87, 0.93, 1)
        );
        checkChildBarycenter(rootIndex, nodes, barycenters, expectedBarycenters);
    }

    private void checkChildBarycenter(
            short parentNodeIndex,
            List<Quadtree.QuadtreeNode> nodes,
            List<Point> barycenters,
            List<Point> expectedBarycenters
    ) {
        double delta = 1e-3;
        Quadtree.QuadtreeNode thisNode = nodes.get(parentNodeIndex);
        short[] subAreaIndexList = thisNode.getChildrenNodeIdFlatten();
        // check all child areas
        for (short subAreaIndex : subAreaIndexList) {
            if (subAreaIndex != Quadtree.NO_CHILDREN) {
                checkChildBarycenter(subAreaIndex, nodes, barycenters, expectedBarycenters);
            }
        }
        // now check this node
        Point barycenter = barycenters.get(parentNodeIndex);
        Point expectedBarycenter = expectedBarycenters.get(parentNodeIndex);

        assertEquals(expectedBarycenter.getMass(), barycenter.getMass(), delta);
        assertEquals(expectedBarycenter.getPosition().getX(), barycenter.getPosition().getX(), delta);
        assertEquals(expectedBarycenter.getPosition().getY(), barycenter.getPosition().getY(), delta);
    }
}
