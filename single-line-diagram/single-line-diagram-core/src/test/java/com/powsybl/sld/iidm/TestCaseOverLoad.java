/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Jamal KHEYYAD {@literal <jamal.kheyyad at rte-international.com>}
 */
public class TestCaseOverLoad extends AbstractTestCaseIidm {

    @Override
    public void setUp() {
        // initialization of networks and graph builder done in each test
    }

    @Test
    public void testLineOverLoad() {
        network = Networks.createNetworkWithLine();
        network.getVoltageLevel("VoltageLevel1")
                .setHighVoltageLimit(400)
                .setLowVoltageLimit(390);
        network.getLine("Line").newCurrentLimits1().setPermanentLimit(250).add();
        network.getLine("Line").getTerminal1().setP(101).setQ(150).getBusView().getBus().setV(390);
        graphBuilder = new NetworkGraphBuilder(network);
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph("VoltageLevel1");
        voltageLevelGraphLayout(g);
        assertEquals(toString("/TestLineFeederInfoOverLoad.svg"), toSVG(g, "/TestLineFeederInfoOverLoad.svg"));
    }

    @Test
    public void test2WTOverLoad() {
        network = Networks.createNetworkWithTwoWindingsTransformer();
        network.getTwoWindingsTransformer("Transformer").newCurrentLimits2().setPermanentLimit(250).add();
        network.getTwoWindingsTransformer("Transformer").getTerminal2().setP(101).setQ(150).getBusView().getBus().setV(390);
        graphBuilder = new NetworkGraphBuilder(network);
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph("VoltageLevel1");
        voltageLevelGraphLayout(g);
        assertEquals(toString("/TestTransformerFeederInfoOverLoad.svg"), toSVG(g, "/TestTransformerFeederInfoOverLoad.svg"));
    }

    @Test
    public void test3WTOverLoad() {
        network = ThreeWindingsTransformerNetworkFactory.create();
        network.getThreeWindingsTransformer("3WT").getLeg1().newCurrentLimits().setPermanentLimit(250).add();
        network.getThreeWindingsTransformer("3WT").getLeg2().newCurrentLimits().setPermanentLimit(250).add();
        network.getThreeWindingsTransformer("3WT").getLeg3().newCurrentLimits().setPermanentLimit(250).add();
        network.getThreeWindingsTransformer("3WT").getLeg1().getTerminal().setP(-2800.0).setQ(400.0);
        network.getThreeWindingsTransformer("3WT").getLeg2().getTerminal().setP(1400.0).setQ(400.0);
        network.getThreeWindingsTransformer("3WT").getLeg3().getTerminal().setP(1400.0).setQ(400.0);
        graphBuilder = new NetworkGraphBuilder(network);
        // Build substation graph and run layout
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph("VL_132");
        voltageLevelGraphLayout(g);

        assertEquals(toString("/Test3WTFeederInfoOverLoad.svg"), toSVG(g, "/Test3WTFeederInfoOverLoad.svg"));
    }

}
