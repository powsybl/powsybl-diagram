/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.build.iidm.VoltageLevelFilter;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.NominalVoltageStyleProvider;
import com.powsybl.nad.svg.iidm.TopologicalStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class ThreeWindingTransformerTest extends AbstractTest {

    private StyleProvider styleProvider;

    @BeforeEach
    void setup() {
        setLayoutParameters(new LayoutParameters());
        setSvgParameters(new SvgParameters()
                .setInsertNameDesc(true)
                .setSvgWidthAndHeightAdded(true)
                .setFixedWidth(800));
    }

    @Override
    protected StyleProvider getStyleProvider(Network network) {
        return styleProvider != null ? styleProvider : new NominalVoltageStyleProvider(network);
    }

    @Override
    protected LabelProvider getLabelProvider(Network network) {
        return new DefaultLabelProvider(network, getSvgParameters());
    }

    @Test
    void test3wt() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        assertSvgEquals("/3wt.svg", network);
    }

    @Test
    void testDisconnected3wt() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        network.getThreeWindingsTransformer("3WT").getTerminal(ThreeSides.TWO).disconnect();
        network.getLoad("LOAD_33").remove();
        assertSvgEquals("/3wt_disconnected.svg", network);
    }

    @Test
    void testDisconnected3wtTopologicalStyle() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        network.getThreeWindingsTransformer("3WT").getTerminal(ThreeSides.TWO).disconnect();
        network.getLoad("LOAD_33").remove();
        styleProvider = new TopologicalStyleProvider(network);
        assertSvgEquals("/3wt_disconnected_topological.svg", network);
    }

    @Test
    void testPartial3wt() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        VoltageLevelFilter filter = VoltageLevelFilter.createVoltageLevelsFilter(network, Collections.singletonList("VL_11"));
        assertSvgEquals("/3wt_partial.svg", network, filter);
    }

    @Test
    void test3wtPhaseShift() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        VoltageLevelFilter filter = VoltageLevelFilter.createVoltageLevelsFilter(network,
                Collections.singletonList("VL_11"));

        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("3WT");

        twt.getLeg1().newPhaseTapChanger()
                .setTapPosition(1)
                .setRegulationTerminal(twt.getLeg1().getTerminal())
                .setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP)
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
        twt.getLeg2().newPhaseTapChanger()
                .setTapPosition(1)
                .setRegulationTerminal(twt.getLeg2().getTerminal())
                .setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP)
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
        twt.getLeg3().newPhaseTapChanger()
                .setTapPosition(1)
                .setRegulationTerminal(twt.getLeg3().getTerminal())
                .setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP)
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
        assertSvgEquals("/3wt_pst.svg", network, filter);
    }

}
