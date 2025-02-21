/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.model.BranchEdge;
import com.powsybl.nad.model.BusNode;
import com.powsybl.nad.model.ThreeWtEdge;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.TopologicalStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class OverridingStyleTest extends AbstractTest {

    @BeforeEach
    void setup() {
        setLayoutParameters(new LayoutParameters());
        setSvgParameters(new SvgParameters()
                .setInsertNameDesc(true)
                .setSvgWidthAndHeightAdded(true)
                .setFixedWidth(800));
    }

    @Override
    protected StyleProvider getStyleProvider(Network network) {
        return new TopologicalStyleProvider(network) {

            @Override
            public String getBusNodeStyle(BusNode busNode) {
                String equipmentId = busNode.getEquipmentId();
                if (equipmentId.startsWith("FFR")) {
                    return busNode.getRingIndex() == 0 ? "fill:blue; background:blue" : "fill:lightblue; background:lightblue";
                } else if (equipmentId.startsWith("NNL")) {
                    return "fill:orange; background:orange";
                } else if (equipmentId.startsWith("BBE")) {
                    return busNode.getRingIndex() == 0 ? "fill:yellow; background:yellow" : "fill:deeppink; background:deeppink";
                } else if (equipmentId.startsWith("DDE")) {
                    return "fill:red; stroke: black; stroke-width: 2px; background:red; border: solid 2px";
                }
                return null;
            }

            @Override
            public String getSideEdgeStyle(BranchEdge edge, BranchEdge.Side side) {
                String equipmentId = edge.getEquipmentId();
                if (!equipmentId.substring(0, 3).equals(equipmentId.substring(9, 12))) {
                    return "stroke-dasharray:15,9;stroke-width:10px;stroke:black";
                }
                return null;
            }

            @Override
            public String getThreeWtEdgeStyle(ThreeWtEdge threeWtEdge) {
                return switch (threeWtEdge.getSide()) {
                    case ONE -> "stroke: lightgray; stroke-dasharray:4,4;";
                    case TWO -> "stroke: gray; stroke-dasharray:8,8";
                    case THREE -> "stroke: black; stroke-dasharray:12,12";
                };
            }
        };
    }

    @Override
    protected LabelProvider getLabelProvider(Network network) {
        return new DefaultLabelProvider(network, getSvgParameters());
    }

    @Test
    void testEurope() {
        Network network = Network.read("simple-eu.uct", getClass().getResourceAsStream("/simple-eu.uct"));
        assertEquals(toString("/simple-eu_overridden_styles.svg"), generateSvgString(network, "/simple-eu_overridden_styles.svg"));
    }

    @Test
    void test3wt() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        assertEquals(toString("/3wt_overridden_styles.svg"), generateSvgString(network, "/3wt_overridden_styles.svg"));
    }
}
