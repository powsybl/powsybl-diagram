/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class ThreeWtEdge extends AbstractEdge {

    public enum Side {
        ONE, TWO, THREE;
    }

    public static final String THREE_WT_EDGE = "ThreeWtEdge";
    public static final String PST_EDGE = "PstEdge";

    private final Side side;
    private Point arrowPoint;

    private List<Point> points;

    private final boolean visible;

    public ThreeWtEdge(String diagramId, String equipmentId, String transformerName, Side side, String type, boolean visible) {
        super(diagramId, equipmentId, transformerName, type);
        this.side = side;
        this.visible = visible;
    }

    public void setPoints(Point point1, Point point2) {
        this.points = Arrays.asList(point1, point2);
    }

    public List<Point> getPoints() {
        return Collections.unmodifiableList(points);
    }

    public Point getArrowPoint() {
        return arrowPoint;
    }

    public void setArrowPoint(Point arrowPoint) {
        this.arrowPoint = arrowPoint;
    }

    public boolean isVisible() {
        return visible;
    }

    public Side getSide() {
        return side;
    }

    public double getEdgeAngle() {
        return points.get(0).getAngle(points.get(1));
    }
}
