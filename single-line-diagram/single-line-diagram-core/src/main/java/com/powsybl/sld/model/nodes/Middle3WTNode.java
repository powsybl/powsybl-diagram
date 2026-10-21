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

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
public class Middle3WTNode extends AbstractMiddleTwtNode {
    private final Map<Winding, NodeSide> windingMap = new EnumMap<>(Winding.class);
    private final boolean embeddedInVlGraph;

    public Middle3WTNode(String id, String name, VoltageLevelInfos voltageLevelInfosLeg1, VoltageLevelInfos voltageLevelInfosLeg2,
                         VoltageLevelInfos voltageLevelInfosLeg3, String componentType, boolean embeddedInVLGraph) {
        super(id, name,
            new VoltageLevelInfos[]{Objects.requireNonNull(voltageLevelInfosLeg1), Objects.requireNonNull(voltageLevelInfosLeg2), Objects.requireNonNull(voltageLevelInfosLeg3)},
            componentType);
        this.embeddedInVlGraph = embeddedInVLGraph;
    }

    public boolean isEmbeddedInVlGraph() {
        return embeddedInVlGraph;
    }

    @Override
    public void setOrientationFromSnakeLines(List<List<Point>> snakeLines) {
        Point leg1 = snakeLines.get(0).get(snakeLines.get(0).size() - 2);
        Point leg2 = snakeLines.get(1).get(snakeLines.get(1).size() - 2);
        Point leg3 = snakeLines.get(2).get(snakeLines.get(2).size() - 2);

        if (leg1.getY() == leg3.getY()) {
            setOrientation(leg2.getY() < leg1.getY() ? Orientation.DOWN : Orientation.UP);
            if (leg1.getX() < leg3.getX()) {
                // line to leg1 ____OO____ line to leg3
                //                  O
                //                  |
                //            line to leg2
                setWindingOrder(Winding.UPPER_LEFT, Winding.DOWN, Winding.UPPER_RIGHT);
            } else {
                // line to leg3 ____OO____ line to leg1
                //                  O
                //                  |
                //            line to leg2
                setWindingOrder(Winding.UPPER_RIGHT, Winding.DOWN, Winding.UPPER_LEFT);
            }
        } else if (leg1.getY() == leg2.getY()) {
            setOrientation(leg3.getY() < leg1.getY() ? Orientation.DOWN : Orientation.UP);
            if (leg1.getX() < leg2.getX()) {
                // line to leg1 ____OO____ line to leg2
                //                  O
                //                  |
                //            line to leg3
                setWindingOrder(Winding.UPPER_LEFT, Winding.UPPER_RIGHT, Winding.DOWN);
            } else {
                // line to leg2 ____OO____ line to leg1
                //                  O
                //                  |
                //            line to leg3
                setWindingOrder(Winding.UPPER_RIGHT, Winding.UPPER_LEFT, Winding.DOWN);
            }
        } else if (leg2.getY() == leg3.getY()) {
            setOrientation(leg1.getY() < leg2.getY() ? Orientation.DOWN : Orientation.UP);
            if (leg2.getX() < leg3.getX()) {
                // line to leg2 ____OO____ line to leg3
                //                  O
                //                  |
                //            line to leg1
                setWindingOrder(Winding.DOWN, Winding.UPPER_LEFT, Winding.UPPER_RIGHT);
            } else {
                // line to leg3 ____OO____ line to leg2
                //                  O
                //                  |
                //            line to leg1
                setWindingOrder(Winding.DOWN, Winding.UPPER_RIGHT, Winding.UPPER_LEFT);
            }
        } else if (leg1.getX() == leg2.getX()) {
            setOrientation(leg3.getX() > leg1.getX() ? Orientation.LEFT : Orientation.RIGHT);
            if ((leg3.getX() > leg1.getX()) == (leg1.getY() > leg2.getY())) {
                // line to leg2
                //      |
                //      8o --- line to leg3
                //      |
                // line to leg1
                setWindingOrder(Winding.UPPER_LEFT, Winding.UPPER_RIGHT, Winding.DOWN);
            } else {
                //           line to leg2
                //                   |
                // line to leg3 --- o8
                //                   |
                //           line to leg1
                setWindingOrder(Winding.UPPER_RIGHT, Winding.UPPER_LEFT, Winding.DOWN);
            }
        } else if (leg2.getX() == leg3.getX()) {
            setOrientation(leg1.getX() > leg2.getX() ? Orientation.LEFT : Orientation.RIGHT);
            if ((leg1.getX() > leg2.getX()) == (leg2.getY() > leg3.getY())) {
                // line to leg3
                //      |
                //      8o --- line to leg1
                //      |
                // line to leg2
                setWindingOrder(Winding.DOWN, Winding.UPPER_LEFT, Winding.UPPER_RIGHT);
            } else {
                //           line to leg3
                //                   |
                // line to leg1 --- o8
                //                   |
                //           line to leg2
                setWindingOrder(Winding.DOWN, Winding.UPPER_RIGHT, Winding.UPPER_LEFT);
            }
        } else if (leg3.getX() == leg1.getX()) {
            setOrientation(leg2.getX() > leg3.getX() ? Orientation.LEFT : Orientation.RIGHT);
            if ((leg2.getX() > leg3.getX()) == (leg3.getY() > leg1.getY())) {
                // line to leg1
                //      |
                //      8o --- line to leg2
                //      |
                // line to leg3
                setWindingOrder(Winding.UPPER_RIGHT, Winding.DOWN, Winding.UPPER_LEFT);
            } else {
                //           line to leg1
                //                   |
                // line to leg2 --- o8
                //                   |
                //           line to leg3
                setWindingOrder(Winding.UPPER_LEFT, Winding.DOWN, Winding.UPPER_RIGHT);
            }
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
