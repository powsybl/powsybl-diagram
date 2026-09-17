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
import com.powsybl.diagram.util.layout.geometry.Vector2D;
import com.powsybl.diagram.util.layout.postprocessing.OverlapPreventionPostProcessing;
import com.powsybl.diagram.util.layout.postprocessing.PostProcessing;
import com.powsybl.diagram.util.layout.setup.SquareRandomSetup;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

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
        checkPointOverlap(new Atlas2ForceLayoutAlgorithm<>(), "atlas2_10_nodes_BH_NoOverlap_force_position_equality.svg");
    }

    @Test
    void calculateLayoutWithOverlappingPointsNoBH() {
        checkPointOverlap(
            new Atlas2ForceLayoutAlgorithm<>(new Atlas2Parameters.Builder().withBarnesHutDisabled().build()),
            "atlas2_10_nodes_BH_NoOverlap_force_position_equality_no_bh.svg");
    }

    private void checkPointOverlap(LayoutAlgorithm<String, DefaultEdge> algorithm, String resourceName) {
        LayoutContext<String, DefaultEdge> layoutContext = GraphTestData.getLayoutContext2();
        //set position of 0 to be position of 1
        Vector2D position1 = layoutContext.getMovingPoints().get("1").getPosition();
        layoutContext.getMovingPoints().get("0").setPosition(new Vector2D(position1.getX(), position1.getY()));
        algorithm.run(layoutContext);
        GraphTestData.checkPointPositionAllDifferent(layoutContext);

        Vector2D position3 = layoutContext.getMovingPoints().get("3").getPosition();
        layoutContext.getMovingPoints().get("2").setPosition(new Vector2D(position3.getX(), position3.getY()));
        PostProcessing<String, DefaultEdge> noOverlapPostProcessing = new OverlapPreventionPostProcessing<>();
        noOverlapPostProcessing.run(layoutContext);
        GraphTestData.checkPointPositionAllDifferent(layoutContext);

        StringWriter sw = new StringWriter();
        layoutContext.toSVG(v -> String.format("Vertex %s", v), sw);
        assertEquals(ResourceUtils.toString(resourceName), sw.toString());
    }

}
