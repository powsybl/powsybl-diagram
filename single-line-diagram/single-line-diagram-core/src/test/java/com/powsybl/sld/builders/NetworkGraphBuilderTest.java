/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.builders;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkGraphBuilderTest {

    @Test
    void isCapacitorTest() {
        Network network = EurostagTutorialExample1Factory.create();
        VoltageLevel vlload = network.getVoltageLevel("VLLOAD");
        ShuntCompensator sc = vlload.newShuntCompensator()
                .setId("SC")
                .setConnectableBus("NLOAD")
                .newLinearModel()
                .setBPerSection(0.05)
                .setMaximumSectionCount(1)
                .add()
                .setSectionCount(0)
                .add();
        assertTrue(NetworkGraphBuilder.isCapacitor(sc));
        sc.getModel(ShuntCompensatorLinearModel.class).setBPerSection(-0.03);
        assertFalse(NetworkGraphBuilder.isCapacitor(sc));
        ShuntCompensator sc2 = vlload.newShuntCompensator()
                .setId("SC2")
                .setConnectableBus("NLOAD")
                .newNonLinearModel()
                .beginSection()
                .setB(0.05)
                .endSection()
                .beginSection()
                .setB(-0.02)
                .endSection()
                .add()
                .setSectionCount(0)
                .add();
        assertTrue(NetworkGraphBuilder.isCapacitor(sc2));
        sc2.getModel(ShuntCompensatorNonLinearModel.class).getAllSections().get(1).setB(-0.07);
        assertFalse(NetworkGraphBuilder.isCapacitor(sc2));
    }
}
