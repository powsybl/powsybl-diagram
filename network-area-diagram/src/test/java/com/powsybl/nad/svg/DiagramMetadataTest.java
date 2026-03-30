/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.diagram.test.Networks;
import com.powsybl.ieeecdf.converter.IeeeCdfNetworkFactory;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.ScadaNetworkFactory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.build.iidm.IntIdProvider;
import com.powsybl.nad.build.iidm.NetworkGraphBuilder;
import com.powsybl.nad.build.iidm.VoltageLevelFilter;
import com.powsybl.nad.layout.BasicForceLayout;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.TopologicalStyleProvider;
import com.powsybl.nad.svg.metadata.DiagramMetadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
class DiagramMetadataTest extends AbstractTest {

    private FileSystem fileSystem;
    private Path tmpDir;
    private LabelProvider labelProvider;

    @BeforeEach
    void setup() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        tmpDir = Files.createDirectory(fileSystem.getPath("tmp"));
        setLayoutParameters(new LayoutParameters());
        setSvgParameters(new SvgParameters()
                .setInsertNameDesc(true)
                .setSvgWidthAndHeightAdded(true)
                .setFixedWidth(800));
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    @Override
    protected StyleProvider getStyleProvider(Network network) {
        return new TopologicalStyleProvider(network);
    }

    @Override
    protected LabelProvider getLabelProvider(Network network) {
        return labelProvider;
    }

    @Test
    void test() throws IOException {
        // Referenced json file
        String referenceMetadata = "/hvdc_metadata.json";
        InputStream in = Objects.requireNonNull(getClass().getResourceAsStream(referenceMetadata));
        // Create Metadata from json file
        DiagramMetadata metadata = DiagramMetadata.parseJson(in);
        // Write Metadata as temporary json file
        Path outMetadataPath = tmpDir.resolve("metadata.json");
        try (Writer writer = Files.newBufferedWriter(outMetadataPath, StandardCharsets.UTF_8)) {
            metadata.writeJson(writer);
        }
        // Checking
        assertFileEquals(referenceMetadata, outMetadataPath);
    }

    @Test
    void test3wt() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        labelProvider = new DefaultLabelProvider(network, getSvgParameters());
        DiagramMetadata diagramMetadata = roundTrip(network, "/3wt_metadata.json", getLayoutParameters());
        assertEquals(3, diagramMetadata.getBusNodesMetadata().size());
        assertEquals(4, diagramMetadata.getNodesMetadata().size());
        assertEquals(3, diagramMetadata.getEdgesMetadata().size());
        assertEquals(3, diagramMetadata.getTextNodesMetadata().size());
    }

    @Test
    void testFictitious() {
        Network network = IeeeCdfNetworkFactory.create14();
        network.getVoltageLevel("VL12").setFictitious(true);
        network.getVoltageLevel("VL14").setFictitious(true);
        labelProvider = new DefaultLabelProvider(network, getSvgParameters());
        DiagramMetadata diagramMetadata = roundTrip(network, "/IEEE_14_bus_fictitious_metadata.json", getLayoutParameters());
        assertEquals(14, diagramMetadata.getBusNodesMetadata().size());
        assertEquals(14, diagramMetadata.getNodesMetadata().size());
        assertEquals(20, diagramMetadata.getEdgesMetadata().size());
        assertEquals(14, diagramMetadata.getTextNodesMetadata().size());
    }

    @Test
    void testInjections() {
        Network network = IeeeCdfNetworkFactory.create14();
        labelProvider = new DefaultLabelProvider(network, getSvgParameters());
        roundTrip(network, "/IEEE_14_bus_injections_metadata.json", new LayoutParameters().setInjectionsAdded(true));
    }

    @Test
    void testUnknownBus() {
        Network network = ScadaNetworkFactory.create();
        labelProvider = new DefaultLabelProvider(network, getSvgParameters());
        roundTrip(network, "/scada_network.json", new LayoutParameters().setInjectionsAdded(true));
    }

    @Test
    void testOverUnderVoltage() {
        Network network = Networks.createTwoVoltageLevelsThreeBuses();
        network.getVoltageLevel("vl1")
                .setHighVoltageLimit(385)
                .getBusView().getBus("vl1_0").setV(385.1);
        network.getVoltageLevel("vl2")
                .setLowVoltageLimit(390)
                .getBusView().getBus("vl2_0").setV(388);
        labelProvider = new DefaultLabelProvider(network, getSvgParameters());
        roundTrip(network, "/under_over_voltage_metadata.json", new LayoutParameters().setInjectionsAdded(true));
    }

    @Test
    void testEdgeInfoMetadata() {
        Network network = Networks.createTwoVoltageLevels();
        labelProvider = new DefaultLabelProvider.Builder()
                .setInfoSideExternal(DefaultLabelProvider.EdgeInfoEnum.CURRENT)
                .setInfoSideInternal(DefaultLabelProvider.EdgeInfoEnum.ACTIVE_POWER)
                .setInfoMiddleSide1(DefaultLabelProvider.EdgeInfoEnum.NAME)
                .setInfoMiddleSide2(DefaultLabelProvider.EdgeInfoEnum.EMPTY)
                .build(network, getSvgParameters());
        Line line = network.getLine("l1");
        line.getTerminal1().setP(1400.0).setQ(400.0);
        line.getTerminal2().setP(1410.0).setQ(410.0);
        line.getTerminal1().getBusBreakerView().getBus().setV(400.0);
        line.getTerminal2().getBusBreakerView().getBus().setV(410.0);
        roundTrip(network, "/edge_info_metadata.json", new LayoutParameters().setInjectionsAdded(true));
    }

    @Test
    void testCustomStyleProviderMetadata() {
        Network network = Networks.createNodeBreakerNetworkWithBranchStatus("TestNodeDecorators", "test");

        Map<String, CustomStyleProvider.BusNodeStyles> busNodesStyles = new HashMap<>();
        busNodesStyles.put("VL1_10", new CustomStyleProvider.BusNodeStyles("yellow", null, "2px"));
        busNodesStyles.put("VL2_30", new CustomStyleProvider.BusNodeStyles("red", "black", "4px"));

        Map<String, CustomStyleProvider.EdgeStyles> edgesStyles = new HashMap<>();
        edgesStyles.put("L11", new CustomStyleProvider.EdgeStyles("blue", "2px", null, "blue", "2px", null));
        edgesStyles.put("L12", new CustomStyleProvider.EdgeStyles("green", "4px", "8,4", "green", "4px", "4,2"));
        edgesStyles.put("T11", new CustomStyleProvider.EdgeStyles("red", "8px", null, "brown", "8px", null));
        edgesStyles.put("T12", new CustomStyleProvider.EdgeStyles("orange", null, null, "orange", null, null));

        Map<String, CustomStyleProvider.ThreeWtStyles> threeWtsStyles = new HashMap<>();
        threeWtsStyles.put("T3_12",
                new CustomStyleProvider.ThreeWtStyles(
                        "gray", "1px", null,
                        "purple", "2px", "2,2",
                        "pink", "3px", null
                )
        );

        labelProvider = new DefaultLabelProvider(network, getSvgParameters());
        StyleProvider styleProvider = new CustomStyleProvider(busNodesStyles, edgesStyles, threeWtsStyles);
        roundTrip(network, "/custom_style_metadata.json", new LayoutParameters().setInjectionsAdded(true), styleProvider);
    }

    private DiagramMetadata roundTrip(Network network, String referenceMetadata, LayoutParameters layoutParameters) {
        return roundTrip(network, referenceMetadata, layoutParameters, getStyleProvider(network));
    }

    private DiagramMetadata roundTrip(Network network, String referenceMetadata, LayoutParameters layoutParameters, StyleProvider styleProvider) {
        Graph graph = new NetworkGraphBuilder(network, VoltageLevelFilter.NO_FILTER, getLabelProvider(network), layoutParameters, new IntIdProvider()).buildGraph();
        new BasicForceLayout().run(graph, layoutParameters);
        NetworkGraphBuilder.applyStyle(graph, styleProvider);
        assertTrue(graph.isStyleApplied());
        // Write Metadata as temporary json file
        Path outMetadataPath = tmpDir.resolve("metadata.json");
        new DiagramMetadata(layoutParameters, getSvgParameters()).addMetadata(graph).writeJson(outMetadataPath);
        assertNotNull(graph.getCssUrls());
        assertNotNull(graph.getCssFilenames());
        // Checking
        assertFileEquals(referenceMetadata, outMetadataPath);
        // Read metadata from file
        return DiagramMetadata.parseJson(outMetadataPath);
    }
}
