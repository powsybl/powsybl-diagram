/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import com.powsybl.sld.layout.BlockOrganizer;
import com.powsybl.sld.layout.ImplicitCellDetector;
import com.powsybl.sld.layout.PositionVoltageLevelLayout;
import com.powsybl.sld.layout.positionfromextension.PositionFromExtension;
import com.powsybl.sld.library.ResourcesComponentLibrary;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.svg.*;
import com.powsybl.sld.util.TopologicalStyleProvider;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class TestCase15GraphWithVoltageIndicator extends AbstractTestCaseIidm {

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

    private DiagramLabelProvider withFullBusInfoProvider;

    private DiagramLabelProvider withIncompleteBusInfoProvider;

    @Before
    public void setUp() throws IOException {
        int order = 0;
        network = Network.create("TestSingleLineDiagramClass", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = createSubstation(network, "s", "s", Country.FR);
        vl = createVoltageLevel(substation, "vl1", "vl1", TopologyKind.NODE_BREAKER, 380, 10);
        createBusBarSection(vl, "bbs1", "bbs1", 0, 1, 1);
        createBusBarSection(vl, "bbs21", "bbs21", 1, 2, 1);
        createBusBarSection(vl, "bbs22", "bbs22", 2, 2, 2);
        createSwitch(vl, "fA", "fA", SwitchKind.BREAKER, false, false, false, 3, 4);
        createLoad(vl, "loadA", "loadA", "loadA", order++, ConnectablePosition.Direction.TOP, 4, 10, 10);
        createSwitch(vl, "d1", "d1", SwitchKind.DISCONNECTOR, false, false, false, 0, 3);
        createSwitch(vl, "d2", "d2", SwitchKind.DISCONNECTOR, false, false, false, 1, 3);

        createSwitch(vl, "fB", "fB", SwitchKind.BREAKER, false, false, false, 5, 6);
        createLoad(vl, "loadB", "loadB", "loadB", order++, ConnectablePosition.Direction.TOP, 6, 10, 10);
        createSwitch(vl, "b1", "b1", SwitchKind.DISCONNECTOR, false, false, false, 2, 5);
        createSwitch(vl, "b2", "b2", SwitchKind.DISCONNECTOR, false, false, false, 0, 5);

        createBusBarSection(vl, "bbs13", "bbs13", 7, 1, 3);
        createBusBarSection(vl, "bbs23", "bbs23", 8, 2, 3);
        createLoad(vl, "loadC", "loadC", "loadC", order++, ConnectablePosition.Direction.TOP, 9, 10, 10);
        createSwitch(vl, "c1", "c1", SwitchKind.BREAKER, false, false, false, 8, 9);
        createSwitch(vl, "c2", "c2", SwitchKind.BREAKER, false, false, false, 7, 9);

        createSwitch(vl, "link", "link", SwitchKind.BREAKER, false, false, false, 5, 9);

        withFullBusInfoProvider = new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters) {
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

        withIncompleteBusInfoProvider = new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters) {
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
    public void testWithoutBusInfo() {
        runTest(new BasicStyleProvider(),  "/TestCase15GraphWithoutVoltageIndicator.svg", new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters));
    }

    @Test
    public void testBasic() {
        DiagramStyleProvider styleProvider = new BasicStyleProvider() {
            @Override
            public Optional<String> getBusInfoStyle(BusInfo info) {
                return Optional.of(((BusVoltageInfo) info).isPowered() ? "sld-powered" : "sld-unpowered");
            }
        };
        runTest(styleProvider,  "/TestCase15GraphWithVoltageIndicator.svg", withIncompleteBusInfoProvider);
    }

    @Test
    public void testTopological() {
        DiagramStyleProvider styleProvider = new TopologicalStyleProvider(network) {
            @Override
            public Optional<String> getBusInfoStyle(BusInfo info) {
                return Optional.of(((BusVoltageInfo) info).isPowered() ? "sld-powered" : "sld-unpowered");
            }
        };
        runTest(styleProvider, "/TestCase15GraphWithVoltageIndicatorTopological.svg", withFullBusInfoProvider);
    }

    private void runTest(DiagramStyleProvider styleProvider, String filename, DiagramLabelProvider labelProvider) {
        layoutParameters.setAdaptCellHeightToContent(true)
                .setBusInfoMargin(5);

        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        // Run layout
        BlockOrganizer blockOrganizer = new BlockOrganizer(new PositionFromExtension(), true, true, true, labelProvider.getBusInfoSides(g));
        new PositionVoltageLevelLayout(g, new ImplicitCellDetector(), blockOrganizer).run(layoutParameters);

        // write SVG and compare to reference
        assertEquals(toString(filename), toSVG(g, filename, labelProvider, styleProvider));
    }
}
