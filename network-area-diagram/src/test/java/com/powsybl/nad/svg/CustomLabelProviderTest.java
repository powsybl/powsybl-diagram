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
import com.powsybl.nad.svg.iidm.CustomLabelProvider;
import com.powsybl.nad.svg.iidm.NominalVoltageStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi at soft.it>}
 */
class CustomLabelProviderTest extends AbstractTest {

    LabelProvider labelProvider;

    @BeforeEach
    void setup() {
        setLayoutParameters(new LayoutParameters());
        setSvgParameters(new SvgParameters()
                .setSvgWidthAndHeightAdded(true)
                .setFixedWidth(800)
                .setEdgeNameDisplayed(true)
                .setVoltageLevelDetails(false)
                .setBusLegend(false));
    }

    @Override
    protected StyleProvider getStyleProvider(Network network) {
        return new NominalVoltageStyleProvider(network);
    }

    @Override
    protected LabelProvider getLabelProvider(Network network) {
        return labelProvider;
    }

    @Test
    void testCustomLabelProvider() {
        Network network = Networks.createNodeBreakerNetworkWithBranchStatus("TestNodeDecorators", "test");

        Map<String, CustomLabelProvider.CustomBranchLabels> branchLabels = new HashMap<>();
        branchLabels.put("L12", new CustomLabelProvider.CustomBranchLabels("L1_1", "L1", "L1_2", EdgeInfo.Direction.IN, EdgeInfo.Direction.IN));
        branchLabels.put("T12", new CustomLabelProvider.CustomBranchLabels("TWT1_1", "TWT1", "TWT1_2", null, null));
        branchLabels.put("L11", new CustomLabelProvider.CustomBranchLabels(null, "L2", null, EdgeInfo.Direction.IN, EdgeInfo.Direction.IN));
        branchLabels.put("T11", new CustomLabelProvider.CustomBranchLabels(null, "TWT2", "TWT2_2", null, EdgeInfo.Direction.OUT));

        labelProvider = new CustomLabelProvider(network, getSvgParameters(), branchLabels);

        assertSvgEquals("/custom_label_provider.svg", network);
    }
}
