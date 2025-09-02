/**
 * Copyright (c) 2019-2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.layout.PositionVoltageLevelLayoutFactory;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestCaseFictitiousSwitchNode extends AbstractTestCaseIidm {

    private PositionVoltageLevelLayoutFactory factory;
    VoltageLevel vl;

    @BeforeEach
    public void setUp() {

        network = Network.create("testFictitiousSwitchNode", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = Networks.createSubstation(network, "s", "s", Country.FR);
        vl = Networks.createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380);
        Networks.createBusBarSection(vl, "bbs", "bbs", 0, 1, 1);
        Networks.createLoad(vl, "l", "l", "l", 0, ConnectablePosition.Direction.TOP, 2, 10, 10);
        Networks.createSwitch(vl, "d", "d", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        Networks.createSwitch(vl, "b", "b", SwitchKind.BREAKER, false, false, true, 1, 2);

        factory = new PositionVoltageLevelLayoutFactory();
    }

    @Test
    void keepFictitiousSwitchNodesTest() {

        // build graph
        VoltageLevelGraph g1 = graphBuilder.buildVoltageLevelGraph("vl");

        // Run layout
        factory.create(g1).run(layoutParameters);

        // write svg and compare to reference
        assertEquals(toString("/TestCaseKeepFictitiousSwitchNode.svg"), toSVG(g1, "/TestCaseKeepFictitiousSwitchNode.svg", componentLibrary, layoutParameters, svgParameters, getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider()));
    }

    @Test
    void removeFictitiousSwitchNodesTest() {

        layoutParameters.setRemoveFictitiousSwitchNodes(true);

        // build graph
        VoltageLevelGraph g2 = graphBuilder.buildVoltageLevelGraph("vl");

        // Run layout
        factory.create(g2).run(layoutParameters);

        // write svg and compare to reference
        assertEquals(toString("/TestCaseRemoveFictitiousSwitchNode.svg"), toSVG(g2, "/TestCaseRemoveFictitiousSwitchNode.svg", componentLibrary, layoutParameters, svgParameters, getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider()));
    }

}
