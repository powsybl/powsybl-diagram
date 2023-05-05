/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.graphs.ZoneGraph;
import com.powsybl.sld.model.nodes.Node;

import java.util.List;

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
        HorizontalLayout.adaptPaddingToSnakeLines(getGraph(), layoutParameters, infosNbSnakeLines);

        manageAllSnakeLines(layoutParameters);
    }
}
