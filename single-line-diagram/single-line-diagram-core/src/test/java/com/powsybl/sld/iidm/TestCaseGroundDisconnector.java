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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sophie Frasnedo  {@literal <sophie.frasnedo at rte-france.com>}
 */
class TestCaseGroundDisconnector extends AbstractTestCaseIidm {

    @BeforeEach
    public void setUp() {
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("provideTestData")
    void test(Network network, String resourceName) {
        // Create network
        this.network = network;

        // Build graph
        graphBuilder = new NetworkGraphBuilder(network);
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph("vl");

        // Run layout
        voltageLevelGraphLayout(g);

        // Write svg and compare to reference
        assertEquals(toString(resourceName), toSVG(g, resourceName, componentLibrary, layoutParameters, svgParameters,
                getDefaultDiagramLabelProvider(), new TopologicalStyleProvider(network)));
    }

    private static List<Arguments> provideTestData() {
        return List.of(
                Arguments.of(Networks.createNetworkGroundDisconnectorOnLineNodeBreaker(), "/GroundDisconnectorOnLineNodeBreaker.svg"),
                Arguments.of(Networks.createNetworkGroundDisconnectorOnBusBarNodeBreaker(), "/GroundDisconnectorOnBusBarNodeBreaker.svg"),
                Arguments.of(Networks.createNetworkGroundDisconnectorOnLineBusBreaker(), "/GroundDisconnectorOnLineBusBreaker.svg"),
                Arguments.of(Networks.createNetworkGroundDisconnectorOnBusBarBusBreaker(), "/GroundDisconnectorOnBusBarBusBreaker.svg")
        );
    }
}
