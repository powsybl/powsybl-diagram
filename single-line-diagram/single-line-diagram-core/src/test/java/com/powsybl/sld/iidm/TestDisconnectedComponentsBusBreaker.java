/**
 * Copyright (c) 2023-2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */

class TestDisconnectedComponentsBusBreaker extends AbstractTestCaseIidm {

    @BeforeEach
    public void setUp() {
        network = Networks.createBusBreakerNetworkWithInternalBranches("TestInternalBranchesBusBreaker", "test");
        substation = network.getSubstation("S1");
    }

    @Test
    void testDisconnectedComponents() {
        network.getLoad("LD1").getTerminal().disconnect();
        network.getGenerator("G").getTerminal().disconnect();
        network.getLine("L12").getTerminal(TwoSides.TWO).disconnect();
        network.getTwoWindingsTransformer("T11").getTerminal(TwoSides.TWO).disconnect();
        network.getThreeWindingsTransformer("T3_12").getTerminal(ThreeSides.THREE).disconnect();

        // build graph
        graphBuilder = new NetworkGraphBuilder(network);
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(network.getVoltageLevel("VL1").getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write SVG and compare to reference
        assertEquals(toString("/disconnectedComponentsBusBreaker.svg"),
                toSVG(g, "/disconnectedComponentsBusBreaker.svg"));
    }
}
