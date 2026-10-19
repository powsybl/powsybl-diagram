/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.Network;
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.build.iidm.VoltageLevelFilter;
import com.powsybl.nad.layout.BasicForceLayout;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.TopologicalStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author Charly Bion {@literal <charly.bion at rte-france.com>}
 */
class EdgesToInvisibleNodesTest extends AbstractTest {

    private Network network;

    @BeforeEach
    void setup() {
        network = Networks.createTwoVoltageLevels();
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
    void testEdgesToInvisibleNodesHidden() {
        getLayoutParameters().setEdgesToInvisibleNodesDisplayed(false);
        assertSvgEquals("/edges_to_invisible_nodes_hidden.svg", network,
                VoltageLevelFilter.createVoltageLevelsFilter(network, List.of("vl1")),
                new BasicForceLayout());
    }

    @Test
    void testEdgesToInvisibleNodesDisplayed() {
        assertSvgEquals("/edges_to_invisible_nodes_displayed.svg", network,
                VoltageLevelFilter.createVoltageLevelsFilter(network, List.of("vl1")),
                new BasicForceLayout());
    }
}
