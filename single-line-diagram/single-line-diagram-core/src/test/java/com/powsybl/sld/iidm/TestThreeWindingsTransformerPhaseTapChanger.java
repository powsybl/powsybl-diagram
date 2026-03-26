/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Leg;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Giovanni Ferrari {@literal <giovanni.ferrari at soft.it>}
 */
class TestThreeWindingsTransformerPhaseTapChanger extends AbstractTestCaseIidm {

    @BeforeEach
    public void setUp() {
        network = ThreeWindingsTransformerNetworkFactory.create();
        network.getThreeWindingsTransformer("3WT").getLeg1().getTerminal().setP(-2800.0).setQ(400.0);
        network.getThreeWindingsTransformer("3WT").getLeg2().getTerminal().setP(1400.0).setQ(400.0);
        network.getThreeWindingsTransformer("3WT").getLeg3().getTerminal().setP(1400.0).setQ(400.0);
        graphBuilder = new NetworkGraphBuilder(network);
    }

    @Test
    void testVoltageLevelGraph3wtWithPhaseTagChangerOnLeg1() {
        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("3WT");
        addPhaseTapChanger(twt.getLeg1());
        testSvg("/Test3WTPhaseTapChangerVoltageLevel1Leg.svg");
    }

    @Test
    void testVoltageLevelGraph3wtWithPhaseTagChangerOnLeg2() {
        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("3WT");
        addPhaseTapChanger(twt.getLeg2());
        testSvg("/Test3WTPhaseTapChangerVoltageLevel1Leg2.svg");
    }

    @Test
    void testVoltageLevelGraph3wtWithPhaseTagChangerOnLeg3() {
        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("3WT");
        addPhaseTapChanger(twt.getLeg3());
        testSvg("/Test3WTPhaseTapChangerVoltageLevel1Leg3.svg");
    }

    @Test
    void testVoltageLevelGraph3wtWithPhaseTagChangerOnLegs1and2() {
        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("3WT");
        addPhaseTapChanger(twt.getLeg1());
        addPhaseTapChanger(twt.getLeg2());
        testSvg("/Test3WTPhaseTapChangerVoltageLevel2Legs.svg");
    }

    @Test
    void testVoltageLevelGraph3wtWithPhaseTagChangerOnLegs2and3() {
        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("3WT");
        addPhaseTapChanger(twt.getLeg2());
        addPhaseTapChanger(twt.getLeg3());
        testSvg("/Test3WTPhaseTapChangerVoltageLevel2Legs23.svg");
    }

    @Test
    void testVoltageLevelGraph3wtWithPhaseTagChangerOnLegs1and3() {
        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("3WT");
        addPhaseTapChanger(twt.getLeg1());
        addPhaseTapChanger(twt.getLeg3());
        testSvg("/Test3WTPhaseTapChangerVoltageLevel2Legs13.svg");
    }

    @Test
    void testVoltageLevelGraph3wtWithPhaseTagChangerOnAllLegs() {
        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("3WT");
        addPhaseTapChanger(twt.getLeg1());
        addPhaseTapChanger(twt.getLeg2());
        addPhaseTapChanger(twt.getLeg3());
        testSvg("/Test3WTPhaseTapChangerVoltageLevel3Legs.svg");
    }

    private void addPhaseTapChanger(Leg leg) {
        leg.newPhaseTapChanger()
                .setTapPosition(1)
                .setRegulationTerminal(leg.getTerminal())
                .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                .setRegulating(false)
                .setRegulationValue(200)
                .beginStep()
                .setAlpha(-20.0)
                .setRho(1.0)
                .setR(0.0)
                .setX(0.0)
                .setG(0.0)
                .setB(0.0)
                .endStep()
                .beginStep()
                .setAlpha(0.0)
                .setRho(1.0)
                .setR(0.0)
                .setX(0.0)
                .setG(0.0)
                .setB(0.0)
                .endStep()
                .beginStep()
                .setAlpha(20.0)
                .setRho(1.0)
                .setR(0.0)
                .setX(0.0)
                .setG(0.0)
                .setB(0.0)
                .endStep()
                .add();
    }

    private void testSvg(String svgName) {
        graphBuilder = new NetworkGraphBuilder(network);
        // Build substation graph and run layout
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph("VL_132");
        voltageLevelGraphLayout(g);

        assertEquals(toString(svgName), toSVG(g, svgName));
    }
}
