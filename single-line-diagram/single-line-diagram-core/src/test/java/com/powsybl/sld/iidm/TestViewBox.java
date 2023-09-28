/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.Network;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.model.graphs.SubstationGraph;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Thomas Adam <tadam at neverhack.com>
 */
class TestViewBox extends AbstractTestCaseIidm {

    @Override
    public void setUp() throws IOException {
        // Nothing to do
    }

    @ParameterizedTest
    @ValueSource(strings = {"BB", "BT", "TB", "TT"})
    void test(String suffix) {
        String filename = "network_common_methods_createLine_" + suffix + ".xiidm";
        network = Network.read(filename, getClass().getResourceAsStream("/" + filename));
        substation = network.getSubstation("ST");
        graphBuilder = new NetworkGraphBuilder(network);

        layoutParameters.setShowGrid(true);

        // build substation ST graph
        SubstationGraph g1 = graphBuilder.buildSubstationGraph("ST");
        substationGraphLayout(g1);
        // write SVGs and compare to reference
        assertEquals(toString("/TestViewBox" + suffix + ".svg"), toSVG(g1, "/TestViewBox" + suffix + ".svg"));
    }
}
