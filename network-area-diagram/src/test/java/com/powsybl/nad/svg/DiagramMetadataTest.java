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
import com.powsybl.ieeecdf.converter.IeeeCdfNetworkFactory;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import com.powsybl.nad.AbstractTest;
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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
class DiagramMetadataTest extends AbstractTest {

    private FileSystem fileSystem;
    private Path tmpDir;

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
        return new DefaultLabelProvider(network, getSvgParameters());
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
        testMetadata(network, "/3wt_metadata.json", 3, 4, 3, 3);
    }

    @Test
    void testFictitious() {
        Network network = IeeeCdfNetworkFactory.create14();
        network.getVoltageLevel("VL12").setFictitious(true);
        network.getVoltageLevel("VL14").setFictitious(true);
        testMetadata(network, "/IEEE_14_bus_fictitious_metadata.json", 14, 14, 20, 14);
    }

    private void testMetadata(Network network, String referenceMetadata, int busNodesNumber, int nodesNumber, int edgesNumber, int textNodesNumber) {
        Graph graph = new NetworkGraphBuilder(network, VoltageLevelFilter.NO_FILTER).buildGraph(false);
        new BasicForceLayout().run(graph, getLayoutParameters());
        // Write Metadata as temporary json file
        Path outMetadataPath = tmpDir.resolve("metadata.json");
        new DiagramMetadata(getLayoutParameters(), getSvgParameters()).addMetadata(graph).writeJson(outMetadataPath);
        // Checking
        assertFileEquals(referenceMetadata, outMetadataPath);
        // Read metadata from file
        DiagramMetadata diagramMetadata = DiagramMetadata.parseJson(outMetadataPath);
        // Check read metadata
        assertEquals(busNodesNumber, diagramMetadata.getBusNodesMetadata().size());
        assertEquals(nodesNumber, diagramMetadata.getNodesMetadata().size());
        assertEquals(edgesNumber, diagramMetadata.getEdgesMetadata().size());
        assertEquals(textNodesNumber, diagramMetadata.getTextNodesMetadata().size());
    }
}
