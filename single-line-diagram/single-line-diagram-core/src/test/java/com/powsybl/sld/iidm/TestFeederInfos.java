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

    @BeforeEach
    public void setUp() {
        network = Network.create("testCase14", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = Networks.createSubstation(network, "s", "s", Country.FR);
        vl = Networks.createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380);
        Networks.createBusBarSection(vl, "bbs", "bbs", 0, 1, 1);
        Networks.createLoad(vl, "l", "l", "l", 0, ConnectablePosition.Direction.TOP, 2, 10, 10);
        Networks.createSwitch(vl, "d", "d", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        Networks.createSwitch(vl, "b", "b", SwitchKind.BREAKER, false, false, false, 1, 2);
    }

    @Test
    void testManyFeederInfos() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        layoutParameters.setSpaceForFeederInfos(100);

        svgParameters.setPowerValuePrecision(3)
                .setFeederInfosIntraMargin(5)
                .setDisplayCurrentFeederInfo(true);

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
                        new DirectionalFeederInfo(ARROW_ACTIVE, LabelDirection.OUT, null, "50", null));
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

        LegendProvider legendProvider = new DefaultLegendProvider(network, svgParameters);

// write SVG and compare to reference
        assertEquals(toString("/TestFeederInfos.svg"), toSVG(g, "/TestFeederInfos.svg", componentLibrary, layoutParameters, svgParameters, labelManyFeederInfoProvider, new BasicStyleProvider(), legendProvider));
    }

    @Test
    void testAllPossibleInfoItems() {
        layoutParameters.setSpaceForFeederInfos(100);
        svgParameters.setFeederInfosIntraMargin(5)
                .setPowerValuePrecision(0)
                .setDisplayCurrentFeederInfo(true);
        // build graph
        network.getLoad("l").getTerminal().setP(100).setQ(10).getBusView().getBus().setV(vl.getNominalV());
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write SVG and compare to reference
        assertEquals(toString("/TestAllPossibleInfoItems.svg"), toSVG(g, "/TestAllPossibleInfoItems.svg", componentLibrary, layoutParameters, svgParameters, getDefaultDiagramLabelProvider(), new BasicStyleProvider(), getDefaultDiagramLegendProvider()));
    }

    @Test
    void testFrenchFormatting() {

        // Add power values to the load
        network.getLoad("l").getTerminal().setP(1200.29);
        network.getLoad("l").getTerminal().setQ(-1);

        svgParameters.setLanguageTag("fr").setPowerValuePrecision(1);

        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write SVG and compare to reference
        assertEquals(toString("/TestFormattingFeederInfos.svg"), toSVG(g, "/TestFormattingFeederInfos.svg", componentLibrary, layoutParameters, svgParameters, getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider(), getDefaultDiagramLegendProvider()));
    }

    @Test
    void testBuildFeederInfosWithCurrent() {
        Network network = IeeeCdfNetworkFactory.create9();
        svgParameters.setDisplayCurrentFeederInfo(true);
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
        svgParameters.setDisplayCurrentFeederInfo(false);
        VoltageLevelGraph g = new NetworkGraphBuilder(network).buildVoltageLevelGraph("VL5");
        List<FeederInfo> feederInfoList = new DefaultLabelProvider(network, componentLibrary, layoutParameters, svgParameters).getFeederInfos(g.getFeederNodes().get(0));
        assertEquals(2, feederInfoList.size());
        assertEquals(ARROW_ACTIVE, feederInfoList.get(0).getComponentType());
        assertEquals(ARROW_REACTIVE, feederInfoList.get(1).getComponentType());
    }

    @Test
    void testAnimation() {

        // Add load at bottom
        Networks.createSwitch(vl, "d2", "d2", SwitchKind.DISCONNECTOR, false, false, false, 0, 3);
        Networks.createLoad(vl, "l2", "l2", "l2", 0, ConnectablePosition.Direction.BOTTOM, 3, 10, 10);

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
        assertEquals(toString("/TestAnimatedFeederInfos.svg"), toSVG(g, "/TestAnimatedFeederInfos.svg", componentLibrary, layoutParameters, svgParameters, labelProvider, styleProvider, new DefaultLegendProvider(network, svgParameters)));
    }

    @Test
    void testBuildFeederInfosWithUnits() {
        Network network = IeeeCdfNetworkFactory.create9();
        svgParameters.setDisplayCurrentFeederInfo(true)
                .setActivePowerUnit("MW")
                .setReactivePowerUnit("MVAR")
                .setCurrentUnit("A");
        VoltageLevelGraph g = new NetworkGraphBuilder(network).buildVoltageLevelGraph("VL5");
        new SmartVoltageLevelLayoutFactory(network).create(g).run(layoutParameters);
        assertEquals(toString("/TestUnitsOnFeederInfos.svg"), toSVG(g, "/TestUnitsOnFeederInfos.svg", componentLibrary, layoutParameters, svgParameters, new DefaultLabelProvider(network, componentLibrary, layoutParameters, svgParameters), new NominalVoltageStyleProvider(), new DefaultLegendProvider(network, svgParameters)));
    }
}
