/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout;

import com.powsybl.commons.*;
import com.powsybl.sld.layout.pathfinding.*;
import com.powsybl.sld.layout.zonebygrid.*;
import com.powsybl.sld.model.coordinate.*;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.graphs.*;
import com.powsybl.sld.model.nodes.Node;
import org.jgrapht.alg.util.*;

import java.util.*;

/**
 * @author Thomas Adam {@literal <tadam at neverhack.com>}
 */
public class MatrixZoneLayout extends AbstractZoneLayout {
    private MatrixZoneLayoutModel model;

    private final String[][] matrixUserDefinition;

    protected MatrixZoneLayout(ZoneGraph graph, String[][] matrixUserDefinition, ZoneLayoutPathFinderFactory pathFinderFactory, SubstationLayoutFactory sLayoutFactory, VoltageLevelLayoutFactory vLayoutFactory) {
        super(graph, sLayoutFactory, vLayoutFactory);
        this.pathFinder = Objects.requireNonNull(pathFinderFactory).create();
        this.matrixUserDefinition = Objects.requireNonNull(matrixUserDefinition);
    }

    /**
     * Calculate relative coordinate of substations in the zone
     */
    @Override
    protected void calculateCoordSubstations(LayoutParameters layoutParameters) {
        // Update model information
        this.model = new MatrixZoneLayoutModel(matrixUserDefinition, layoutParameters);
        for (int row = 0; row < matrixUserDefinition.length; row++) {
            for (int col = 0; col < matrixUserDefinition[row].length; col++) {
                String id = matrixUserDefinition[row][col];
                SubstationGraph sGraph = getGraph().getSubstationGraph(id);
                if (sGraph == null && !id.isEmpty()) {
                    throw new PowsyblException("Substation '" + id + "' was not found in zone graph '" + getGraph().getId() + "'");
                }
                model.addSubstationGraph(sGraph, row, col);
            }
        }

        Matrix matrix = model.getMatrix();
        // Display substations on not empty Matrix cell
        matrix.stream().filter(c -> !c.isEmpty()).map(MatrixCell::graph).forEach(graph -> layoutBySubstation.get(graph).run(layoutParameters));
        // Height by rows
        int maxHeightRow = 0;
        // Width by col
        int maxWidthCol = 0;
        // Snakeline hallway (horizontal & vertical)
        int snakelineMargin = layoutParameters.getZoneLayoutSnakeLinePadding();
        // Zone size
        int nbRows = matrix.rowCount();
        int nbCols = matrix.columnCount();
        LayoutParameters.Padding diagramPadding = layoutParameters.getDiagramPadding();
        // Move each substation into its matrix position
        for (int row = 0; row < nbRows; row++) {
            maxWidthCol = 0;
            for (int col = 0; col < nbCols; col++) {
                MatrixCell cell = matrix.get(row, col);
                BaseGraph graph = cell.graph();
                if (graph != null) {
                    // Compute delta in order to center substations into own matrix cell
                    int deltaX = (int) (matrix.getMatrixCellWidth(col) % graph.getWidth()) / 2;
                    int deltaY = (int) (matrix.getMatrixCellHeight(row) % graph.getHeight()) / 2;
                    double dx = maxWidthCol + (col + 1.0) * snakelineMargin;
                    double dy = maxHeightRow + (row + 1.0) * snakelineMargin;
                    move(graph, dx + deltaX, dy + deltaY);
                }
                maxWidthCol += matrix.getMatrixCellWidth(col);
            }
            maxHeightRow += matrix.getMatrixCellHeight(row);
        }
        double zoneWidth = maxWidthCol + (nbCols + 1.0) * snakelineMargin;
        double zoneHeight = maxHeightRow + (nbRows + 1.0) * snakelineMargin;
        getGraph().setSize(diagramPadding.getLeft() + zoneWidth + diagramPadding.getRight(),
                diagramPadding.getTop() + zoneHeight + diagramPadding.getBottom());
    }

    @Override
    protected List<Point> calculatePolylineSnakeLine(LayoutParameters layoutParameters, Pair<Node, Node> nodes,
                                                     boolean increment) {
        List<Point> polyline = new ArrayList<>();
        Node node1 = nodes.getFirst();
        Node node2 = nodes.getSecond();
        VoltageLevelGraph vlGraph1 = getGraph().getVoltageLevelGraph(node1);
        VoltageLevelGraph vlGraph2 = getGraph().getVoltageLevelGraph(node2);
        SubstationGraph ss1Graph = getGraph().getSubstationGraph(node1).orElse(null);
        SubstationGraph ss2Graph = getGraph().getSubstationGraph(node2).orElse(null);
        if (ss1Graph != null && ss2Graph != null &&
                model.contains(ss1Graph.getId()) && model.contains(ss2Graph.getId())) { // in the same Zone
            Point p1 = vlGraph1.getShiftedPoint(node1);
            Point p2 = vlGraph2.getShiftedPoint(node2);
            Direction dNode1 = getNodeDirection(node1, 1);
            Direction dNode2 = getNodeDirection(node2, 2);
            polyline = new ArrayList<>();
            // Add starting point
            polyline.add(p1);
            // Find snakeline path
            polyline.addAll(model.buildSnakeline(pathFinder, ss1Graph.getId(), p1, dNode1, ss2Graph.getId(), p2, dNode2, layoutParameters));
            // Add ending point
            polyline.add(p2);
        }
        return polyline;
    }

    @Override
    public void manageSnakeLines(LayoutParameters layoutParameters) {
        model.computePathFindingGrid(getGraph(), layoutParameters);

        // Draw snakelines between Substations
        manageSnakeLines(getGraph(), layoutParameters);
    }
}
