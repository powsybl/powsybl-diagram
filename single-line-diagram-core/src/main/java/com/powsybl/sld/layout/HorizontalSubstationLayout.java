/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class HorizontalSubstationLayout extends AbstractSubstationLayout {

    public HorizontalSubstationLayout(SubstationGraph graph, VoltageLevelLayoutFactory vLayoutFactory) {
        super(graph, vLayoutFactory);
    }

    /**
     * Calculate relative coordinate of the next voltageLevel in the substation
     */
    @Override
    protected Coord calculateCoordVoltageLevel(LayoutParameters layoutParam, Graph vlGraph) {
        return new Coord(vlGraph.getWidth(), 0);
    }

    /*
     * Calculate polyline points of a snakeLine in the substation graph
     */
    @Override
    protected List<Double> calculatePolylineSnakeLine(LayoutParameters layoutParam, Node node1, Node node2,
                                                      InfosNbSnakeLines infosNbSnakeLines, boolean increment) {
        BusCell.Direction dNode1 = getNodeDirection(node1, 1);
        BusCell.Direction dNode2 = getNodeDirection(node2, 2);

        double xMaxGraph;
        String idMaxGraph;

        if (node1.getGraph().getX() > node2.getGraph().getX()) {
            xMaxGraph = node1.getGraph().getX();
            idMaxGraph = node1.getGraph().getVoltageLevelId();
        } else {
            xMaxGraph = node2.getGraph().getX();
            idMaxGraph = node2.getGraph().getVoltageLevelId();
        }

        double x1 = node1.getX();
        double y1 = node1.getY();
        double initY1 = node1.getInitY() != -1 ? node1.getInitY() : y1;
        double x2 = node2.getX();
        double y2 = node2.getY();
        double initY2 = node2.getInitY() != -1 ? node2.getInitY() : y2;

        InfoCalcPoints info = new InfoCalcPoints();
        info.setLayoutParam(layoutParam);
        info.setdNode1(dNode1);
        info.setdNode2(dNode2);
        info.setNbSnakeLinesTopBottom(infosNbSnakeLines.getNbSnakeLinesTopBottom());
        info.setNbSnakeLinesBetween(infosNbSnakeLines.getNbSnakeLinesBetween());
        info.setX1(x1);
        info.setX2(x2);
        info.setY1(y1);
        info.setInitY1(initY1);
        info.setY2(y2);
        info.setInitY2(initY2);
        info.setxMaxGraph(xMaxGraph);
        info.setIdMaxGraph(idMaxGraph);
        info.setIncrement(increment);

        return calculatePolylinePoints(info);
    }

    public static List<Double> calculatePolylinePoints(InfoCalcPoints info) {
        List<Double> pol = new ArrayList<>();

        LayoutParameters layoutParam = info.getLayoutParam();
        BusCell.Direction dNode1 = info.getdNode1();
        BusCell.Direction dNode2 = info.getdNode2();
        Map<BusCell.Direction, Integer> nbSnakeLinesTopBottom = info.getNbSnakeLinesTopBottom();
        Map<String, Integer> nbSnakeLinesBetween = info.getNbSnakeLinesBetween();
        double x1 = info.getX1();
        double x2 = info.getX2();
        double y1 = info.getY1();
        double initY1 = info.getInitY1();
        double y2 = info.getY2();
        double initY2 = info.getInitY2();
        double xMaxGraph = info.getxMaxGraph();
        String idMaxGraph = info.getIdMaxGraph();

        switch (dNode1) {
            case BOTTOM:
                if (dNode2 == BusCell.Direction.BOTTOM) {  // BOTTOM to BOTTOM
                    if (info.isIncrement()) {
                        nbSnakeLinesTopBottom.compute(dNode1, (k, v) -> v + 1);
                    }
                    double decalV = nbSnakeLinesTopBottom.get(dNode1) * layoutParam.getVerticalSnakeLinePadding();
                    double yDecal = Math.max(initY1 + decalV, initY2 + decalV);

                    pol.addAll(Arrays.asList(x1, y1,
                            x1, yDecal,
                            x2, yDecal,
                            x2, y2));

                } else {  // BOTTOM to TOP
                    if (info.isIncrement()) {
                        nbSnakeLinesTopBottom.compute(dNode1, (k, v) -> v + 1);
                        nbSnakeLinesTopBottom.compute(dNode2, (k, v) -> v + 1);
                    }
                    nbSnakeLinesBetween.compute(idMaxGraph, (k, v) -> v + 1);

                    double decal1V = nbSnakeLinesTopBottom.get(dNode1) * layoutParam.getVerticalSnakeLinePadding();
                    double decal2V = nbSnakeLinesTopBottom.get(dNode2) * layoutParam.getVerticalSnakeLinePadding();
                    double xBetweenGraph = xMaxGraph - (nbSnakeLinesBetween.get(idMaxGraph) * layoutParam.getHorizontalSnakeLinePadding());

                    pol.addAll(Arrays.asList(x1, y1,
                            x1, initY1 + decal1V,
                            xBetweenGraph, initY1 + decal1V,
                            xBetweenGraph, initY2 - decal2V,
                            x2, initY2 - decal2V,
                            x2, y2));
                }
                break;

            case TOP:
                if (dNode2 == BusCell.Direction.TOP) {  // TOP to TOP
                    if (info.isIncrement()) {
                        nbSnakeLinesTopBottom.compute(dNode1, (k, v) -> v + 1);
                    }
                    double decalV = nbSnakeLinesTopBottom.get(dNode1) * layoutParam.getVerticalSnakeLinePadding();
                    double yDecal = Math.min(initY1 - decalV, initY2 - decalV);

                    pol.addAll(Arrays.asList(x1, y1,
                            x1, yDecal,
                            x2, yDecal,
                            x2, y2));
                } else {  // TOP to BOTTOM
                    if (info.isIncrement()) {
                        nbSnakeLinesTopBottom.compute(dNode1, (k, v) -> v + 1);
                        nbSnakeLinesTopBottom.compute(dNode2, (k, v) -> v + 1);
                    }
                    nbSnakeLinesBetween.compute(idMaxGraph, (k, v) -> v + 1);
                    double decal1V = nbSnakeLinesTopBottom.get(dNode1) * layoutParam.getVerticalSnakeLinePadding();
                    double decal2V = nbSnakeLinesTopBottom.get(dNode2) * layoutParam.getVerticalSnakeLinePadding();

                    double xBetweenGraph = xMaxGraph - (nbSnakeLinesBetween.get(idMaxGraph) * layoutParam.getHorizontalSnakeLinePadding());

                    pol.addAll(Arrays.asList(x1, y1,
                            x1, initY1 - decal1V,
                            xBetweenGraph, initY1 - decal1V,
                            xBetweenGraph, initY2 + decal2V,
                            x2, initY2 + decal2V,
                            x2, y2));
                }
                break;
            default:
        }
        return pol;
    }

    @Override
    protected double getHorizontalSubstationPadding(LayoutParameters layoutParameters) {
        return layoutParameters.getHorizontalSubstationPadding();
    }

    @Override
    protected double getVerticalSubstationPadding(LayoutParameters layoutParameters) {
        return 0;
    }

}
