/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

import com.powsybl.nad.build.iidm.IdProvider;
import com.powsybl.nad.svg.EdgeInfo;
import com.powsybl.nad.svg.SvgEdgeInfo;

import java.util.*;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class BranchEdge extends AbstractEdge {

    public enum Side {
        ONE, TWO;

        public Side getOpposite() {
            return this == ONE ? TWO : ONE;
        }

        public int getNum() {
            return ordinal() + 1;
        }
    }

    public static final String TWO_WT_EDGE = "TwoWtEdge";
    public static final String PST_EDGE = "PstEdge";
    public static final String LINE_EDGE = "LineEdge";
    public static final String HVDC_LINE_LCC_EDGE = "HvdcLineLccEdge";
    public static final String HVDC_LINE_VSC_EDGE = "HvdcLineVscEdge";
    public static final String DANGLING_LINE_EDGE = "DanglingLineEdge";
    public static final String TIE_LINE_EDGE = "TieLineEdge";

    private List<Point> points1 = Collections.emptyList();
    private List<Point> points2 = Collections.emptyList();
    private Point arrowPoint1 = new Point();
    private Point arrowPoint2 = new Point();
    private double arrowAngle1;
    private double arrowAngle2;
    private final boolean[] visible = new boolean[] {true, true};
    private final SvgEdgeInfo svgEdgeInfo1;
    private final SvgEdgeInfo svgEdgeInfo2;
    private final SvgEdgeInfo svgEdgeInfoMiddle;

    public BranchEdge(IdProvider idProvider, String equipmentId, String nameOrId, String type,
                      EdgeInfo edgeInfo1, EdgeInfo edgeInfo2, EdgeInfo edgeInfoMiddle) {
        super(idProvider.createSvgId(equipmentId), equipmentId, nameOrId, type);
        this.svgEdgeInfo1 = isEdgeInfoNotEmptyNorNull(edgeInfo1) ? new SvgEdgeInfo(idProvider.createSvgId(equipmentId), edgeInfo1) : null;
        this.svgEdgeInfo2 = isEdgeInfoNotEmptyNorNull(edgeInfo2) ? new SvgEdgeInfo(idProvider.createSvgId(equipmentId), edgeInfo2) : null;
        this.svgEdgeInfoMiddle = isEdgeInfoNotEmptyNorNull(edgeInfoMiddle) ? new SvgEdgeInfo(idProvider.createSvgId(equipmentId), edgeInfoMiddle) : null;
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

    public Point getMiddlePoint() {
        return Point.createMiddlePoint(points1.getLast(), points2.getLast());
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

    public Point getArrow(Side side) {
        Objects.requireNonNull(side);
        return side == Side.ONE ? getArrowPoint1() : getArrowPoint2();
    }

    public Point getArrowPoint1() {
        return arrowPoint1;
    }

    public Point getArrowPoint2() {
        return arrowPoint2;
    }

    public void setArrow(Side side, Point arrow) {
        Objects.requireNonNull(side);
        if (side == Side.ONE) {
            setArrowPoint1(arrow);
        } else {
            setArrowPoint2(arrow);
        }
    }

    public void setArrowPoint1(Point arrowPoint1) {
        this.arrowPoint1 = arrowPoint1;
    }

    public void setArrowPoint2(Point arrowPoint2) {
        this.arrowPoint2 = arrowPoint2;
    }

    public void setArrowAngle(Side side, double edgeStartAngle) {
        Objects.requireNonNull(side);
        if (side == Side.ONE) {
            this.arrowAngle1 = edgeStartAngle;
        } else {
            this.arrowAngle2 = edgeStartAngle;
        }
    }

    public double getArrowAngle(Side side) {
        Objects.requireNonNull(side);
        return side == Side.ONE ? arrowAngle1 : arrowAngle2;
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

    public Optional<SvgEdgeInfo> getSvgEdgeInfo(Side side) {
        Objects.requireNonNull(side);
        return Optional.ofNullable(side == Side.ONE ? svgEdgeInfo1 : svgEdgeInfo2);
    }

    public Optional<SvgEdgeInfo> getSvgEdgeInfoMiddle() {
        return Optional.ofNullable(svgEdgeInfoMiddle);
    }

    private static boolean isEdgeInfoNotEmptyNorNull(EdgeInfo edgeInfo) {
        return edgeInfo != null && (edgeInfo.getInfoType2() != null || edgeInfo.getInfoType1() != null);
    }
}
