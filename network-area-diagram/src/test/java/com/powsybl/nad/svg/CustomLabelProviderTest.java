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
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.iidm.TopologicalStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi at soft.it>}
 */
class CustomLabelProviderTest extends AbstractTest {

    LabelProvider labelProvider;

    @BeforeEach
    void setup() {
        setLayoutParameters(new LayoutParameters());

        //Note: SvgParameters EdgeNameDisplayed, VoltageLevelDetails, and BusLegend must be true,
        // for the edge names, the VL descriptions plus VL details, and bus descriptions to be rendered
        setSvgParameters(new SvgParameters()
                .setSvgWidthAndHeightAdded(true)
                .setFixedWidth(800)
                .setEdgeNameDisplayed(true)
                .setVoltageLevelDetails(true)
                .setBusLegend(true));
    }

    @Override
    protected StyleProvider getStyleProvider(Network network) {
        return new TopologicalStyleProvider(network);
    }

    @Override
    protected LabelProvider getLabelProvider(Network network) {
        return labelProvider;
    }

    @Test
    void testCustomLabelProvider() {
        Network network = Networks.createNodeBreakerNetworkWithBranchStatus("TestNodeDecorators", "test");

        Map<String, CustomLabelProvider.BranchLabels> branchLabels = new HashMap<>();
        branchLabels.put("L12", new CustomLabelProvider.BranchLabels("L1_1", "L1", "L1_2", EdgeInfo.Direction.IN, EdgeInfo.Direction.IN));
        branchLabels.put("T12", new CustomLabelProvider.BranchLabels("TWT1_1", "TWT1", "TWT1_2", null, null));
        branchLabels.put("L11", new CustomLabelProvider.BranchLabels(null, "L2", null, EdgeInfo.Direction.IN, EdgeInfo.Direction.IN));
        branchLabels.put("T11", new CustomLabelProvider.BranchLabels(null, "TWT2", "TWT2_2", null, EdgeInfo.Direction.OUT));

        Map<String, CustomLabelProvider.ThreeWtLabels> threeWtLabels = new HashMap<>();

        Map<String, String> busDescriptions = new HashMap<>();
        busDescriptions.put("VL1_10", "VL1 10");
        busDescriptions.put("VL2_30", "VL2 30");

        Map<String, List<String>> vlDescriptions = new HashMap<>();
        vlDescriptions.put("VL1", List.of("VL1 description1", "VL1 description2"));
        vlDescriptions.put("VL2", List.of("VL2 description1"));

        Map<String, List<String>> vlDetails = new HashMap<>();
        vlDetails.put("VL2", List.of("VL2 details1", "VL2 details2"));

        labelProvider = new CustomLabelProvider(branchLabels, threeWtLabels, busDescriptions, vlDescriptions, vlDetails);

        assertSvgEquals("/custom_label_provider.svg", network);
    }

    @Test
    void testCustomLabelProvider3wt() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();

        Map<String, CustomLabelProvider.BranchLabels> branchLabels = new HashMap<>();
        Map<String, CustomLabelProvider.ThreeWtLabels> threeWtLabels = new HashMap<>();
        threeWtLabels.put("3WT", new CustomLabelProvider.ThreeWtLabels("SIDE1", "SIDE2", "SIDE3", EdgeInfo.Direction.IN, EdgeInfo.Direction.OUT, EdgeInfo.Direction.IN));

        Map<String, String> busDescriptions = new HashMap<>();
        busDescriptions.put("VL_132_0", "VL1 132");
        busDescriptions.put("VL_33_0", "VL2 33");
        busDescriptions.put("VL_11_0", "VL2 11");

        Map<String, List<String>> vlDescriptions = new HashMap<>();
        vlDescriptions.put("VL_132", List.of("VL 132 description1", "VL1 132 description2"));
        vlDescriptions.put("VL_33", List.of("VL 33 description1"));
        vlDescriptions.put("VL_11", List.of("VL 11 description1"));

        Map<String, List<String>> vlDetails = new HashMap<>();
        vlDetails.put("VL_132", List.of("VL 132 details1"));
        vlDetails.put("VL_33", List.of("VL 33 details1", "VL 33 details2"));
        vlDetails.put("VL_11", List.of("VL 1 details1"));

        labelProvider = new CustomLabelProvider(branchLabels, threeWtLabels, busDescriptions, vlDescriptions, vlDetails);

        assertSvgEquals("/custom_label_provider_3wt.svg", network);
    }

}
