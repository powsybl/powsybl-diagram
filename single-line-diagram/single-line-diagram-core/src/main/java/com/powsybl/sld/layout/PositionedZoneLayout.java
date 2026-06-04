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
 * substations that would overlap — including the snakeline margin — are nudged
 * in list order: earlier entries win and later ones are moved away.
 *
 * @author Frédéric Sabot
 */
public class PositionedZoneLayout extends AbstractStrictlyPositionedZoneLayout {

    /** Mutable bounding box used during overlap resolution. */
    private static class Rectangle {
        double x;
        double y;
        final double w;
        final double h;

        Rectangle(double x, double y, double w, double h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        /**
         * Returns true if this rectangle and {@code other} overlap, including the required margin gap.
         * Two rectangles are clear when there is at least {@code margin} space between them on both axes.
         */
        boolean overlaps(Rectangle other, int margin) {
            boolean xOverlap = x < other.x + other.w + margin && x + w + margin > other.x;
            boolean yOverlap = y < other.y + other.h + margin && y + h + margin > other.y;
            return xOverlap && yOverlap;
        }
    }

    /** Desired top-left (x, y) per substation, in priority order for overlap resolution. */
    private final List<Pair<String, Point>> desiredPositions;

    protected PositionedZoneLayout(ZoneGraph graph,
                                   List<Pair<String, Point>> desiredPositions,
                                   ZoneLayoutPathFinderFactory pathFinderFactory,
                                   SubstationLayoutFactory sLayoutFactory,
                                   VoltageLevelLayoutFactory vLayoutFactory) {
        super(graph, pathFinderFactory, sLayoutFactory, vLayoutFactory);
        this.desiredPositions = List.copyOf(Objects.requireNonNull(desiredPositions));
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
                    Rectangle a = rectangles.get(j); // winner (fixed)
                    Rectangle b = rectangles.get(i); // candidate (may move)
                    if (a.overlaps(b, margin)) {
                        moveToResolveOverlap(a, b, margin);
                        moved = true;
                    }
                }
            } while (moved);
        }
    }

    /**
     * Moves rectangle {@code b} in the direction that requires the smallest displacement
     * to clear rectangle {@code a}, including the required margin gap.
     */
    private void moveToResolveOverlap(Rectangle a, Rectangle b, int margin) {
        double moveRight = (a.x + a.w + margin) - b.x;
        double moveLeft = b.x + b.w + margin - a.x;
        double moveDown = (a.y + a.h + margin) - b.y;
        double moveUp = b.y + b.h + margin - a.y;

        double minDisplacement = Math.min(Math.min(moveRight, moveLeft), Math.min(moveDown, moveUp));

        if (minDisplacement == moveRight) {
            b.x += moveRight;
        } else if (minDisplacement == moveLeft) {
            b.x -= moveLeft;
        } else if (minDisplacement == moveDown) {
            b.y += moveDown;
        } else {
            b.y -= moveUp;
        }
    }
}
