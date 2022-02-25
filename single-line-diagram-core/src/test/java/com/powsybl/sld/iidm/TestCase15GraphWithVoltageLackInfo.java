/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import com.powsybl.sld.layout.BlockOrganizer;
import com.powsybl.sld.layout.ImplicitCellDetector;
import com.powsybl.sld.layout.PositionVoltageLevelLayout;
import com.powsybl.sld.layout.positionfromextension.PositionFromExtension;
import com.powsybl.sld.library.ComponentTypeName;
import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.VoltageLevelGraph;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.svg.*;
import com.powsybl.sld.util.NominalVoltageDiagramStyleProvider;
import com.powsybl.sld.util.TopologicalStyleProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
@RunWith(Parameterized.class)
public class TestCase15GraphWithVoltageLackInfo extends AbstractTestCaseIidm {

    enum StyleProviderType { BASIC, TOPOLOGICAL, NOMINAL };

    @Parameterized.Parameters(name = "StyleProvider {1}")
    public static Collection<Object[]> params() {
        return Arrays.asList(
                // Filename, StyleProvider
                new Object[] {"/TestCase15GraphWithVoltageLackInfo.svg", StyleProviderType.BASIC},
                new Object[] {"/TestCase15GraphWithVoltageLackInfoTopological.svg", StyleProviderType.TOPOLOGICAL},
                new Object[] {"/TestCase15GraphWithVoltageLackInfoNominal.svg", StyleProviderType.NOMINAL}
        );
    }

    private DiagramLabelProvider withBusInfoProvider;

    private final String filename;

    private final StyleProviderType styleProviderType;

    public TestCase15GraphWithVoltageLackInfo(String filename, StyleProviderType styleProviderType) {
        this.filename = filename;
        this.styleProviderType = styleProviderType;
    }

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

        withBusInfoProvider = new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters) {
            @Override
            public Optional<BusInfo> getBusInfo(BusNode node) {
                Objects.requireNonNull(node);
                BusInfo result;
                if (node.getBusbarIndex() % 2 != 0) {
                    result = new BusInfo(ComponentTypeName.LACK_VOLTAGE, "Left", null,
                            Side.RIGHT, null);
                } else {
                    result = new BusInfo(ComponentTypeName.LACK_VOLTAGE, null, "Right",
                            Side.LEFT, null);
                }
                return Optional.of(result);
            }
        };
    }

    @Test
    public void test() throws IOException {
        layoutParameters.setAdaptCellHeightToContent(true)
                .setBusInfoMargin(5);

        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId(), true);

        // Run layout
        new ImplicitCellDetector().detectCells(g);
        new BlockOrganizer(new PositionFromExtension(), true, true, true, true).organize(g);
        new PositionVoltageLevelLayout(g).run(layoutParameters);

        DiagramStyleProvider styleProvider = null;
        switch (styleProviderType) {
            case BASIC:
                styleProvider = new BasicStyleProvider() {
                    @Override
                    public List<String> getCssFilenames() {
                        return Arrays.asList("tautologies.css", "TestWithLackVoltageIndicator.css");
                    }
                };
                break;
            case TOPOLOGICAL:
                styleProvider = new TopologicalStyleProvider(network) {
                    @Override
                    public List<String> getCssFilenames() {
                        return Arrays.asList("tautologies.css", "topologicalBaseVoltages.css", "highlightLineStates.css", "TestWithLackVoltageIndicator.css");
                    }
                };
                break;
            case NOMINAL:
                styleProvider = new NominalVoltageDiagramStyleProvider(network) {
                    @Override
                    public List<String> getCssFilenames() {
                        return Arrays.asList("tautologies.css", "baseVoltages.css", "highlightLineStates.css", "TestWithLackVoltageIndicator.css");
                    }
                };
                break;
            default:
                Assert.fail("StyleProviderType[" + styleProviderType + "] need to be managed here");
        }

        // write SVG and compare to reference
        assertEquals(toString(this.filename), toSVG(g, this.filename, withBusInfoProvider, styleProvider));
    }
}
