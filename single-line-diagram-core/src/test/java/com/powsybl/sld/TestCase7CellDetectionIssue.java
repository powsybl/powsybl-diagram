/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.iidm.extensions.BusbarSectionPosition;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import com.powsybl.sld.layout.ImplicitCellDetector;
import com.powsybl.sld.model.Graph;
import org.junit.Before;

import static org.junit.Assert.assertEquals;

/**
 * <PRE>
 * l
 * |
 * ------ bbs
 * </PRE>
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TestCase7CellDetectionIssue extends AbstractTestCase {

    private VoltageLevel vl;

    @Before
    public void setUp() {
        network = Network.create("testCase1", "test");
        Substation s = network.newSubstation()
                .setId("s")
                .setCountry(Country.FR)
                .add();
        vl = s.newVoltageLevel()
                .setId("vl")
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .setNominalV(400)
                .add();
        VoltageLevel.NodeBreakerView view = vl.getNodeBreakerView()
                .setNodeCount(10);
        Load l = vl.newLoad()
                .setId("l")
                .setNode(0)
                .setP0(10)
                .setQ0(10)
                .add();
        l.addExtension(ConnectablePosition.class, new ConnectablePosition<>(l, new ConnectablePosition
                .Feeder("l", 0, ConnectablePosition.Direction.TOP), null, null, null));
        view.newInternalConnection()
                .setNode1(0)
                .setNode2(1)
                .add();
        BusbarSection bbs = view.newBusbarSection()
                .setId("bbs")
                .setNode(1)
                .add();
        bbs.addExtension(BusbarSectionPosition.class, new BusbarSectionPosition(bbs, 1, 1));
    }

//    @Test
    public void test() {
        // build graph
        Graph g = Graph.create(vl);

        // assert graph structure
        assertEquals(2, g.getNodes().size());

        // detect cells
        new ImplicitCellDetector().detectCells(g);

        // assert cells
        assertEquals(1, g.getCells().size());
    }
}
