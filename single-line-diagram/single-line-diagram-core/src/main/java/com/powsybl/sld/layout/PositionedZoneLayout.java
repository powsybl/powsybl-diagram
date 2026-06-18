/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.layout.pathfinding.ZoneLayoutPathFinderFactory;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.graphs.ZoneGraph;
import org.jgrapht.alg.util.Pair;

import java.util.*;

/**
 * Zone layout that places substations at desired top-left coordinates (x, y).
 * <p>
 * Each substation is assigned a desired top-left position via a {@code List<Pair<String, Point>>}.
 * After running the sub-layouts (which determine each substation's pixel size),
 * substations that would overlap — including the snakeline margin — are moved to the right/bottom.
 * Overlap resolution is done iteratively, keeping the first entries at their desired location and
 * moving the later ones in case of overlap.
 *
 * @author Frédéric Sabot {@literal <frederic.sabot at haulogy.net>}
 */
public class PositionedZoneLayout extends AbstractManuallyPositionedZoneLayout {

    /** Mutable bounding box used during overlap resolution. */
    private static class Rectangle {
        double x;
        double y;
        final double width;
        final double height;

        Rectangle(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        /**
         * Returns true if this rectangle and {@code other} overlap, including the required margin gap.
         * Two rectangles are clear when there is at least {@code margin} space between them on both axes.
         */
        boolean overlaps(Rectangle other, int margin) {
            boolean xOverlap = x < other.x + other.width + margin && x + width + margin > other.x;
            boolean yOverlap = y < other.y + other.height + margin && y + height + margin > other.y;
            return xOverlap && yOverlap;
        }
    }

    /** Desired top-left (x, y) per substation, in priority order for overlap resolution. */
    private final List<Pair<String, Point>> desiredPositions;

    /**
     * @param graph the zone graph to lay out
     * @param desiredPositions desired top-left position for each substation. In case of overlap of two substations,
     *                         the overlap resolution will move the substation with the position of higher index.
     */
    public PositionedZoneLayout(ZoneGraph graph,
                                   List<Pair<String, Point>> desiredPositions,
                                   ZoneLayoutPathFinderFactory pathFinderFactory,
                                   SubstationLayoutFactory sLayoutFactory,
                                   VoltageLevelLayoutFactory vLayoutFactory) {
        super(graph, pathFinderFactory, sLayoutFactory, vLayoutFactory);
        this.desiredPositions = Objects.requireNonNull(desiredPositions);
    }

    @Override
    protected List<Pair<String, Point>> computeSubstationPositions(LayoutParameters layoutParameters) {
        int snakeLinePadding = layoutParameters.getZoneLayoutSnakeLinePadding();

        List<SubstationGraph> substationGraphs = new ArrayList<>(desiredPositions.size());
        for (Pair<String, Point> entry : desiredPositions) {
            String id = entry.getFirst();
            SubstationGraph sGraph = getGraph().getSubstationGraph(id);
            if (sGraph == null) {
                throw new PowsyblException("Substation '" + id + "' was not found in zone graph '" + getGraph().getId() + "'");
            }
            layoutBySubstation.get(sGraph).run(layoutParameters);
            substationGraphs.add(sGraph);
        }

        // Build bounding boxes from desired top-left positions
        List<Rectangle> rectangles = new ArrayList<>(substationGraphs.size());
        for (int i = 0; i < substationGraphs.size(); i++) {
            Point topLeft = desiredPositions.get(i).getSecond();
            SubstationGraph sg = substationGraphs.get(i);
            rectangles.add(new Rectangle(topLeft.getX(), topLeft.getY(), sg.getWidth(), sg.getHeight()));
        }

        // Resolve overlaps (greedy: insertion order wins)
        resolveOverlaps(rectangles, snakeLinePadding);

        // Shift all rectangles so the top-left of the bounding box starts at (snakeLinePadding, snakeLinePadding)
        // This disregards the absolute values in desiredPositions and only keeps relative positions
        double minX = rectangles.stream().mapToDouble(r -> r.x).min().orElse(0);
        double minY = rectangles.stream().mapToDouble(r -> r.y).min().orElse(0);
        for (Rectangle r : rectangles) {
            r.x += snakeLinePadding - minX;
            r.y += snakeLinePadding - minY;
        }

        List<Pair<String, Point>> positions = new ArrayList<>();
        for (int i = 0; i < substationGraphs.size(); i++) {
            Rectangle r = rectangles.get(i);
            positions.add(Pair.of(substationGraphs.get(i).getId(), new Point(r.x, r.y)));
        }
        return positions;
    }

    /**
     * Greedy overlap resolution in insertion order.
     * For each substation (starting from the second), check all already-placed
     * substations. If an overlap is found, move the current one just outside the
     * blocking one — choosing the direction that requires the smallest displacement.
     * Repeat until no overlaps remain with any earlier substation.
     */
    private void resolveOverlaps(List<Rectangle> rectangles, int margin) {
        for (int i = 1; i < rectangles.size(); i++) {
            boolean moved;
            do {
                moved = false;
                for (int j = 0; j < i; j++) {
                    Rectangle fixedRectangle = rectangles.get(j);
                    Rectangle movingRectangle = rectangles.get(i);
                    if (fixedRectangle.overlaps(movingRectangle, margin)) {
                        moveToResolveOverlap(fixedRectangle, movingRectangle, margin);
                        moved = true;
                    }
                }
            } while (moved);
        }
    }

    /**
     * Moves rectangle {@code movingRectangle} in the direction that requires the smallest displacement
     * to clear rectangle {@code fixedRectangle}, including the required margin gap.
     * Only move to the right/down to avoid infinite loops in overlap resolution pushing a given rectangle left and right.
     */
    private void moveToResolveOverlap(Rectangle fixedRectangle, Rectangle movingRectangle, int margin) {
        double moveRight = (fixedRectangle.x + fixedRectangle.width + margin) - movingRectangle.x;
        double moveDown = (fixedRectangle.y + fixedRectangle.height + margin) - movingRectangle.y;

        if (moveRight <= moveDown) {
            movingRectangle.x += moveRight;
        } else {
            movingRectangle.y += moveDown;
        }
    }
}
