/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.Config;
import com.powsybl.sld.ConfigBuilder;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.iidm.AbstractTestCaseIidm;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ConvergenceComponentLibrary;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.FeederNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.powsybl.sld.library.ComponentTypeName.ARROW_ACTIVE;
import static com.powsybl.sld.library.ComponentTypeName.ARROW_REACTIVE;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Thomas Adam <tadam at silicom.fr>
 */
class FeederInfoProviderTest extends AbstractTestCaseIidm {

    private VoltageLevel vl2;

    @BeforeEach
    public void setUp() {
        network = Networks.createNetworkWithSvcVscScDl();
        graphBuilder = new NetworkGraphBuilder(network);
        vl = network.getVoltageLevel("vl");
        vl2 = network.getVoltageLevel("vl2");
    }

    @Test
    void test() {
        ComponentLibrary componentLibrary = new ConvergenceComponentLibrary();
        svgParameters.setFeederInfoSymmetry(true);

        // build first voltage level graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());
        voltageLevelGraphLayout(g); // to have cell orientations (bottom / up)
        Config config = new ConfigBuilder(network)
                .withComponentLibrary(componentLibrary)
                .withLayoutParameters(layoutParameters)
                .withSvgParameters(svgParameters)
                .build();
        assertEquals(toString("/feederInfoTest.svg"), toSVG(g, "/feederInfoTest.svg", config));

        Network network2 = Network.create("testCase2", "test2");
        DefaultLabelProvider wrongLabelProvider = new DefaultLabelProvider(network2, componentLibrary, layoutParameters, svgParameters);
        List<FeederInfo> feederInfos = wrongLabelProvider.getFeederInfos((FeederNode) g.getNode("svc"));
        assertTrue(feederInfos.isEmpty());

        DefaultLabelProvider labelProvider = new DefaultLabelProvider(network, componentLibrary, layoutParameters, svgParameters);
        List<FeederInfo> feederInfos1 = labelProvider.getFeederInfos((FeederNode) g.getNode("svc"));
        assertEquals(2, feederInfos1.size());
        assertEquals(ARROW_ACTIVE, feederInfos1.get(0).getComponentType());
        assertEquals(ARROW_REACTIVE, feederInfos1.get(1).getComponentType());
        assertTrue(feederInfos1.get(0).getRightLabel().isPresent());
        assertTrue(feederInfos1.get(1).getRightLabel().isPresent());
        assertFalse(feederInfos1.get(0).getLeftLabel().isPresent());
        assertFalse(feederInfos1.get(1).getLeftLabel().isPresent());

        List<FeederInfo> feederInfosVsc1 = labelProvider.getFeederInfos((FeederNode) g.getNode("vsc"));
        assertEquals(2, feederInfosVsc1.size());
        assertEquals(ARROW_ACTIVE, feederInfosVsc1.get(0).getComponentType());
        assertEquals(ARROW_REACTIVE, feederInfosVsc1.get(1).getComponentType());
        assertTrue(feederInfosVsc1.get(0).getRightLabel().isPresent());
        assertTrue(feederInfosVsc1.get(1).getRightLabel().isPresent());
        assertFalse(feederInfosVsc1.get(0).getLeftLabel().isPresent());
        assertFalse(feederInfosVsc1.get(1).getLeftLabel().isPresent());

        // build second voltage level graph
        VoltageLevelGraph g2 = graphBuilder.buildVoltageLevelGraph(vl2.getId());
        voltageLevelGraphLayout(g2); // to have cell orientations (bottom / up)

        List<FeederInfo> feederInfosVsc2 = labelProvider.getFeederInfos((FeederNode) g2.getNode("vsc2"));
        assertEquals(2, feederInfosVsc2.size());
        assertEquals(ARROW_ACTIVE, feederInfosVsc2.get(0).getComponentType());
        assertEquals(ARROW_REACTIVE, feederInfosVsc2.get(1).getComponentType());
        assertTrue(feederInfosVsc2.get(0).getRightLabel().isPresent());
        assertTrue(feederInfosVsc2.get(1).getRightLabel().isPresent());
        assertFalse(feederInfosVsc2.get(0).getLeftLabel().isPresent());
        assertFalse(feederInfosVsc2.get(1).getLeftLabel().isPresent());

        List<FeederInfo> feederInfos3 = labelProvider.getFeederInfos((FeederNode) g.getNode("C1"));
        assertEquals(2, feederInfos3.size());
        assertEquals(ARROW_ACTIVE, feederInfos3.get(0).getComponentType());
        assertEquals(ARROW_REACTIVE, feederInfos3.get(1).getComponentType());
        assertTrue(feederInfos3.get(0).getRightLabel().isPresent());
        assertTrue(feederInfos3.get(1).getRightLabel().isPresent());
        assertFalse(feederInfos3.get(0).getLeftLabel().isPresent());
        assertFalse(feederInfos3.get(1).getLeftLabel().isPresent());

        List<FeederInfo> feederInfos4 = labelProvider.getFeederInfos((FeederNode) g.getNode("dl1"));
        assertEquals(2, feederInfos4.size());
        assertEquals(ARROW_ACTIVE, feederInfos4.get(0).getComponentType());
        assertEquals(ARROW_REACTIVE, feederInfos4.get(1).getComponentType());
        assertTrue(feederInfos4.get(0).getRightLabel().isPresent());
        assertTrue(feederInfos4.get(1).getRightLabel().isPresent());
        assertFalse(feederInfos4.get(0).getLeftLabel().isPresent());
        assertFalse(feederInfos4.get(1).getLeftLabel().isPresent());

        // Reverse order
        svgParameters.setFeederInfoSymmetry(false);
        List<FeederInfo> feederInfos5 = labelProvider.getFeederInfos((FeederNode) g.getNode("dl1"));
        assertEquals(ARROW_REACTIVE, feederInfos5.get(0).getComponentType());
        assertEquals(ARROW_ACTIVE, feederInfos5.get(1).getComponentType());
    }
}
