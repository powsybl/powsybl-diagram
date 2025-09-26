/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg.iidm;

import com.powsybl.commons.config.BaseVoltagesConfig;
import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.nad.model.BranchEdge;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TopologicalStyleProviderTest {

    @Test
    void customLimitViolationStyleOverridesDefaultBranchStyle() {
        Network network = Networks.createNetworkWithLine();
        TopologicalStyleProvider provider = new TopologicalStyleProvider(network, Map.of("Line", "custom-style"));
        BranchEdge branchEdge = new BranchEdge("diagram", "Line", "Line", BranchEdge.LINE_EDGE);

        List<String> styles = provider.getBranchEdgeStyleClasses(branchEdge);

        assertEquals(List.of("custom-style"), styles);
    }

    @Test
    void missingCustomStyleFallsBackToDefaultBranchStyle() {
        Network network = Networks.createNetworkWithLine();
        BranchEdge branchEdge = new BranchEdge("diagram", "Line", "Line", BranchEdge.LINE_EDGE);

        TopologicalStyleProvider defaultProvider = new TopologicalStyleProvider(network);
        List<String> defaultStyles = defaultProvider.getBranchEdgeStyleClasses(branchEdge);

        TopologicalStyleProvider providerWithCustomMap = new TopologicalStyleProvider(network, Map.of("OtherLine", "custom-style"));
        List<String> styles = providerWithCustomMap.getBranchEdgeStyleClasses(branchEdge);

        assertEquals(defaultStyles, styles);
    }

    @Test
    void constructorWithBaseVoltageConfigKeepsCustomStyles() {
        Network network = Networks.createNetworkWithLine();
        BaseVoltagesConfig baseVoltagesConfig = BaseVoltagesConfig.fromPlatformConfig();
        BranchEdge branchEdge = new BranchEdge("diagram", "Line", "Line", BranchEdge.LINE_EDGE);

        TopologicalStyleProvider provider = new TopologicalStyleProvider(network, baseVoltagesConfig, Map.of("Line", "custom-style"));

        assertEquals(List.of("custom-style"), provider.getBranchEdgeStyleClasses(branchEdge));
    }

    @Test
    void baseVoltageStyleReturnsEmptyForNullTerminal() {
        Network network = Networks.createNetworkWithLine();
        BaseVoltagesConfig baseVoltagesConfig = BaseVoltagesConfig.fromPlatformConfig();
        TestTopologicalStyleProvider provider = new TestTopologicalStyleProvider(network, baseVoltagesConfig);

        assertTrue(provider.getBaseVoltageStyleForTerminal(null).isEmpty());
    }

    private static final class TestTopologicalStyleProvider extends TopologicalStyleProvider {

        private TestTopologicalStyleProvider(Network network, BaseVoltagesConfig baseVoltagesConfig) {
            super(network, baseVoltagesConfig);
        }

        private Optional<String> getBaseVoltageStyleForTerminal(Terminal terminal) {
            return super.getBaseVoltageStyle(terminal);
        }
    }
}
