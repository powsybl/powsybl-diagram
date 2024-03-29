/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.builders.VoltageLevelRawBuilder;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.NodeSide;
import com.powsybl.sld.model.nodes.SwitchNode;
import com.powsybl.sld.svg.styles.BasicStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.powsybl.sld.model.coordinate.Direction.BOTTOM;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * testFeederOnBus
 * <PRE>
 * line
 * |
 * ------ bbs
 * </PRE>
 *
 * testFeederOnBusDisconnector
 * <PRE>
 * ------ bbs
 * |
 * d1
 * |
 * line
 * </PRE>
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class TestInsertFictitiousNodesAtFeeder extends AbstractTestCaseRaw {

    @BeforeEach
    public void setUp() {
    }

    @Test
    void testFeederOnBus() {
        VoltageLevelRawBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 400);
        BusNode bbs = vlBuilder.createBusBarSection("bbs", 1, 1);
        FeederNode feederLineNode = vlBuilder.createFeederLineNode("line", "otherVl", NodeSide.ONE, 0, null);
        vlBuilder.connectNode(bbs, feederLineNode);
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl");
        voltageLevelGraphLayout(g);

        assertEquals(toString("/TestFeederOnBus.svg"), toSVG(g, "/TestFeederOnBus.svg", componentLibrary, layoutParameters, svgParameters, getLabelRawProvider(), new BasicStyleProvider()));
    }

    @Test
    void testFeederOnBusDisconnector() {
        VoltageLevelRawBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 400);
        BusNode bbs = vlBuilder.createBusBarSection("bbs", 1, 1);
        SwitchNode busDisconnector = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "busDisconnector", false, false);
        FeederNode feederLineNode = vlBuilder.createFeederLineNode("line", "otherVl", NodeSide.ONE, 0, BOTTOM);
        vlBuilder.connectNode(bbs, busDisconnector);
        vlBuilder.connectNode(busDisconnector, feederLineNode);
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl");
        voltageLevelGraphLayout(g);

        assertEquals(toString("/TestFeederOnBusDisconnector.svg"), toSVG(g, "/TestFeederOnBusDisconnector.svg", componentLibrary, layoutParameters, svgParameters, getLabelRawProvider(), new BasicStyleProvider()));
    }
}
