/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.layout.pathfinding.Grid;
import com.powsybl.sld.layout.pathfinding.ZoneLayoutPathFinderFactory;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.graphs.ZoneGraph;
import com.powsybl.sld.model.nodes.BranchEdge;
import com.powsybl.sld.model.nodes.Node;
import org.jgrapht.alg.util.Pair;

import java.util.*;

/**
 * Zone layout that places substations at desired center coordinates (x, y).
 * <p>
 * Each substation is assigned a desired center via a {@code List<Pair<String, Point>>}.
 * After running the sub-layouts (which determine each substation's pixel size),
 * substations that would overlap — including the snakeline margin — are nudged
 * in list order: earlier entries win and later ones are moved away.
 *
 * @author Frédéric Sabot
 */
public class PositionedZoneLayout extends AbstractZoneLayout {

    /** Desired center (x, y) per substation, in priority order for overlap resolution. */
    private final List<Pair<String, Point>> desiredCenters;

    /** Path-finding grid built in manageSnakeLines, used in calculatePolylineSnakeLine. */
    private Grid pathFinderGrid;

    protected PositionedZoneLayout(ZoneGraph graph,
                                   List<Pair<String, Point>> desiredCenters,
                                   ZoneLayoutPathFinderFactory pathFinderFactory,
                                   SubstationLayoutFactory sLayoutFactory,
                                   VoltageLevelLayoutFactory vLayoutFactory) {
        super(graph, sLayoutFactory, vLayoutFactory);
        this.pathFinder = Objects.requireNonNull(pathFinderFactory).create();
        this.desiredCenters = List.copyOf(Objects.requireNonNull(desiredCenters));
    }

    @Override
    protected void calculateCoordSubstations(LayoutParameters layoutParameters) {
        int snakeLinePadding = layoutParameters.getZoneLayoutSnakeLinePadding();
        LayoutParameters.Padding diagramPadding = layoutParameters.getDiagramPadding();

        List<SubstationGraph> substationGraphs = new ArrayList<>(desiredCenters.size());
        for (Pair<String, Point> entry : desiredCenters) {
            String id = entry.getFirst();
            SubstationGraph sGraph = getGraph().getSubstationGraph(id);
            if (sGraph == null) {
                throw new PowsyblException("Substation '" + id + "' was not found in zone graph '" + getGraph().getId() + "'");
            }
            layoutBySubstation.get(sGraph).run(layoutParameters);
            substationGraphs.add(sGraph);
        }

        // Convert desired centers to desired top-left corners
        List<double[]> boxes = new ArrayList<>(substationGraphs.size());  // [x, y, w, h]
        for (int i = 0; i < substationGraphs.size(); i++) {
            Point center = desiredCenters.get(i).getSecond();
            SubstationGraph sg = substationGraphs.get(i);
            double w = sg.getWidth();
            double h = sg.getHeight();
            boxes.add(new double[]{center.getX() - w / 2.0, center.getY() - h / 2.0, w, h});
        }

        // Resolve overlaps (greedy: insertion order wins)
        resolveOverlaps(boxes, snakeLinePadding);

        // Shift all boxes so the top-left of the bounding box starts at (padding.left(), padding.top())
        // This disregard the absolute values in desiredCenters and only keeps relative positions
        double minX = boxes.stream().mapToDouble(b -> b[0]).min().orElse(0);
        double minY = boxes.stream().mapToDouble(b -> b[1]).min().orElse(0);
        for (double[] box : boxes) {
            box[0] += diagramPadding.left() + snakeLinePadding - minX;
            box[1] += diagramPadding.top() + snakeLinePadding - minY;
        }

        // Apply positions via (relative) move()
        for (int i = 0; i < substationGraphs.size(); i++) {
            SubstationGraph sg = substationGraphs.get(i);
            double[] box = boxes.get(i);
            move(sg, box[0], box[1]);
        }

        // Compute zone size from bounding box
        double zoneWidth = boxes.stream().mapToDouble(b -> b[0] + b[2]).max().orElse(0);
        double zoneHeight = boxes.stream().mapToDouble(b -> b[1] + b[3]).max().orElse(0);
        getGraph().setSize(
            diagramPadding.left() + zoneWidth + diagramPadding.right() + snakeLinePadding,
            diagramPadding.top() + zoneHeight + diagramPadding.bottom() + snakeLinePadding
        );
    }

    @Override
    public void manageSnakeLines(LayoutParameters layoutParameters) {
        computePathFindingGrid(layoutParameters);
        manageSnakeLines(getGraph(), layoutParameters);
    }

    @Override
    protected List<Point> calculatePolylineSnakeLine(LayoutParameters layoutParameters, Pair<Node, Node> nodes,
                                                     boolean increment) {
        Node node1 = nodes.getFirst();
        Node node2 = nodes.getSecond();
        VoltageLevelGraph vlGraph1 = getGraph().getVoltageLevelGraph(node1);
        VoltageLevelGraph vlGraph2 = getGraph().getVoltageLevelGraph(node2);
        SubstationGraph ss1Graph = getGraph().getSubstationGraph(node1).orElse(null);
        SubstationGraph ss2Graph = getGraph().getSubstationGraph(node2).orElse(null);

        if (ss1Graph == null || ss2Graph == null
                || !containsSubstation(ss1Graph.getId())
                || !containsSubstation(ss2Graph.getId())) {
            return new ArrayList<>();
        }

        Point p1 = vlGraph1.getShiftedPoint(node1);
        Point p2 = vlGraph2.getShiftedPoint(node2);
        Direction dNode1 = getNodeDirection(node1, 1);
        Direction dNode2 = getNodeDirection(node2, 2);

        // Open exit corridors from each node into the margin hallway, then find path
        insertFreePathInSubstation(ss1Graph, p1, dNode1, layoutParameters);
        insertFreePathInSubstation(ss2Graph, p2, dNode2, layoutParameters);

        List<Point> polyline = new ArrayList<>();
        polyline.add(p1);
        polyline.addAll(pathFinder.findShortestPath(pathFinderGrid, p1, p2));
        polyline.add(p2);
        return polyline;
    }

    // -------------------------------------------------------------------------
    // Path-finding grid
    // -------------------------------------------------------------------------

    private void computePathFindingGrid(LayoutParameters layoutParameters) {
        int width  = (int) getGraph().getWidth();
        int height = (int) getGraph().getHeight();
        pathFinderGrid = new Grid(width, height);

        int margin = layoutParameters.getZoneLayoutSnakeLinePadding();
        LayoutParameters.Padding diagramPadding = layoutParameters.getDiagramPadding();

        // Mark the margin bands around every substation as available (hallways),
        // then mark substation interiors as unavailable.
        for (SubstationGraph sg : getGraph().getSubstations()) {
            Point origin = getSubstationOrigin(sg);
            int ssX = (int) origin.getX();
            int ssY = (int) origin.getY();
            int ssW = (int) sg.getWidth();
            int ssH = (int) sg.getHeight();

            // Horizontal hallway above and below this substation
            int hStep = (int) layoutParameters.getHorizontalSnakeLinePadding();
            int zoneW  = (int)(width  - diagramPadding.left() - diagramPadding.right());
            int startX = (int) diagramPadding.left();
            for (int x = startX; x < startX + zoneW; x++) {
                for (int y = ssY - margin; y < ssY; y += Math.max(1, hStep)) {
                    pathFinderGrid.setAvailability(x, y, true);
                }
                for (int y = ssY + ssH; y <= ssY + ssH + margin; y += Math.max(1, hStep)) {
                    pathFinderGrid.setAvailability(x, y, true);
                }
            }

            // Vertical hallway left and right of this substation
            int vStep = (int) layoutParameters.getVerticalSnakeLinePadding();
            int zoneH  = (int)(height - diagramPadding.top() - diagramPadding.bottom());
            int startY = (int) diagramPadding.top();
            for (int y = startY; y < startY + zoneH; y++) {
                for (int x = ssX - margin; x < ssX; x += Math.max(1, vStep)) {
                    pathFinderGrid.setAvailability(x, y, true);
                }
                for (int x = ssX + ssW; x <= ssX + ssW + margin; x += Math.max(1, vStep)) {
                    pathFinderGrid.setAvailability(x, y, true);
                }
            }
        }

        // Mark substation interiors (voltage levels + existing edges) unavailable
        for (SubstationGraph sg : getGraph().getSubstations()) {
            sg.getVoltageLevelStream().forEach(vlGraph -> {
                LayoutParameters.Padding vlPadding = layoutParameters.getVoltageLevelPadding();
                double elementaryWidth = layoutParameters.getCellWidth() / 2;
                double wNoPad = vlGraph.getMaxH() * elementaryWidth;
                double hNoPad = vlGraph.getInnerHeight(layoutParameters.getVerticalSpaceBus());
                int xVl = (int) vlGraph.getX();
                int yVl = (int) vlGraph.getY();
                for (int x = xVl - ((int) vlPadding.left() - 1); x < xVl + wNoPad + (int) vlPadding.right(); x++) {
                    for (int y = yVl - ((int) vlPadding.top() - 1); y < yVl + hNoPad + (int) vlPadding.bottom(); y++) {
                        pathFinderGrid.setAvailability(x, y, false);
                    }
                }
            });
            sg.getMultiTermNodes().forEach(node -> {
                pathFinderGrid.setAvailability(node.getCoordinates(), false);
                node.getAdjacentEdges().forEach(edge -> {
                    if (edge instanceof BranchEdge branch) {
                        Grid.getPointsAlongSnakeline(branch.getSnakeLine())
                            .forEach(p -> pathFinderGrid.setAvailability(p, false));
                    }
                });
            });
            sg.getLineEdges().forEach(e ->
                Grid.getPointsAlongSnakeline(e.getSnakeLine())
                    .forEach(p -> pathFinderGrid.setAvailability(p, false)));
        }
    }

    /**
     * Opens a vertical exit corridor from node {@code p} into the margin hallway
     * above or below the substation, then a horizontal strip across the full
     * substation width — exactly as MatrixZoneLayoutModel does per cell.
     */
    private void insertFreePathInSubstation(SubstationGraph sg, Point p, Direction d,
                                            LayoutParameters layoutParameters) {
        LayoutParameters.Padding vlPadding = layoutParameters.getVoltageLevelPadding();
        int margin = layoutParameters.getZoneLayoutSnakeLinePadding();

        double x1   = p.getX();
        double y1   = p.getY();
        double minY = d == Direction.BOTTOM ? y1 : y1 - vlPadding.top();
        double maxY = d == Direction.BOTTOM ? y1 + vlPadding.bottom() : y1;

        // Vertical corridor out of the voltage level
        for (int y = (int) minY; y <= (int) maxY; y++) {
            pathFinderGrid.setAvailability(x1, y, true);
        }

        // Horizontal strip across the substation + margin so paths can route laterally
        int ssX = (int) getSubstationOrigin(sg).getX();
        int ssW = (int) sg.getWidth();
        double exitY = d == Direction.TOP ? minY : maxY;
        for (int x = ssX - margin; x < ssX + ssW + margin; x++) {
            pathFinderGrid.setAvailability(x, exitY, true);
        }
    }

    /** Returns the top-left corner of a substation as the minimum VL coordinate. */
    private Point getSubstationOrigin(SubstationGraph sg) {
        double x = sg.getVoltageLevels().stream().mapToDouble(VoltageLevelGraph::getX).min().orElse(0);
        double y = sg.getVoltageLevels().stream().mapToDouble(VoltageLevelGraph::getY).min().orElse(0);
        return new Point(x, y);
    }

    private boolean containsSubstation(String id) {
        return desiredCenters.stream().anyMatch(p -> p.getFirst().equals(id));
    }

    /**
     * Greedy overlap resolution in insertion order.
     * For each substation (starting from the second), check all already-placed
     * substations. If an overlap is found, move the current one just outside the
     * blocking one — choosing the direction that requires the smallest displacement.
     * Repeat until no overlaps remain with any earlier substation.
     */
    private void resolveOverlaps(List<double[]> boxes, int margin) {
        for (int i = 1; i < boxes.size(); i++) {
            boolean moved;
            do {
                moved = false;
                for (int j = 0; j < i; j++) {
                    double[] a = boxes.get(j); // winner (fixed)
                    double[] b = boxes.get(i); // candidate (may move)
                    if (overlaps(a, b, margin)) {
                        nudge(a, b, margin);
                        moved = true;
                    }
                }
            } while (moved);
        }
    }

    private boolean overlaps(double[] a, double[] b, int margin) {
        boolean xOverlap = a[0] < b[0] + b[2] + margin && a[0] + a[2] + margin > b[0];
        boolean yOverlap = a[1] < b[1] + b[3] + margin && a[1] + a[3] + margin > b[1];
        return xOverlap && yOverlap;
    }

    private void nudge(double[] a, double[] b, int margin) {
        double moveRight = (a[0] + a[2] + margin) - b[0];
        double moveLeft  = b[0] + b[2] + margin - a[0];
        double moveDown  = (a[1] + a[3] + margin) - b[1];
        double moveUp    = b[1] + b[3] + margin - a[1];

        double minDisplacement = Math.min(Math.min(moveRight, moveLeft), Math.min(moveDown, moveUp));

        if (minDisplacement == moveRight) {
            b[0] += moveRight;
        } else if (minDisplacement == moveLeft) {
            b[0] -= moveLeft;
        } else if (minDisplacement == moveDown) {
            b[1] += moveDown;
        } else {
            b[1] -= moveUp;
        }
    }
}
