/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.layout.algorithms;

import com.powsybl.diagram.util.layout.GraphTestData;
import com.powsybl.diagram.util.layout.ResourceUtils;
import com.powsybl.diagram.util.layout.algorithms.parameters.BasicForceLayoutParameters;
import com.powsybl.diagram.util.layout.geometry.LayoutContext;
import com.powsybl.diagram.util.layout.geometry.Vector2D;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Dissoubray Nathan {@literal <nathan.dissoubray at rte-france.com>}
 */
class BasicForceLayoutAlgorithmTest {
    @Test
    void basicForcePositionEqual() {
        LayoutContext<String, DefaultEdge> layoutContext = GraphTestData.getLayoutContext1();
        Function<String, String> tooltip = v -> String.format("Vertex %s", v);
        LayoutAlgorithm<String, DefaultEdge> layoutAlgorithm = new BasicForceLayoutAlgorithm<>(new BasicForceLayoutParameters.Builder().build());

        Vector2D position2 = layoutContext.getMovingPoints().get("2").getPosition();
        layoutContext.getMovingPoints().get("3").setPosition(new Vector2D(position2.getX(), position2.getY()));

        layoutAlgorithm.run(layoutContext);
        GraphTestData.checkPointPositionAllDifferent(layoutContext);
        StringWriter sw = new StringWriter();
        layoutContext.toSVG(tooltip, sw);
        assertEquals(ResourceUtils.toString("basic_5_nodes_force_position_equality.svg"), sw.toString());
    }

}
