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
    void test() {
        // Referenced json file
        String reference = "/hvdc_metadata.json";
        InputStream in = Objects.requireNonNull(getClass().getResourceAsStream(reference));
        // Create Metadata from json file
        DiagramMetadata metadata = DiagramMetadata.parseJson(in);
        // Write Metadata as temporary json file
        Path outPath = tmpDir.resolve("metadata.json");
        writeMetadata(metadata, outPath);
        // Read generated json file
        String actual = getContentFile(outPath);
        // Read reference json file
        String expected = toString(reference);
        // Checking
        assertEquals(expected, actual);
    }

    @Test
    void test3wt() {
        // Referenced json file
        String reference = "/3wt_metadata.json";
        // Write Metadata as temporary json file
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        Graph graph = new NetworkGraphBuilder(network, VoltageLevelFilter.NO_FILTER).buildGraph();
        new BasicForceLayout().run(graph, getLayoutParameters());
        Path outPath = tmpDir.resolve("metadata.json");
        new DiagramMetadata(getLayoutParameters(), getSvgParameters()).addMetadata(graph).writeJson(outPath);
        // Read generated json file
        String actual = getContentFile(outPath);
        // Read reference json file
        String expected = toString(reference);
        // Checking
        assertEquals(expected, actual);
    }

    private void writeMetadata(DiagramMetadata metadata, Path outPath) {
        try {
            Writer writer = Files.newBufferedWriter(outPath, StandardCharsets.UTF_8);
            metadata.writeJson(writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
