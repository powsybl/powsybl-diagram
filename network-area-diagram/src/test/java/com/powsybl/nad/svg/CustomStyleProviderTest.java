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
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.CustomStyleProvider.CustomBusNodeStyles;
import com.powsybl.nad.svg.CustomStyleProvider.CustomEdgeStyles;
import com.powsybl.nad.svg.CustomStyleProvider.CustomThreeWtStyles;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi at soft.it>}
 */
class CustomStyleProviderTest extends AbstractTest {
    StyleProvider styleProvider;

    @BeforeEach
    void setup() {
        setLayoutParameters(new LayoutParameters());
        setSvgParameters(new SvgParameters()
                .setSvgWidthAndHeightAdded(true)
                .setFixedWidth(800)
                .setEdgeNameDisplayed(true)
                .setVoltageLevelDetails(true)
                .setBusLegend(true));
    }

    @Override
    protected StyleProvider getStyleProvider(Network network) {
        return styleProvider;
    }

    @Override
    protected LabelProvider getLabelProvider(Network network) {
        return new DefaultLabelProvider(network, getSvgParameters());

    }

    @Test
    void testCustomStyleProvider() {
        Network network = Networks.createNodeBreakerNetworkWithBranchStatus("TestNodeDecorators", "test");

        Map<String, CustomBusNodeStyles> busNodesStyles = new HashMap<>();
        busNodesStyles.put("VL1_10", new CustomBusNodeStyles("yellow", null, null));
        busNodesStyles.put("VL2_30", new CustomBusNodeStyles("red", "black", "4px"));

        Map<String, CustomEdgeStyles> edgesStyles = new HashMap<>();
        edgesStyles.put("L11", new CustomEdgeStyles("blue", "2px", null, "blue", "2px", null));
        edgesStyles.put("L12", new CustomEdgeStyles("green", "4px", "8,4", "green", "4px", "8,4"));
        edgesStyles.put("T11", new CustomEdgeStyles("red", "8px", null, "brown", "8px", null));
        edgesStyles.put("T12", new CustomEdgeStyles("orange", null, null, "orange", null, null));

        Map<String, CustomThreeWtStyles> threeWtsStyles = new HashMap<>();
        threeWtsStyles.put("T3_12",
                new CustomThreeWtStyles(
                        "gray", "4px", null,
                        "purple", "4px", "4,4",
                        "pink", "6px", null
                )
        );

        styleProvider = new CustomStyleProvider(busNodesStyles, edgesStyles, threeWtsStyles);
        assertSvgEquals("/custom_style_provider.svg", network);
    }

    @Test
    void testCustomStyleProviderEmpty() {
        Network network = Networks.createNodeBreakerNetworkWithBranchStatus("TestNodeDecorators", "test");
        styleProvider = new CustomStyleProvider(new HashMap<>(), new HashMap<>(), new HashMap<>());
        assertSvgEquals("/custom_style_provider_empty.svg", network);
    }

}
