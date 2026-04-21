/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.layout.algorithms;

import com.powsybl.diagram.util.layout.GraphTestData;
import com.powsybl.diagram.util.layout.Layout;
import com.powsybl.diagram.util.layout.ResourceUtils;
import com.powsybl.diagram.util.layout.algorithms.parameters.Atlas2Parameters;
import com.powsybl.diagram.util.layout.geometry.LayoutContext;
import com.powsybl.diagram.util.layout.geometry.Point;
import com.powsybl.diagram.util.layout.postprocessing.OverlapPreventionPostProcessing;
import com.powsybl.diagram.util.layout.postprocessing.PostProcessing;
import com.powsybl.diagram.util.layout.setup.SquareRandomSetup;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
class Atlas2ForceLayoutAlgorithmTest {

    @Test
    void calculateLayoutNoBH() {
        LayoutContext<String, DefaultEdge> layoutContext = GraphTestData.getLayoutContext1();
        Atlas2Parameters layoutParameters = new Atlas2Parameters.Builder().withBarnesHutDisabled().build();
        LayoutAlgorithm<String, DefaultEdge> atlas2 = new Atlas2ForceLayoutAlgorithm<>(layoutParameters);
        atlas2.run(layoutContext);
        StringWriter sw = new StringWriter();
        layoutContext.toSVG(v -> String.format("Vertex %s", v), sw);
        assertEquals(ResourceUtils.toString("atlas2_5_nodes_no_BH.svg"), sw.toString());
    }

    @Test
    void calculateLayoutNoOverlapNoBH() {
        LayoutContext<String, DefaultEdge> layoutContext = GraphTestData.getLayoutContext1();
        Layout<String, DefaultEdge> atlas2 = new Layout<>(
            new SquareRandomSetup<>(),
            new Atlas2ForceLayoutAlgorithm<>(new Atlas2Parameters.Builder()
                    .withBarnesHutDisabled()
                    .build()
            ),
            new OverlapPreventionPostProcessing<>()
        );
        atlas2.run(layoutContext);
        StringWriter sw = new StringWriter();
        layoutContext.toSVG(v -> String.format("Vertex %s", v), sw);
        assertEquals(ResourceUtils.toString("atlas2_5_nodes_noOverlap.svg"), sw.toString());
    }

    @Test
    void calculateLayoutYesBH() {
        LayoutContext<String, DefaultEdge> layoutContext = GraphTestData.getLayoutContext1();
        Atlas2Parameters layoutParameters = new Atlas2Parameters.Builder().withBarnesHutTheta(1.5).build();
        LayoutAlgorithm<String, DefaultEdge> atlas2 = new Atlas2ForceLayoutAlgorithm<>(layoutParameters);
        atlas2.run(layoutContext);
        StringWriter sw = new StringWriter();
        layoutContext.toSVG(v -> String.format("Vertex %s", v), sw);
        assertEquals(ResourceUtils.toString("atlas2_5_nodes_yes_BH.svg"), sw.toString());
    }

    @Test
    void calculateLayoutWithOverlappingPoints() {
        LayoutContext<String, DefaultEdge> layoutContext = GraphTestData.getLayoutContext1();
        //set position of 0 to be position of 1
        layoutContext.getMovingPoints().get("0").setPosition(GraphTestData.getPoints1()[1].getPosition());
        LayoutAlgorithm<String, DefaultEdge> atlas2 = new Atlas2ForceLayoutAlgorithm<>();
        atlas2.run(layoutContext);
        checkPointPositionAllDifferent(layoutContext);

        layoutContext.getMovingPoints().get("2").setPosition(GraphTestData.getPoints1()[3].getPosition());
        PostProcessing<String, DefaultEdge> noOverlapPostProcessing = new OverlapPreventionPostProcessing<>();
        noOverlapPostProcessing.run(layoutContext);
        checkPointPositionAllDifferent(layoutContext);
    }

    public void checkPointPositionAllDifferent(LayoutContext<String, DefaultEdge> layoutContext) {
        List<Point> allPoints = new ArrayList<>(layoutContext.getAllPoints().values());
        for (int i = 0; i < allPoints.size(); ++i) {
            for (int j = i + 1; j < allPoints.size(); ++j) {
                assertNotEquals(allPoints.get(i).getPosition(), allPoints.get(j).getPosition());
            }
        }
    }
}
