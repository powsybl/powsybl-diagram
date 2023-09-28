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
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.graphs.ZoneGraph;
import com.powsybl.sld.model.nodes.Node;
import org.jgrapht.alg.util.Pair;

import java.util.List;

import static com.powsybl.sld.model.coordinate.Direction.BOTTOM;
import static com.powsybl.sld.model.coordinate.Direction.TOP;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class VerticalZoneLayout extends AbstractZoneLayout {

    private final InfosNbSnakeLinesVertical infosNbSnakeLines;

    protected VerticalZoneLayout(ZoneGraph graph, SubstationLayoutFactory sLayoutFactory, VoltageLevelLayoutFactory vLayoutFactory, InfosNbSnakeLinesVertical infosNbSnakeLines) {
        super(graph, sLayoutFactory, vLayoutFactory);
        this.infosNbSnakeLines = infosNbSnakeLines;
    }

    public VerticalZoneLayout(ZoneGraph graph, SubstationLayoutFactory sLayoutFactory, VoltageLevelLayoutFactory vLayoutFactory) {
        this(graph, sLayoutFactory, vLayoutFactory, InfosNbSnakeLinesVertical.create(graph));
    }

    @Override
    protected List<Point> calculatePolylineSnakeLine(LayoutParameters layoutParam, Pair<Node, Node> nodes,
                                                     boolean increment) {
        return calculatePolylineSnakeLineForVerticalLayout(layoutParam, nodes, increment, infosNbSnakeLines, facingNodes(nodes));
    }

    private boolean facingNodes(Pair<Node, Node> nodes) {
        Node node1 = nodes.getFirst();
        Node node2 = nodes.getSecond();
        Direction dNode1 = getNodeDirection(node1, 1);
        Direction dNode2 = getNodeDirection(node2, 2);
        VoltageLevelGraph vlGraph1 = getGraph().getVoltageLevelGraph(node1);
        VoltageLevelGraph vlGraph2 = getGraph().getVoltageLevelGraph(node2);
        boolean isVl1Vl2Adjacent = false;
        boolean isVl2Vl1Adjacent = false;
        for (SubstationGraph substation : getGraph().getSubstations()) {
            isVl1Vl2Adjacent |= substation.graphAdjacents(vlGraph1, vlGraph2);
            isVl2Vl1Adjacent |= substation.graphAdjacents(vlGraph2, vlGraph1);
        }
        return dNode1 == Direction.BOTTOM && dNode2 == Direction.TOP && isVl1Vl2Adjacent
                || dNode1 == Direction.TOP && dNode2 == Direction.BOTTOM && isVl2Vl1Adjacent;
    }

    /**
     * Calculate relative coordinate of substations in the zone
     */
    @Override
    protected void calculateCoordSubstations(LayoutParameters layoutParameters) {
        double zoneWidth = 0;
        double zoneHeight = 0;
        LayoutParameters.Padding diagramPadding = layoutParameters.getDiagramPadding();

        for (SubstationGraph subGraph : getGraph().getSubstations()) {
            // Calculate the objects coordinates inside the substation graph
            Layout sLayout = sLayoutFactory.create(subGraph, vLayoutFactory);
            sLayout.run(layoutParameters);

            move(subGraph, 0, zoneHeight);

            zoneHeight += subGraph.getHeight() - diagramPadding.getTop();
            zoneWidth = Math.max(zoneWidth, subGraph.getWidth());
        }
        getGraph().setSize(zoneWidth, zoneHeight);
        maxVoltageLevelWidth = zoneWidth;
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
        double widthSnakeLinesLeft = Math.max(infosNbSnakeLines.getNbSnakeLinesLeftRight().get(Side.LEFT) - 1, 0) * layoutParameters.getHorizontalSnakeLinePadding();

        LayoutParameters.Padding diagramPadding = layoutParameters.getDiagramPadding();
        LayoutParameters.Padding voltageLevelPadding = layoutParameters.getVoltageLevelPadding();

        double y = diagramPadding.getTop()
                + getGraph().getVoltageLevelStream().findFirst().map(vlg -> getHeightHorizontalSnakeLines(vlg.getId(), TOP, layoutParameters)).orElse(0.);

        for (SubstationGraph subGraph : getGraph().getSubstations()) {
            move(subGraph, widthSnakeLinesLeft, y + voltageLevelPadding.getTop());
        }

        for (VoltageLevelGraph vlGraph : getGraph().getVoltageLevels()) {
            y += vlGraph.getHeight() + getHeightHorizontalSnakeLines(vlGraph.getId(), BOTTOM, layoutParameters);
        }

        double widthSnakeLinesRight = Math.max(infosNbSnakeLines.getNbSnakeLinesLeftRight().get(Side.RIGHT) - 1, 0) * layoutParameters.getHorizontalSnakeLinePadding();
        double substationWidth = getGraph().getWidth() + widthSnakeLinesLeft + widthSnakeLinesRight;
        double substationHeight = y - diagramPadding.getTop();
        getGraph().setSize(substationWidth, substationHeight);

        infosNbSnakeLines.reset();
    }

    private double getHeightHorizontalSnakeLines(String vlGraphId, Direction direction, LayoutParameters layoutParameters) {
        return Math.max(infosNbSnakeLines.getNbSnakeLinesHorizontalBetween(vlGraphId, direction) - 1, 0) * layoutParameters.getHorizontalSnakeLinePadding();
    }
}
