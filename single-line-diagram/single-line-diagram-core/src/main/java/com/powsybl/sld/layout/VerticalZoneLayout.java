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
public class VerticalZoneLayout extends AbstractZoneLayout implements VerticalLayout {

    private final InfosNbSnakeLinesVertical infosNbSnakeLines;
    private double maxVoltageLevelWidth;

    protected VerticalZoneLayout(ZoneGraph graph, SubstationLayoutFactory sLayoutFactory, VoltageLevelLayoutFactory vLayoutFactory, InfosNbSnakeLinesVertical infosNbSnakeLines) {
        super(graph, sLayoutFactory, vLayoutFactory);
        this.infosNbSnakeLines = infosNbSnakeLines;
    }

    public VerticalZoneLayout(ZoneGraph graph, SubstationLayoutFactory sLayoutFactory, VoltageLevelLayoutFactory vLayoutFactory) {
        this(graph, sLayoutFactory, vLayoutFactory, InfosNbSnakeLinesVertical.create(graph));
    }

    /**
     * Calculate relative coordinate of substations in the zone
     */
    @Override
    protected void calculateCoordSubstations(LayoutParameters layoutParameters) {
        LayoutParameters.Padding diagramPadding = layoutParameters.getDiagramPadding();

        double zoneWidth = 0;
        double y = 0;

        for (SubstationGraph subGraph : getGraph().getSubstations()) {
            // Calculate the objects coordinates inside the substation graph
            Layout vLayout = sLayoutFactory.create(subGraph, vLayoutFactory);
            vLayout.run(layoutParameters);

            zoneWidth = Math.max(zoneWidth, subGraph.getWidth());
            y += subGraph.getHeight();
        }

        double zoneHeight = y - diagramPadding.getTop();
        getGraph().setSize(zoneWidth, zoneHeight);

        maxVoltageLevelWidth = zoneWidth;
    }

    @Override
    public void manageSnakeLines(LayoutParameters layoutParameters) {
        manageAllSnakeLines(layoutParameters);

        adaptPaddingToSnakeLines(layoutParameters);
    }

    private void adaptPaddingToSnakeLines(LayoutParameters layoutParameters) {
        adaptPaddingToSnakeLines(getGraph(), layoutParameters);
        manageAllSnakeLines(layoutParameters);
    }

    /*
     * Calculate polyline points of a snakeLine in the substation graph
     */
    @Override
    protected List<Point> calculatePolylineSnakeLine(LayoutParameters layoutParam, Node node1, Node node2,
                                                     boolean increment) {
        return calculatePolylineSnakeLine(getGraph(),
                layoutParam, node1, node2,
                increment);
    }

    @Override
    public boolean facingNodes(Node node1, Node node2) {
        Direction dNode1 = getNodeDirection(getGraph(), node1, 1);
        Direction dNode2 = getNodeDirection(getGraph(), node2, 2);
        VoltageLevelGraph vlGraph1 = getGraph().getVoltageLevelGraph(node1);
        VoltageLevelGraph vlGraph2 = getGraph().getVoltageLevelGraph(node2);
        boolean isVl1Vl2Adjacent = false;
        boolean isVl2Vl1Adjacent = false;
        for (SubstationGraph substation : getGraph().getSubstations()) {
            isVl1Vl2Adjacent |= substation.graphAdjacents(vlGraph1, vlGraph2);
            isVl2Vl1Adjacent |= substation.graphAdjacents(vlGraph2, vlGraph1);
        }
        return (dNode1 == BOTTOM && dNode2 == TOP && isVl1Vl2Adjacent)
                || (dNode1 == TOP && dNode2 == BOTTOM && isVl2Vl1Adjacent);
    }

    public InfosNbSnakeLinesVertical getInfosNbSnakeLines() {
        return infosNbSnakeLines;
    }

    public double getMaxVoltageLevelWidth() {
        return maxVoltageLevelWidth;
    }
}
