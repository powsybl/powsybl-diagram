/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.layout.SmartVoltageLevelLayoutFactory;
import com.powsybl.sld.layout.VerticalSubstationLayoutFactory;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.svg.DefaultLabelProvider;
import com.powsybl.sld.svg.DefaultSVGLegendWriter;
import com.powsybl.sld.svg.styles.iidm.TopologicalStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Charly BOUTIER {@literal <charly.boutier at rte-france.com>}
 */
class TestUnifyVoltageLevelColors extends AbstractTestCaseIidm {

    @BeforeEach
    @Override
    public void setUp() throws IOException {
        network = Networks.createNetworkWithTieLineInSubstation();
        graphBuilder = new NetworkGraphBuilder(network);
        substation = network.getSubstation("S1");
    }

    @Test
    void testSubstationRegularVoltageLevelColors() {
        svgParameters.setUnifyVoltageLevelColors(false);

        // build graph
        SubstationGraph g = graphBuilder.buildSubstationGraph(substation.getId());

        // Run layout
        new VerticalSubstationLayoutFactory().create(g, new SmartVoltageLevelLayoutFactory(network)).run(layoutParameters);

        // write SVG and compare to reference
        assertEquals(toString("/TestTieLineSubstation.svg"), toSVG(g, "/TestTieLineSubstation.svg", componentLibrary, layoutParameters, svgParameters, new DefaultLabelProvider(network, componentLibrary, layoutParameters, svgParameters), new TopologicalStyleProvider(network, svgParameters), new DefaultSVGLegendWriter(network, svgParameters)));
    }

    @Test
    void testSubstationUnifiedVoltageLevelColors() {
        svgParameters.setUnifyVoltageLevelColors(true);

        // build graph
        SubstationGraph g = graphBuilder.buildSubstationGraph(substation.getId());

        // Run layout
        new VerticalSubstationLayoutFactory().create(g, new SmartVoltageLevelLayoutFactory(network)).run(layoutParameters);

        // write SVG and compare to reference
        assertEquals(toString("/TestTieLineSubstationUnifiedColors.svg"), toSVG(g, "/TestTieLineSubstationUnifiedColors.svg", componentLibrary, layoutParameters, svgParameters, new DefaultLabelProvider(network, componentLibrary, layoutParameters, svgParameters), new TopologicalStyleProvider(network, svgParameters), new DefaultSVGLegendWriter(network, svgParameters)));
    }

}
