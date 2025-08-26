/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout.pathfinding;

import com.powsybl.sld.model.coordinate.PointInteger;

/**
 * A structure to represent explored path when doing pathfinding
 * parent is where we came from to get to this node
 * pathCost is the realCost of the path up to this node
 * totalCost is the realCost + an optional heuristic distance
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class PathNode {
    private final PointInteger pointInteger;
    private PathNode parent;
    private int pathCost;
    private double totalCost;

    public PathNode(PointInteger pointInteger, PathNode parent, int pathCost, double totalCost) {
        this.pointInteger = pointInteger;
        this.parent = parent;
        this.pathCost = pathCost;
        this.totalCost = totalCost;
    }

    public PointInteger getPointInteger() {
        return pointInteger;
    }

    public PathNode getParent() {
        return parent;
    }

    public int getPathCost() {
        return pathCost;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setParent(PathNode parent) {
        this.parent = parent;
    }

    public void setPathCost(int pathCost) {
        this.pathCost = pathCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }
}

