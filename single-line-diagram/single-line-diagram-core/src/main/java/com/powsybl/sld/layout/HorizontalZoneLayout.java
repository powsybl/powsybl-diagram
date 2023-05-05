/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.graphs.ZoneGraph;
import com.powsybl.sld.model.nodes.Node;

import java.util.List;

import static com.powsybl.sld.model.coordinate.Direction.BOTTOM;
import static com.powsybl.sld.model.coordinate.Direction.TOP;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class HorizontalZoneLayout extends AbstractZoneLayout {

    private final InfosNbSnakeLinesHorizontal infosNbSnakeLines;

    public HorizontalZoneLayout(ZoneGraph graph, SubstationLayoutFactory sLayoutFactory, VoltageLevelLayoutFactory vLayoutFactory) {
        super(graph, sLayoutFactory, vLayoutFactory);
        this.infosNbSnakeLines = InfosNbSnakeLinesHorizontal.create(graph);
    }

    @Override
    protected List<Point> calculatePolylineSnakeLine(LayoutParameters layoutParam, Node node1, Node node2,
                                                     boolean increment) {
        double yMin = getGraph().getVoltageLevels().stream().mapToDouble(VoltageLevelGraph::getY).min().orElse(0.0);
        double yMax = getGraph().getVoltageLevels().stream().mapToDouble(g -> g.getY() + g.getInnerHeight(layoutParam.getVerticalSpaceBus())).max().orElse(0.0);
        return calculatePolylineSnakeLineForHorizontalLayout(layoutParam, node1, node2, increment, infosNbSnakeLines, yMin, yMax);
    }

    /**
     * Calculate relative coordinate of substations in the zone
     */
    @Override
    protected void calculateCoordSubstations(LayoutParameters layoutParameters) {

        LayoutParameters.Padding diagramPadding = layoutParameters.getDiagramPadding();

        double x = diagramPadding.getLeft();
        double zoneHeight = 0;

        for (SubstationGraph subGraph : getGraph().getSubstations()) {

            // Calculate the objects coordinates inside the zone graph
            Layout sLayout = sLayoutFactory.create(subGraph, vLayoutFactory);
            sLayout.run(layoutParameters);

            x += subGraph.getWidth();
            zoneHeight = Math.max(zoneHeight, subGraph.getHeight());
        }

        double zoneWidth = x - diagramPadding.getLeft();
        getGraph().setSize(zoneWidth, zoneHeight);
    }

    @Override
    public void manageSnakeLines(LayoutParameters layoutParameters) {
        manageAllSnakeLines(layoutParameters);

        adaptPaddingToSnakeLines(layoutParameters);
    }

    private void adaptPaddingToSnakeLines(LayoutParameters layoutParameters) {
        double heightSnakeLinesTop = getHeightSnakeLines(layoutParameters, TOP, infosNbSnakeLines);

        LayoutParameters.Padding diagramPadding = layoutParameters.getDiagramPadding();
        LayoutParameters.Padding voltageLevelPadding = layoutParameters.getVoltageLevelPadding();

        double topPadding = heightSnakeLinesTop + diagramPadding.getTop() + voltageLevelPadding.getTop();
        double x = diagramPadding.getLeft();

        for (VoltageLevelGraph vlGraph : getGraph().getVoltageLevels()) {
            x += getWidthVerticalSnakeLines(vlGraph.getId(), layoutParameters, infosNbSnakeLines);
            vlGraph.setCoord(x + voltageLevelPadding.getLeft(), computeCoordY(layoutParameters, topPadding, vlGraph));
            x += vlGraph.getWidth();
        }

        double zoneWidth = x - diagramPadding.getLeft();
        double heightSnakeLinesBottom = getHeightSnakeLines(layoutParameters, BOTTOM, infosNbSnakeLines);
        double zoneHeight = getGraph().getHeight() + heightSnakeLinesTop + heightSnakeLinesBottom;

        getGraph().setSize(zoneWidth, zoneHeight);

        infosNbSnakeLines.reset();

        manageAllSnakeLines(layoutParameters);
    }

    double computeCoordY(LayoutParameters layoutParameters, double topPadding, VoltageLevelGraph vlGraph) {
        double y;
        // Find maximum voltage level top height
        double maxTopExternCellHeight = getGraph().getVoltageLevelStream().mapToDouble(g -> g.getExternCellHeight(Direction.TOP)).max().orElse(0.0);
        // Get gap between current voltage level and maximum height one
        double delta = maxTopExternCellHeight - vlGraph.getExternCellHeight(Direction.TOP);
        // Find maximum voltage level maxV
        double maxV = getGraph().getVoltageLevelStream().mapToDouble(VoltageLevelGraph::getMaxV).max().orElse(0.0);
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

    private void manageAllSnakeLines(LayoutParameters layoutParameters) {
        getGraph().getVoltageLevels().forEach(g -> manageSnakeLines(g, layoutParameters));
        getGraph().getSubstations().forEach(g -> manageSnakeLines(g, layoutParameters));
        manageSnakeLines(getGraph(), layoutParameters);
    }
}
