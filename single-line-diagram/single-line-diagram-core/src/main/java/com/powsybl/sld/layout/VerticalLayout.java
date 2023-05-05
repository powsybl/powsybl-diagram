/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.graphs.AbstractBaseGraph;
import com.powsybl.sld.model.graphs.BaseGraph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.Node;

import java.util.List;

import static com.powsybl.sld.model.coordinate.Direction.BOTTOM;
import static com.powsybl.sld.model.coordinate.Direction.TOP;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public final class VerticalLayout {

    private VerticalLayout() {
    }

    public static void adaptPaddingToSnakeLines(AbstractBaseGraph graph, LayoutParameters layoutParameters, InfosNbSnakeLinesVertical infosNbSnakeLines) {
        double widthSnakeLinesLeft = Math.max(infosNbSnakeLines.getNbSnakeLinesLeftRight().get(Side.LEFT) - 1, 0) * layoutParameters.getHorizontalSnakeLinePadding();

        LayoutParameters.Padding diagramPadding = layoutParameters.getDiagramPadding();
        LayoutParameters.Padding voltageLevelPadding = layoutParameters.getVoltageLevelPadding();

        double xVoltageLevels = widthSnakeLinesLeft + diagramPadding.getLeft() + voltageLevelPadding.getLeft();
        double y = diagramPadding.getTop()
                + graph.getVoltageLevelStream().findFirst().map(vlg -> getHeightHorizontalSnakeLines(vlg.getId(), TOP, layoutParameters, infosNbSnakeLines)).orElse(0.);

        for (VoltageLevelGraph vlGraph : graph.getVoltageLevels()) {
            vlGraph.setCoord(xVoltageLevels, y + voltageLevelPadding.getTop());
            y += vlGraph.getHeight() + getHeightHorizontalSnakeLines(vlGraph.getId(), BOTTOM, layoutParameters, infosNbSnakeLines);
        }

        double widthSnakeLinesRight = Math.max(infosNbSnakeLines.getNbSnakeLinesLeftRight().get(Side.RIGHT) - 1, 0) * layoutParameters.getHorizontalSnakeLinePadding();
        double substationWidth = graph.getWidth() + widthSnakeLinesLeft + widthSnakeLinesRight;
        double substationHeight = y - diagramPadding.getTop();
        graph.setSize(substationWidth, substationHeight);

        infosNbSnakeLines.reset();
    }

    private static double getHeightHorizontalSnakeLines(String vlGraphId, Direction direction, LayoutParameters layoutParameters, InfosNbSnakeLinesVertical infosNbSnakeLines) {
        return Math.max(infosNbSnakeLines.getNbSnakeLinesHorizontalBetween(vlGraphId, direction) - 1, 0) * layoutParameters.getHorizontalSnakeLinePadding();
    }

    public static double getVerticalShift(LayoutParameters layoutParam, Direction dNode1, int nbSnakeLines1) {
        return (nbSnakeLines1 - 1) * layoutParam.getVerticalSnakeLinePadding()
                + (dNode1 == Direction.TOP ? layoutParam.getVoltageLevelPadding().getTop() : layoutParam.getVoltageLevelPadding().getBottom());
    }

    /**
     * Dispatching the snake lines to the right and to the left
     */
    public static Side getSide(boolean increment) {
        return increment ? Side.LEFT : Side.RIGHT;
    }

    public static double getXSnakeLine(BaseGraph graph, Node node, Side side, LayoutParameters layoutParam, InfosNbSnakeLinesVertical infosNbSnakeLines, double maxVoltageLevelWidth) {
        double shiftLeftRight = Math.max(infosNbSnakeLines.getNbSnakeLinesLeftRight().compute(side, (k, v) -> v + 1) - 1, 0) * layoutParam.getHorizontalSnakeLinePadding();
        return graph.getVoltageLevelGraph(node).getX() - layoutParam.getVoltageLevelPadding().getLeft()
                + (side == Side.LEFT ? -shiftLeftRight : shiftLeftRight + maxVoltageLevelWidth);
    }

    public static double getYSnakeLine(BaseGraph graph, Node node, Direction dNode1, double decalV, LayoutParameters layoutParam) {
        double y = graph.getShiftedPoint(node).getY();
        if (dNode1 == BOTTOM) {
            return y + decalV;
        } else {
            List<VoltageLevelGraph> vls = graph.getVoltageLevels();
            int iVl = vls.indexOf(graph.getVoltageLevelGraph(node));
            if (iVl == 0) {
                return y - decalV;
            } else {
                VoltageLevelGraph vlAbove = vls.get(iVl - 1);
                return vlAbove.getY()
                        + vlAbove.getHeight() - layoutParam.getVoltageLevelPadding().getTop() - layoutParam.getVoltageLevelPadding().getBottom()
                        + decalV;
            }
        }
    }
}
