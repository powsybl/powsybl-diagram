/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.iidm;

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

}
