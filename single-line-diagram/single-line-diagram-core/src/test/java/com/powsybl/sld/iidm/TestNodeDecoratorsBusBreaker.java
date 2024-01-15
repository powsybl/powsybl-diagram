/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 * @author Slimane Amar {@literal <slimane.amar at rte-france.com>}
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
        assertEquals(toString("/NodeDecoratorsBranchStatusBusBreaker.svg"),
                toSVG(g, "/NodeDecoratorsBranchStatusBusBreaker.svg", componentLibrary, layoutParameters, svgParameters, getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider()));
    }
}
