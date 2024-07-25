/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.Network;
import com.powsybl.nad.build.iidm.NetworkGraphBuilder;
import com.powsybl.nad.build.iidm.VoltageLevelFilter;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.Point;
import com.powsybl.nad.model.TextPosition;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro at soft.it>}
 */
public class LayoutWithFixedTextNodePositionsTest {

    private Network network;
    private LayoutParameters layoutParameters;
    private Layout basicForceLayout;

    @BeforeEach
    void setup() {
        network = Networks.createTwoVoltageLevels();
        layoutParameters = new LayoutParameters();
        basicForceLayout = new BasicForceLayoutFactory().create();
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
            checkShift(textPair.getFirst().getPosition(), textPair.getSecond().getConnection(),
                       layoutParameters.getTextNodeFixedShift().getX(),
                       layoutParameters.getTextNodeFixedShift().getY() + layoutParameters.getDetailedTextNodeYShift());
        });
    }

    @Test
    void testFixedTextNodePositions() {
        Map<String, TextPosition> textNodeFixedPositions = new HashMap<String, TextPosition>();
        textNodeFixedPositions.put("0-textnode", new TextPosition(new Point(100, -50), new Point(90, -25)));
        basicForceLayout.setTextNodesWithFixedPosition(textNodeFixedPositions);
        Graph graph = new NetworkGraphBuilder(network, VoltageLevelFilter.NO_FILTER).buildGraph();
        basicForceLayout.run(graph, layoutParameters);
        graph.getVoltageLevelTextPairs().forEach(textPair -> {
            String textNodeId = textPair.getSecond().getDiagramId();
            checkShift(textPair.getFirst().getPosition(), textPair.getSecond().getPosition(),
                       textNodeFixedPositions.containsKey(textNodeId)
                           ? textNodeFixedPositions.get(textNodeId).position().getX()
                           : layoutParameters.getTextNodeFixedShift().getX(),
                       textNodeFixedPositions.containsKey(textNodeId)
                           ? textNodeFixedPositions.get(textNodeId).position().getY()
                           : layoutParameters.getTextNodeFixedShift().getY());
            checkShift(textPair.getFirst().getPosition(), textPair.getSecond().getConnection(),
                       textNodeFixedPositions.containsKey(textNodeId)
                           ? textNodeFixedPositions.get(textNodeId).connection().getX()
                           : layoutParameters.getTextNodeFixedShift().getX(),
                       textNodeFixedPositions.containsKey(textNodeId)
                           ? textNodeFixedPositions.get(textNodeId).connection().getY()
                           : layoutParameters.getTextNodeFixedShift().getY() + layoutParameters.getDetailedTextNodeYShift());
        });
    }
}
