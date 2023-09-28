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
import org.jgrapht.alg.util.Pair;

import java.util.List;

import static com.powsybl.sld.model.coordinate.Direction.BOTTOM;
import static com.powsybl.sld.model.coordinate.Direction.TOP;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public class HorizontalZoneLayout extends AbstractZoneLayout {

    private final InfosNbSnakeLinesHorizontal infosNbSnakeLines;

    public HorizontalZoneLayout(ZoneGraph graph, SubstationLayoutFactory sLayoutFactory, VoltageLevelLayoutFactory vLayoutFactory) {
        super(graph, sLayoutFactory, vLayoutFactory);
        this.infosNbSnakeLines = InfosNbSnakeLinesHorizontal.create(graph);
    }

    @Override
    protected List<Point> calculatePolylineSnakeLine(LayoutParameters layoutParam, Pair<Node, Node> nodes,
                                                     boolean increment) {
        double yMin = getGraph().getVoltageLevels().stream().mapToDouble(VoltageLevelGraph::getY).min().orElse(0.0);
        double yMax = getGraph().getVoltageLevels().stream().mapToDouble(g -> g.getY() + g.getInnerHeight(layoutParam.getVerticalSpaceBus())).max().orElse(0.0);
        return calculatePolylineSnakeLineForHorizontalLayout(layoutParam, nodes, increment, infosNbSnakeLines, yMin, yMax);
    }

    /**
     * Calculate relative coordinate of substations in the zone
     */
    @Override
    protected void calculateCoordSubstations(LayoutParameters layoutParameters) {
        double zoneWidth = 0.0;
        double zoneHeight = 0.0;

        for (SubstationGraph subGraph : getGraph().getSubstations()) {
            layoutBySubstation.get(subGraph).run(layoutParameters);

            move(subGraph, zoneWidth, 0);

            zoneWidth += subGraph.getWidth();
            zoneHeight = Math.max(zoneHeight, subGraph.getHeight());
        }
        getGraph().setSize(zoneWidth, zoneHeight);
    }

    @Override
    public void manageSnakeLines(LayoutParameters layoutParameters) {
        // Draw snakelines for each Substations
        getGraph().getSubstations().forEach(g -> manageSnakeLines(g, layoutParameters));
        // Draw snakelines between all Substations
        manageSnakeLines(getGraph(), layoutParameters);
        // Change Voltagelevels coordinates in function of snakelines drawn
        adaptPaddingToSnakeLines(layoutParameters);
        // Redraw all snakelines
        getGraph().getSubstations().forEach(g -> manageSnakeLines(g, layoutParameters));
        manageSnakeLines(getGraph(), layoutParameters);
    }

    private void adaptPaddingToSnakeLines(LayoutParameters layoutParameters) {
        double heightSnakeLinesTop = AbstractLayout.getHeightSnakeLines(layoutParameters, TOP, infosNbSnakeLines);
        for (SubstationGraph subGraph : getGraph().getSubstations()) {
            move(subGraph, 0, heightSnakeLinesTop);
        }
        double heightSnakeLinesBottom = AbstractLayout.getHeightSnakeLines(layoutParameters, BOTTOM, infosNbSnakeLines);
        double zoneHeight = getGraph().getHeight() + heightSnakeLinesTop + heightSnakeLinesBottom;
        getGraph().setSize(getGraph().getWidth(), zoneHeight);

        infosNbSnakeLines.reset();
    }
}
