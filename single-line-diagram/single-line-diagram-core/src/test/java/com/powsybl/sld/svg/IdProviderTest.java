/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg;

import com.powsybl.diagram.test.Networks;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.iidm.AbstractTestCaseIidm;
import com.powsybl.sld.layout.SmartVoltageLevelLayoutFactory;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.svg.styles.StyleProvider;
import com.powsybl.sld.svg.styles.iidm.TopologicalStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class IdProviderTest extends AbstractTestCaseIidm {

    VoltageLevelGraph g;
    LabelProvider labelProvider;
    StyleProvider styleProvider;
    SVGLegendWriter svgLegendWriter;
    IdProvider idProvider;

    @Override
    @BeforeEach
    public void setUp() throws IOException {
        network = Networks.createNetworkWithTieLineInVoltageLevel();
        graphBuilder = new NetworkGraphBuilder(network);
        // build graph
        g = graphBuilder.buildVoltageLevelGraph("VL1");

        // Run layout
        new SmartVoltageLevelLayoutFactory(network).create(g).run(layoutParameters);

        // Providers
        labelProvider = new DefaultLabelProvider(network, componentLibrary, layoutParameters, svgParameters);
        styleProvider = new TopologicalStyleProvider(network);
        svgLegendWriter = new DefaultSVGLegendWriter(network, svgParameters);
    }

    @Test
    void testLegacyIdProvider() {
        // Id provider
        idProvider = new LegacyIdProvider(svgParameters.getPrefixId());

        // write SVG and compare to reference
        assertEquals(toString("/TestLegacyIdProvider.svg"), toSVG(g, "/TestLegacyIdProvider.svg", componentLibrary,
            layoutParameters, svgParameters, labelProvider, styleProvider, idProvider, svgLegendWriter));

        assertEquals(toString("/TestLegacyIdProvider.json"), toMetadata(g, "/TestLegacyIdProvider.json",
            componentLibrary, layoutParameters, svgParameters, labelProvider, styleProvider, idProvider, svgLegendWriter));
    }

    @Test
    void testDefaultIdProvider() {
        // Id provider
        idProvider = new DefaultIdProvider(svgParameters.getPrefixId());

        // write SVG and compare to reference
        assertEquals(toString("/TestDefaultIdProvider.svg"), toSVG(g, "/TestDefaultIdProvider.svg", componentLibrary,
            layoutParameters, svgParameters, labelProvider, styleProvider, idProvider, svgLegendWriter));

        assertEquals(toString("/TestDefaultIdProvider.json"), toMetadata(g, "/TestDefaultIdProvider.json",
            componentLibrary, layoutParameters, svgParameters, labelProvider, styleProvider, idProvider, svgLegendWriter));
    }
}
