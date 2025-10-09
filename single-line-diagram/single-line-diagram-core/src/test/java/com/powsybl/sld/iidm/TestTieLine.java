/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.Network;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.layout.SmartVoltageLevelLayoutFactory;
import com.powsybl.sld.layout.VerticalSubstationLayoutFactory;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.svg.DefaultLabelProvider;
import com.powsybl.sld.svg.styles.iidm.TopologicalStyleProvider;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
class TestTieLine extends AbstractTestCaseIidm {

    @Override
    public void setUp() throws IOException {
    }

    @Test
    void testTieLineInVoltageLevel() {
        Network network = Networks.createNetworkWithTieLineInVoltageLevel();
        graphBuilder = new NetworkGraphBuilder(network);
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph("VL1");

        // Run layout
        new SmartVoltageLevelLayoutFactory(network).create(g).run(layoutParameters);

        // write SVG and compare to reference
        assertEquals(toString("/TestTieLineVoltageLevel.svg"), toSVG(g, "/TestTieLineVoltageLevel.svg", componentLibrary, layoutParameters, svgParameters, new DefaultLabelProvider(network, componentLibrary, layoutParameters, svgParameters), new TopologicalStyleProvider(network)));
    }

    @Test
    void testTieLineInSubstation() {
        Network network = Networks.createNetworkWithTieLineInSubstation();
        graphBuilder = new NetworkGraphBuilder(network);
        // build graph
        SubstationGraph g = graphBuilder.buildSubstationGraph("S1");

        // Run layout
        new VerticalSubstationLayoutFactory().create(g, new SmartVoltageLevelLayoutFactory(network)).run(layoutParameters);

        // write SVG and compare to reference
        assertEquals(toString("/TestTieLineSubstation.svg"), toSVG(g, "/TestTieLineSubstation.svg", componentLibrary, layoutParameters, svgParameters, new DefaultLabelProvider(network, componentLibrary, layoutParameters, svgParameters), new TopologicalStyleProvider(network)));
    }

}
