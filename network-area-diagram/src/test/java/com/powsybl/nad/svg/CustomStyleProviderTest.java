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
import com.powsybl.nad.NadParameters;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.CustomLabelProvider.InjectionLabels;
import com.powsybl.nad.svg.CustomStyleProvider.BusNodeStyles;
import com.powsybl.nad.svg.CustomStyleProvider.EdgeStyles;
import com.powsybl.nad.svg.CustomStyleProvider.InjectionStyles;
import com.powsybl.nad.svg.CustomStyleProvider.ThreeWtStyles;
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
                .setFixedWidth(800));
    }

    @Override
    protected StyleProvider getStyleProvider(Network network) {
        return styleProvider;
    }

    @Override
    protected LabelProvider getLabelProvider(Network network) {
        return new DefaultLabelProvider.Builder()
            .setInfoSideExternal(EdgeInfoEnum.ACTIVE_POWER)
            .setInfoSideInternal(EdgeInfoEnum.EMPTY)
            .setInfoMiddleSide1(EdgeInfoEnum.EMPTY)
            .setInfoMiddleSide2(EdgeInfoEnum.NAME)
            .setBusLegend(true)
            .setVoltageLevelDetails(true)
            .build(network, getSvgParameters());
    }

    @Test
    void testCustomStyleProvider() {
        Network network = Networks.createNodeBreakerNetworkWithBranchStatus("TestNodeDecorators", "test");

        Map<String, BusNodeStyles> busNodesStyles = new HashMap<>();
        busNodesStyles.put("VL1_10", new BusNodeStyles("yellow", null, null));
        busNodesStyles.put("VL2_30", new BusNodeStyles("red", "black", "4px"));

        Map<String, EdgeStyles> edgesStyles = new HashMap<>();
        edgesStyles.put("L11", new EdgeStyles("blue", "2px", null, "blue", "2px", null));
        edgesStyles.put("L12", new EdgeStyles("green", "4px", "8,4", "green", "4px", "8,4"));
        edgesStyles.put("T11", new EdgeStyles("red", "8px", null, "brown", "8px", null));
        edgesStyles.put("T12", new EdgeStyles("orange", null, null, "orange", null, null));

        Map<String, ThreeWtStyles> threeWtsStyles = new HashMap<>();
        threeWtsStyles.put("T3_12",
                new ThreeWtStyles(
                        "gray", "4px", null,
                        "purple", "4px", "4,4",
                        "pink", "6px", null
                )
        );

        styleProvider = new CustomStyleProvider(busNodesStyles, edgesStyles, threeWtsStyles, new HashMap<>());
        assertSvgEquals("/custom_style_provider.svg", network);
    }

    @Test
    void testCustomStyleProviderEmpty() {
        Network network = Networks.createNodeBreakerNetworkWithBranchStatus("TestNodeDecorators", "test");
        styleProvider = new CustomStyleProvider(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
        assertSvgEquals("/custom_style_provider_empty.svg", network);
    }

    @Test
    void testInjectionWithCustomLabelAndStyleProvider() {
        Network network = Networks.createNodeBreakerNetworkWithBranchStatus("test", "test");
        NadParameters nadParameters = new NadParameters()
                .setLayoutParameters(new LayoutParameters().setInjectionsAdded(true))
                .setStyleProviderFactory(network1 -> createCustomStyleInjections())
                .setLabelProviderFactory((network1, svgParameters) -> createCustomLabelInjections());
        setNadParameters(nadParameters);
        assertSvgEqualsWithNadParameters("/nad-injection-with-custom-label-and-style.svg", network);
    }

    @Test
    void testInjectionWithCustomStyleProvider() {
        Network network = Networks.createNodeBreakerNetworkWithBranchStatus("test", "test");
        NadParameters nadParameters = new NadParameters()
                .setLayoutParameters(new LayoutParameters().setInjectionsAdded(true))
                .setStyleProviderFactory(network1 -> createCustomStyleInjections());
        setNadParameters(nadParameters);
        assertSvgEqualsWithNadParameters("/nad-injection-with-custom-style.svg", network);
    }

    private CustomStyleProvider createCustomStyleInjections() {
        Map<String, InjectionStyles> injectionStyles = new HashMap<>();
        injectionStyles.put("G", new InjectionStyles("red", "4px", null));
        injectionStyles.put("L1", new InjectionStyles("green", "4px", "2"));
        injectionStyles.put("L2", new InjectionStyles("green", "4px", null));
        return new CustomStyleProvider(new HashMap<>(), new HashMap<>(), new HashMap<>(), injectionStyles);
    }

    private CustomLabelProvider createCustomLabelInjections() {
        Map<String, InjectionLabels> injectionLabels = new HashMap<>();
        injectionLabels.put("G", new InjectionLabels(null, "gen", EdgeInfo.Direction.IN));
        injectionLabels.put("L1", new InjectionLabels(null, "label 1", EdgeInfo.Direction.OUT));
        injectionLabels.put("L2", new InjectionLabels(null, "label 2", EdgeInfo.Direction.OUT));
        return new CustomLabelProvider(new HashMap<>(), new HashMap<>(), injectionLabels, new HashMap<>());
    }

}
