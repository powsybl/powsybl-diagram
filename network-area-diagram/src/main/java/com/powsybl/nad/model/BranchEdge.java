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
import java.util.Objects;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class BranchEdge extends AbstractEdge {

    public enum Side {
        ONE, TWO;

        public Side getOpposite() {
            return this == ONE ? TWO : ONE;
        }
    }

    public static final String TWO_WT_EDGE = "TwoWtEdge";
    public static final String PST_EDGE = "PstEdge";
    public static final String LINE_EDGE = "LineEdge";
    public static final String HVDC_LINE_EDGE = "HvdcLineEdge";

    private List<Point> points1 = Collections.emptyList();
    private List<Point> points2 = Collections.emptyList();
    private final boolean[] visible = new boolean[] {true, true};
    private final String type;

    public BranchEdge(String diagramId, String equipmentId, String nameOrId, String type) {
        super(diagramId, equipmentId, nameOrId);
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public boolean isTransformerEdge() {
        return PST_EDGE.equals(type) || TWO_WT_EDGE.equals(type);
    }

    public List<Point> getPoints(Side side) {
        Objects.requireNonNull(side);
        return side == Side.ONE ? getPoints1() : getPoints2();
    }

    public List<Point> getPoints1() {
        return Collections.unmodifiableList(points1);
    }

    public List<Point> getPoints2() {
        return Collections.unmodifiableList(points2);
    }

    public void setPoints(Side side, Point... points) {
        Objects.requireNonNull(side);
        if (side == Side.ONE) {
            setPoints1(points);
        } else {
            setPoints2(points);
        }
    }

    public void setPoints1(Point... points) {
        Arrays.stream(points).forEach(Objects::requireNonNull);
        this.points1 = Arrays.asList(points);
    }

    public void setPoints2(Point... points) {
        Arrays.stream(points).forEach(Objects::requireNonNull);
        this.points2 = Arrays.asList(points);
    }

    public boolean isVisible(Side side) {
        Objects.requireNonNull(side);
        return visible[side.ordinal()];
    }

    public void setVisible(Side side, boolean visible) {
        Objects.requireNonNull(side);
        this.visible[side.ordinal()] = visible;
    }

    public double getEdgeStartAngle(Side side) {
        List<Point> points = getPoints(side);
        return points.get(0).getAngle(points.get(1));
    }

    public double getEdgeEndAngle(Side side) {
        List<Point> points = getPoints(side);
        return points.get(points.size() - 2).getAngle(points.get(points.size() - 1));
    }
}
