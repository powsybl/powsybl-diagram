/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.builders.VoltageLevelRawBuilder;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.ConnectivityNode;
import com.powsybl.sld.model.nodes.SwitchNode;
import com.powsybl.sld.svg.BasicStyleProvider;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */

public class TestCaseComplexCoupling extends AbstractTestCaseRaw {

    @Before
    public void setUp() {
        VoltageLevelRawBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 380);
        BusNode bbs1 = vlBuilder.createBusBarSection("bbs1", 1, 1);
        BusNode bbs2 = vlBuilder.createBusBarSection("bbs2", 2, 1);
        SwitchNode d1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d1", false, false);
        ConnectivityNode f1 = vlBuilder.createConnectivityNode("f1");
        SwitchNode bA = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bA", false, false);
        SwitchNode bB = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bB", false, false);
        SwitchNode bC = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bC", false, false);
        SwitchNode bD = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bD", false, false);
        ConnectivityNode f2 = vlBuilder.createConnectivityNode("f2");
        SwitchNode d2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d2", false, false);
        vlBuilder.connectNode(bbs1, d1);
        vlBuilder.connectNode(d1, f1);
        vlBuilder.connectNode(f1, bA);
        vlBuilder.connectNode(f2, bA);
        vlBuilder.connectNode(f1, bB);
        vlBuilder.connectNode(f2, bB);
        vlBuilder.connectNode(f1, bC);
        vlBuilder.connectNode(f2, bC);
        vlBuilder.connectNode(f2, bD);
        vlBuilder.connectNode(d2, bD);
        vlBuilder.connectNode(d2, bbs2);
    }

    @Test
    public void test() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl");
        layoutParameters.setAdaptCellHeightToContent(true);
        voltageLevelGraphLayout(g);

        assertEquals(toString("/TestCaseComplexCoupling.svg"), toSVG(g, "/TestCaseComplexCoupling.svg", getRawLabelProvider(g), new BasicStyleProvider()));
    }
}
