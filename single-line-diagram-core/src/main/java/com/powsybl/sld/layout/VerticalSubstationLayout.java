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
public class VerticalSubstationLayout extends AbstractSubstationLayout {

    public VerticalSubstationLayout(SubstationGraph graph, VoltageLevelLayoutFactory vLayoutFactory) {
        super(graph, vLayoutFactory);
    }

    /**
     * Calculate relative coordinate of voltageLevels in the substation
     */
    @Override
    protected Coord calculateCoordVoltageLevel(LayoutParameters layoutParam, Graph vlGraph) {
        int maxV = vlGraph.getNodeBuses().stream()
                .mapToInt(nodeBus -> nodeBus.getPosition().getV() + nodeBus.getPosition().getVSpan())
                .max().orElse(0);

        double x = 0;
        double y = layoutParam.getInitialYBus() + layoutParam.getStackHeight() +
                layoutParam.getExternCellHeight() + layoutParam.getVerticalSpaceBus() * (maxV + 2);

        return new Coord(x, y);
    }

    /*
     * Calculate polyline points of a snakeLine in the substation graph
     */
    @Override
    protected List<Double> calculatePolylineSnakeLine(LayoutParameters layoutParam,
                                                   Edge edge,
                                                   Map<BusCell.Direction, Integer> nbSnakeLinesTopBottom,
                                                   Map<Side, Integer> nbSnakeLinesLeftRight,
                                                   Map<String, Integer> nbSnakeLinesBetween,
                                                   Map<String, Integer> nbSnakeLinesBottomVL,
                                                   Map<String, Integer> nbSnakeLinesTopVL) {
        Node node1 = edge.getNode1();
        Node node2 = edge.getNode2();

        BusCell.Direction dNode1 = getNodeDirection(node1, 1);
        BusCell.Direction dNode2 = getNodeDirection(node2, 2);

        double xMinGraph;

        if (node1.getGraph().getX() < node2.getGraph().getX()) {
            xMinGraph = node1.getGraph().getX();
        } else {
            xMinGraph = node2.getGraph().getX();
        }

        double x1 = node1.getX();
        double y1 = node1.getY();
        double x2 = node2.getX();
        double y2 = node2.getY();

        int maxH1 = node1.getGraph().getNodeBuses().stream()
                .mapToInt(nodeBus -> nodeBus.getPosition().getH() + nodeBus.getPosition().getHSpan())
                .max().orElse(0);
        int maxH2 = node2.getGraph().getNodeBuses().stream()
                .mapToInt(nodeBus -> nodeBus.getPosition().getH() + nodeBus.getPosition().getHSpan())
                .max().orElse(0);
        double maxH = layoutParam.getTranslateX() +
                nbSnakeLinesLeftRight.get(Side.LEFT) * layoutParam.getHorizontalSnakeLinePadding() +
                layoutParam.getInitialXBus() +
                (Math.max(maxH1, maxH2)) * layoutParam.getCellWidth();

        List<Double> pol = new ArrayList<>();
        switch (dNode1) {
            case BOTTOM:
                if (dNode2 == BusCell.Direction.BOTTOM) {  // BOTTOM to BOTTOM
                    nbSnakeLinesBottomVL.compute(node1.getGraph().getVoltageLevelId(), (k, v) -> v + 1);
                    nbSnakeLinesBottomVL.compute(node2.getGraph().getVoltageLevelId(), (k, v) -> v + 1);
                    nbSnakeLinesLeftRight.compute(Side.RIGHT, (k, v) -> v + 1);
                    double decal1V = nbSnakeLinesBottomVL.get(node1.getGraph().getVoltageLevelId()) * layoutParam.getVerticalSnakeLinePadding();
                    double decal2V = nbSnakeLinesBottomVL.get(node2.getGraph().getVoltageLevelId()) * layoutParam.getVerticalSnakeLinePadding();
                    double xSnakeLine = maxH + nbSnakeLinesLeftRight.get(Side.RIGHT) * layoutParam.getHorizontalSnakeLinePadding();

                    pol.addAll(Arrays.asList(x1, y1,
                            x1, y1 + decal1V,
                            xSnakeLine, y1 + decal1V,
                            xSnakeLine, y2 + decal2V,
                            x2, y2 + decal2V,
                            x2, y2));
                } else {  // BOTTOM to TOP
                    if (!graph.graphAdjacents(node1.getGraph(), node2.getGraph())) {
                        nbSnakeLinesBottomVL.compute(node1.getGraph().getVoltageLevelId(), (k, v) -> v + 1);
                        nbSnakeLinesTopVL.compute(node2.getGraph().getVoltageLevelId(), (k, v) -> v + 1);
                        nbSnakeLinesLeftRight.compute(Side.RIGHT, (k, v) -> v + 1);
                        double decal1V = nbSnakeLinesBottomVL.get(node1.getGraph().getVoltageLevelId()) * layoutParam.getVerticalSnakeLinePadding();
                        double decal2V = nbSnakeLinesTopVL.get(node2.getGraph().getVoltageLevelId()) * layoutParam.getVerticalSnakeLinePadding();
                        double xSnakeLine = maxH + nbSnakeLinesLeftRight.get(Side.RIGHT) * layoutParam.getHorizontalSnakeLinePadding();

                        pol.addAll(Arrays.asList(x1, y1,
                                x1, y1 + decal1V,
                                xSnakeLine, y1 + decal1V,
                                xSnakeLine, y2 - decal2V,
                                x2, y2 - decal2V,
                                x2, y2));
                    } else {  // node1 and node2 adjacent and node1 before node2
                        nbSnakeLinesBottomVL.compute(node1.getGraph().getVoltageLevelId(), (k, v) -> v + 1);
                        nbSnakeLinesTopVL.compute(node2.getGraph().getVoltageLevelId(), (k, v) -> v + 1);
                        double decal1V = nbSnakeLinesBottomVL.get(node1.getGraph().getVoltageLevelId()) * layoutParam.getVerticalSnakeLinePadding();
                        double decal2V = nbSnakeLinesTopVL.get(node2.getGraph().getVoltageLevelId()) * layoutParam.getVerticalSnakeLinePadding();
                        double ySnakeLine = Math.max(y1 + decal1V, y2 - decal2V);

                        pol.addAll(Arrays.asList(x1, y1,
                                x1, ySnakeLine,
                                x2, ySnakeLine,
                                x2, y2));
                    }
                }
                break;

            case TOP:
                if (dNode2 == BusCell.Direction.TOP) {  // TOP to TOP
                    nbSnakeLinesTopVL.compute(node1.getGraph().getVoltageLevelId(), (k, v) -> v + 1);
                    nbSnakeLinesTopVL.compute(node2.getGraph().getVoltageLevelId(), (k, v) -> v + 1);
                    nbSnakeLinesLeftRight.compute(Side.LEFT, (k, v) -> v + 1);
                    double decal1V = nbSnakeLinesTopVL.get(node1.getGraph().getVoltageLevelId()) * layoutParam.getVerticalSnakeLinePadding();
                    double decal2V = nbSnakeLinesTopVL.get(node2.getGraph().getVoltageLevelId()) * layoutParam.getVerticalSnakeLinePadding();
                    double xSnakeLine = xMinGraph - nbSnakeLinesLeftRight.get(Side.LEFT) * layoutParam.getHorizontalSnakeLinePadding();

                    pol.addAll(Arrays.asList(x1, y1,
                            x1, y1 - decal1V,
                            xSnakeLine, y1 - decal1V,
                            xSnakeLine, y2 - decal2V,
                            x2, y2 - decal2V,
                            x2, y2));
                } else {  // TOP to BOTTOM
                    if (!graph.graphAdjacents(node2.getGraph(), node1.getGraph())) {
                        nbSnakeLinesTopVL.compute(node1.getGraph().getVoltageLevelId(), (k, v) -> v + 1);
                        nbSnakeLinesBottomVL.compute(node2.getGraph().getVoltageLevelId(), (k, v) -> v + 1);
                        nbSnakeLinesLeftRight.compute(Side.LEFT, (k, v) -> v + 1);
                        double decal1V = nbSnakeLinesTopVL.get(node1.getGraph().getVoltageLevelId()) * layoutParam.getVerticalSnakeLinePadding();
                        double decal2V = nbSnakeLinesBottomVL.get(node2.getGraph().getVoltageLevelId()) * layoutParam.getVerticalSnakeLinePadding();
                        double xSnakeLine = xMinGraph - nbSnakeLinesLeftRight.get(Side.LEFT) * layoutParam.getHorizontalSnakeLinePadding();

                        pol.addAll(Arrays.asList(x1, y1,
                                x1, y1 - decal1V,
                                xSnakeLine, y1 - decal1V,
                                xSnakeLine, y2 + decal2V,
                                x2, y2 + decal2V,
                                x2, y2));
                    } else {  // node1 and node2 adjacent and node2 before node1
                        nbSnakeLinesTopVL.compute(node1.getGraph().getVoltageLevelId(), (k, v) -> v + 1);
                        nbSnakeLinesBottomVL.compute(node2.getGraph().getVoltageLevelId(), (k, v) -> v + 1);
                        double decal1V = nbSnakeLinesTopVL.get(node1.getGraph().getVoltageLevelId()) * layoutParam.getVerticalSnakeLinePadding();
                        double decal2V = nbSnakeLinesBottomVL.get(node2.getGraph().getVoltageLevelId()) * layoutParam.getVerticalSnakeLinePadding();
                        double ySnakeLine = Math.max(y1 - decal1V, y2 + decal2V);

                        pol.addAll(Arrays.asList(x1, y1,
                                x1, ySnakeLine,
                                x2, ySnakeLine,
                                x2, y2));
                    }
                }
                break;
            default:
        }
        return pol;
    }

    @Override
    protected double getHorizontalSubstationPadding(LayoutParameters layoutParameters) {
        return 0;
    }

    @Override
    protected double getVerticalSubstationPadding(LayoutParameters layoutParameters) {
        return layoutParameters.getVerticalSubstationPadding();
    }

}
