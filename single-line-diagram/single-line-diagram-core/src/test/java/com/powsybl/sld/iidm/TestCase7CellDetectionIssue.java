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
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.layout.ImplicitCellDetector;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <PRE>
 * l
 * |
 * ------ bbs
 * </PRE>
 *
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
class TestCase7CellDetectionIssue extends AbstractTestCaseIidm {

    private VoltageLevel vl;

    @BeforeEach
    public void setUp() {
        network = Network.create("testCase1", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = Networks.createSubstation(network, "s", "s", Country.FR);
        vl = Networks.createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380);
        Networks.createLoad(vl, "l", "l", "l", 0, ConnectablePosition.Direction.TOP, 0, 10, 10);

        vl.getNodeBreakerView().newInternalConnection()
                .setNode1(0)
                .setNode2(1)
                .add();

        Networks.createBusBarSection(vl, "bbs", "bbs", 1, 1, 1);
    }

    @Test
    void test() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        // detect cells
        new ImplicitCellDetector().detectCells(g);

        // assert cells
        assertEquals(1, g.getCellStream().count());
    }
}
