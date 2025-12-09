/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.cgmes.layout;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.test.ComparisonUtils;
import com.powsybl.iidm.network.Network;
import com.powsybl.sld.SingleLineDiagram;
import com.powsybl.sld.SldParameters;
import com.powsybl.sld.cgmes.dl.iidm.extensions.NetworkDiagramData;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.svg.SvgParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public abstract class AbstractTest {

    private FileSystem fileSystem;
    protected Path tmpDir;
    protected Network network;

    protected boolean overrideTestReferences = false;

    @AfterEach
    void tearDown() throws Exception {
        fileSystem.close();
    }

    @BeforeEach
    void setup() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        tmpDir = Files.createDirectory(fileSystem.getPath("/tmp"));
    }

    protected void assertSvgDrawnEqualsReference(String containerId, String filename) throws IOException {
        assertSvgDrawnEqualsReference(containerId, filename, new LayoutParameters(), new SvgParameters());
    }

    protected void assertSvgDrawnEqualsReference(String containerId, String filename, LayoutParameters layoutParameters,
                                                 SvgParameters svgParameters) throws IOException {
        Path svgOutput = tmpDir.resolve(filename);
        var sldParameters = new SldParameters().setLayoutParameters(layoutParameters).setSvgParameters(svgParameters.setUseName(true));
        SingleLineDiagram.draw(network, containerId, svgOutput, sldParameters);
        assertSvgEqualsReference(filename, svgOutput);
    }

    protected void assertSvgEqualsReference(String filename, Path svgOutput) throws IOException {
        if (overrideTestReferences) {
            overrideTestReference(filename, svgOutput);
        }
        InputStream svgRef = Objects.requireNonNull(AbstractTest.class.getResourceAsStream(filename));
        ComparisonUtils.assertTxtEquals(svgRef, Files.newInputStream(svgOutput));
    }

    protected void overrideTestReference(String filename, Path actual) throws IOException {
        Path testReference = Path.of("src", "test", "resources", filename);
        if (!Files.exists(testReference)) {
            return;
        }
        Files.copy(actual, testReference, StandardCopyOption.REPLACE_EXISTING);
    }
}
