/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.ieeecdf.converter.IeeeCdfNetworkFactory;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.iidm.serde.NetworkSerDe;
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.NominalVoltageStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class NominalVoltageStyleTest extends AbstractTest {

    @BeforeEach
    void setup() {
        setLayoutParameters(new LayoutParameters());
        setSvgParameters(new SvgParameters()
                .setInsertNameDesc(true)
                .setSvgWidthAndHeightAdded(true)
                .setVoltageLevelDetails(false)
                .setFixedWidth(800)
                .setEdgeStartShift(2));
    }

    @Override
    protected StyleProvider getStyleProvider(Network network) {
        return new NominalVoltageStyleProvider(network);
    }

    @Override
    protected LabelProvider getLabelProvider(Network network) {
        return new DefaultLabelProvider(network, getSvgParameters());
    }

    @Test
    void testIEEE30() {
        Network network = IeeeCdfNetworkFactory.create30();
        assertSvgEquals("/IEEE_30_bus.svg", network);
    }

    @Test

    void testIEEE14() {
        Network network = IeeeCdfNetworkFactory.create14Solved();
        assertSvgEquals("/IEEE_14_bus.svg", network);
    }

    @Test
    void testIEEE14ForceLayoutWithTextNodes() {
        Network network = IeeeCdfNetworkFactory.create14();
        getLayoutParameters().setTextNodesForceLayout(true);
        assertSvgEquals("/IEEE_14_bus_text_nodes.svg", network);
    }

    @Test
    void testIEEE14FWithSvgPrefix() {
        Network network = IeeeCdfNetworkFactory.create14();
        getSvgParameters().setSvgPrefix("test_");
        assertSvgEquals("/IEEE_14_id_prefixed.svg", network);
    }

    @Test
    void testDisconnection() {
        Network network = IeeeCdfNetworkFactory.create14Solved();
        network.getLine("L3-4-1").getTerminal1().disconnect();
        network.getTwoWindingsTransformer("T4-7-1").getTerminal1().disconnect();
        network.getVoltageLevel("VL14").getConnectableStream().map(connectable -> (Connectable<?>) connectable).forEach(connectable -> connectable.getTerminals().forEach(Terminal::disconnect));
        network.getVoltageLevel("VL5").getBusView().getBusStream().findFirst()
                .ifPresent(bus -> bus.getConnectedTerminals().forEach(Terminal::disconnect));
        network.getVoltageLevel("VL4").getBusView().getBusStream().findFirst()
                .ifPresent(bus -> bus.getConnectedTerminals().forEach(Terminal::disconnect));
        assertSvgEquals("/IEEE_14_bus_disconnection.svg", network);
    }

    @Test
    void testFictitiousVoltageLevel() {
        Network network = IeeeCdfNetworkFactory.create14();
        network.getVoltageLevel("VL12").setFictitious(true);
        network.getVoltageLevel("VL14").setFictitious(true);
        assertSvgEquals("/IEEE_14_bus_fictitious.svg", network);
    }

    @Test
    void testIEEE24() {
        Network network = NetworkSerDe.read(getClass().getResourceAsStream("/IEEE_24_bus.xiidm"));
        assertSvgEquals("/IEEE_24_bus.svg", network);
    }

    @Test
    void testHvdc() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        assertSvgEquals("/hvdc.svg", network);
    }
}
