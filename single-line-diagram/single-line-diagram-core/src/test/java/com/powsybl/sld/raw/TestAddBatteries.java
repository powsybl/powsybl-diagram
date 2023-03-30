/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.builders.VoltageLevelRawBuilder;
import com.powsybl.sld.library.FlatDesignLibrary;
import com.powsybl.sld.library.ResourcesComponentLibrary;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.SwitchNode;
import com.powsybl.sld.svg.BasicStyleProvider;
import org.junit.Before;
import org.junit.Test;

import static com.powsybl.sld.model.coordinate.Direction.TOP;
import static org.junit.Assert.assertEquals;

/**
 * @author Sophie Frasnedo <sophie.frasnedo at rte-france.com>
 */
public class TestAddBatteries extends AbstractTestCaseRaw {

    @Before
    public void setUp() {
        VoltageLevelRawBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 380);
        BusNode bbs = vlBuilder.createBusBarSection("bbs", 1, 1);
        FeederNode battery1 = vlBuilder.createBattery("batt1", 0, TOP);
        SwitchNode d1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d1", false, false);
        SwitchNode b1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b1", false, false);
        FeederNode battery2 = vlBuilder.createBattery("batt2");
        SwitchNode d2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d2", false, false);
        SwitchNode b2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b2", false, false);
        vlBuilder.connectNode(bbs, b1);
        vlBuilder.connectNode(b1, d1);
        vlBuilder.connectNode(d1, battery1);
        vlBuilder.connectNode(bbs, b2);
        vlBuilder.connectNode(b2, d2);
        vlBuilder.connectNode(d2, battery2);
    }

    @Override
    protected ResourcesComponentLibrary getResourcesComponentLibrary() {
        return new FlatDesignLibrary();
    }

    @Test
    public void test() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl");
        voltageLevelGraphLayout(g);
        assertEquals(toString("/TestBatteriesRaw.svg"),
                toSVG(g, "/TestBatteriesRaw.svg", getRawLabelProvider(), new BasicStyleProvider()));
    }
}
