/**
 * Copyright (c) 2024-2025, RTE (http://www.rte-france.com)
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
import com.powsybl.sld.svg.styles.AnimatedFeederInfoStyleProvider;
import com.powsybl.sld.svg.styles.StyleProvider;
import com.powsybl.sld.svg.styles.StyleProvidersList;
import com.powsybl.sld.svg.styles.iidm.LimitHighlightStyleProvider;
import com.powsybl.sld.svg.styles.iidm.TopologicalStyleProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Jamal KHEYYAD {@literal <jamal.kheyyad at rte-international.com>}
 */
class TestCaseLimitViolation extends AbstractTestCaseIidm {

    @Override
    public void setUp() {
        // initialization of networks and graph builder done in each test
    }

    @Override
    public StyleProvider getDefaultDiagramStyleProvider() {
        return new StyleProvidersList(new TopologicalStyleProvider(network), new LimitHighlightStyleProvider(network), new AnimatedFeederInfoStyleProvider(500, 1000));
    }

    @Test
    void testLineOverLoad() {
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
    void test2WTOverLoad() {
        network = Networks.createNetworkWithTwoWindingsTransformer();
        network.getTwoWindingsTransformer("Transformer").newCurrentLimits2().setPermanentLimit(250).add();
        network.getTwoWindingsTransformer("Transformer").getTerminal2().setP(101).setQ(150).getBusView().getBus().setV(390);
        graphBuilder = new NetworkGraphBuilder(network);
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph("VoltageLevel1");
        voltageLevelGraphLayout(g);
        assertEquals(toString("/TestTransformerFeederInfoOverLoad.svg"), toSVG(g, "/TestTransformerFeederInfoOverLoad.svg"));
    }

    @Test
    void test3WTOverLoad() {
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

    @Test
    void testBusOverVoltageLimitViolation() {
        network = Networks.createComplexExternCellOnFourSections();

        network.getBusbarSection("bbs3").getTerminal().getBusView().getBus().setV(390);
        network.getBusbarSection("bbs3").getTerminal().getVoltageLevel().setHighVoltageLimit(1);
        network.getBusbarSection("bbs3").getTerminal().getVoltageLevel().setLowVoltageLimit(0);

        graphBuilder = new NetworkGraphBuilder(network);
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph("vl");
        voltageLevelGraphLayout(g);
        assertEquals(toString("/TestBusBarOverVoltageLimitHightlight.svg"), toSVG(g, "/TestBusBarOverVoltageLimitHightlight.svg"));
    }

    @Test
    void testBusUnderVoltageLimitViolation() {
        network = Networks.createComplexExternCellOnFourSections();

        network.getBusbarSection("bbs3").getTerminal().getBusView().getBus().setV(200);
        network.getBusbarSection("bbs3").getTerminal().getVoltageLevel().setHighVoltageLimit(400);
        network.getBusbarSection("bbs3").getTerminal().getVoltageLevel().setLowVoltageLimit(390);

        graphBuilder = new NetworkGraphBuilder(network);
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph("vl");
        voltageLevelGraphLayout(g);
        assertEquals(toString("/TestBusBarUnderVoltageLimitHightlight.svg"), toSVG(g, "/TestBusBarUnderVoltageLimitHightlight.svg"));
    }

}
