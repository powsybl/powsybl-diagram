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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    private final SvgEdgeInfo svgEdgeInfo;

    public ThreeWtEdge(IdProvider idProvider, String equipmentId, String transformerName, Side side, String type, boolean visible, EdgeInfo edgeInfo) {
        super(idProvider.createSvgId(equipmentId), equipmentId, transformerName, type);
        this.side = side;
        this.visible = visible;
        this.svgEdgeInfo = edgeInfo != null ? new SvgEdgeInfo(idProvider.createSvgId(equipmentId), edgeInfo) : null;
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

    public Optional<SvgEdgeInfo> getSvgEdgeInfo() {
        return Optional.ofNullable(svgEdgeInfo);
    }
}
