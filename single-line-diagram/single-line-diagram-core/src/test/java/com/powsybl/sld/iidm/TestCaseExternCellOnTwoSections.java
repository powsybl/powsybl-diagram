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
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <PRE>
 *         l
 *         |
 *     ___ b ____
 *    |          |
 *    |          |
 * --d1--  x  --d2--
 *  bbs1  dc   bbs2
 * </PRE>
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class TestCaseExternCellOnTwoSections extends AbstractTestCaseIidm {

    @BeforeEach
    public void setUp() {
        network = Network.create("test", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = Networks.createSubstation(network, "s", "s", Country.FR);
        vl = Networks.createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 225);
        Networks.createBusBarSection(vl, "bbs1", "bbs1", 0, 1, 1);
        Networks.createBusBarSection(vl, "bbs2", "bbs2", 1, 1, 2);
        Networks.createLoad(vl, "l", "l", "l", 0, ConnectablePosition.Direction.TOP, 2, 10, 10);
        Networks.createSwitch(vl, "d1", "d1", SwitchKind.DISCONNECTOR, false, false, false, 0, 3);
        Networks.createSwitch(vl, "d2", "d2", SwitchKind.DISCONNECTOR, false, false, false, 1, 3);
        Networks.createSwitch(vl, "dc", "dc", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        Networks.createSwitch(vl, "b", "b", SwitchKind.BREAKER, false, false, false, 3, 2);
    }

    @Test
    void test() {
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());
        voltageLevelGraphLayout(g);
        assertEquals(toString("/TestCaseExternCellOnTwoSections.svg"), toSVG(g, "/TestCaseExternCellOnTwoSections.svg"));
    }
}
