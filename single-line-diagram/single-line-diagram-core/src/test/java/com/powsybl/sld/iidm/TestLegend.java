/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.*;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.svg.*;
import com.powsybl.sld.svg.styles.StyleProvider;
import com.powsybl.sld.util.IdUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void testLegendDisplaysFictitiousInjectionsWhenBusBreakerTopology() {
        svgParameters.setBusesLegendAdded(true);
        legendWriter = new DefaultSVGLegendWriter(network, svgParameters);
        VoltageLevel vl = network.getVoltageLevel("VoltageLevel1"); // BUS_BREAKER

        // fictitious injections: Bus1
        vl.getBusBreakerView().getBus("Bus1")
                .setFictitiousP0(1)
                .setFictitiousQ0(-1);

        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph("VoltageLevel1");

        // Run layout
        voltageLevelGraphLayout(g);

        String svg = toSVG(g, "/legend-fictitious-injection-bus-breaker.svg");
        assertEquals(toString("/legend-fictitious-injection-bus-breaker.svg"), svg);
        assertTrue(svg.contains("1 MW"));
        assertTrue(svg.contains("-1 MVar"));
    }

    @Test
    void testLegendDisplaysFictitiousInjectionsWhenNodeBreakerTopology() {
        svgParameters.setBusesLegendAdded(true);
        network = Networks.createBusbarLoadNetwork(); // NODE_BREAKER
        legendWriter = new DefaultSVGLegendWriter(network, svgParameters);
        graphBuilder = new NetworkGraphBuilder(network);
        VoltageLevel vl = network.getVoltageLevel("VoltageLevel1");

        // fictitious injections: node 1
        vl.getNodeBreakerView()
                .setFictitiousP0(1, 1)
                .setFictitiousQ0(1, -1.0);

        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph("VoltageLevel1");
        // Run layout
        voltageLevelGraphLayout(g);

        String svg = toSVG(g, "/legend-fictitious-injection-node-breaker.svg");
        assertEquals(toString("/legend-fictitious-injection-node-breaker.svg"), svg);
        assertTrue(svg.contains("1 MW"));
        assertTrue(svg.contains("-1 MVar"));
    }

    @Override
    protected SVGLegendWriter getDefaultSVGLegendWriter() {
        return legendWriter;
    }
}
