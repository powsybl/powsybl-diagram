/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.layout.PositionVoltageLevelLayoutFactory;
import com.powsybl.sld.layout.PositionVoltageLevelLayoutFactoryParameters;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
class TestCaseGraphAdaptCellHeightToContent extends AbstractTestCaseIidm {

    @BeforeEach
    public void setUp() {
        layoutParameters.setExternCellHeight(200);

        network = Network.create("testCaseGraphAdaptCellHeightToContent", "test");
        graphBuilder = new NetworkGraphBuilder(network);

        substation = Networks.createSubstation(network, "subst", "subst", Country.FR);

        vl = Networks.createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380);

        Networks.createBusBarSection(vl, "bbs1", "bbs1", 0, 1, 1);
        Networks.createBusBarSection(vl, "bbs2", "bbs2", 1, 2, 1);

        // coupling (intern cell)
        Networks.createSwitch(vl, "d1", "d1", SwitchKind.DISCONNECTOR, false, false, false, 0, 2);
        Networks.createSwitch(vl, "b1", "b1", SwitchKind.BREAKER, false, false, false, 2, 3);
        Networks.createSwitch(vl, "d2", "d2", SwitchKind.DISCONNECTOR, false, false, false, 3, 1);

        // load (huge feeder cell with serial blocks)
        Networks.createSwitch(vl, "d3", "d3", SwitchKind.DISCONNECTOR, false, false, false, 0, 4);
        Networks.createSwitch(vl, "b2", "b2", SwitchKind.BREAKER, false, true, false, 4, 5);
        Networks.createSwitch(vl, "b3", "b3", SwitchKind.BREAKER, false, false, false, 5, 6);
        Networks.createSwitch(vl, "b4", "b4", SwitchKind.BREAKER, false, true, false, 6, 7);
        Networks.createSwitch(vl, "b5", "b5", SwitchKind.BREAKER, false, false, false, 7, 8);
        Networks.createSwitch(vl, "b6", "b6", SwitchKind.BREAKER, false, false, false, 8, 9);
        Networks.createSwitch(vl, "b7", "b7", SwitchKind.BREAKER, false, true, false, 9, 10);
        Networks.createSwitch(vl, "b8", "b8", SwitchKind.BREAKER, false, true, false, 10, 11);
        Networks.createSwitch(vl, "b9", "b9", SwitchKind.BREAKER, false, false, false, 11, 12);
        Networks.createSwitch(vl, "b10", "b10", SwitchKind.BREAKER, false, false, false, 12, 13);
        Networks.createSwitch(vl, "b11", "b11", SwitchKind.BREAKER, false, false, false, 13, 14);
        Networks.createSwitch(vl, "b12", "b12", SwitchKind.BREAKER, false, false, false, 14, 15);
        Networks.createLoad(vl, "load1", "load1", "load1", 0, ConnectablePosition.Direction.TOP, 15, 10, 10);

        // generator (small feeder cell with serial blocks)
        Networks.createSwitch(vl, "d4", "d4", SwitchKind.DISCONNECTOR, false, true, false, 1, 16);
        Networks.createSwitch(vl, "b13", "b13", SwitchKind.BREAKER, true, false, false, 16, 17);
        Networks.createGenerator(vl, "gen1", "gen1", "gen1", 3, ConnectablePosition.Direction.BOTTOM, 17, 0, 20, false, 10, 10);

        // load (small feeder cell with parallel blocks)
        Networks.createSwitch(vl, "d5", "d5", SwitchKind.DISCONNECTOR, false, true, false, 0, 18);
        Networks.createSwitch(vl, "d6", "d6", SwitchKind.DISCONNECTOR, false, true, false, 1, 19);
        Networks.createSwitch(vl, "b14", "b14", SwitchKind.BREAKER, true, false, false, 18, 20);
        Networks.createSwitch(vl, "b15", "b15", SwitchKind.BREAKER, true, false, false, 19, 20);
        Networks.createSwitch(vl, "b16", "b16", SwitchKind.BREAKER, true, false, false, 20, 21);
        Networks.createLoad(vl, "load2", "load2", "load2", 1, ConnectablePosition.Direction.TOP, 21, 10, 10);

        // undefined block
        Networks.createSwitch(vl, "d7", "d7", SwitchKind.DISCONNECTOR, false, true, false, 0, 22);
        Networks.createSwitch(vl, "b17", "b17", SwitchKind.BREAKER, true, false, false, 22, 23);
        Networks.createSwitch(vl, "b18", "b18", SwitchKind.BREAKER, true, false, false, 23, 24);
        Networks.createSwitch(vl, "b19", "b19", SwitchKind.BREAKER, true, false, false, 23, 24);
        Networks.createSwitch(vl, "b20", "b20", SwitchKind.BREAKER, true, false, false, 24, 25);
        Networks.createGenerator(vl, "gen2", "gen2", "gen2", 2, ConnectablePosition.Direction.BOTTOM, 25, 0, 20, false, 10, 10);
    }

    @Test
    void testHeightFixed() {
        // layout parameters with extern cell height fixed
        layoutParameters.setAdaptCellHeightToContent(false);

        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        PositionVoltageLevelLayoutFactoryParameters positionVoltageLevelLayoutFactoryParameters = new PositionVoltageLevelLayoutFactoryParameters()
                .setFeederStacked(false)
                .setRemoveUnnecessaryFictitiousNodes(false);
        new PositionVoltageLevelLayoutFactory(positionVoltageLevelLayoutFactoryParameters)
                .create(g)
                .run(layoutParameters);

        assertEquals(toString("/TestCaseGraphExternCellHeightFixed.json"), toJson(g, "/TestCaseGraphExternCellHeightFixed.json"));
    }

    @Test
    void testAdaptHeight() {
        // layout parameters with adapt cell height to content
        layoutParameters.setAdaptCellHeightToContent(true);

        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        new PositionVoltageLevelLayoutFactory(new PositionVoltageLevelLayoutFactoryParameters().setRemoveUnnecessaryFictitiousNodes(false))
                .create(g)
                .run(layoutParameters);

        assertEquals(toString("/TestCaseGraphAdaptCellHeightToContent.json"), toJson(g, "/TestCaseGraphAdaptCellHeightToContent.json"));
    }
}
