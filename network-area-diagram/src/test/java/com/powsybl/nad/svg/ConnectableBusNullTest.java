/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg;

import com.powsybl.ieeecdf.converter.IeeeCdfNetworkFactory;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.TopologicalStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class ConnectableBusNullTest extends AbstractTest {

    @BeforeEach
    void setup() {
        setLayoutParameters(new LayoutParameters()
                .setInjectionsAdded(true));
        setSvgParameters(new SvgParameters());
    }

    @Override
    protected StyleProvider getStyleProvider(Network network) {
        return new TopologicalStyleProvider(network);
    }

    @Override
    protected LabelProvider getLabelProvider(Network network) {
        return new DefaultLabelProvider(network, getSvgParameters());
    }

    @Test
    void test() {
        Network network = IeeeCdfNetworkFactory.create14Solved();
        network.getLine("L3-4-1").getTerminal1().disconnect();
        network.getTwoWindingsTransformer("T4-7-1").getTerminal1().disconnect();
        network.getVoltageLevel("VL14").getConnectableStream().map(connectable -> (Connectable<?>) connectable).forEach(connectable -> connectable.getTerminals().forEach(Terminal::disconnect));
        network.getVoltageLevel("VL5").getBusView().getBusStream().findFirst()
                .ifPresent(bus -> bus.getConnectedTerminals().forEach(Terminal::disconnect));
        network.getVoltageLevel("VL4").getBusView().getBusStream().findFirst()
                .ifPresent(bus -> bus.getConnectedTerminals().forEach(Terminal::disconnect));
        assertSvgEquals("/IEEE_14_null_connectable_bus.svg", network);
    }
}
