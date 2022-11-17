/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.Network;
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.TopologicalStyleProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class DiagramMetadataTest extends AbstractTest {

    private static final String INDENT = "    ";
    private static final String METADATA = "metadata";
    private static final String METADATA_START_TOKEN = "<" + METADATA + ">";
    private static final String METADATA_END_TOKEN = "</" + METADATA + ">";

    private FileSystem fileSystem;
    private Path tmpDir;

    @Before
    public void setup() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        tmpDir = Files.createDirectory(fileSystem.getPath("tmp"));
        setLayoutParameters(new LayoutParameters());
        setSvgParameters(new SvgParameters()
                .setInsertNameDesc(true)
                .setSvgWidthAndHeightAdded(true)
                .setFixedWidth(800));
    }

    @After
    public void tearDown() throws IOException {
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
    public void test() throws XMLStreamException {
        // Referenced svg file
        String reference = "/hvdc.svg";
        InputStream in = Objects.requireNonNull(getClass().getResourceAsStream(reference));
        // Create Metadata from svg file
        DiagramMetadata metadata = DiagramMetadata.parseXml(in);
        // Write Metadata as temporary xml file
        Path outPath = tmpDir.resolve("metadata.xml");
        writeMetadata(metadata, outPath);
        // Read xml file
        String actual = toString(outPath);
        // remove xml header (first line)
        actual = actual.substring(actual.indexOf(METADATA_START_TOKEN));
        // Keep only metadata from svg file
        String expected = toString(reference);
        expected = expected.substring(expected.indexOf(METADATA_START_TOKEN), expected.indexOf(METADATA_END_TOKEN) + METADATA_END_TOKEN.length());
        // Checking
        assertEquals(removeWhiteSpaces(expected), removeWhiteSpaces(actual));
    }

    @Test
    public void testInvalid() throws XMLStreamException {
        // Referenced svg file
        String reference = "<metadata>\n" +
                "        <nad:nad xmlns:nad=\"http://www.powsybl.org/schema/nad-metadata/1_0\">\n" +
                "            <nad:nodes>\n" +
                "                <nad:edge diagramId=\"10\" equipmentId=\"TWT\"/>\n" +
                "            </nad:nodes>\n" +
                "            <nad:edges>\n" +
                "                <nad:node diagramId=\"0\" equipmentId=\"S1VL1\"/>\n" +
                "            </nad:edges>\n" +
                "        </nad:nad>\n" +
                "    </metadata>";
        InputStream in = new ByteArrayInputStream(reference.getBytes(StandardCharsets.UTF_8));
        // Create Metadata from svg file
        DiagramMetadata metadata = DiagramMetadata.parseXml(in);
        // Write Metadata as temporary xml file
        Path outPath = tmpDir.resolve("metadataInvalid.xml");
        writeMetadata(metadata, outPath);
        // Read xml file
        String actual = toString(outPath);
        // remove xml header (first line)
        actual = actual.substring(actual.indexOf(METADATA_START_TOKEN));
        // Keep only metadata from svg file
        String expected = "<metadata>\n" +
                "        <nad:nad xmlns:nad=\"http://www.powsybl.org/schema/nad-metadata/1_0\">" +
                "            <nad:busNodes/>\n" +
                "            <nad:nodes/>\n" +
                "            <nad:edges/>\n" +
                "        </nad:nad>" +
                "    </metadata>";
        // Checking
        assertEquals(removeWhiteSpaces(expected), removeWhiteSpaces(actual));
    }

    private void writeMetadata(DiagramMetadata metadata, Path outPath) throws XMLStreamException {
        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(outPath))) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, INDENT, os);
            writer.writeStartElement(METADATA);
            metadata.writeXml(writer);
            writer.writeEndElement();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String toString(Path outPath) {
        String content;
        try {
            byte[] encoded = Files.readAllBytes(outPath);
            content = new String(encoded, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return content;
    }

    private String removeWhiteSpaces(String input) {
        return input.replaceAll("\\s+", "");
    }
}
