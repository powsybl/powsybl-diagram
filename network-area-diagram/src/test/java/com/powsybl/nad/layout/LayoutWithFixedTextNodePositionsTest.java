/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.Network;
import com.powsybl.nad.build.iidm.NetworkGraphBuilder;
import com.powsybl.nad.build.iidm.VoltageLevelFilter;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.Point;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro at soft.it>}
 */
class LayoutWithFixedTextNodePositionsTest {

    private Network network;
    private LayoutParameters layoutParameters;
    private Layout basicForceLayout;

    @BeforeEach
    void setup() {
        network = Networks.createTwoVoltageLevels();
        layoutParameters = new LayoutParameters();
        basicForceLayout = new ForceLayoutFactory().create();
    }

    private void checkShift(Point point1, Point point2, double shiftX, double shiftY) {
        assertEquals(point2.getX() - point1.getX(), shiftX, 0);
        assertEquals(point2.getY() - point1.getY(), shiftY, 0);
    }

    @Test
    void testTextNodePositions() {
        Graph graph = new NetworkGraphBuilder(network, VoltageLevelFilter.NO_FILTER).buildGraph();
        basicForceLayout.run(graph, layoutParameters);
        graph.getVoltageLevelTextPairs().forEach(textPair -> {
            checkShift(textPair.getFirst().getPosition(), textPair.getSecond().getPosition(),
                       layoutParameters.getTextNodeFixedShift().getX(),
                       layoutParameters.getTextNodeFixedShift().getY());
            checkShift(textPair.getFirst().getPosition(), textPair.getSecond().getEdgeConnection(),
                       layoutParameters.getTextNodeFixedShift().getX(),
                       layoutParameters.getTextNodeFixedShift().getY() + layoutParameters.getTextNodeEdgeConnectionYShift());
        });
    }

    @Test
    void testFixedTextNodePositions() {
        String voltageLevelId = "vl1";
        Point topLeftPosition = new Point(100, -50);
        Point edgeConnection = new Point(90, -25);
        basicForceLayout.setTextNodeFixedPosition(voltageLevelId, topLeftPosition, edgeConnection);
        Graph graph = new NetworkGraphBuilder(network, VoltageLevelFilter.NO_FILTER).buildGraph();
        basicForceLayout.run(graph, layoutParameters);
        graph.getVoltageLevelTextPairs().forEach(textPair -> {
            boolean fixedPosition = voltageLevelId.equals(textPair.getFirst().getEquipmentId());
            checkShift(textPair.getFirst().getPosition(), textPair.getSecond().getPosition(),
                       fixedPosition ? topLeftPosition.getX() : layoutParameters.getTextNodeFixedShift().getX(),
                       fixedPosition ? topLeftPosition.getY() : layoutParameters.getTextNodeFixedShift().getY());
            checkShift(textPair.getFirst().getPosition(), textPair.getSecond().getEdgeConnection(),
                       fixedPosition ? edgeConnection.getX() : layoutParameters.getTextNodeFixedShift().getX(),
                       fixedPosition ? edgeConnection.getY() : layoutParameters.getTextNodeFixedShift().getY() + layoutParameters.getTextNodeEdgeConnectionYShift());
        });
    }
}
