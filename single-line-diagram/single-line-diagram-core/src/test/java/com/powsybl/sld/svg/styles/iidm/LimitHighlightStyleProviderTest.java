/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg.styles.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.Network;
import com.powsybl.sld.model.graphs.VoltageLevelInfos;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.Edge;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.FeederType;
import com.powsybl.sld.model.nodes.NodeSide;
import com.powsybl.sld.model.nodes.feeders.FeederWithSides;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LimitHighlightStyleProviderTest {

    private static Edge buildEdge(String equipmentId) {
        FeederWithSides feeder = new FeederWithSides(
                FeederType.BRANCH,
                NodeSide.ONE,
                new VoltageLevelInfos("VL1", "VoltageLevel1", 225.0),
                new VoltageLevelInfos("VL2", "VoltageLevel2", 225.0)
        );
        FeederNode feederNode = new FeederNode("feederNode", "Feeder", equipmentId, "component", false, feeder, null);
        BusNode busNode = new BusNode("busNode", "Bus", false);
        return new Edge(feederNode, busNode);
    }

    @Test
    void customLimitViolationStyleOverridesDefaultEdgeStyles() {
        Network network = Networks.createNetworkWithLine();
        LimitHighlightStyleProvider provider = new LimitHighlightStyleProvider(network, Map.of("Line", "custom-style"));
        Edge edge = buildEdge("Line");

        List<String> styles = provider.getEdgeStyles(null, edge);

        assertEquals(List.of("custom-style"), styles);
    }

    @Test
    void missingCustomStyleFallsBackToDefaultEdgeStyles() {
        Network network = Networks.createNetworkWithLine();
        Edge edge = buildEdge("Line");

        LimitHighlightStyleProvider defaultProvider = new LimitHighlightStyleProvider(network);
        List<String> defaultStyles = defaultProvider.getEdgeStyles(null, edge);

        LimitHighlightStyleProvider providerWithCustomMap = new LimitHighlightStyleProvider(network, Map.of("OtherLine", "custom-style"));
        List<String> styles = providerWithCustomMap.getEdgeStyles(null, edge);

        assertEquals(defaultStyles, styles);
    }
}
