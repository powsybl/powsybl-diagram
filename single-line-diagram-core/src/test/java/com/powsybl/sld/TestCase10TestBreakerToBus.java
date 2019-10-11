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

/**
 * <PRE>
 * l
 * |
 * b
 * |
 * d
 * |
 * ------ bbs
 * </PRE>
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TestCase10TestBreakerToBus extends AbstractTestCase {

    @Override
    void setUp() {
        network = Network.create("testCase1", "AbstractTest");
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
        BusbarSection bbs = view.newBusbarSection()
                .setId("bbs")
                .setNode(0)
                .add();
        bbs.addExtension(BusbarSectionPosition.class, new BusbarSectionPosition(bbs, 1, 1));
        Load l = vl.newLoad()
                .setId("l")
                .setNode(2)
                .setP0(10)
                .setQ0(10)
                .add();
        l.addExtension(ConnectablePosition.class, new ConnectablePosition<>(l, new ConnectablePosition
                .Feeder("l", 0, ConnectablePosition.Direction.TOP), null, null, null));
        view.newBreaker()
                .setId("b")
                .setNode1(0)
                .setNode2(2)
                .add();
    }
}
