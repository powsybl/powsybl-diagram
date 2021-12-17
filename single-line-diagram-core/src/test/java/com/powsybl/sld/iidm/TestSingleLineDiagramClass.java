/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.SingleLineDiagram;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import org.apache.commons.io.output.NullWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class TestSingleLineDiagramClass extends AbstractTestCaseIidm {

    private FileSystem fileSystem;
    private Path tmpDir;

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    @Before
    public void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        tmpDir = Files.createDirectory(fileSystem.getPath("/tmp"));

        network = Network.create("TestSingleLineDiagramClass", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = createSubstation(network, "s", "s", Country.FR);
        vl = createVoltageLevel(substation, "vl1", "vl1", TopologyKind.NODE_BREAKER, 380, 10);
        createBusBarSection(vl, "bbs1", "bbs1", 0, 1, 1);
        createLoad(vl, "l", "l", "l", 0, ConnectablePosition.Direction.TOP, 2, 10, 10);
        createSwitch(vl, "d1", "d1", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        createSwitch(vl, "b1", "b1", SwitchKind.BREAKER, false, false, false, 1, 2);
        createSwitch(vl, "d2", "d2", SwitchKind.DISCONNECTOR, false, false, false, 0, 3);
        createSwitch(vl, "b2", "b2", SwitchKind.BREAKER, false, false, false, 3, 4);

        VoltageLevel vl2 = createVoltageLevel(substation, "vl2", "vl2", TopologyKind.NODE_BREAKER, 225, 10);
        createBusBarSection(vl2, "bbs2", "bbs2", 0, 1, 1);
        createGenerator(vl2, "g", "g", "g", -1, ConnectablePosition.Direction.BOTTOM, 2, 0, 20, false, 10, 10);
        createSwitch(vl2, "d3", "d3", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        createSwitch(vl2, "b3", "b3", SwitchKind.BREAKER, false, false, false, 1, 2);
        createSwitch(vl2, "d4", "d4", SwitchKind.DISCONNECTOR, false, false, false, 0, 3);
        createSwitch(vl2, "b4", "b4", SwitchKind.BREAKER, false, false, false, 3, 4);

        createTwoWindingsTransformer(substation, "trf", "trf", 2.0, 14.745, 0.0, 3.2E-5, 400.0, 225.0,
                4, 4, vl.getId(), vl2.getId(),
                "trf", -1, ConnectablePosition.Direction.BOTTOM,
                "trf", -1, ConnectablePosition.Direction.TOP);
    }

    @Test
    public void testSvgVl1() throws IOException {
        String expected = toString("/TestSldClassVl.svg");
        String expectedMetadata = toString("/TestSldClassVlMetadata.json");
        assertEquals(expected, toDefaultSVG(network, vl.getId(), "/TestSldClassVl.svg"));

        Writer writerForSvg = new StringWriter();
        SingleLineDiagram.drawVoltageLevel(network, vl.getId(), writerForSvg, new NullWriter());
        assertEquals(expected, fixSvg(normalizeLineSeparator(writerForSvg.toString())));

        Path svgPath = tmpDir.resolve("result.svg");
        Path metadataPath = tmpDir.resolve("result_metadata.json");
        SingleLineDiagram.drawVoltageLevel(network, vl.getId(), svgPath);
        assertEquals(expected, fixSvg(toString(Files.newInputStream(svgPath))));
        assertEquals(expectedMetadata, toString(Files.newInputStream(metadataPath)));

        SingleLineDiagram.draw(network, vl.getId(), svgPath);
        assertEquals(expected, fixSvg(toString(Files.newInputStream(svgPath))));
        assertEquals(expectedMetadata, toString(Files.newInputStream(metadataPath)));
    }

    @Test
    public void testMetadataSubs() {
        String expectedMetadata = toString("/TestSldClassSubstationMetadata.json");
        try (Writer writer = new NullWriter();
             StringWriter metadataWriter = new StringWriter()) {
            SingleLineDiagram.draw(network, substation.getId(), writer, metadataWriter);
            assertEquals(expectedMetadata, normalizeLineSeparator(metadataWriter.toString()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    public void testSvgSubs() throws IOException {
        String expected = fixSvg(toString("/TestSldClassSubstation.svg"));
        String expectedMetadata = toString("/TestSldClassSubstationMetadata.json");
        assertEquals(expected, toDefaultSVG(network, substation.getId(), "/TestSldClassSubstation.svg"));

        PowsyblException e1 = assertThrows(PowsyblException.class, () -> SingleLineDiagram.draw(network, "d1", new NullWriter(), new NullWriter()));
        assertEquals("Given id 'd1' is not a substation or voltage level id in given network 'TestSingleLineDiagramClass'", e1.getMessage());

        Writer writerForSvg = new StringWriter();
        SingleLineDiagram.drawSubstation(network, substation.getId(), writerForSvg, new NullWriter());
        assertEquals(expected, fixSvg(normalizeLineSeparator(writerForSvg.toString())));

        Path svgPath = tmpDir.resolve("result.svg");
        Path metadataPath = tmpDir.resolve("result_metadata.json");
        SingleLineDiagram.drawSubstation(network, substation.getId(), svgPath);
        assertEquals(expected, fixSvg(toString(Files.newInputStream(svgPath))));
        assertEquals(expectedMetadata, toString(Files.newInputStream(metadataPath)));

        SingleLineDiagram.draw(network, substation.getId(), svgPath);
        assertEquals(expected, fixSvg(toString(Files.newInputStream(svgPath))));
        assertEquals(expectedMetadata, toString(Files.newInputStream(metadataPath)));

        PowsyblException e2 = assertThrows(PowsyblException.class, () -> SingleLineDiagram.draw(network, "bbs2", svgPath));
        assertEquals("Given id 'bbs2' is not a substation or voltage level id in given network 'TestSingleLineDiagramClass'", e2.getMessage());
    }

    private String toDefaultSVG(Network network, String id, String filename) {
        try (StringWriter writer = new StringWriter()) {
            SingleLineDiagram.draw(network, id, writer, new NullWriter());

            if (debugSvgFiles) {
                writeToFileInDebugDir(filename, writer);
            }
            if (overrideTestReferences) {
                overrideTestReference(filename, writer);
            }

            return fixSvg(normalizeLineSeparator(writer.toString()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
