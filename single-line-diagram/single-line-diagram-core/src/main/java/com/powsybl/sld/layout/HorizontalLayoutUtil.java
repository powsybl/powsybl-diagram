/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.graphs.AbstractBaseGraph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;

import static com.powsybl.sld.model.coordinate.Direction.BOTTOM;
import static com.powsybl.sld.model.coordinate.Direction.TOP;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public final class HorizontalLayoutUtil {

    private HorizontalLayoutUtil() {
    }

    public static double computeCoordY(AbstractBaseGraph graph, LayoutParameters layoutParameters, double topPadding, VoltageLevelGraph vlGraph) {
        double y;
        // Find maximum voltage level top height
        double maxTopExternCellHeight = graph.getVoltageLevelStream().mapToDouble(g -> g.getExternCellHeight(Direction.TOP)).max().orElse(0.0);
        // Get gap between current voltage level and maximum height one
        double delta = maxTopExternCellHeight - vlGraph.getExternCellHeight(Direction.TOP);
        // Find maximum voltage level maxV
        double maxV = graph.getVoltageLevelStream().mapToDouble(VoltageLevelGraph::getMaxV).max().orElse(0.0);
        // Get all busbar section height
        double bbsHeight = layoutParameters.getVerticalSpaceBus() * (maxV - vlGraph.getMaxV());

        switch (layoutParameters.getBusbarsAlignment()) {
            case FIRST: {
                // Align on First busbar section
                y = topPadding + delta;
                break;
            }
            case LAST: {
                // Align on Last busbar section
                y = topPadding + delta + bbsHeight;
                break;
            }
            case MIDDLE: {
                // Align on middle of all busbar section
                y = topPadding + delta + bbsHeight / 2;
                break;
            }
            case NONE: // None alignment
            default:
                y = topPadding;
        }
        return y;
    }

    public static void adaptPaddingToSnakeLines(AbstractBaseGraph graph, LayoutParameters layoutParameters, InfosNbSnakeLinesHorizontal infosNbSnakeLines) {
        double heightSnakeLinesTop = AbstractLayout.getHeightSnakeLines(layoutParameters, TOP, infosNbSnakeLines);

        LayoutParameters.Padding diagramPadding = layoutParameters.getDiagramPadding();
        LayoutParameters.Padding voltageLevelPadding = layoutParameters.getVoltageLevelPadding();

        double topPadding = heightSnakeLinesTop + diagramPadding.getTop() + voltageLevelPadding.getTop();
        double x = diagramPadding.getLeft();

        for (VoltageLevelGraph vlGraph : graph.getVoltageLevels()) {
            x += AbstractLayout.getWidthVerticalSnakeLines(vlGraph.getId(), layoutParameters, infosNbSnakeLines);
            vlGraph.setCoord(x + voltageLevelPadding.getLeft(), HorizontalLayoutUtil.computeCoordY(graph, layoutParameters, topPadding, vlGraph));
            x += vlGraph.getWidth();
        }

        double zoneWidth = x - diagramPadding.getLeft();
        double heightSnakeLinesBottom = AbstractLayout.getHeightSnakeLines(layoutParameters, BOTTOM, infosNbSnakeLines);
        double zoneHeight = graph.getHeight() + heightSnakeLinesTop + heightSnakeLinesBottom;

        graph.setSize(zoneWidth, zoneHeight);

        infosNbSnakeLines.reset();
    }
}
