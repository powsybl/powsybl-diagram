/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.coordinate.*;
import com.powsybl.sld.model.graphs.*;
import com.powsybl.sld.model.nodes.*;
import org.jgrapht.alg.util.*;

import java.util.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class MatrixZoneLayout extends AbstractZoneLayout {

    private final String[][] matrix;

    protected MatrixZoneLayout(ZoneGraph graph, String[][] matrix, SubstationLayoutFactory sLayoutFactory, VoltageLevelLayoutFactory vLayoutFactory) {
        super(graph, sLayoutFactory, vLayoutFactory);
        this.matrix = matrix;
    }

    /**
     * Calculate relative coordinate of substations in the zone
     */
    @Override
    protected void calculateCoordSubstations(LayoutParameters layoutParameters) {
        // Height by rows
        List<Double> rowsHeight = new ArrayList<>();
        // Width by col
        List<Double> colsWidth = new ArrayList<>();

        for (String[] strings : matrix) {
            double rowHeight = 0;
            double colWidth = 0;
            for (String id : strings) {
                Optional<SubstationGraph> subGraph = getGraph().getSubstations().stream().filter(s -> s.getSubstationId().equals(id)).findFirst();
                if (subGraph.isPresent()) {
                    SubstationGraph graph = subGraph.get();
                    // Display substations
                    Layout sLayout = sLayoutFactory.create(graph, vLayoutFactory);
                    sLayout.run(layoutParameters);

                    rowHeight = Math.max(rowHeight, graph.getHeight());
                    colWidth = Math.max(colWidth, graph.getWidth());
                }
                colsWidth.add(colWidth);
            }
            rowsHeight.add(rowHeight);
        }

        double zoneWidth = 0.0;
        double zoneHeight = 0.0;
        double dy = 0.0;

        for (int row = 0; row < matrix.length; row++) {
            double maxColWidth = 0.0;
            for (int col = 0; col < matrix[row].length; col++) {
                String id = matrix[row][col];

                Optional<SubstationGraph> subGraph = getGraph().getSubstations().stream().filter(s -> s.getSubstationId().equals(id)).findFirst();
                if (subGraph.isPresent()) {
                    SubstationGraph graph = subGraph.get();
                    double dx = colsWidth.get(col);
                    move(graph, col * dx, row * dy);
                }
                maxColWidth += colsWidth.get(col);
            }
            dy = rowsHeight.get(row);
            zoneWidth = Math.max(maxColWidth, zoneWidth);
            zoneHeight += rowsHeight.get(row);
        }

        getGraph().setSize(zoneWidth, zoneHeight);
        maxVoltageLevelWidth = zoneWidth;
    }

    @Override
    protected List<Point> calculatePolylineSnakeLine(LayoutParameters layoutParam, Pair<Node, Node> nodes,
                                                     boolean increment) {
        // FIXME: need to be implemented
        Node node1 = nodes.getFirst();
        Node node2 = nodes.getSecond();
        List<Point> pol = new ArrayList<>();
        pol.add(getGraph().getShiftedPoint(node1));
        pol.add(getGraph().getShiftedPoint(node2));
        return pol;
    }

    @Override
    public void manageSnakeLines(LayoutParameters layoutParameters) {
        // Draw snakelines for each Substations
        getGraph().getSubstations().forEach(g -> manageSnakeLines(g, layoutParameters));
        // Draw snakelines between all Substations
        //manageSnakeLines(getGraph(), layoutParameters);
        /*
        // Change Voltagelevels coordinates in function of snakelines drawn
        adaptPaddingToSnakeLines(layoutParameters);
        // Redraw all snakelines
        getGraph().getSubstations().forEach(g -> manageSnakeLines(g, layoutParameters));
        manageSnakeLines(getGraph(), layoutParameters);
        */
    }
/*
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
 */
}
