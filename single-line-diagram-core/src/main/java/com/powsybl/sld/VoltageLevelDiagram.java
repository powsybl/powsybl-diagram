/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.VoltageLevelLayout;
import com.powsybl.sld.layout.VoltageLevelLayoutFactory;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.svg.DefaultNodeLabelConfiguration;
import com.powsybl.sld.svg.GraphMetadata;
import com.powsybl.sld.svg.DefaultSVGWriter;
import com.powsybl.sld.svg.NodeLabelConfiguration;
import com.powsybl.sld.svg.SVGWriter;
import com.powsybl.sld.svg.DiagramInitialValueProvider;
import com.powsybl.sld.svg.DiagramStyleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public final class VoltageLevelDiagram {

    private static final Logger LOGGER = LoggerFactory.getLogger(VoltageLevelDiagram.class);

    private final Graph graph;

    private final VoltageLevelLayout vlLayout;

    private VoltageLevelDiagram(Graph graph, VoltageLevelLayout layout) {
        this.graph = Objects.requireNonNull(graph);
        this.vlLayout = Objects.requireNonNull(layout);
    }

    public static VoltageLevelDiagram build(GraphBuilder graphBuilder, String voltageLevelId,
                                            VoltageLevelLayoutFactory layoutFactory,
                                            boolean useName) {
        Objects.requireNonNull(graphBuilder);
        Objects.requireNonNull(voltageLevelId);
        Objects.requireNonNull(layoutFactory);

        Graph graph = graphBuilder.buildVoltageLevelGraph(voltageLevelId, useName, true);

        VoltageLevelLayout layout = layoutFactory.create(graph);

        return new VoltageLevelDiagram(graph, layout);
    }

    public Graph getGraph() {
        return graph;
    }

    public void writeSvg(String prefixId,
                         ComponentLibrary componentLibrary,
                         LayoutParameters layoutParameters,
                         DiagramInitialValueProvider initialValueProvider,
                         DiagramStyleProvider styleProvider,
                         Path svgFile) {
        SVGWriter writer = new DefaultSVGWriter(componentLibrary, layoutParameters);
        writeSvg(prefixId, writer,
                initialValueProvider,
                styleProvider,
                new DefaultNodeLabelConfiguration(writer.getComponentLibrary(), layoutParameters),
                svgFile);
    }

    public void writeSvg(String prefixId,
                         SVGWriter writer,
                         DiagramInitialValueProvider initProvider,
                         DiagramStyleProvider styleProvider,
                         NodeLabelConfiguration nodeLabelConfiguration,
                         Path svgFile) {
        Path dir = svgFile.toAbsolutePath().getParent();
        String svgFileName = svgFile.getFileName().toString();
        if (!svgFileName.endsWith(".svg")) {
            svgFileName = svgFileName + ".svg";
        }
        try (Writer svgWriter = Files.newBufferedWriter(svgFile, StandardCharsets.UTF_8);
                Writer metadataWriter = Files.newBufferedWriter(dir.resolve(svgFileName.replace(".svg", "_metadata.json")), StandardCharsets.UTF_8)) {
            writeSvg(prefixId, writer, initProvider, styleProvider, nodeLabelConfiguration, svgWriter, metadataWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeSvg(String prefixId,
                         SVGWriter writer,
                         DiagramInitialValueProvider initProvider,
                         DiagramStyleProvider styleProvider,
                         NodeLabelConfiguration nodeLabelConfiguration,
                         Writer svgWriter,
                         Writer metadataWriter) {
        Objects.requireNonNull(writer);
        Objects.requireNonNull(writer.getLayoutParameters());
        Objects.requireNonNull(svgWriter);
        Objects.requireNonNull(metadataWriter);

        // calculate coordinate
        vlLayout.run(writer.getLayoutParameters());

        // write SVG file
        LOGGER.info("Writing SVG and JSON metadata files...");

        GraphMetadata metadata = writer.write(prefixId, graph, initProvider, styleProvider, nodeLabelConfiguration, svgWriter);

        // write metadata file
        metadata.writeJson(metadataWriter);
    }
}
