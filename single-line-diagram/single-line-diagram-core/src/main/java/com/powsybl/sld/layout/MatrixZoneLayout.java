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
        double maxHeightRow = 0.0;
        // Width by col
        double maxWidthCol = 0.0;

        for (String[] strings : matrix) {
            for (String id : strings) {
                SubstationGraph graph = getGraph().getSubstationGraph(id);
                if (graph != null) {
                    // Display substations
                    layoutBySubstation.get(graph).run(layoutParameters);
                    maxHeightRow = Math.max(maxHeightRow, graph.getHeight());
                    maxWidthCol = Math.max(maxWidthCol, graph.getWidth());
                }
            }
        }

        LayoutParameters.Padding diagramPadding = layoutParameters.getDiagramPadding();
        double zoneWidth = 0.0;
        double zoneHeight = 0.0;

        for (int row = 0; row < matrix.length; row++) {
            double maxColWidth = 0.0;
            for (int col = 0; col < matrix[row].length; col++) {
                String id = matrix[row][col];
                SubstationGraph graph = getGraph().getSubstationGraph(id);
                if (graph != null) {
                    double dx = col * maxWidthCol + diagramPadding.getLeft();
                    double dy = row * maxHeightRow + diagramPadding.getTop();
                    move(graph, dx, dy);
                }
                maxColWidth += maxWidthCol;
            }
            zoneWidth = Math.max(maxColWidth, zoneWidth);
            zoneHeight += maxHeightRow;
        }

        getGraph().setSize(zoneWidth, zoneHeight);
        maxVoltageLevelWidth = zoneWidth;
    }

    @Override
    protected List<Point> calculatePolylineSnakeLine(LayoutParameters layoutParam, Pair<Node, Node> nodes,
                                                     boolean increment) {
        List<Point> polyline;
        Node node1 = nodes.getFirst();
        Node node2 = nodes.getSecond();
        VoltageLevelGraph vl1Graph = getGraph().getVoltageLevelGraph(node1);
        VoltageLevelGraph vl2Graph = getGraph().getVoltageLevelGraph(node2);
        SubstationGraph ss1Graph = getGraph().getSubstationGraph(node1).orElse(null);
        SubstationGraph ss2Graph = getGraph().getSubstationGraph(node2).orElse(null);
        if (vl1Graph == vl2Graph) { // in the same VoltageLevel
            throw new UnsupportedOperationException();
        } else if (ss1Graph != null && ss1Graph == ss2Graph) { // in the same Substation
            polyline = layoutBySubstation.get(ss1Graph).calculatePolylineSnakeLine(layoutParam, nodes, increment);
        } else { // in the same Zone
            // FIXME: need to be improved
            polyline = new ArrayList<>();
            //polyline.add(getGraph().getShiftedPoint(node1));
            //polyline.add(getGraph().getShiftedPoint(node2));
        }
        return polyline;
    }

    @Override
    public void manageSnakeLines(LayoutParameters layoutParameters) {
        // Draw snakelines between Substations
        // manageSnakeLines(getGraph(), layoutParameters);
        // Move each substation taking into account snakelines
        adaptPaddingToSnakeLines(layoutParameters);
    }

    private void adaptPaddingToSnakeLines(LayoutParameters layoutParameters) {
        // FIXME: need to be implemented

        // Re-draw snakelines between VoltageLevel for each Substations
        // getGraph().getSubstations().forEach(g -> manageSnakeLines(g, layoutParameters));
        // Re-draw snakelines between Substations
        // manageSnakeLines(getGraph(), layoutParameters);
    }
}
