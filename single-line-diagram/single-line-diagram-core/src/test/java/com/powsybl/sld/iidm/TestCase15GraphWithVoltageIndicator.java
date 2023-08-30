/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.layout.PositionVoltageLevelLayoutFactory;
import com.powsybl.sld.library.ResourcesComponentLibrary;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.svg.BusInfo;
import com.powsybl.sld.svg.DefaultLabelProvider;
import com.powsybl.sld.svg.DefaultSVGWriter;
import com.powsybl.sld.svg.LabelProvider;
import com.powsybl.sld.svg.styles.BasicStyleProvider;
import com.powsybl.sld.svg.styles.StyleProvider;
import com.powsybl.sld.svg.styles.iidm.TopologicalStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class TestCase15GraphWithVoltageIndicator extends AbstractTestCaseIidm {

    private static class BusVoltageInfo extends BusInfo {

        private final boolean powered;

        BusVoltageInfo(boolean powered) {
            super("VOLTAGE_INDICATOR");
            this.powered = powered;
        }

        BusVoltageInfo(boolean powered, String labelTop, String labelBottom, Side side) {
            super("VOLTAGE_INDICATOR", labelTop, labelBottom, side, null);
            this.powered = powered;
        }

        public boolean isPowered() {
            return powered;
        }
    }

    private LabelProvider withFullBusInfoProvider;

    private LabelProvider withIncompleteBusInfoProvider;

    @BeforeEach
    public void setUp() throws IOException {
        network = Networks.createNetworkWithFiveBusesFourLoads();
        graphBuilder = new NetworkGraphBuilder(network);
        svgParameters.setBusInfoMargin(5);

        withFullBusInfoProvider = new DefaultLabelProvider(network, componentLibrary, layoutParameters, svgParameters) {

            @Override
            public Optional<BusInfo> getBusInfo(BusNode node) {
                Objects.requireNonNull(node);
                BusInfo result = null;
                switch (node.getId()) {
                    case "bbs21":
                        result = new BusVoltageInfo(false);
                        break;
                    case "bbs1":
                    case "bbs13":
                        result = new BusVoltageInfo(true, "Top", null, Side.RIGHT);
                        break;
                    case "bbs22":
                    case "bbs23":
                        result = new BusVoltageInfo(false, null, "Bottom", Side.LEFT);
                        break;
                }
                return Optional.ofNullable(result);
            }
        };

        withIncompleteBusInfoProvider = new DefaultLabelProvider(network, componentLibrary, layoutParameters, svgParameters) {

            @Override
            public Optional<BusInfo> getBusInfo(BusNode node) {
                Objects.requireNonNull(node);
                BusInfo result = null;
                switch (node.getId()) {
                    case "bbs21":
                        result = new BusVoltageInfo(false);
                        break;
                    case "bbs13":
                        result = new BusVoltageInfo(true, "Top", null, Side.RIGHT);
                        break;
                    case "bbs23":
                        result = new BusVoltageInfo(false, null, "Bottom", Side.LEFT);
                        break;
                }
                return Optional.ofNullable(result);
            }
        };
    }

    @Override
    protected ResourcesComponentLibrary getResourcesComponentLibrary() {
        return new ResourcesComponentLibrary("VoltageIndicator", "/ConvergenceLibrary", "/VoltageIndicatorLibrary");
    }

    @Test
    void testWithoutBusInfo() {
        runTest(new BasicStyleProvider(), "/TestCase15GraphWithoutVoltageIndicator.svg", new DefaultLabelProvider(network, getResourcesComponentLibrary(), layoutParameters, svgParameters));
    }

    @Test
    void testBasic() {
        StyleProvider styleProvider = new BasicStyleProvider() {
            @Override
            public List<String> getBusInfoStyle(BusInfo info) {
                return List.of(((BusVoltageInfo) info).isPowered() ? "sld-powered" : "sld-unpowered");
            }
        };
        runTest(styleProvider, "/TestCase15GraphWithVoltageIndicator.svg", withIncompleteBusInfoProvider);
    }

    @Test
    void testTopological() {
        StyleProvider styleProvider = new TopologicalStyleProvider(network) {
            @Override
            public List<String> getBusInfoStyle(BusInfo info) {
                return List.of(((BusVoltageInfo) info).isPowered() ? "sld-powered" : "sld-unpowered");
            }
        };
        runTest(styleProvider, "/TestCase15GraphWithVoltageIndicatorTopological.svg", withFullBusInfoProvider);
    }

    private void runTest(StyleProvider styleProvider, String filename, LabelProvider labelProvider) {

        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph("vl1");

        // Run layout
        new PositionVoltageLevelLayoutFactory()
                .setBusInfoMap(labelProvider.getBusInfoSides(g))
                .setExceptionIfPatternNotHandled(true)
                .setHandleShunts(true)
                .create(g)
                .run(layoutParameters);

        // write SVG and compare to reference
        DefaultSVGWriter defaultSVGWriter = new DefaultSVGWriter(getResourcesComponentLibrary(), layoutParameters, svgParameters);
        assertEquals(toString(filename), toSVG(g, filename, defaultSVGWriter, labelProvider, styleProvider));
    }
}
