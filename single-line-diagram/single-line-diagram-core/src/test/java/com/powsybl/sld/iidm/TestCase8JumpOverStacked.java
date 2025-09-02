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
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
class TestCase8JumpOverStacked extends AbstractTestCaseIidm {

    @Override
    public void setUp() {
        network = Network.create("testCase", "test");
        substation = Networks.createSubstation(network, "s", "s", Country.FR);
        vl = Networks.createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380);
        Networks.createBusBarSection(vl, "bbs11", "bbs11", 0, 1, 1);
        Networks.createBusBarSection(vl, "bbs12", "bbs12", 1, 1, 2);
        Networks.createSwitch(vl, "d1", "d1", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        Networks.createBusBarSection(vl, "bbs21", "bbs21", 2, 2, 1);
        Networks.createBusBarSection(vl, "bbs22", "bbs22", 3, 2, 2);
        Networks.createSwitch(vl, "d2", "d2", SwitchKind.DISCONNECTOR, false, false, false, 2, 3);
        Networks.createSwitch(vl, "d11", "d11", SwitchKind.DISCONNECTOR, false, false, false, 0, 4);
        Networks.createSwitch(vl, "d12", "d12", SwitchKind.DISCONNECTOR, false, false, false, 1, 5);
        Networks.createSwitch(vl, "d21", "d21", SwitchKind.DISCONNECTOR, false, false, false, 2, 4);
        Networks.createSwitch(vl, "d22", "d22", SwitchKind.DISCONNECTOR, false, false, false, 3, 5);
        Networks.createSwitch(vl, "b", "b", SwitchKind.BREAKER, false, false, false, 4, 5);
    }
}
