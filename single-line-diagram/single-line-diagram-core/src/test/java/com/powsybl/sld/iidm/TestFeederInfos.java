/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.ieeecdf.converter.IeeeCdfNetworkFactory;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.layout.SmartVoltageLevelLayoutFactory;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.svg.*;
import com.powsybl.sld.svg.styles.*;
import com.powsybl.sld.svg.styles.iidm.TopologicalStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.powsybl.sld.library.SldComponentTypeName.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <PRE>
 * l
 * |
 * b
 * |
 * d
 * |
 * ------ bbs
 * </PRE>
 *
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
class TestFeederInfos extends AbstractTestCaseIidm {

    protected VoltageLevel vl2;
    protected Substation substation2;
    protected Bus bus;
    protected Bus bus2;

    @BeforeEach
    public void setUp() {
        network = Network.create("testCase14", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = Networks.createSubstation(network, "s", "s", Country.FR);
        vl = Networks.createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380);
        substation2 = Networks.createSubstation(network, "s2", "s2", Country.FR);
        vl2 = Networks.createVoltageLevel(substation2, "vl2", "vl2", TopologyKind.NODE_BREAKER, 380);

        Networks.createBusBarSection(vl, "bbs", "bbs", 0, 1, 1);
        Networks.createBusBarSection(vl2, "bbs2", "bbs2", 5, 1, 1);

        Networks.createLoad(vl, "l", "l", "l", 0, ConnectablePosition.Direction.TOP, 2, 10, 10);
        Networks.createSwitch(vl, "d", "d", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        Networks.createSwitch(vl, "b", "b", SwitchKind.BREAKER, false, false, false, 1, 2);

        Networks.createLine(network, "line", "line", 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 3, 4, vl.getId(), vl2.getId(), "fn1", 1, ConnectablePosition.Direction.TOP, "fn2", 0, ConnectablePosition.Direction.TOP);
        Networks.createSwitch(vl, "d2", "d2", SwitchKind.DISCONNECTOR, false, false, false, 0, 3);
        Networks.createSwitch(vl2, "d3", "d3", SwitchKind.DISCONNECTOR, false, false, false, 4, 5);
    }

    @Test
    void testManyFeederInfos() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        layoutParameters.setSpaceForFeederInfos(100);

        svgParameters.setPowerValuePrecision(3)
                .setCurrentValuePrecision(2)
                .setPercentageValuePrecision(2)
                .setFeederInfosIntraMargin(5)
                .setDisplayCurrentFeederInfo(true)
                .setDisplayPermanentLimitPercentageFeederInfo(true);

        // Run layout
        voltageLevelGraphLayout(g);

        // many feeder values provider example for the test:
        LabelProvider labelManyFeederInfoProvider = new DefaultLabelProvider(network, componentLibrary, layoutParameters, svgParameters) {

            @Override
            public List<FeederInfo> getFeederInfos(FeederNode node) {
                List<FeederInfo> feederInfos = Arrays.asList(
                        new DirectionalFeederInfo(ARROW_ACTIVE, 1000.967543, valueFormatter::formatPower, null),
                        new DirectionalFeederInfo(ARROW_REACTIVE, Double.NaN, valueFormatter::formatPower, null),
                        new DirectionalFeederInfo(ARROW_REACTIVE, LabelDirection.IN, null, "3000", null),
                        new DirectionalFeederInfo(ARROW_ACTIVE, LabelDirection.OUT, null, "40", null), // Not displayed
                        new DirectionalFeederInfo(ARROW_ACTIVE, LabelDirection.OUT, null, "50", null),
                        new DirectionalFeederInfo(ARROW_CURRENT, 123.456789, valueFormatter::formatCurrent),
                        new ValueFeederInfo(VALUE_PERMANENT_LIMIT_PERCENTAGE, 30, valueFormatter::formatPercentage));
                boolean feederArrowSymmetry = node.getDirection() == Direction.TOP || svgParameters.isFeederInfoSymmetry();
                if (!feederArrowSymmetry) {
                    Collections.reverse(feederInfos);
                }
                return feederInfos;
            }

            @Override
            public List<LabelProvider.NodeDecorator> getNodeDecorators(Node node, Direction direction) {
                return new ArrayList<>();
            }

        };

// write SVG and compare to reference
        assertEquals(toString("/TestFeederInfos.svg"), toSVG(g, "/TestFeederInfos.svg", componentLibrary, layoutParameters, svgParameters, labelManyFeederInfoProvider, new BasicStyleProvider()));
    }

    @Test
    void testAllPossibleInfoItems() {
        layoutParameters.setSpaceForFeederInfos(100);
        svgParameters.setFeederInfosIntraMargin(5)
                .setPowerValuePrecision(0)
                .setCurrentValuePrecision(0)
                .setPercentageValuePrecision(1)
                .setDisplayCurrentFeederInfo(true)
                .setDisplayPermanentLimitPercentageFeederInfo(true);

        // build graph
        network.getLoad("l").getTerminal().setP(100).setQ(10);
        network.getLine("line").getTerminal1().setP(100).setQ(10).connect();
        network.getLine("line").getTerminal2().setP(90).setQ(10).connect();

        network.getLine("line").getTerminal1().getBusView().getBus().setV(vl.getNominalV());
        network.getLine("line").getTerminal2().getBusView().getBus().setV(vl2.getNominalV());

        network.getLine("line").getOrCreateSelectedOperationalLimitsGroup1().newCurrentLimits().setPermanentLimit(100).add();
        network.getLine("line").getOrCreateSelectedOperationalLimitsGroup2().newCurrentLimits().setPermanentLimit(200).add();

        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write SVG and compare to reference
        assertEquals(toString("/TestAllPossibleInfoItems.svg"), toSVG(g, "/TestAllPossibleInfoItems.svg", componentLibrary, layoutParameters, svgParameters, getDefaultDiagramLabelProvider(), new BasicStyleProvider()));
    }

    @Test
    void testFrenchFormatting() {

        // Add power values to the equipments
        network.getLoad("l").getTerminal().setP(1200.29).setQ(-1);
        network.getLine("line").getTerminal1().setP(1010).setQ(-10).connect();
        network.getLine("line").getTerminal2().setP(10).setQ(10).connect();

        network.getLine("line").getTerminal1().getBusView().getBus().setV(vl.getNominalV());
        network.getLine("line").getTerminal2().getBusView().getBus().setV(vl2.getNominalV());

        svgParameters.setLanguageTag("fr")
                .setPowerValuePrecision(1)
                .setCurrentValuePrecision(1)
                .setPercentageValuePrecision(0)
                .setDisplayCurrentFeederInfo(true)
                .setDisplayPermanentLimitPercentageFeederInfo(true);

        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write SVG and compare to reference
        assertEquals(toString("/TestFormattingFeederInfos.svg"), toSVG(g, "/TestFormattingFeederInfos.svg", componentLibrary, layoutParameters, svgParameters, getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider()));
    }

    @Test
    void testBuildFeederInfosWithCurrent() {
        Network network = IeeeCdfNetworkFactory.create9();
        svgParameters.setDisplayCurrentFeederInfo(true).setDisplayPermanentLimitPercentageFeederInfo(true);
        VoltageLevelGraph g = new NetworkGraphBuilder(network).buildVoltageLevelGraph("VL5");
        List<FeederInfo> feederInfoList = new DefaultLabelProvider(network, componentLibrary, layoutParameters, svgParameters).getFeederInfos(g.getFeederNodes().get(0));
        assertEquals(3, feederInfoList.size());
        assertEquals(ARROW_ACTIVE, feederInfoList.get(0).getComponentType());
        assertEquals(ARROW_REACTIVE, feederInfoList.get(1).getComponentType());
        assertEquals(ARROW_CURRENT, feederInfoList.get(2).getComponentType());
    }

    @Test
    void testBuildFeederInfosWithoutCurrent() {
        Network network = IeeeCdfNetworkFactory.create9();
        svgParameters.setDisplayCurrentFeederInfo(false).setDisplayPermanentLimitPercentageFeederInfo(true);
        VoltageLevelGraph g = new NetworkGraphBuilder(network).buildVoltageLevelGraph("VL5");
        List<FeederInfo> feederInfoList = new DefaultLabelProvider(network, componentLibrary, layoutParameters, svgParameters).getFeederInfos(g.getFeederNodes().get(0));
        assertEquals(2, feederInfoList.size());
        assertEquals(ARROW_ACTIVE, feederInfoList.get(0).getComponentType());
        assertEquals(ARROW_REACTIVE, feederInfoList.get(1).getComponentType());
    }

    @Test
    void testAnimation() {

        // Add load at bottom
        Networks.createSwitch(vl, "d4", "d4", SwitchKind.DISCONNECTOR, false, false, false, 0, 10);
        Networks.createLoad(vl, "l2", "l2", "l2", 0, ConnectablePosition.Direction.BOTTOM, 10, 10, 10);

        // Add power values to the load
        network.getLoad("l").getTerminal().setP(1200.29);
        network.getLoad("l").getTerminal().setQ(-1.0);

        network.getLoad("l2").getTerminal().setP(501.0);
        network.getLoad("l2").getTerminal().setQ(0.0);

        svgParameters.setFeederInfosIntraMargin(20);

        LabelProvider labelProvider = new DefaultLabelProvider(network, componentLibrary, layoutParameters, svgParameters) {

            @Override
            public List<FeederInfo> getFeederInfos(FeederNode node) {
                Load l = network.getLoad("l");
                Load l2 = network.getLoad("l2");

                if (Objects.equals(l.getNameOrId(), node.getEquipmentId())) {
                    return Arrays.asList(
                            new DirectionalFeederInfo(ARROW_ACTIVE, l.getTerminal().getP(), valueFormatter::formatPower, null),
                            new DirectionalFeederInfo(ARROW_REACTIVE, l.getTerminal().getQ(), valueFormatter::formatPower, null));
                } else {
                    return Arrays.asList(
                            new DirectionalFeederInfo(ARROW_ACTIVE, l2.getTerminal().getP(), valueFormatter::formatPower, null),
                            new DirectionalFeederInfo(ARROW_REACTIVE, l2.getTerminal().getQ(), valueFormatter::formatPower, null),
                            new DirectionalFeederInfo(ARROW_REACTIVE, Double.NaN, valueFormatter::formatPower, null),
                            new FeederInfo() {
                                @Override
                                public String getUserDefinedId() {
                                    return null;
                                }

                                @Override
                                public String getComponentType() {
                                    return ARROW_ACTIVE;
                                }

                                @Override
                                public Optional<String> getLeftLabel() {
                                    return Optional.of("Left");
                                }

                                @Override
                                public Optional<String> getRightLabel() {
                                    return Optional.of("Right");
                                }
                            });
                }
            }

        };

        StyleProvider styleProvider = new StyleProvidersList(
                new TopologicalStyleProvider(network),
                new AnimatedFeederInfoStyleProvider(500, 1000));

        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write SVG and compare to reference
        assertEquals(toString("/TestAnimatedFeederInfos.svg"), toSVG(g, "/TestAnimatedFeederInfos.svg", componentLibrary, layoutParameters, svgParameters, labelProvider, styleProvider));
    }

    @Test
    void testBuildFeederInfosWithUnits() {
        Network network = IeeeCdfNetworkFactory.create9();
        svgParameters.setDisplayCurrentFeederInfo(true)
                .setDisplayPermanentLimitPercentageFeederInfo(true)
                .setActivePowerUnit("MW")
                .setReactivePowerUnit("MVAR")
                .setCurrentUnit("A");
        VoltageLevelGraph g = new NetworkGraphBuilder(network).buildVoltageLevelGraph("VL5");
        new SmartVoltageLevelLayoutFactory(network).create(g).run(layoutParameters);
        assertEquals(toString("/TestUnitsOnFeederInfos.svg"), toSVG(g, "/TestUnitsOnFeederInfos.svg", componentLibrary, layoutParameters, svgParameters, new DefaultLabelProvider(network, componentLibrary, layoutParameters, svgParameters), new NominalVoltageStyleProvider()));
    }
}
