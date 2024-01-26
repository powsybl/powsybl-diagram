/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
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
import com.powsybl.sld.svg.styles.iidm.TopologicalStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sophie Frasnedo  {@literal <sophie.frasnedo at rte-france.com>}
 */
class TestCaseGroundDisconnector extends AbstractTestCaseIidm {

    @BeforeEach
    public void setUp() {
    }

    @Test
    void testGroundDisconnectorOnLineNodeBreaker() {
        // Create network
        network = Networks.createNetworkGroundDisconnectorOnLineNodeBreaker();

        // Build graph
        graphBuilder = new NetworkGraphBuilder(network);
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph("vl");

        // Run layout
        voltageLevelGraphLayout(g);

        // Write Json and compare to reference
        assertEquals(toString("/TestCaseGroundDisconnectorOnLineNodeBreaker.svg"), toSVG(g, "/TestCaseGroundDisconnectorOnLineNodeBreaker.svg", componentLibrary, layoutParameters, svgParameters, getDefaultDiagramLabelProvider(), new TopologicalStyleProvider(network)));
    }

    @Test
    void testGroundDisconnectorOnBusBarNodeBreaker() {
        // Create network
        network = Networks.createNetworkGroundDisconnectorOnBusBarNodeBreaker();

        // build graph
        graphBuilder = new NetworkGraphBuilder(network);
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph("vl");

        // Run layout
        voltageLevelGraphLayout(g);

        // Write Json and compare to reference
        assertEquals(toString("/TestCaseGroundDisconnectorOnBusBarNodeBreaker.svg"), toSVG(g, "/TestCaseGroundDisconnectorOnBusBarNodeBreaker.svg", componentLibrary, layoutParameters, svgParameters, getDefaultDiagramLabelProvider(), new TopologicalStyleProvider(network)));
    }

    @Test
    void testGroundDisconnectorOnLineBusBreaker() {
        // Create network
        network = Networks.createNetworkGroundDisconnectorOnLineBusBreaker();

        // build graph
        graphBuilder = new NetworkGraphBuilder(network);
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph("vl");

        // Run layout
        voltageLevelGraphLayout(g);

        // Write Json and compare to reference
        assertEquals(toString("/TestCaseGroundDisconnectorOnLineBusBreaker.svg"), toSVG(g, "/TestCaseGroundDisconnectorOnLineBusBreaker.svg", componentLibrary, layoutParameters, svgParameters, getDefaultDiagramLabelProvider(), new TopologicalStyleProvider(network)));
    }

    @Test
    void testGroundDisconnectorOnBusBarBusBreaker() {
        // Create network
        network = Networks.createNetworkGroundDisconnectorOnBusBarBusBreaker();

        // build graph
        graphBuilder = new NetworkGraphBuilder(network);
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph("vl");

        // Run layout
        voltageLevelGraphLayout(g);

        // Write Json and compare to reference
        assertEquals(toString("/TestCaseGroundDisconnectorOnBusBarBusBreaker.svg"), toSVG(g, "/TestCaseGroundDisconnectorOnBusBarBusBreaker.svg", componentLibrary, layoutParameters, svgParameters, getDefaultDiagramLabelProvider(), new TopologicalStyleProvider(network)));
    }
}
