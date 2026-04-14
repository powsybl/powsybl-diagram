/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.Network;
import com.powsybl.sld.iidm.AbstractTestCaseIidm;
import com.powsybl.sld.svg.LegacyIdProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class SingleLineDiagramTest extends AbstractTestCaseIidm {

    private FileSystem fileSystem;
    protected Path tempDirectory;
    private Network network;
    private Path svgPath;
    private final SldParameters sldParameters = new SldParameters();

    @Override
    @BeforeEach
    public void setUp() throws IOException {
        network = Networks.createNetworkWithTieLineInVoltageLevel();
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        tempDirectory = fileSystem.getPath("/tmp");
        Files.createDirectory(tempDirectory);
        svgPath = tempDirectory.resolve("test.svg");
        sldParameters.getSvgParameters().setPrefixId("Test");
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    void testDraw() throws IOException {
        SingleLineDiagram.draw(network, "VL1", svgPath);
        assertEquals(toString("/TestDrawVL1PrefixEmpty.svg"), toString(Files.newInputStream(svgPath)));

        SingleLineDiagram.draw(network, "VL1", svgPath, new LegacyIdProvider("Legacy"));
        assertEquals(toString("/TestDrawVL1PrefixLegacy.svg"), toString(Files.newInputStream(svgPath)));

        try (StringWriter writer = new StringWriter()) {
            SingleLineDiagram.draw(network, "VL1", writer, new StringWriter());
            assertEquals(toString("/TestDrawVL1PrefixEmpty.svg"), writer.toString());
        }

        try (StringWriter writer = new StringWriter()) {
            SingleLineDiagram.draw(network, "VL1", writer, new StringWriter(), new LegacyIdProvider("Legacy"));
            assertEquals(toString("/TestDrawVL1PrefixLegacy.svg"), writer.toString());
        }

        SingleLineDiagram.draw(network, "VL1", svgPath, sldParameters);
        assertEquals(toString("/TestDrawVL1PrefixTest.svg"), toString(Files.newInputStream(svgPath)));

        SingleLineDiagram.draw(network, "VL1", svgPath, sldParameters, new LegacyIdProvider("Legacy"));
        assertEquals(toString("/TestDrawVL1PrefixLegacy.svg"), toString(Files.newInputStream(svgPath)));

        try (StringWriter writer = new StringWriter()) {
            SingleLineDiagram.draw(network, "VL1", writer, new StringWriter(), sldParameters);
            assertEquals(toString("/TestDrawVL1PrefixTest.svg"), writer.toString());
        }

        try (StringWriter writer = new StringWriter()) {
            SingleLineDiagram.draw(network, "VL1", writer, new StringWriter(), sldParameters, new LegacyIdProvider("Legacy"));
            assertEquals(toString("/TestDrawVL1PrefixLegacy.svg"), writer.toString());
        }
    }

    @Test
    void testDrawVoltageLevel() throws IOException {
        SingleLineDiagram.drawVoltageLevel(network, "VL1", svgPath);
        assertEquals(toString("/TestDrawVL1PrefixEmpty.svg"), toString(Files.newInputStream(svgPath)));

        SingleLineDiagram.drawVoltageLevel(network, "VL1", svgPath, new LegacyIdProvider("Legacy"));
        assertEquals(toString("/TestDrawVL1PrefixLegacy.svg"), toString(Files.newInputStream(svgPath)));

        try (StringWriter writer = new StringWriter()) {
            SingleLineDiagram.drawVoltageLevel(network, "VL1", writer, new StringWriter(), sldParameters);
            assertEquals(toString("/TestDrawVL1PrefixTest.svg"), writer.toString());
        }

        try (StringWriter writer = new StringWriter()) {
            SingleLineDiagram.drawVoltageLevel(network, "VL1", writer, new StringWriter(), sldParameters, new LegacyIdProvider("Legacy"));
            assertEquals(toString("/TestDrawVL1PrefixLegacy.svg"), writer.toString());
        }
    }

    @Test
    void testDrawSubstation() throws IOException {
        SingleLineDiagram.drawSubstation(network, "S1", svgPath);
        assertEquals(toString("/TestDrawS1PrefixEmpty.svg"), toString(Files.newInputStream(svgPath)));

        SingleLineDiagram.drawSubstation(network, "S1", svgPath, new LegacyIdProvider("Legacy"));
        assertEquals(toString("/TestDrawS1PrefixLegacy.svg"), toString(Files.newInputStream(svgPath)));

        try (StringWriter writer = new StringWriter()) {
            SingleLineDiagram.drawSubstation(network, "S1", writer, new StringWriter(), sldParameters);
            assertEquals(toString("/TestDrawS1PrefixTest.svg"), writer.toString());
        }

        try (StringWriter writer = new StringWriter()) {
            SingleLineDiagram.drawSubstation(network, "S1", writer, new StringWriter(), sldParameters, new LegacyIdProvider("Legacy"));
            assertEquals(toString("/TestDrawS1PrefixLegacy.svg"), writer.toString());
        }
    }

    @Test
    void testDrawMultiSubstation() throws IOException {
        network = Networks.createTestCase11Network();

        SingleLineDiagram.drawMultiSubstations(network, List.of("subst", "subst2"), svgPath);
        assertEquals(toString("/TestDrawS1S2PrefixEmpty.svg"), toString(Files.newInputStream(svgPath)));

        SingleLineDiagram.drawMultiSubstations(network, List.of("subst", "subst2"), svgPath, new LegacyIdProvider("Legacy"));
        assertEquals(toString("/TestDrawS1S2PrefixLegacy.svg"), toString(Files.newInputStream(svgPath)));

        try (StringWriter writer = new StringWriter()) {
            SingleLineDiagram.drawMultiSubstations(network, List.of("subst", "subst2"), writer, new StringWriter(), sldParameters);
            assertEquals(toString("/TestDrawS1S2PrefixTest.svg"), writer.toString());
        }

        try (StringWriter writer = new StringWriter()) {
            SingleLineDiagram.drawMultiSubstations(network, List.of("subst", "subst2"), writer, new StringWriter(), sldParameters, new LegacyIdProvider("Legacy"));
            assertEquals(toString("/TestDrawS1S2PrefixLegacy.svg"), writer.toString());
        }
    }
}
