/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.Network;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.svg.DefaultSVGLegendWriter;
import com.powsybl.sld.svg.GraphMetadata;
import com.powsybl.sld.svg.SVGLegendWriter;
import com.powsybl.sld.svg.SvgParameters;
import com.powsybl.sld.svg.styles.StyleProvider;
import com.powsybl.sld.util.IdUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Slimane Amar {@literal <slimane.amar at rte-france.com>}
 */
class TestLegend extends AbstractTestCaseIidm {

    SVGLegendWriter legendWriter;

    public class SpecificLegendWriter extends DefaultSVGLegendWriter {

        public SpecificLegendWriter(Network network, SvgParameters svgParameters) {
            super(network, svgParameters);
        }

        @Override
        public void drawLegend(VoltageLevelGraph graph, GraphMetadata metadata, StyleProvider styleProvider, Element legendRootElement, double positionX, double positionY) {
            Element table = legendRootElement.getOwnerDocument().createElement("foreignObject");
            table.setAttribute("id", IdUtil.escapeId("test_legend"));
            table.setAttribute("x", String.valueOf(positionX));
            table.setAttribute("y", String.valueOf(positionY));
            table.setAttribute("height", "100%");
            table.setAttribute("width", "100%");
            Element div = table.getOwnerDocument().createElementNS("http://www.w3.org/1999/xhtml", "div");
            div.appendChild(table.getOwnerDocument().createTextNode("TEST"));
            table.appendChild(div);
            legendRootElement.appendChild(table);
        }
    }

    @BeforeEach
    public void setUp() throws IOException {
        network = Networks.createNetworkWithLine();
        graphBuilder = new NetworkGraphBuilder(network);
    }

    @Test
    void testDefaultLegend() {
        legendWriter = new DefaultSVGLegendWriter(network, svgParameters);
        svgParameters.setBusesLegendAdded(true);

        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph("VoltageLevel1");

        // Run layout
        voltageLevelGraphLayout(g);

        // write SVG and compare to reference
        assertEquals(toString("/TestLegendDefault.svg"), toSVG(g, "/TestLegendDefault.svg"));
    }

    @Test
    void testSpecificLegend() {
        legendWriter = new SpecificLegendWriter(network, svgParameters);
        svgParameters.setBusesLegendAdded(true);

        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph("VoltageLevel1");

        // Run layout
        voltageLevelGraphLayout(g);

        // write SVG and compare to reference
        assertEquals(toString("/TestLegendSpecific.svg"), toSVG(g, "/TestLegendSpecific.svg"));
    }

    @Override
    protected SVGLegendWriter getDefaultSVGLegendWriter() {
        return legendWriter;
    }
}
