/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.RawGraphBuilder;
import com.powsybl.sld.model.*;
import com.powsybl.sld.svg.BasicStyleProvider;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class TestInsertFictitiousNodesAtFeeder extends AbstractTestCaseRaw {

    @Before
    public void setUp() {
    }

    @Test
    public void testFeederOnBus() {
        RawGraphBuilder.VoltageLevelBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 400);
        BusNode bbs = vlBuilder.createBusBarSection("bbs", 1, 1);
        FeederLineNode feederLineNode = vlBuilder.createFeederLineNode("line", "otherVl", FeederWithSideNode.Side.ONE, 0, null);
        vlBuilder.connectNode(bbs, feederLineNode);
        VoltageLevelGraph g = rawGraphBuilder.buildOrphanVoltageLevelGraph("vl");
        layoutParameters.setAdaptCellHeightToContent(true);
        voltageLevelGraphLayout(g);
        assertEquals(toString("/TestFeederOnBus.svg"), toSVG(g, "/TestFeederOnBus.svg", getRawLabelProvider(g), new BasicStyleProvider()));
    }

    @Test
    public void testFeederOnBusDisconnector() {
        RawGraphBuilder.VoltageLevelBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 400);
        BusNode bbs = vlBuilder.createBusBarSection("bbs", 1, 1);
        SwitchNode busDisconnector = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "busDisconnector", false, false);
        FeederLineNode feederLineNode = vlBuilder.createFeederLineNode("line", "otherVl", FeederWithSideNode.Side.ONE, 0, BusCell.Direction.BOTTOM);
        vlBuilder.connectNode(bbs, busDisconnector);
        vlBuilder.connectNode(busDisconnector, feederLineNode);
        VoltageLevelGraph g = rawGraphBuilder.buildOrphanVoltageLevelGraph("vl");
        layoutParameters.setAdaptCellHeightToContent(true);
        voltageLevelGraphLayout(g);
        assertEquals(toString("/TestFeederOnBusDisconnector.svg"), toSVG(g, "/TestFeederOnBusDisconnector.svg", getRawLabelProvider(g), new BasicStyleProvider()));
    }
}
