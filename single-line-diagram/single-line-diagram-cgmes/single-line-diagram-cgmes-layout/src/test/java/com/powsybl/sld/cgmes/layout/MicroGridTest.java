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
import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.commons.test.ComparisonUtils;
import com.powsybl.iidm.network.Network;
import com.powsybl.sld.SingleLineDiagram;
import com.powsybl.sld.SldParameters;
import com.powsybl.sld.cgmes.dl.iidm.extensions.NetworkDiagramData;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.svg.SvgParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class MicroGridTest {

    private static final String VL_S2_10 = "b2707f00-2554-41d2-bde2-7dd80a669e50";
    private static final String VL_S5_10 = "8d4a8238-5b31-4c16-8692-0265dae5e132";
    private static final String SUB_S3 = "974565b1-ac55-4901-9f48-afc7ef5486df";

    private FileSystem fileSystem;
    private Path tmpDir;
    private Network network;
    private SldParameters sldParameters;

    @AfterEach
    void tearDown() throws Exception {
        fileSystem.close();
    }

    @BeforeEach
    void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        tmpDir = Files.createDirectory(fileSystem.getPath("/tmp"));

        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.post-processors", List.of("cgmesDLImport"));
        network = Network.read(CgmesConformity1ModifiedCatalog.miniNodeBreakerMeasurements().dataSource(), properties);

        List<String> names = NetworkDiagramData.getDiagramsNames(network);
        var layoutParameters = new LayoutParameters().setCgmesDiagramName(names.getFirst()).setCgmesUseNames(true);
        var svgParameters = new SvgParameters().setUseName(true);
        sldParameters = new SldParameters().setLayoutParameters(layoutParameters).setSvgParameters(svgParameters);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideTestData")
    void test(String testName, String filename, String containerId) throws IOException {
        Path svgOutput = tmpDir.resolve(filename);
        SingleLineDiagram.draw(network, containerId, svgOutput, sldParameters);
        assertSvgOutputEqualsReference(filename, svgOutput);
    }

    @Test
    void testOpenSwitch() throws IOException {
        String filename = "/microgrid_S2_10kV_open_switch.svg";
        Path svgOutput = tmpDir.resolve(filename);
        network.getSwitch("1287758d-606d-44c9-9e93-2f465ebf54b7").setOpen(true);
        SingleLineDiagram.draw(network, VL_S2_10, svgOutput, sldParameters);
        assertSvgOutputEqualsReference(filename, svgOutput);
    }

    private static void assertSvgOutputEqualsReference(String filename, Path svgOutput) throws IOException {
        InputStream svgRef = Objects.requireNonNull(MicroGridTest.class.getResourceAsStream(filename));
        ComparisonUtils.assertTxtEquals(svgRef, Files.newInputStream(svgOutput));
    }

    private static List<Arguments> provideTestData() {
        return List.of(
                Arguments.of("Test voltage level 'S2 10kV' diagram", "/microgrid_S2_10kV.svg", VL_S2_10),
                Arguments.of("Test voltage level 'S5 10kV' diagram", "/microgrid_S5_10kV.svg", VL_S5_10),
                Arguments.of("Test substation 'S3' diagram", "/microgrid_S3.svg", SUB_S3)
        );
    }
}
