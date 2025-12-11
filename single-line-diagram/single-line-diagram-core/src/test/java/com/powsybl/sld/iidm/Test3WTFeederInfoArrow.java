/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.svg.SvgParameters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
class Test3WTFeederInfoArrow extends AbstractTestCaseIidm {

    @BeforeEach
    public void setUp() {
        network = ThreeWindingsTransformerNetworkFactory.create();
        network.getThreeWindingsTransformer("3WT").getLeg1().getTerminal().setP(-2800.0).setQ(800.0);
        network.getThreeWindingsTransformer("3WT").getLeg2().getTerminal().setP(1400.0).setQ(400.0);
        network.getThreeWindingsTransformer("3WT").getLeg3().getTerminal().setP(1400.0).setQ(400.0);
        graphBuilder = new NetworkGraphBuilder(network);
    }

    @Test
    void test3WTVoltageLevelGraphFeederInfoInside() {

        // Build voltage level graph and run layout with INSIDE_VOLTAGE_LEVEL mode (default)
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph("VL_132");
        voltageLevelGraphLayout(g);

        assertEquals(toString("/Test3WTFeederInfoArrowVoltageLevel.svg"), toSVG(g, "/Test3WTFeederInfoArrowVoltageLevel.svg"));
    }

    @Test
    void tes3WTVoltageLevelGraphFullFeederInfos() {

        // Build voltage level graph and run layout with FULL_3WT mode
        svgParameters.setThreeWindingsTransformerFeederInfoMode(SvgParameters.ThreeWindingsTransformerFeederInfoMode.FULL_3WT);

        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph("VL_132");
        voltageLevelGraphLayout(g);

        assertEquals(toString("/Test3WTFeederInfoArrowVoltageLevelFull3WT.svg"), toSVG(g, "/Test3WTFeederInfoArrowVoltageLevelFull3WT.svg"));
    }

    @Test
    void testSubstationGraphInsideFeederInfo() {

        // Build substation graph and run layout with INSIDE_VOLTAGE_LEVEL mode (default)
        SubstationGraph g = graphBuilder.buildSubstationGraph("SUBSTATION");
        substationGraphLayout(g);

        assertEquals(toString("/Test3WTFeederInfoArrowSubstation.svg"), toSVG(g, "/Test3WTFeederInfoArrowSubstation.svg"));

    }

    @Test
    void testSubstationGraphFullFeederInfos() {

        // Build substation graph and run layout with FULL_3WT mode
        svgParameters.setThreeWindingsTransformerFeederInfoMode(SvgParameters.ThreeWindingsTransformerFeederInfoMode.FULL_3WT);
        SubstationGraph g = graphBuilder.buildSubstationGraph("SUBSTATION");
        substationGraphLayout(g);

        assertEquals(toString("/Test3WTFeederInfoArrowSubstationFull3WT.svg"), toSVG(g, "/Test3WTFeederInfoArrowSubstationFull3WT.svg"));

    }

	@Test
    void testVoltageLevelGraph3WTSwitch() {

        // Build voltage level graph and run layout with FULL_3WT mode
        svgParameters.setThreeWindingsTransformerFeederInfoMode(SvgParameters.ThreeWindingsTransformerFeederInfoMode.FULL_3WT);
        network = get3WtWithSwitchNetwork();
        NetworkGraphBuilder graphBuilder2 = new NetworkGraphBuilder(network);
        VoltageLevelGraph g = graphBuilder2.buildVoltageLevelGraph("vl3");
        voltageLevelGraphLayout(g);

        assertEquals(toString("/Test3WTSwitchFeederInfoArrowVoltageLevel.svg"), toSVG(g, "/Test3WTSwitchFeederInfoArrowVoltageLevel.svg"));
    }

    private Network get3WtWithSwitchNetwork() {
        Network network = Network.create("testCase1", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = Networks.createSubstation(network, "s", "s", Country.FR);

        // first voltage level
        VoltageLevel vl1 = Networks.createVoltageLevel(substation, "vl1", "vl1", TopologyKind.NODE_BREAKER, 380);
        Networks.createBusBarSection(vl1, "bbs1", "bbs1", 0, 1, 1);
        Networks.createLoad(vl1, "l", "l", "l", 0, ConnectablePosition.Direction.TOP, 2, 10, 10);
        Networks.createSwitch(vl1, "d", "d", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        Networks.createSwitch(vl1, "b", "b", SwitchKind.BREAKER, false, false, false, 1, 2);

        // second voltage level
        VoltageLevel vl2 = Networks.createVoltageLevel(substation, "vl2", "vl2", TopologyKind.NODE_BREAKER, 225);
        Networks.createBusBarSection(vl2, "bbs2", "bbs2", 0, 1, 1);

        // third voltage level
        VoltageLevel vl3 = Networks.createVoltageLevel(substation, "vl3", "vl3", TopologyKind.NODE_BREAKER, 63);
        Networks.createBusBarSection(vl3, "bbs3", "bbs3", 0, 1, 1);

        // 2WT between first and second voltage level
        Networks.createTwoWindingsTransformer(substation, "2WT", "2WT", 1, 1, 1, 1, 1, 1,
                3, 1, vl1.getId(), vl2.getId(),
                "2WT_1", 1, ConnectablePosition.Direction.TOP,
                "2WT_2", 0, ConnectablePosition.Direction.TOP);
        Networks.createSwitch(vl1, "d2WT_1", "d2WT_1", SwitchKind.DISCONNECTOR, false, false, true, 0, 4);
        Networks.createSwitch(vl1, "b2WT_1", "b2WT_1", SwitchKind.BREAKER, true, false, true, 3, 4);
        Networks.createSwitch(vl2, "d2WT_2", "d2WT_2", SwitchKind.DISCONNECTOR, false, false, true, 0, 2);
        Networks.createSwitch(vl2, "b2WT_2", "b2WT_2", SwitchKind.BREAKER, true, true, true, 1, 2);

        // 3WT between the 3 voltage levels
        Networks.createThreeWindingsTransformer(substation, "3WT", "3WT", vl1.getId(), vl2.getId(), vl3.getId(),
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                5, 3, 1,
                "3WT_1", 2, ConnectablePosition.Direction.TOP,
                "3WT_2", 1, ConnectablePosition.Direction.TOP,
                "3WT_3", 0, ConnectablePosition.Direction.TOP);
        Networks.createSwitch(vl1, "d3WT_1", "d3WT_1", SwitchKind.DISCONNECTOR, false, false, true, 0, 6);
        Networks.createSwitch(vl1, "b3WT_1", "b3WT_1", SwitchKind.BREAKER, true, false, true, 5, 6);
        Networks.createSwitch(vl2, "d3WT_2", "d3WT_2", SwitchKind.DISCONNECTOR, false, false, true, 0, 4);
        Networks.createSwitch(vl2, "b3WT_2", "b3WT_2", SwitchKind.BREAKER, true, false, true, 3, 4);
        Networks.createSwitch(vl3, "d3WT_3", "d3WT_3", SwitchKind.DISCONNECTOR, false, false, true, 0, 2);
        Networks.createSwitch(vl3, "b3WT_3", "b3WT_3", SwitchKind.BREAKER, true, false, true, 1, 2);
        return network;
    }

}
