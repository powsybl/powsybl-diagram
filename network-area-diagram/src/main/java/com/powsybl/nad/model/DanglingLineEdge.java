/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class DanglingLineEdge extends AbstractEdge {

    public enum Side {
        NETWORK, BOUNDARY
    }

    private List<Point> pointsNetwork = Collections.emptyList();
    private List<Point> pointsBoundary = Collections.emptyList();
    private final boolean[] visible = new boolean[] {true, true};

    public DanglingLineEdge(String diagramId, String equipmentId, String nameOrId) {
        super(diagramId, equipmentId, nameOrId);
    }

    public List<Point> getPoints(Side side) {
        Objects.requireNonNull(side);
        return side == Side.NETWORK ? getPointsNetwork() : getPointsBoundary();
    }

    public List<Point> getPointsNetwork() {
        return Collections.unmodifiableList(pointsNetwork);
    }

    public List<Point> getPointsBoundary() {
        return Collections.unmodifiableList(pointsBoundary);
    }

    public void setPointsNetwork(Point... points) {
        Arrays.stream(points).forEach(Objects::requireNonNull);
        this.pointsNetwork = Arrays.asList(points);
    }

    public void setPointsBoundary(Point... points) {
        Arrays.stream(points).forEach(Objects::requireNonNull);
        this.pointsBoundary = Arrays.asList(points);
    }

    public boolean isVisible(Side side) {
        Objects.requireNonNull(side);
        return visible[side.ordinal()];
    }

    public double getEdgeAngle(Side side) {
        List<Point> points = side == Side.NETWORK ? pointsNetwork : pointsBoundary;
        return points.get(0).getAngle(points.get(1));
    }
}
