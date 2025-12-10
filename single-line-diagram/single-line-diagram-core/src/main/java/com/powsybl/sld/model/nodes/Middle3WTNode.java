/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.nodes;

import com.powsybl.sld.model.coordinate.Orientation;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.graphs.VoltageLevelInfos;

import java.util.*;
import java.util.function.BooleanSupplier;

/**
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
public class Middle3WTNode extends MiddleTwtNode {
    private final Map<Winding, NodeSide> windingMap = new EnumMap<>(Winding.class);
    private final boolean embeddedInVlGraph;

    public Middle3WTNode(String id, String name, VoltageLevelInfos voltageLevelInfosLeg1, VoltageLevelInfos voltageLevelInfosLeg2, VoltageLevelInfos voltageLevelInfosLeg3, String componentType, boolean embeddedInVLGraph) {
        super(id, name,
            new VoltageLevelInfos[]{Objects.requireNonNull(voltageLevelInfosLeg1), Objects.requireNonNull(voltageLevelInfosLeg2), Objects.requireNonNull(voltageLevelInfosLeg3)},
            componentType);
        this.embeddedInVlGraph = embeddedInVLGraph;
    }

    public boolean isEmbeddedInVlGraph() {
        return embeddedInVlGraph;
    }

    /**
     * Deduce the node orientation based on the lines coordinates supporting the svg component.
     * As we are dealing with straight lines, we always have two out of three snake lines which are in line, the third
     * one being perpendicular.
     */
    public void handle3wtNodeOrientation(List<List<Point>> snakeLines) {
        List<Point> snakeLineLeg1 = snakeLines.get(0); // snakeline from leg1 feeder node to 3wt
        List<Point> snakeLineLeg2 = snakeLines.get(1); // snakeline with simply two points going from leg2 feeder node to 3wt
        List<Point> snakeLineLeg3 = snakeLines.get(2); // snakeline from leg3 feeder node to 3wt

        // Orientation.UP example:
        // line going  _____OO_____ line going
        //   to leg1        O        to leg3
        //                  |
        //                  o leg2
        Point leg1 = snakeLineLeg1.get(snakeLineLeg1.size() - 2);
        Point leg2 = snakeLineLeg2.get(snakeLineLeg2.size() - 2);
        Point leg3 = snakeLineLeg3.get(snakeLineLeg3.size() - 2);

        if (leg1.getY() == leg3.getY()) {
            // General case
            setOrientation(leg2.getY() < leg1.getY() ? Orientation.DOWN : Orientation.UP);
            setWindingOrder(() -> leg1.getX() < leg3.getX(),
                    Arrays.asList(Middle3WTNode.Winding.UPPER_LEFT, Middle3WTNode.Winding.DOWN, Middle3WTNode.Winding.UPPER_RIGHT,
                            Middle3WTNode.Winding.UPPER_RIGHT, Middle3WTNode.Winding.DOWN, Middle3WTNode.Winding.UPPER_LEFT));
        } else if (leg1.getY() == leg2.getY()) {
            // General case
            setOrientation(leg3.getY() < leg1.getY() ? Orientation.DOWN : Orientation.UP);
            setWindingOrder(() -> leg1.getX() < leg2.getX(),
                    Arrays.asList(Middle3WTNode.Winding.UPPER_LEFT, Middle3WTNode.Winding.UPPER_RIGHT, Middle3WTNode.Winding.DOWN,
                            Middle3WTNode.Winding.UPPER_RIGHT, Middle3WTNode.Winding.UPPER_LEFT, Middle3WTNode.Winding.DOWN));
        } else if (leg2.getX() == leg1.getX()) {
            // Specific case of leg1 and leg2 facing feeder nodes with same abscissa
            setOrientation(leg3.getX() > leg1.getX() ? Orientation.LEFT : Orientation.RIGHT);
            setWindingOrder(() -> leg3.getX() > leg1.getX() == leg1.getY() > leg2.getY(),
                    Arrays.asList(Middle3WTNode.Winding.UPPER_LEFT, Middle3WTNode.Winding.UPPER_RIGHT, Middle3WTNode.Winding.DOWN,
                            Middle3WTNode.Winding.UPPER_RIGHT, Middle3WTNode.Winding.UPPER_LEFT, Middle3WTNode.Winding.DOWN));
        } else if (leg2.getX() == leg3.getX()) {
            // Specific case of leg2 and leg3 facing feeder nodes with same abscissa
            setOrientation(leg1.getX() > leg3.getX() ? Orientation.LEFT : Orientation.RIGHT);
            setWindingOrder(() -> leg1.getX() > leg3.getX() == leg2.getY() > leg3.getY(),
                    Arrays.asList(Middle3WTNode.Winding.DOWN, Middle3WTNode.Winding.UPPER_LEFT, Middle3WTNode.Winding.UPPER_RIGHT,
                            Middle3WTNode.Winding.DOWN, Middle3WTNode.Winding.UPPER_RIGHT, Middle3WTNode.Winding.UPPER_LEFT));
        }
    }

    private void setWindingOrder(BooleanSupplier cond, List<Middle3WTNode.Winding> windings) {
        if (cond.getAsBoolean()) {
            setWindingOrder(windings.get(0), windings.get(1), windings.get(2));
        } else {
            setWindingOrder(windings.get(3), windings.get(4), windings.get(5));
        }
    }

    public void setWindingOrder(Winding first, Winding second, Winding third) {
        setWindingOrder(first, NodeSide.ONE);
        setWindingOrder(second, NodeSide.TWO);
        setWindingOrder(third, NodeSide.THREE);
    }

    public void setWindingOrder(Winding winding, NodeSide legSide) {
        windingMap.put(winding, legSide);
    }

    public Node getAdjacentNode(Winding winding) {
        Edge edge = getAdjacentEdges().get(windingToLegIndex(winding));
        return edge.getOppositeNode(this);
    }

    public VoltageLevelInfos getVoltageLevelInfos(Winding winding) {
        return voltageLevelInfosLeg[windingToLegIndex(winding)];
    }

    private int windingToLegIndex(Winding winding) {
        return windingMap.getOrDefault(winding, NodeSide.ONE).getIntValue() - 1;
    }

    public enum Winding {
        UPPER_LEFT, UPPER_RIGHT, DOWN
    }
}
