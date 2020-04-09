/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

import com.google.common.io.ByteStreams;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ResourcesComponentLibrary;
import com.powsybl.sld.model.*;
import com.powsybl.sld.svg.DefaultNodeLabelConfiguration;
import com.powsybl.sld.svg.DefaultSVGWriter;
import com.powsybl.sld.svg.DiagramInitialValueProvider;
import com.powsybl.sld.svg.DiagramStyleProvider;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public abstract class AbstractTestCaseNoIidm {

    protected RawGraphBuilder rawGraphBuilder = new RawGraphBuilder();

    protected final ResourcesComponentLibrary componentLibrary = new ResourcesComponentLibrary("/ConvergenceLibrary");

    protected static String normalizeLineSeparator(String str) {
        return str.replace("\r\n", "\n")
                .replace("\r", "\n");
    }

    public abstract void setUp() throws IOException;

    String getName() {
        return getClass().getSimpleName();
    }

    public String toSVG(Graph graph,
                        String filename,
                        LayoutParameters layoutParameters,
                        DiagramInitialValueProvider initValueProvider,
                        DiagramStyleProvider styleProvider) {
        try (StringWriter writer = new StringWriter()) {
            new DefaultSVGWriter(componentLibrary, layoutParameters)
                    .write("", graph,
                            initValueProvider,
                            styleProvider,
                            new DefaultNodeLabelConfiguration(componentLibrary, layoutParameters),
                            writer);

//            FileWriter fw = new FileWriter(System.getProperty("user.home") + filename);
//            fw.write(writer.toString());
//            fw.close();

            return normalizeLineSeparator(writer.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void compareMetadata(VoltageLevelDiagram diagram, LayoutParameters layoutParameters,
                                String refMetdataName,
                                DiagramInitialValueProvider initValueProvider,
                                DiagramStyleProvider styleProvider) {
        try (StringWriter writer = new StringWriter();
             StringWriter metadataWriter = new StringWriter()) {
            diagram.writeSvg("",
                    new DefaultSVGWriter(componentLibrary, layoutParameters),
                    initValueProvider, styleProvider,
                    new DefaultNodeLabelConfiguration(componentLibrary, layoutParameters),
                    writer, metadataWriter);

//            FileWriter fw = new FileWriter(System.getProperty("user.home") + refMetdataName);
//            fw.write(metadataWriter.toString());
//            fw.close();

            String refMetadata = normalizeLineSeparator(new String(ByteStreams.toByteArray(getClass().getResourceAsStream(refMetdataName)), StandardCharsets.UTF_8));
            String metadata = normalizeLineSeparator(metadataWriter.toString());
            assertEquals(refMetadata, metadata);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String toSVG(SubstationGraph graph,
                        String filename,
                        LayoutParameters layoutParameters,
                        DiagramInitialValueProvider initValueProvider,
                        DiagramStyleProvider styleProvider) {
        try (StringWriter writer = new StringWriter()) {
            new DefaultSVGWriter(componentLibrary, layoutParameters)
                    .write("", graph,
                            initValueProvider,
                            styleProvider,
                            new DefaultNodeLabelConfiguration(componentLibrary, layoutParameters),
                            writer);

//            FileWriter fw = new FileWriter(System.getProperty("user.home") + filename);
//            fw.write(writer.toString());
//            fw.close();

            return normalizeLineSeparator(writer.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void compareMetadata(SubstationDiagram diagram, LayoutParameters layoutParameters,
                                String refMetdataName,
                                DiagramInitialValueProvider initValueProvider,
                                DiagramStyleProvider styleProvider) {
        try (StringWriter writer = new StringWriter();
             StringWriter metadataWriter = new StringWriter()) {
            diagram.writeSvg("",
                    new DefaultSVGWriter(componentLibrary, layoutParameters),
                    initValueProvider,
                    styleProvider,
                    new DefaultNodeLabelConfiguration(componentLibrary, layoutParameters),
                    writer, metadataWriter);

//            FileWriter fw = new FileWriter(System.getProperty("user.home") + refMetdataName);
//            fw.write(metadataWriter.toString());
//            fw.close();

            String refMetadata = normalizeLineSeparator(new String(ByteStreams.toByteArray(getClass().getResourceAsStream(refMetdataName)), StandardCharsets.UTF_8));
            String metadata = normalizeLineSeparator(metadataWriter.toString());
            assertEquals(refMetadata, metadata);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String toJson(Graph graph, String filename) {
        try (StringWriter writer = new StringWriter()) {
            graph.writeJson(writer);

//            FileWriter fw = new FileWriter(System.getProperty("user.home") + filename);
//            fw.write(writer.toString());
//            fw.close();

            return normalizeLineSeparator(writer.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String toJson(SubstationGraph graph, String filename) {
        try (StringWriter writer = new StringWriter()) {
            graph.writeJson(writer);

//            FileWriter fw = new FileWriter(System.getProperty("user.home") + filename);
//            fw.write(writer.toString());
//            fw.close();

            return normalizeLineSeparator(writer.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String toJson(SubstationGraph graph, String filename, boolean generateCoordsInJson) {
        graph.setGenerateCoordsInJson(generateCoordsInJson);
        return toJson(graph, filename);
    }

    public String toString(String resourceName) {
        try {
            return normalizeLineSeparator(new String(ByteStreams.toByteArray(getClass().getResourceAsStream(resourceName)), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String toSVG(ZoneGraph graph, String filename, LayoutParameters layoutParameters, DiagramInitialValueProvider initValueProvider, DiagramStyleProvider styleProvider) {
        try (StringWriter writer = new StringWriter()) {
            new DefaultSVGWriter(componentLibrary, layoutParameters)
                    .write("", graph,
                            initValueProvider,
                            styleProvider,
                            new DefaultNodeLabelConfiguration(componentLibrary, layoutParameters),
                            writer);

//            FileWriter fw = new FileWriter(System.getProperty("user.home") + filename);
//            fw.write(writer.toString());
//            fw.close();

            return normalizeLineSeparator(writer.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
