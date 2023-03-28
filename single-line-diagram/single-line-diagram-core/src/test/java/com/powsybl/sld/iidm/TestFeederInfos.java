/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.sld.SingleLineDiagramConfiguration;
import com.powsybl.sld.SingleLineDiagramConfigurationBuilder;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.svg.*;
import com.powsybl.sld.util.AnimatedFeederInfoStyleProvider;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static com.powsybl.sld.library.ComponentTypeName.ARROW_ACTIVE;
import static com.powsybl.sld.library.ComponentTypeName.ARROW_REACTIVE;
import static org.junit.Assert.assertEquals;

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
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class TestFeederInfos extends AbstractTestCaseIidm {

    @Before
    public void setUp() {
        network = Network.create("testCase14", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = createSubstation(network, "s", "s", Country.FR);
        vl = createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380, 10);
        createBusBarSection(vl, "bbs", "bbs", 0, 1, 1);
        createLoad(vl, "l", "l", "l", 0, ConnectablePosition.Direction.TOP, 2, 10, 10);
        createSwitch(vl, "d", "d", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        createSwitch(vl, "b", "b", SwitchKind.BREAKER, false, false, false, 1, 2);
    }

    @Test
    public void testManyFeederInfos() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        layoutParameters.setSpaceForFeederInfos(100);

        svgParameters.setPowerValuePrecision(3)
                     .setFeederInfosIntraMargin(5);

        // Run layout
        voltageLevelGraphLayout(g);

        // many feeder values provider example for the test:
        DiagramLabelProviderFactory diagramLabelManyFeederInfoProviderFactory = new DefaultDiagramLabelProviderFactory() {
            @Override
            public DiagramLabelProvider create(Network network, ComponentLibrary componentLibrary, LayoutParameters layoutParameters, SvgParameters svgParameters) {
                return new DefaultDiagramLabelProvider(Network.create("empty", ""), componentLibrary, layoutParameters, svgParameters) {

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
                    public List<DiagramLabelProvider.NodeDecorator> getNodeDecorators(Node node, Direction direction) {
                        return new ArrayList<>();
                    }
                };
            }
        };

        // write SVG and compare to reference
        SingleLineDiagramConfiguration singleLineDiagramConfiguration = new SingleLineDiagramConfigurationBuilder(network)
                .withLayoutParameters(layoutParameters)
                .withComponentLibrary(componentLibrary)
                .withSvgParameters(svgParameters)
                .withDiagramLabelProviderFactory(diagramLabelManyFeederInfoProviderFactory)
                .withDiagramStyleProvider(new BasicStyleProvider())
                .build();
        assertEquals(toString("/TestFeederInfos.svg"), toSVG(g, "/TestFeederInfos.svg", singleLineDiagramConfiguration));
    }

    @Test
    public void testFrenchFormatting() {
        // Add power values to the load
        network.getLoad("l").getTerminal().setP(1200.29);
        network.getLoad("l").getTerminal().setQ(-1);

        svgParameters.setLanguageTag("fr").setPowerValuePrecision(1);

        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write SVG and compare to reference
        SingleLineDiagramConfiguration singleLineDiagramConfiguration = new SingleLineDiagramConfigurationBuilder(network)
                .withSvgParameters(svgParameters)
                .build();
        assertEquals(toString("/TestFormattingFeederInfos.svg"), toSVG(g, "/TestFormattingFeederInfos.svg", singleLineDiagramConfiguration));
    }

    @Test
    public void testAnimation() {
        // Add load at bottom
        createSwitch(vl, "d2", "d2", SwitchKind.DISCONNECTOR, false, false, false, 0, 3);
        createLoad(vl, "l2", "l2", "l2", 0, ConnectablePosition.Direction.BOTTOM, 3, 10, 10);

        // Add power values to the load
        network.getLoad("l").getTerminal().setP(1200.29);
        network.getLoad("l").getTerminal().setQ(-1.0);

        network.getLoad("l2").getTerminal().setP(501.0);
        network.getLoad("l2").getTerminal().setQ(0.0);

        layoutParameters.setFeederInfosIntraMargin(20);

        DiagramLabelProvider labelProvider = new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters) {
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

        DiagramStyleProvider styleProvider = new AnimatedFeederInfoStyleProvider(network, 500, 1000);

        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write SVG and compare to reference
        assertEquals(toString("/TestAnimatedFeederInfos.svg"), toSVG(g, "/TestAnimatedFeederInfos.svg", labelProvider, styleProvider));
    }
}
