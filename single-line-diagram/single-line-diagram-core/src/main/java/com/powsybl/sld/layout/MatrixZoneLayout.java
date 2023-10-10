/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.coordinate.*;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.graphs.*;
import com.powsybl.sld.model.nodes.Node;
import org.jgrapht.alg.util.*;

import java.util.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class MatrixZoneLayout extends AbstractZoneLayout {
    private final MatrixZoneLayoutModel model;
    private final String[][] matrix;

    protected MatrixZoneLayout(ZoneGraph graph, String[][] matrix, SubstationLayoutFactory sLayoutFactory, VoltageLevelLayoutFactory vLayoutFactory) {
        super(graph, sLayoutFactory, vLayoutFactory);
        this.model = new MatrixZoneLayoutModel(90);
        this.matrix = matrix;
    }

    /**
     * Calculate relative coordinate of substations in the zone
     */
    @Override
    protected void calculateCoordSubstations(LayoutParameters layoutParameters) {
        for (int row = 0; row < matrix.length; row++) {
            for (int col = 0; col < matrix[row].length; col++) {
                String id = matrix[row][col];
                SubstationGraph graph = getGraph().getSubstationGraph(id);
                if (graph != null) {
                    // Display substations
                    layoutBySubstation.get(graph).run(layoutParameters);
                }
                model.addGraph(graph, col, row);
            }
        }
        // Height by rows
        int maxHeightRow = model.getMatrixCellHeight();
        // Width by col
        int maxWidthCol = model.getMatrixCellWidth();
        // Snakeline hallway
        int hallway = model.getSnakelineHallwayWidth();
        // Padding
        LayoutParameters.Padding diagramPadding = layoutParameters.getDiagramPadding();
        // Zone size
        double zoneWidth = 0.0;
        double zoneHeight = diagramPadding.getTop() + hallway;
        // Move each substation into its matrix position
        for (int row = 0; row < matrix.length; row++) {
            zoneWidth = hallway;
            int hallwayRow = (row + 1) * hallway;
            for (int col = 0; col < matrix[row].length; col++) {
                String id = matrix[row][col];
                SubstationGraph graph = getGraph().getSubstationGraph(id);
                int hallwayCol = (col + 1) * hallway;
                if (graph != null) {
                    double dx = col * maxWidthCol + hallwayCol + diagramPadding.getLeft();
                    double dy = row * maxHeightRow + hallwayRow + diagramPadding.getTop();
                    move(graph, dx, dy);
                }
                zoneWidth += maxWidthCol + hallway;
            }
            zoneHeight += maxHeightRow + hallway;
        }

        getGraph().setSize(diagramPadding.getLeft() + zoneWidth + diagramPadding.getRight(), zoneHeight + diagramPadding.getBottom());
    }

    @Override
    protected List<Point> calculatePolylineSnakeLine(LayoutParameters layoutParam, Pair<Node, Node> nodes,
                                                     boolean increment) {
        List<Point> polyline = new ArrayList<>();
        Node node1 = nodes.getFirst();
        Node node2 = nodes.getSecond();
        VoltageLevelGraph vlGraph1 = getGraph().getVoltageLevelGraph(node1);
        VoltageLevelGraph vlGraph2 = getGraph().getVoltageLevelGraph(node2);
        SubstationGraph ss1Graph = getGraph().getSubstationGraph(node1).orElse(null);
        SubstationGraph ss2Graph = getGraph().getSubstationGraph(node2).orElse(null);
        if (ss1Graph != null && ss1Graph == ss2Graph) { // in the same Substation
            polyline = layoutBySubstation.get(ss1Graph).calculatePolylineSnakeLine(layoutParam, nodes, increment);
        } else if (ss1Graph != null && ss2Graph != null &&
                   model.gridContains(ss1Graph.getId()) && model.gridContains(ss2Graph.getId())) { // in the same Zone
            String ss1Id = ss1Graph.getId();
            String ss2Id = ss2Graph.getId();
            Point p1 = vlGraph1.getShiftedPoint(node1);
            Point p2 = vlGraph2.getShiftedPoint(node2);
            Direction dNode1 = getNodeDirection(node1, 1);
            Direction dNode2 = getNodeDirection(node2, 2);
            // Add starting point
            polyline.add(p1);
            // Find snakeline path
            polyline.addAll(model.buildSnakeline(ss1Id, p1, dNode1, ss2Id, p2, dNode2));
            // Add ending point
            polyline.add(p2);
        } else {
            // not found
        }
        return polyline;
    }

    @Override
    public void manageSnakeLines(LayoutParameters layoutParameters) {
        model.computePathFindingGrid(getGraph(), layoutParameters);

        // Draw snakelines between Substations
        manageSnakeLines(getGraph(), layoutParameters);

        // FIXME: use diagram padding
        getGraph().setSize(getGraph().getWidth() + 100, getGraph().getHeight() + 100);
    }
}
