/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.sld.Config;
import com.powsybl.sld.ConfigBuilder;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
class TestNodeDecoratorsBusBreaker extends AbstractTestCaseIidm {

    @BeforeEach
    public void setUp() {
        network = Networks.createBusBreakerNetworkWithBranchStatus("TestNodeDecorators", "test");
        graphBuilder = new NetworkGraphBuilder(network);
    }

    @Test
    void testBranchStatusDecorators() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(network.getVoltageLevel("VL1").getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write SVG and compare to reference
        Config config = new ConfigBuilder(network)
                .withLayoutParameters(layoutParameters)
                .withComponentLibrary(componentLibrary)
                .withSvgParameters(svgParameters)
                .build();
        assertEquals(toString("/NodeDecoratorsBranchStatusBusBreaker.svg"),
                toSVG(g, "/NodeDecoratorsBranchStatusBusBreaker.svg", config));
    }
}
