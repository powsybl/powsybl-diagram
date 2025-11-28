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
import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.SingleLineDiagram;
import com.powsybl.sld.SldParameters;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.cgmes.dl.iidm.extensions.NetworkDiagramData;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.svg.SvgParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
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

    private FileSystem fileSystem;
    private Path tmpDir;
    private Network network;
    private String vlId;
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
        vlId = "b2707f00-2554-41d2-bde2-7dd80a669e50";

        List<String> names = NetworkDiagramData.getDiagramsNames(network);
        var layoutParameters = new LayoutParameters().setCgmesDiagramName(names.getFirst()).setCgmesUseNames(true);
        var svgParameters = new SvgParameters().setUseName(true);
        sldParameters = new SldParameters().setLayoutParameters(layoutParameters).setSvgParameters(svgParameters);
    }

    @Test
    void test() throws IOException {
        String filename = "/microgrid.svg";
        Path svgOutput = tmpDir.resolve(filename);
        SingleLineDiagram.draw(network, vlId, svgOutput, sldParameters);
        InputStream svgRef = Objects.requireNonNull(getClass().getResourceAsStream(filename));
        ComparisonUtils.assertTxtEquals(svgRef, Files.newInputStream(svgOutput));
        ComparisonUtils.assertTxtEquals(svgRef, Files.newInputStream(svgOutput));
    }

    @Test
    void testOpenSwitch() throws IOException {
        String filename = "/microgrid_open_switch.svg";
        Path svgOutput = tmpDir.resolve(filename);
        network.getSwitch("1287758d-606d-44c9-9e93-2f465ebf54b7").setOpen(true);
        SingleLineDiagram.draw(network, vlId, svgOutput, sldParameters);
        InputStream svgRef = Objects.requireNonNull(getClass().getResourceAsStream(filename));
        ComparisonUtils.assertTxtEquals(svgRef, Files.newInputStream(svgOutput));
    }
}
