/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class SmartVoltageLevelLayoutFactoryTest {

    @Test
    void test() {
        Network network = Network.create("test", "code");
        Substation s = network.newSubstation()
                .setId("S")
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("vl")
                .setNominalV(380)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        BusbarSection bbs = vl.getNodeBreakerView().newBusbarSection()
                .setId("bbs")
                .setNode(0)
                .add();

        assertThat(VoltageLevelLayoutFactorySmartSelector.findBest(vl))
                .isPresent()
                .get()
                .isInstanceOf(PositionByClusterVoltageLevelLayoutFactorySmartSelector.class);

        bbs.newExtension(BusbarSectionPositionAdder.class).withBusbarIndex(1).withSectionIndex(1).add();

        assertThat(VoltageLevelLayoutFactorySmartSelector.findBest(vl))
                .isPresent()
                .get()
                .isInstanceOf(PositionFromExtensionVoltageLevelLayoutFactorySmartSelector.class);
    }
}
