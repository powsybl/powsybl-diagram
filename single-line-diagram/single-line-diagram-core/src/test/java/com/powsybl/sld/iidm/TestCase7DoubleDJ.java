/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.*;

/**
 * <pre>
 *
 *  bbs1---d1-b1-b2-d2--- bbs2
 *
 * </pre>
 *
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
class TestCase7DoubleDJ extends AbstractTestCaseIidm {

    @Override
    public void setUp() {
        network = Network.create("testCase", "test");
        substation = Networks.createSubstation(network, "s", "s", Country.FR);
        vl = Networks.createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380);
        Networks.createBusBarSection(vl, "bbs1", "bbs1", 0, 1, 1);
        Networks.createSwitch(vl, "d1", "d1", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        Networks.createSwitch(vl, "b1", "b1", SwitchKind.BREAKER, false, false, false, 1, 2);
        Networks.createSwitch(vl, "b2", "b2", SwitchKind.BREAKER, false, false, false, 2, 3);
        Networks.createSwitch(vl, "d2", "d2", SwitchKind.DISCONNECTOR, false, false, false, 3, 4);
        Networks.createBusBarSection(vl, "bbs2", "bbs2", 4, 1, 2);
    }
}
