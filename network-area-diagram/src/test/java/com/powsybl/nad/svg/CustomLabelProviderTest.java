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

        //Note: SvgParameters and EdgeNameDisplayed are set explicitly at false to demonstrate that
        // the custom label provider ignores them when rendering edge names, the VL descriptions, and VL details.
        setSvgParameters(new SvgParameters()
                .setSvgWidthAndHeightAdded(true)
                .setFixedWidth(800));
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
        branchLabels.put("L12", new CustomLabelProvider.BranchLabels(null, "L1_1", null, "L1", null, "L1_2", EdgeInfo.Direction.IN, null, EdgeInfo.Direction.IN));
        branchLabels.put("T12", new CustomLabelProvider.BranchLabels(null, "TWT1_1", null, "TWT1", null, "TWT1_2", null, null, null));
        branchLabels.put("L11", new CustomLabelProvider.BranchLabels(null, null, null, "L2",  null, null, EdgeInfo.Direction.IN, null, EdgeInfo.Direction.IN));
        branchLabels.put("T11", new CustomLabelProvider.BranchLabels(null, null, null, "TWT2",  null, "TWT2_2", null, null, EdgeInfo.Direction.OUT));

        Map<String, CustomLabelProvider.ThreeWtLabels> threeWtLabels = new HashMap<>();

        Map<String, VoltageLevelLegend> vlDescriptions = new HashMap<>();
        var vl1Legend = new VoltageLevelLegend(
                List.of("VL1 description1", "VL1 description2"),
                List.of(),
                Map.of("VL1_10", "VL1 10"));
        var vl2Legend = new VoltageLevelLegend(
                List.of("VL2 description1"),
                List.of("VL2 details1", "VL2 details2"),
                Map.of("VL2_30", "VL2 30"));
        vlDescriptions.put("VL1", vl1Legend);
        vlDescriptions.put("VL2", vl2Legend);

        labelProvider = new CustomLabelProvider(branchLabels, threeWtLabels, new HashMap<>(), vlDescriptions);

        assertSvgEquals("/custom_label_provider.svg", network);
    }

    @Test
    void testCustomLabelProvider3wt() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();

        Map<String, CustomLabelProvider.BranchLabels> branchLabels = new HashMap<>();
        Map<String, CustomLabelProvider.ThreeWtLabels> threeWtLabels = new HashMap<>();
        threeWtLabels.put("3WT", new CustomLabelProvider.ThreeWtLabels(null, "SIDE1", null, "SIDE2", null, "SIDE3", EdgeInfo.Direction.IN, EdgeInfo.Direction.OUT, EdgeInfo.Direction.IN));

        Map<String, CustomLabelProvider.InjectionLabels> injLabels = new HashMap<>();
        injLabels.put("GEN_132", new CustomLabelProvider.InjectionLabels(null, "G132", EdgeInfo.Direction.IN));
        injLabels.put("LOAD_33", new CustomLabelProvider.InjectionLabels(null, "L33", EdgeInfo.Direction.OUT));

        Map<String, VoltageLevelLegend> vlDescriptions = new HashMap<>();
        var vl132Legend = new VoltageLevelLegend(
                List.of("VL 132 description1", "VL1 132 description2"),
                List.of("VL 132 details1"),
                Map.of("VL_132_0", "VL1 132"));
        var vl33Legend = new VoltageLevelLegend(
                List.of("VL 33 description1"),
                List.of("VL 33 details1", "VL 33 details2"),
                Map.of("VL_33_0", "VL2 33")
        );
        var vl11Legend = new VoltageLevelLegend(
                List.of("VL 11 description1"),
                List.of("VL 1 details1"),
                Map.of("VL_11_0", "VL2 11")
        );
        vlDescriptions.put("VL_132", vl132Legend);
        vlDescriptions.put("VL_33", vl33Legend);
        vlDescriptions.put("VL_11", vl11Legend);

        labelProvider = new CustomLabelProvider(branchLabels, threeWtLabels, injLabels, vlDescriptions);
        getLayoutParameters().setInjectionsAdded(true);

        assertSvgEquals("/custom_label_provider_3wt.svg", network);
    }

}
