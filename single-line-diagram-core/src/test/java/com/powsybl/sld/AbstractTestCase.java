/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

import com.google.common.io.ByteStreams;
import com.powsybl.sld.layout.HorizontalSubstationLayoutFactory;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.SubstationLayoutFactory;
import com.powsybl.sld.layout.VoltageLevelLayoutFactory;
import com.powsybl.sld.layout.PositionVoltageLevelLayoutFactory;
import com.powsybl.sld.library.ConvergenceComponentLibrary;
import com.powsybl.sld.library.ResourcesComponentLibrary;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.model.VoltageLevelGraph;
import com.powsybl.sld.svg.DiagramLabelProvider;
import com.powsybl.sld.svg.DiagramStyleProvider;
import org.apache.commons.io.output.NullWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public abstract class AbstractTestCase {

    private static final Pattern SVG_FIX_PATTERN = Pattern.compile(">\\s*(<\\!\\[CDATA\\[.*?]]>)\\s*</", Pattern.DOTALL);

    protected boolean debugJsonFiles = false;
    protected boolean debugSvgFiles = false;
    protected boolean overrideTestReferences = false;

    protected final ResourcesComponentLibrary componentLibrary = getResourcesComponentLibrary();

    protected final LayoutParameters layoutParameters = createDefaultLayoutParameters();

    private static LayoutParameters createDefaultLayoutParameters() {
        return new LayoutParameters()
            .setAdaptCellHeightToContent(false)
            .setVerticalSpaceBus(25)
            .setHorizontalBusPadding(20)
            .setCellWidth(50)
            .setExternCellHeight(250)
            .setInternCellHeight(40)
            .setStackHeight(30)
            .setShowGrid(true)
            .setShowInternalNodes(true)
            .setScaleFactor(1)
            .setArrowDistance(20)
            .setDrawStraightWires(false)
            .setHorizontalSnakeLinePadding(30)
            .setVerticalSnakeLinePadding(30)
            .setCssLocation(LayoutParameters.CssLocation.INSERTED_IN_SVG)
            .setSvgWidthAndHeightAdded(true)
            .setUseName(true);
    }

    protected ResourcesComponentLibrary getResourcesComponentLibrary() {
        return new ConvergenceComponentLibrary();
    }

    protected static String normalizeLineSeparator(String str) {
        return str.replace("\r\n", "\n")
                .replace("\r", "\n");
    }

    public abstract void setUp() throws IOException;

    String getName() {
        return getClass().getSimpleName();
    }

    protected void writeDebugFilesFromJson(String refMetdataName, StringWriter writer, StringWriter metadataWriter) {
        if (debugJsonFiles && metadataWriter != null) {
            writeToFileInDebugDir(refMetdataName, metadataWriter);
        }
        if (overrideTestReferences && metadataWriter != null) {
            overrideTestReference(refMetdataName, metadataWriter);
        }
        if (debugSvgFiles && writer != null) {
            writeToFileInDebugDir(refMetdataName.replace(".json", ".svg"), writer);
        }
    }

    protected void writeToFileInDebugDir(String filename, StringWriter content) {
        File debugFolder = new File(System.getProperty("user.home"), ".powsybl");
        if (debugFolder.exists() || debugFolder.mkdir()) {
            File file = new File(debugFolder, filename);
            try (OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                fw.write(content.toString());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    protected void overrideTestReference(String filename, StringWriter content) {
        File testReference = new File("src/test/resources", filename);
        if (!testReference.exists()) {
            return;
        }
        try (OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(testReference), StandardCharsets.UTF_8)) {
            fw.write(fixSvg(content.toString()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public abstract void toSVG(Graph g, String filename);

    /**
     * Between Java 9 and 14 an extra new lines is added before and after CDATA element. To support both Java 11 and 17
     * we need to remove these new lines => To remove when migrating to Java 17.
     *
     * See https://stackoverflow.com/questions/55853220/handling-change-in-newlines-by-xml-transformation-for-cdata-from-java-8-to-java
     */
    protected static String fixSvg(String svg) {
        return SVG_FIX_PATTERN.matcher(Objects.requireNonNull(svg)).replaceAll(">$1</");
    }

    public String toSVG(Graph graph, String filename, DiagramLabelProvider labelProvider, DiagramStyleProvider styleProvider) {
        try (StringWriter writer = new StringWriter()) {
            SingleLineDiagram.draw(graph, writer, new NullWriter(), layoutParameters, componentLibrary,
                    labelProvider, styleProvider, "");

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

    public void compareMetadata(VoltageLevelGraph graph, String refMetadataName,
                                VoltageLevelLayoutFactory voltageLevelLayoutFactory,
                                DiagramLabelProvider labelProvider, DiagramStyleProvider styleProvider) {

        InputStream isRefMetadata = Objects.requireNonNull(getClass().getResourceAsStream(refMetadataName));

        try (StringWriter writer = new StringWriter();
             StringWriter metadataWriter = new StringWriter()) {

            voltageLevelLayoutFactory.create(graph).run(layoutParameters);
            SingleLineDiagram.draw(graph, writer, metadataWriter, layoutParameters, componentLibrary,
                    labelProvider, styleProvider, "");

            if (debugJsonFiles) {
                writeToFileInDebugDir(refMetadataName, metadataWriter);
            }
            if (overrideTestReferences) {
                overrideTestReference(refMetadataName, metadataWriter);
            }
            if (debugSvgFiles) {
                writeToFileInDebugDir(refMetadataName.replace(".json", ".svg"), writer);
            }

            String refMetadata = normalizeLineSeparator(new String(ByteStreams.toByteArray(isRefMetadata), StandardCharsets.UTF_8));
            String metadata = normalizeLineSeparator(metadataWriter.toString());
            assertEquals(refMetadata, metadata);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void compareMetadata(SubstationGraph graph, String refMetdataName,
                                SubstationLayoutFactory sLayoutFactory, VoltageLevelLayoutFactory vlLayoutFactory,
                                DiagramLabelProvider labelProvider, DiagramStyleProvider styleProvider) {

        InputStream isRefMetadata = Objects.requireNonNull(getClass().getResourceAsStream(refMetdataName));

        try (StringWriter writer = new StringWriter();
             StringWriter metadataWriter = new StringWriter()) {

            sLayoutFactory.create(graph, vlLayoutFactory).run(layoutParameters);
            SingleLineDiagram.draw(graph, writer, metadataWriter, layoutParameters, componentLibrary,
                    labelProvider, styleProvider, "");

            if (debugJsonFiles) {
                writeToFileInDebugDir(refMetdataName, metadataWriter);
            }
            if (overrideTestReferences) {
                overrideTestReference(refMetdataName, metadataWriter);
            }
            if (debugSvgFiles) {
                writeToFileInDebugDir(refMetdataName.replace(".json", ".svg"), writer);
            }

            String refMetadata = normalizeLineSeparator(new String(ByteStreams.toByteArray(isRefMetadata), StandardCharsets.UTF_8));
            String metadata = normalizeLineSeparator(metadataWriter.toString());
            assertEquals(refMetadata, metadata);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String toJson(Graph graph, String filename, boolean genCoords) {
        graph.setGenerateCoordsInJson(genCoords);
        return toJson(graph, filename);
    }

    public String toJson(Graph graph, String filename) {
        try (StringWriter writer = new StringWriter()) {
            graph.writeJson(writer);

            if (debugJsonFiles) {
                writeToFileInDebugDir(filename, writer);
            }
            if (overrideTestReferences) {
                overrideTestReference(filename, writer);
            }
            if (debugSvgFiles) {
                toSVG(graph, filename.replace(".json", ".svg"));
            }

            return normalizeLineSeparator(writer.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String toString(String resourceName) {
        InputStream resourceAsStream = Objects.requireNonNull(getClass().getResourceAsStream(resourceName));
        return toString(resourceAsStream);
    }

    public String toString(InputStream resourceAsStream) {
        try {
            return normalizeLineSeparator(new String(ByteStreams.toByteArray(resourceAsStream), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected void voltageLevelGraphLayout(VoltageLevelGraph voltageLevelGraph) {
        new PositionVoltageLevelLayoutFactory().create(voltageLevelGraph).run(layoutParameters);
    }

    protected void substationGraphLayout(SubstationGraph substationGraph) {
        new HorizontalSubstationLayoutFactory().create(substationGraph, new PositionVoltageLevelLayoutFactory()).run(layoutParameters);
    }
}
