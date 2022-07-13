/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.builders.VoltageLevelRawBuilder;
import com.powsybl.sld.library.ResourcesComponentLibrary;
import com.powsybl.sld.model.graphs.NodeFactory;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.model.nodes.SwitchNode;
import com.powsybl.sld.svg.BasicStyleProvider;
import org.junit.Before;
import org.junit.Test;

import static com.powsybl.sld.model.coordinate.Direction.TOP;
import static org.junit.Assert.assertEquals;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public class TestAddExternalComponent extends AbstractTestCaseRaw {
    private static final String PACMAN = "PACMAN";

    @Before
    public void setUp() {
        VoltageLevelRawBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 380);
        BusNode bbs = vlBuilder.createBusBarSection("bbs", 1, 1);
        FeederNode load = vlBuilder.createLoad("l", 0, TOP);
        SwitchNode d = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d", false, false);
        Node pacMan = NodeFactory.createNode(vlBuilder.getGraph(), Node.NodeType.INTERNAL, "pacMan", null, null, PACMAN, false);
        SwitchNode b = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b", false, false);
        vlBuilder.connectNode(bbs, d);
        vlBuilder.connectNode(d,b);
        vlBuilder.connectNode(b, pacMan);
        vlBuilder.connectNode(pacMan, load);
    }

    @Test
    public void test() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl");
        voltageLevelGraphLayout(g);
        componentLibrary = new ResourcesComponentLibrary("pacman", "/ConvergenceLibrary", "/PacmanLibrary");
        assertEquals(toString("/TestPacman.svg"),
                toSVG(g, "/TestPacman.svg", getRawLabelProvider(g), new BasicStyleProvider()));
    }
}
