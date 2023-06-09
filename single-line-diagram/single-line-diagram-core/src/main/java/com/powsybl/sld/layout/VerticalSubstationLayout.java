/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.graphs.*;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.model.coordinate.Direction;

import java.util.List;

import static com.powsybl.sld.model.coordinate.Direction.*;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class VerticalSubstationLayout extends AbstractSubstationLayout implements VerticalLayout {

    private final InfosNbSnakeLinesVertical infosNbSnakeLines;
    private double maxVoltageLevelWidth;

    protected VerticalSubstationLayout(SubstationGraph graph, VoltageLevelLayoutFactory vLayoutFactory, InfosNbSnakeLinesVertical infosNbSnakeLines) {
        super(graph, vLayoutFactory);
        this.infosNbSnakeLines = infosNbSnakeLines;
    }

    public VerticalSubstationLayout(SubstationGraph graph, VoltageLevelLayoutFactory vLayoutFactory) {
        this(graph, vLayoutFactory, InfosNbSnakeLinesVertical.create(graph));
    }

    /**
     * Calculate relative coordinate of voltageLevels in the substation
     */
    @Override
    protected void calculateCoordVoltageLevels(LayoutParameters layoutParameters) {

        LayoutParameters.Padding diagramPadding = layoutParameters.getDiagramPadding();
        LayoutParameters.Padding voltageLevelPadding = layoutParameters.getVoltageLevelPadding();

        double xVoltageLevels = diagramPadding.getLeft() + voltageLevelPadding.getLeft();
        double substationWidth = 0;
        double y = diagramPadding.getTop();

        for (VoltageLevelGraph vlGraph : getGraph().getVoltageLevels()) {
            // Calculate the objects coordinates inside the voltageLevel graph
            Layout vLayout = vLayoutFactory.create(vlGraph);
            vLayout.run(layoutParameters);

            vlGraph.setCoord(xVoltageLevels, y + voltageLevelPadding.getTop());

            substationWidth = Math.max(substationWidth, vlGraph.getWidth());
            y += vlGraph.getHeight();
        }

        double substationHeight = y - diagramPadding.getTop();
        getGraph().setSize(substationWidth, substationHeight);

        maxVoltageLevelWidth = substationWidth;
    }

    @Override
    public void manageSnakeLines(LayoutParameters layoutParameters) {
        getGraph().getVoltageLevels().forEach(g -> manageSnakeLines(g, layoutParameters));
        manageSnakeLines(getGraph(), layoutParameters);

        adaptPaddingToSnakeLines(layoutParameters);
    }

    private void adaptPaddingToSnakeLines(LayoutParameters layoutParameters) {
        double widthSnakeLinesLeft = Math.max(infosNbSnakeLines.getNbSnakeLinesLeftRight().get(Side.LEFT) - 1, 0) * layoutParameters.getHorizontalSnakeLinePadding();

        LayoutParameters.Padding diagramPadding = layoutParameters.getDiagramPadding();
        LayoutParameters.Padding voltageLevelPadding = layoutParameters.getVoltageLevelPadding();

        double xVoltageLevels = widthSnakeLinesLeft + diagramPadding.getLeft() + voltageLevelPadding.getLeft();
        double y = diagramPadding.getTop()
            + getGraph().getVoltageLevelStream().findFirst().map(vlg -> getHeightHorizontalSnakeLines(vlg.getId(), TOP, layoutParameters)).orElse(0.);

        for (VoltageLevelGraph vlGraph : getGraph().getVoltageLevels()) {
            vlGraph.setCoord(xVoltageLevels, y + voltageLevelPadding.getTop());
            y += vlGraph.getHeight() + getHeightHorizontalSnakeLines(vlGraph.getId(), BOTTOM, layoutParameters);
        }

        double widthSnakeLinesRight = Math.max(infosNbSnakeLines.getNbSnakeLinesLeftRight().get(Side.RIGHT) - 1, 0) * layoutParameters.getHorizontalSnakeLinePadding();
        double substationWidth = getGraph().getWidth() + widthSnakeLinesLeft + widthSnakeLinesRight;
        double substationHeight = y - diagramPadding.getTop();
        getGraph().setSize(substationWidth, substationHeight);

        infosNbSnakeLines.reset();
        getGraph().getVoltageLevels().forEach(g -> manageSnakeLines(g, layoutParameters));
        manageSnakeLines(getGraph(), layoutParameters);
    }

    private double getHeightHorizontalSnakeLines(String vlGraphId, Direction direction, LayoutParameters layoutParameters) {
        return Math.max(infosNbSnakeLines.getNbSnakeLinesHorizontalBetween(vlGraphId, direction) - 1, 0) * layoutParameters.getHorizontalSnakeLinePadding();
    }

    /*
     * Calculate polyline points of a snakeLine in the substation graph
     */
    @Override
    protected List<Point> calculatePolylineSnakeLine(LayoutParameters layoutParam, Node node1, Node node2,
                                                     boolean increment) {
        return VerticalLayoutUtil.calculatePolylineSnakeLine(this,
                getGraph(), infosNbSnakeLines,
                layoutParam, node1, node2,
                increment);
    }

    @Override
    public void addMiddlePoints(LayoutParameters layoutParam, Node node1, Node node2, boolean increment, List<Point> polyline) {
        VerticalLayoutUtil.addMiddlePoints(this,
                getGraph(),
                infosNbSnakeLines, maxVoltageLevelWidth,
                layoutParam, node1, node2, increment, polyline);
    }

    @Override
    public boolean facingNodes(Node node1, Node node2) {
        Direction dNode1 = getNodeDirection(getGraph(), node1, 1);
        Direction dNode2 = getNodeDirection(getGraph(), node2, 2);
        VoltageLevelGraph vlGraph1 = getGraph().getVoltageLevelGraph(node1);
        VoltageLevelGraph vlGraph2 = getGraph().getVoltageLevelGraph(node2);
        return dNode1 == BOTTOM && dNode2 == TOP && getGraph().graphAdjacents(vlGraph1, vlGraph2)
                || dNode1 == TOP && dNode2 == BOTTOM && getGraph().graphAdjacents(vlGraph2, vlGraph1);
    }
}
