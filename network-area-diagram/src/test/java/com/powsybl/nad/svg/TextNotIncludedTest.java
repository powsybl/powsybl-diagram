/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.TopologicalStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class TextNotIncludedTest extends AbstractTest {

    private Network network;

    @BeforeEach
    void setup() {
        network = Networks.createTwoVoltageLevels();
        network.getVoltageLevelStream()
                .flatMap(VoltageLevel::getConnectableStream)
                .flatMap(c -> c.getTerminals().stream())
                .forEach(t -> ((Terminal) t).setP(0));
        setLayoutParameters(new LayoutParameters());
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
    void testRemoveTextNode() {
        getSvgParameters().setVoltageLevelLegendsIncluded(false);
        assertSvgEquals("/legend_removed.svg", network);
    }

    @Test
    void testRemoveEdgeInfo() {
        getSvgParameters().setEdgeInfosIncluded(false);
        assertSvgEquals("/edge_infos_removed.svg", network);
    }
}
