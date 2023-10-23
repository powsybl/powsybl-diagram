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
import com.powsybl.iidm.xml.NetworkXml;
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.NominalVoltageStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertEquals(toString("/IEEE_30_bus.svg"), generateSvgString(network, "/IEEE_30_bus.svg"));
    }

    @Test

    void testIEEE14() {
        Network network = IeeeCdfNetworkFactory.create14Solved();
        assertEquals(toString("/IEEE_14_bus.svg"), generateSvgString(network, "/IEEE_14_bus.svg"));
    }

    @Test
    void testIEEE14ForceLayoutWithTextNodes() {
        Network network = IeeeCdfNetworkFactory.create14();
        getLayoutParameters().setTextNodesForceLayout(true);
        assertEquals(toString("/IEEE_14_bus_text_nodes.svg"), generateSvgString(network, "/IEEE_14_bus_text_nodes.svg"));
    }

    @Test
    void testIEEE14FWithSvgPrefix() {
        Network network = IeeeCdfNetworkFactory.create14();
        getSvgParameters().setSvgPrefix("test_");
        assertEquals(toString("/IEEE_14_id_prefixed.svg"), generateSvgString(network, "/IEEE_14_id_prefixed.svg"));
    }

    @Test
    void testDisconnection() {
        Network network = IeeeCdfNetworkFactory.create14Solved();
        network.getLine("L3-4-1").getTerminal1().disconnect();
        network.getTwoWindingsTransformer("T4-7-1").getTerminal1().disconnect();
        network.getVoltageLevel("VL14").getConnectableStream().map(connectable -> (Connectable<?>) connectable).forEach(connectable -> connectable.getTerminals().forEach(Terminal::disconnect));
        assertEquals(toString("/IEEE_14_bus_disconnection.svg"), generateSvgString(network, "/IEEE_14_bus_disconnection.svg"));
    }

    @Test
    void testFictitiousVoltageLevel() {
        Network network = IeeeCdfNetworkFactory.create14();
        network.getVoltageLevel("VL12").setFictitious(true);
        network.getVoltageLevel("VL14").setFictitious(true);
        assertEquals(toString("/IEEE_14_bus_fictitious.svg"), generateSvgString(network, "/IEEE_14_bus_fictitious.svg"));
    }

    @Test
    void testIEEE24() {
        Network network = NetworkXml.read(getClass().getResourceAsStream("/IEEE_24_bus.xiidm"));
        assertEquals(toString("/IEEE_24_bus.svg"), generateSvgString(network, "/IEEE_24_bus.svg"));
    }

    @Test
    void testHvdc() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        assertEquals(toString("/hvdc.svg"), generateSvgString(network, "/hvdc.svg"));
    }
}
