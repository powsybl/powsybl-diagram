/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.iidm.extensions.BusbarSectionPosition;

/**
 * <pre>
 *
 *              *-----b-----*
 *             /\          /\
 *            |  |        |  |
 *  bbs11 ---d11-|---d1--d12-|--- bbs12
 *               |           |
 *  bbs21 ------d21--d2-----d22-- bbs12
 *
 * </pre>
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TestCase8JumpOverStacked extends AbstractTestCase {

    @Override
    void setUp() {
        network = Network.create("testCase", "test");
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
                .setNodeCount(15);

        BusbarSection bbs11 = view.newBusbarSection()
                .setId("bbs11")
                .setNode(0)
                .add();
        bbs11.addExtension(BusbarSectionPosition.class, new BusbarSectionPosition(bbs11, 1, 1));

        BusbarSection bbs12 = view.newBusbarSection()
                .setId("bbs12")
                .setNode(1)
                .add();
        bbs12.addExtension(BusbarSectionPosition.class, new BusbarSectionPosition(bbs12, 1, 2));

        view.newDisconnector()
                .setId("d1")
                .setNode1(0)
                .setNode2(1)
                .add();

        BusbarSection bbs21 = view.newBusbarSection()
                .setId("bbs21")
                .setNode(2)
                .add();
        bbs21.addExtension(BusbarSectionPosition.class, new BusbarSectionPosition(bbs21, 2, 1));

        BusbarSection bbs22 = view.newBusbarSection()
                .setId("bbs22")
                .setNode(3)
                .add();
        bbs22.addExtension(BusbarSectionPosition.class, new BusbarSectionPosition(bbs22, 2, 2));

        view.newDisconnector()
                .setId("d2")
                .setNode1(2)
                .setNode2(3)
                .add();

        view.newDisconnector()
                .setId("d11")
                .setNode1(0)
                .setNode2(4)
                .add();

        view.newDisconnector()
                .setId("d12")
                .setNode1(1)
                .setNode2(5)
                .add();

        view.newDisconnector()
                .setId("d21")
                .setNode1(2)
                .setNode2(4)
                .add();

        view.newDisconnector()
                .setId("d22")
                .setNode1(3)
                .setNode2(5)
                .add();

        view.newBreaker()
                .setId("b")
                .setNode1(4)
                .setNode2(5)
                .add();
    }
}
