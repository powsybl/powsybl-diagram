/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.iidm.network.Network;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.iidm.AbstractTestCaseIidm;
import com.powsybl.sld.iidm.CreateNetworksUtil;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ConvergenceComponentLibrary;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.FeederNode;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.powsybl.sld.library.ComponentTypeName.ARROW_ACTIVE;
import static com.powsybl.sld.library.ComponentTypeName.ARROW_REACTIVE;
import static org.junit.Assert.*;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class FeederInfoProviderTest extends AbstractTestCaseIidm {

    @Before
    public void setUp() {
        network = CreateNetworksUtil.createNetworkWithSvcVscScDl();
        graphBuilder = new NetworkGraphBuilder(network);
        vl = network.getVoltageLevel("vl");
    }

    @Test
    public void test() {
        ComponentLibrary componentLibrary = new ConvergenceComponentLibrary();
        layoutParameters.setFeederInfoSymmetry(true);
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());
        voltageLevelGraphLayout(g); // to have cell orientations (bottom / up)
        assertEquals(toString("/feederInfoTest.svg"), toSVG(g, "/feederInfoTest.svg"));

        Network network2 = Network.create("testCase2", "test2");
        DefaultDiagramLabelProvider wrongLabelProvider = new DefaultDiagramLabelProvider(network2, componentLibrary, layoutParameters);
        List<FeederInfo> feederInfos = wrongLabelProvider.getFeederInfos((FeederNode) g.getNode("svc"));
        assertTrue(feederInfos.isEmpty());

        DefaultDiagramLabelProvider labelProvider = new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters);
        List<FeederInfo> feederInfos1 = labelProvider.getFeederInfos((FeederNode) g.getNode("svc"));
        assertEquals(2, feederInfos1.size());
        assertEquals(ARROW_ACTIVE, feederInfos1.get(0).getComponentType());
        assertEquals(ARROW_REACTIVE, feederInfos1.get(1).getComponentType());
        assertTrue(feederInfos1.get(0).getRightLabel().isPresent());
        assertTrue(feederInfos1.get(1).getRightLabel().isPresent());
        assertFalse(feederInfos1.get(0).getLeftLabel().isPresent());
        assertFalse(feederInfos1.get(1).getLeftLabel().isPresent());

        List<FeederInfo> feederInfos2 = labelProvider.getFeederInfos((FeederNode) g.getNode("vsc"));
        assertEquals(2, feederInfos2.size());
        assertEquals(ARROW_ACTIVE, feederInfos2.get(0).getComponentType());
        assertEquals(ARROW_REACTIVE, feederInfos2.get(1).getComponentType());
        assertTrue(feederInfos2.get(0).getRightLabel().isPresent());
        assertTrue(feederInfos2.get(1).getRightLabel().isPresent());
        assertFalse(feederInfos2.get(0).getLeftLabel().isPresent());
        assertFalse(feederInfos2.get(1).getLeftLabel().isPresent());

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
        layoutParameters.setFeederInfoSymmetry(false);
        List<FeederInfo> feederInfos5 = labelProvider.getFeederInfos((FeederNode) g.getNode("dl1"));
        assertEquals(ARROW_REACTIVE, feederInfos5.get(0).getComponentType());
        assertEquals(ARROW_ACTIVE, feederInfos5.get(1).getComponentType());
    }
}
