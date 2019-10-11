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
 *
 *     n
 *    / \
 *   |   |
 * -d1---|---- bbs1
 * -----d2---- bbs2
 *
 * </pre>
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TestCase9singularInternCell extends AbstractTestCase {

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
        BusbarSection bbs1 = view.newBusbarSection()
                .setId("bbs1")
                .setNode(0)
                .add();
        bbs1.addExtension(BusbarSectionPosition.class, new BusbarSectionPosition(bbs1, 1, 1));
        BusbarSection bbs2 = view.newBusbarSection()
                .setId("bbs2")
                .setNode(1)
                .add();
        bbs2.addExtension(BusbarSectionPosition.class, new BusbarSectionPosition(bbs2, 2, 1));
        view.newDisconnector()
                .setId("d1")
                .setNode1(0)
                .setNode2(2)
                .add();
        view.newDisconnector()
                .setId("d2")
                .setNode1(1)
                .setNode2(2)
                .add();
    }
}
