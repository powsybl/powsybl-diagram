/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.BusCell;

import java.util.Map;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class InfoCalcPoints {
    private LayoutParameters layoutParam;
    private BusCell.Direction dNode1;
    private BusCell.Direction dNode2;
    private Map<BusCell.Direction, Integer> nbSnakeLinesTopBottom;
    private Map<String, Integer> nbSnakeLinesBetween;
    private double x1;
    private double x2;
    private double y1;
    private double y2;
    private double xMaxGraph;
    private String idMaxGraph;

    public LayoutParameters getLayoutParam() {
        return layoutParam;
    }

    public void setLayoutParam(LayoutParameters layoutParam) {
        this.layoutParam = layoutParam;
    }

    public BusCell.Direction getdNode1() {
        return dNode1;
    }

    public void setdNode1(BusCell.Direction dNode1) {
        this.dNode1 = dNode1;
    }

    public BusCell.Direction getdNode2() {
        return dNode2;
    }

    public void setdNode2(BusCell.Direction dNode2) {
        this.dNode2 = dNode2;
    }

    public Map<BusCell.Direction, Integer> getNbSnakeLinesTopBottom() {
        return nbSnakeLinesTopBottom;
    }

    public void setNbSnakeLinesTopBottom(Map<BusCell.Direction, Integer> nbSnakeLinesTopBottom) {
        this.nbSnakeLinesTopBottom = nbSnakeLinesTopBottom;
    }

    public Map<String, Integer> getNbSnakeLinesBetween() {
        return nbSnakeLinesBetween;
    }

    public void setNbSnakeLinesBetween(Map<String, Integer> nbSnakeLinesBetween) {
        this.nbSnakeLinesBetween = nbSnakeLinesBetween;
    }

    public double getX1() {
        return x1;
    }

    public void setX1(double x1) {
        this.x1 = x1;
    }

    public double getX2() {
        return x2;
    }

    public void setX2(double x2) {
        this.x2 = x2;
    }

    public double getY1() {
        return y1;
    }

    public void setY1(double y1) {
        this.y1 = y1;
    }

    public double getY2() {
        return y2;
    }

    public void setY2(double y2) {
        this.y2 = y2;
    }

    public double getxMaxGraph() {
        return xMaxGraph;
    }

    public void setxMaxGraph(double xMaxGraph) {
        this.xMaxGraph = xMaxGraph;
    }

    public String getIdMaxGraph() {
        return idMaxGraph;
    }

    public void setIdMaxGraph(String idMaxGraph) {
        this.idMaxGraph = idMaxGraph;
    }
}
