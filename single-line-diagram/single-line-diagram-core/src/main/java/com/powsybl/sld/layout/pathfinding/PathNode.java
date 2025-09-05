/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout.pathfinding;

/**
 * A structure to represent explored path when doing pathfinding
 * pointHeading represents the point of the node, and the direction we are going in (depending on where we came from)
 * pathCost is the realCost of the path up to this node
 * totalCost is the realCost + an optional heuristic distance
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class PathNode {
    private PointHeading pointHeading;
    private PathNode parentNode;
    private int pathCost;
    private double totalCost;

    public PathNode(PointHeading pointHeading, PathNode parentNode, int pathCost, double totalCost) {
        this.pointHeading = pointHeading;
        this.parentNode = parentNode;
        this.pathCost = pathCost;
        this.totalCost = totalCost;
    }

    public PointHeading getPointHeading() {
        return pointHeading;
    }

    public void setPointHeading(PointHeading pointHeading) {
        this.pointHeading = pointHeading;
    }

    public PathNode getParentNode() {
        return parentNode;
    }

    public void setParentNode(PathNode parentNode) {
        this.parentNode = parentNode;
    }

    public int getPathCost() {
        return pathCost;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setPathCost(int pathCost) {
        this.pathCost = pathCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }
}

