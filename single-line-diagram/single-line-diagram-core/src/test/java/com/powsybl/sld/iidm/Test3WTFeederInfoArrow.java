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
import com.powsybl.sld.svg.DefaultLabelProvider;
import com.powsybl.sld.svg.styles.BasicStyleProvider;
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
        network.getThreeWindingsTransformer("3WT").getLeg1().getTerminal().setP(-2800.0).setQ(400.0);
        network.getThreeWindingsTransformer("3WT").getLeg2().getTerminal().setP(1400.0).setQ(400.0);
        network.getThreeWindingsTransformer("3WT").getLeg3().getTerminal().setP(1400.0).setQ(400.0);
        graphBuilder = new NetworkGraphBuilder(network);
    }

    @Test
    void testVoltageLevelGraph() {

        // Build substation graph and run layout
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph("VL_132");
        voltageLevelGraphLayout(g);

        assertEquals(toString("/Test3WTFeederInfoArrowVoltageLevel.svg"), toSVG(g, "/Test3WTFeederInfoArrowVoltageLevel.svg"));

    }

    @Test
    void testVoltageLevelGraphWithFeederInfos() {

        layoutParameters.setSpaceForFeederInfos(90);
        DefaultLabelProvider labelProvider = getDefaultDiagramLabelProvider();
        labelProvider.setDisplayCurrent(true);
        labelProvider.setDisplayArrowForCurrent(true);
        labelProvider.setDisplayPermanentLimitPercentage(true);

        // Build substation graph and run layout
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph("VL_132");
        voltageLevelGraphLayout(g);

        assertEquals(toString("/Test3WTFeederInfoArrowVoltageLevelWithAllPossibleFeederInfos.svg"), toSVG(g, "/Test3WTFeederInfoArrowVoltageLevelWithAllPossibleFeederInfos.svg", componentLibrary, layoutParameters, svgParameters, labelProvider, new BasicStyleProvider(), getDefaultSVGLegendWriter()));
    }

    @Test
    void testSubstationGraph() {

        // Build substation graph and run layout
        SubstationGraph g = graphBuilder.buildSubstationGraph("SUBSTATION");
        substationGraphLayout(g);

        assertEquals(toString("/Test3WTFeederInfoArrowSubstation.svg"), toSVG(g, "/Test3WTFeederInfoArrowSubstation.svg"));

    }
}
