/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout.setup;

import com.powsybl.diagram.util.forcelayout.GraphTestData;
import com.powsybl.diagram.util.forcelayout.geometry.LayoutContext;
import com.powsybl.diagram.util.forcelayout.geometry.Point;
import com.powsybl.diagram.util.forcelayout.geometry.Vector2D;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
class SimpleSetupTest {

    @Test
    void run() {
        Graph<String, DefaultEdge> graph = GraphTestData.getGraph();
        SimpleSetup<String, DefaultEdge> simpleSetup = new SimpleSetup<>();
        LayoutContext<String, DefaultEdge> layoutContext = new LayoutContext<>(graph);
        simpleSetup.run(layoutContext);
        checkPointsPosition(layoutContext.getMovingPoints().values());
        checkPointsPosition(layoutContext.getFixedPoints().values());

    }

    private void checkPointsPosition(Collection<Point> points) {
        for (Point point : points) {
            Vector2D position = point.getPosition();
            assertTrue(position.getX() <= 0.5 && position.getX() >= -0.5);
            assertTrue(position.getY() <= 0.5 && position.getY() >= -0.5);
        }
    }
}
