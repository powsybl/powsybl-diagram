/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.svg.styles.iidm.TopologicalStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <pre>
 *     vl1  vl2
 *     |    |
 * L1  |    |  L2
 *     |    |
 *     --*---  Fictitious busbar section
 *       |
 *   L3  |
 *       |
 *       vl3
 *
 * </pre>
 *
 * @author Thomas Adam <tadam at silicom.fr>
 */
class TestCaseFictitiousBus extends AbstractTestCaseIidm {

    private VoltageLevel vl1;
    private VoltageLevel vl2;
    private VoltageLevel vl3;

    @BeforeEach
    public void setUp() {
        network = Network.create("testCase1", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = null;
        vl = network.newVoltageLevel()
                .setId("vl")
                .setNominalV(50)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        vl1 = network.newVoltageLevel()
                .setId("vl1")
                .setNominalV(10)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        vl2 = network.newVoltageLevel()
                .setId("vl2")
                .setNominalV(30)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        vl3 = network.newVoltageLevel()
                .setId("vl3")
                .setNominalV(10)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        Networks.createInternalConnection(vl, 1, 0);
        Networks.createInternalConnection(vl, 2, 0);
        Networks.createInternalConnection(vl, 3, 0);

        Networks.createLine(network, "L1", "L1", 1.0, 1.0, 1.0, 0.0, 0.0, 0.0,
                1, 10, vl.getId(), vl1.getId(),
                "L1", 0, ConnectablePosition.Direction.TOP,
                "L1", 1, ConnectablePosition.Direction.TOP);

        Networks.createLine(network, "L2", "L2", 1.0, 1.0, 1.0, 0.0, 0.0, 0.0,
                2, 20, vl.getId(), vl2.getId(),
                "L2", 1, ConnectablePosition.Direction.BOTTOM,
                "L2", 0, ConnectablePosition.Direction.TOP);

        Networks.createLine(network, "L3", "L3", 1.0, 1.0, 1.0, 0.0, 0.0, 0.0,
                3, 30, vl.getId(), vl3.getId(),
                "L3", 2, ConnectablePosition.Direction.TOP,
                "L3", 0, ConnectablePosition.Direction.TOP);

    }

    @Test
    void testBasic() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write Json and compare to reference
        assertEquals(toString("/TestCaseFictitiousBus.svg"),
                toSVG(g, "/TestCaseFictitiousBus.svg", getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider()));
    }

    @Test
    void testTopological() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write Json and compare to reference
        assertEquals(toString("/TestCaseFictitiousBusTopological.svg"),
                toSVG(g, "/TestCaseFictitiousBusTopological.svg", getDefaultDiagramLabelProvider(), new TopologicalStyleProvider(network)));
    }
}
