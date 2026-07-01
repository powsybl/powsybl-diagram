/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.VoltageLevelLayoutFactory;
import com.powsybl.sld.library.SldComponentLibrary;
import com.powsybl.sld.model.graphs.Graph;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.graphs.ZoneGraph;
import com.powsybl.sld.svg.*;
import com.powsybl.sld.svg.styles.StyleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static com.powsybl.diagram.metadata.AbstractMetadata.DEFAULT_DIAGRAM_VERSION;
import static com.powsybl.iidm.network.IdentifiableType.SUBSTATION;
import static com.powsybl.iidm.network.IdentifiableType.VOLTAGE_LEVEL;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public final class SingleLineDiagram {

    private static final Logger LOGGER = LoggerFactory.getLogger(SingleLineDiagram.class);

    private SingleLineDiagram() {
    }

    private static Identifiable<?> getIdentifiable(Network network, String id) {
        Identifiable<?> identifiable = network.getIdentifiable(id);
        if (identifiable == null) {
            throw new PowsyblException("Network element '" + id + "' not found");
        }
        return identifiable;
    }

    public static void draw(Network network, String id, String svgFile) {
        draw(network, id, svgFile, DEFAULT_DIAGRAM_VERSION);
    }

    public static void draw(Network network, String id, String svgFile, String diagramVersion) {
        draw(network, id, Path.of(svgFile), diagramVersion);
    }

    public static void draw(Network network, String id, Path svgFile) {
        draw(network, id, svgFile, DEFAULT_DIAGRAM_VERSION);
    }

    public static void draw(Network network, String id, Path svgFile, String diagramVersion) {
        draw(network, id, svgFile, new SldParameters(), diagramVersion);
    }

    public static void draw(Network network, String id, Writer writerForSvg, Writer metadataWriter) {
        draw(network, id, writerForSvg, metadataWriter, DEFAULT_DIAGRAM_VERSION);
    }

    public static void draw(Network network, String id, Writer writerForSvg, Writer metadataWriter, String diagramVersion) {
        draw(network, id, writerForSvg, metadataWriter, new SldParameters(), diagramVersion);
    }

    public static void draw(Network network, String id, Path svgFile, SldParameters sldParameters) {
        draw(network, id, svgFile, sldParameters, DEFAULT_DIAGRAM_VERSION);
    }

    public static void draw(Network network, String id, Path svgFile, SldParameters sldParameters, String diagramVersion) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(id);

        Identifiable<?> identifiable = getIdentifiable(network, id);

        if (identifiable.getType() == VOLTAGE_LEVEL) {
            drawVoltageLevel(network, id, svgFile, sldParameters, diagramVersion);
        } else if (identifiable.getType() == SUBSTATION) {
            drawSubstation(network, id, svgFile, sldParameters, diagramVersion);
        } else {
            throw new PowsyblException("Given id '" + id + "' is not a substation or voltage level id in given network '" + network.getId() + "'");
        }
    }

    public static void draw(Network network, String id, Writer writerForSvg, Writer metadataWriter, SldParameters sldParameters) {
        draw(network, id, writerForSvg, metadataWriter, sldParameters, DEFAULT_DIAGRAM_VERSION);
    }

    public static void draw(Network network, String id, Writer writerForSvg, Writer metadataWriter, SldParameters sldParameters,
                            String diagramVersion) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(id);

        Identifiable<?> identifiable = getIdentifiable(network, id);
        if (identifiable.getType() == VOLTAGE_LEVEL) {
            drawVoltageLevel(network, id, writerForSvg, metadataWriter, sldParameters, diagramVersion);
        } else if (identifiable.getType() == SUBSTATION) {
            drawSubstation(network, id, writerForSvg, metadataWriter, sldParameters, diagramVersion);
        } else {
            throw new PowsyblException("Given id '" + id + "' is not a substation or voltage level id in given network '" + network.getId() + "'");
        }
    }

    public static void drawVoltageLevel(Network network, String id, String svgFile) {
        drawVoltageLevel(network, id, svgFile, DEFAULT_DIAGRAM_VERSION);
    }

    public static void drawVoltageLevel(Network network, String id, String svgFile, String diagramVersion) {
        drawVoltageLevel(network, id, Path.of(svgFile), diagramVersion);
    }

    public static void drawVoltageLevel(Network network, String voltageLevelId, Path svgFile) {
        drawVoltageLevel(network, voltageLevelId, svgFile, DEFAULT_DIAGRAM_VERSION);
    }

    public static void drawVoltageLevel(Network network, String voltageLevelId, Path svgFile, String diagramVersion) {
        drawVoltageLevel(network, voltageLevelId, svgFile, new SldParameters(), diagramVersion);
    }

    private static void drawVoltageLevel(Network network, String voltageLevelId, Path svgFile, SldParameters sldParameters) {
        drawVoltageLevel(network, voltageLevelId, svgFile, sldParameters, DEFAULT_DIAGRAM_VERSION);
    }

    private static void drawVoltageLevel(Network network, String voltageLevelId, Path svgFile, SldParameters sldParameters, String diagramVersion) {
        VoltageLevelGraph voltageLevelGraph = new NetworkGraphBuilder(network, sldParameters.getLayoutParameters()).buildVoltageLevelGraph(voltageLevelId);
        DefaultSVGWriter svgWriter = preDraw(voltageLevelGraph, sldParameters, network);
        draw(voltageLevelGraph, svgFile, svgWriter,
            sldParameters.createLabelProvider(network),
            sldParameters.getStyleProviderFactory().create(network, sldParameters.getSvgParameters()),
            sldParameters.createLegendWriter(network),
            diagramVersion);
    }

    public static void drawVoltageLevel(Network network, String voltageLevelId, Writer writerForSvg, Writer metadataWriter, SldParameters sldParameters) {
        drawVoltageLevel(network, voltageLevelId, writerForSvg, metadataWriter, sldParameters, DEFAULT_DIAGRAM_VERSION);
    }

    public static void drawVoltageLevel(Network network, String voltageLevelId, Writer writerForSvg, Writer metadataWriter,
                                        SldParameters sldParameters, String diagramVersion) {
        VoltageLevelGraph voltageLevelGraph = new NetworkGraphBuilder(network, sldParameters.getLayoutParameters()).buildVoltageLevelGraph(voltageLevelId);
        DefaultSVGWriter svgWriter = preDraw(voltageLevelGraph, sldParameters, network);
        draw(voltageLevelGraph, writerForSvg, metadataWriter, svgWriter,
            sldParameters.createLabelProvider(network),
            sldParameters.getStyleProviderFactory().create(network, sldParameters.getSvgParameters()),
            sldParameters.createLegendWriter(network),
            diagramVersion);
    }

    public static void drawSubstation(Network network, String id, String svgFile) {
        drawSubstation(network, id, svgFile, DEFAULT_DIAGRAM_VERSION);
    }

    public static void drawSubstation(Network network, String id, String svgFile, String diagramVersion) {
        drawSubstation(network, id, Path.of(svgFile), diagramVersion);
    }

    public static void drawSubstation(Network network, String id, Path svgFile) {
        drawSubstation(network, id, svgFile, DEFAULT_DIAGRAM_VERSION);
    }

    public static void drawSubstation(Network network, String id, Path svgFile, String diagramVersion) {
        drawSubstation(network, id, svgFile, new SldParameters(), diagramVersion);
    }

    private static void drawSubstation(Network network, String substationId, Path svgFile, SldParameters sldParameters) {
        drawSubstation(network, substationId, svgFile, sldParameters, DEFAULT_DIAGRAM_VERSION);
    }

    private static void drawSubstation(Network network, String substationId, Path svgFile, SldParameters sldParameters, String diagramVersion) {
        SubstationGraph substationGraph = new NetworkGraphBuilder(network, sldParameters.getLayoutParameters()).buildSubstationGraph(substationId);
        DefaultSVGWriter svgWriter = preDraw(substationGraph, sldParameters, network);
        draw(substationGraph, svgFile, svgWriter,
            sldParameters.createLabelProvider(network),
            sldParameters.getStyleProviderFactory().create(network, sldParameters.getSvgParameters()),
            sldParameters.createLegendWriter(network),
            diagramVersion);
    }

    public static void drawSubstation(Network network, String substationId, Writer writerForSvg, Writer metadataWriter, SldParameters sldParameters) {
        drawSubstation(network, substationId, writerForSvg, metadataWriter, sldParameters, DEFAULT_DIAGRAM_VERSION);
    }

    public static void drawSubstation(Network network, String substationId, Writer writerForSvg, Writer metadataWriter,
                                      SldParameters sldParameters, String diagramVersion) {
        SubstationGraph substationGraph = new NetworkGraphBuilder(network, sldParameters.getLayoutParameters()).buildSubstationGraph(substationId);
        DefaultSVGWriter svgWriter = preDraw(substationGraph, sldParameters, network);
        draw(substationGraph, writerForSvg, metadataWriter, svgWriter,
            sldParameters.createLabelProvider(network),
            sldParameters.getStyleProviderFactory().create(network, sldParameters.getSvgParameters()),
            sldParameters.createLegendWriter(network),
            diagramVersion);
    }

    public static void drawMultiSubstations(Network network, List<String> substationIdList, Path svgFile) {
        drawMultiSubstations(network, substationIdList, svgFile, DEFAULT_DIAGRAM_VERSION);
    }

    public static void drawMultiSubstations(Network network, List<String> substationIdList, Path svgFile, String diagramVersion) {
        drawMultiSubstations(network, substationIdList, svgFile, new SldParameters(), diagramVersion);
    }

    public static void drawMultiSubstations(Network network, List<String> substationIdList, Path svgFile, SldParameters sldParameters) {
        drawMultiSubstations(network, substationIdList, svgFile, sldParameters, DEFAULT_DIAGRAM_VERSION);
    }

    public static void drawMultiSubstations(Network network, List<String> substationIdList, Path svgFile,
                                            SldParameters sldParameters, String diagramVersion) {
        ZoneGraph zoneGraph = new NetworkGraphBuilder(network).buildZoneGraph(substationIdList);
        DefaultSVGWriter svgWriter = preDraw(zoneGraph, sldParameters, network);
        draw(zoneGraph, svgFile, svgWriter,
            sldParameters.createLabelProvider(network),
            sldParameters.getStyleProviderFactory().create(network, sldParameters.getSvgParameters()),
            sldParameters.createLegendWriter(network),
            diagramVersion);
    }

    public static void drawMultiSubstations(Network network, List<String> substationIdList, Writer writerForSvg, Writer metadataWriter, SldParameters sldParameters) {
        drawMultiSubstations(network, substationIdList, writerForSvg, metadataWriter, sldParameters, DEFAULT_DIAGRAM_VERSION);
    }

    public static void drawMultiSubstations(Network network, List<String> substationIdList, Writer writerForSvg,
                                            Writer metadataWriter, SldParameters sldParameters, String diagramVersion) {
        ZoneGraph zoneGraph = new NetworkGraphBuilder(network).buildZoneGraph(substationIdList);
        DefaultSVGWriter svgWriter = preDraw(zoneGraph, sldParameters, network);
        draw(zoneGraph, writerForSvg, metadataWriter, svgWriter,
            sldParameters.createLabelProvider(network),
            sldParameters.getStyleProviderFactory().create(network, sldParameters.getSvgParameters()),
            sldParameters.createLegendWriter(network),
            diagramVersion);
    }

    public static void draw(Graph graph, Path svgFile, DefaultSVGWriter svgWriter, LabelProvider labelProvider, StyleProvider styleProvider, SVGLegendWriter legendWriter) {
        draw(graph, svgFile, svgWriter, labelProvider, styleProvider, legendWriter, DEFAULT_DIAGRAM_VERSION);
    }

    public static void draw(Graph graph, Path svgFile, DefaultSVGWriter svgWriter, LabelProvider labelProvider,
                            StyleProvider styleProvider, SVGLegendWriter legendWriter, String diagramVersion) {
        Objects.requireNonNull(svgFile);

        Path dir = svgFile.toAbsolutePath().getParent();
        String svgFileName = svgFile.getFileName().toString();
        if (!svgFileName.endsWith(".svg")) {
            svgFileName = svgFileName + ".svg";
        }
        try (Writer writerForSvg = Files.newBufferedWriter(svgFile, StandardCharsets.UTF_8);
             Writer metadataWriter = Files.newBufferedWriter(dir.resolve(svgFileName.replace(".svg", "_metadata.json")), StandardCharsets.UTF_8)) {
            draw(graph, writerForSvg, metadataWriter, svgWriter, labelProvider, styleProvider, legendWriter, diagramVersion);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void draw(Graph graph, Writer writerForSvg, Writer metadataWriter, DefaultSVGWriter svgWriter, LabelProvider labelProvider, StyleProvider styleProvider, SVGLegendWriter legendWriter) {
        draw(graph, writerForSvg, metadataWriter, svgWriter, labelProvider, styleProvider, legendWriter, DEFAULT_DIAGRAM_VERSION);
    }

    public static void draw(Graph graph, Writer writerForSvg, Writer metadataWriter, DefaultSVGWriter svgWriter,
                            LabelProvider labelProvider, StyleProvider styleProvider, SVGLegendWriter legendWriter,
                            String diagramVersion) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(writerForSvg);
        Objects.requireNonNull(metadataWriter);
        Objects.requireNonNull(labelProvider);
        Objects.requireNonNull(styleProvider);
        Objects.requireNonNull(legendWriter);

        LOGGER.info("Writing SVG and JSON metadata files...");
        // write SVG file
        GraphMetadata metadata = svgWriter.write(graph, labelProvider, styleProvider, legendWriter, writerForSvg);
        // write metadata JSON file
        metadata.writeJson(metadataWriter, diagramVersion);
    }

    public static void draw(Graph graph, Writer writerForSvg, Writer metadataWriter, SldComponentLibrary componentLibrary, LayoutParameters layoutParameters, SvgParameters svgParameters, LabelProvider labelProvider, StyleProvider styleProvider, SVGLegendWriter legendWriter) {
        draw(graph, writerForSvg, metadataWriter, componentLibrary, layoutParameters, svgParameters, labelProvider,
            styleProvider, legendWriter, DEFAULT_DIAGRAM_VERSION);
    }

    public static void draw(Graph graph, Writer writerForSvg, Writer metadataWriter, SldComponentLibrary componentLibrary,
                            LayoutParameters layoutParameters, SvgParameters svgParameters, LabelProvider labelProvider,
                            StyleProvider styleProvider, SVGLegendWriter legendWriter, String diagramVersion) {
        Objects.requireNonNull(componentLibrary);
        Objects.requireNonNull(layoutParameters);
        Objects.requireNonNull(svgParameters);
        DefaultSVGWriter svgWriter = new DefaultSVGWriter(componentLibrary, layoutParameters, svgParameters);
        draw(graph, writerForSvg, metadataWriter, svgWriter, labelProvider, styleProvider, legendWriter, diagramVersion);
    }

    private static DefaultSVGWriter preDraw(Graph graph, SldParameters sldParameters, Network network) {
        LayoutParameters layoutParameters = sldParameters.getLayoutParameters();
        VoltageLevelLayoutFactory voltageLevelLayoutFactory = sldParameters.createVoltageLevelLayoutFactory(network);
        switch (graph) {
            case VoltageLevelGraph voltageLevelGraph -> voltageLevelLayoutFactory.create(voltageLevelGraph).run(layoutParameters);
            case SubstationGraph substationGraph -> sldParameters.getSubstationLayoutFactory().create(substationGraph, voltageLevelLayoutFactory).run(layoutParameters);
            case ZoneGraph zoneGraph -> sldParameters.getZoneLayoutFactory().create(zoneGraph, sldParameters.getZoneLayoutPathFinderFactory(), sldParameters.getSubstationLayoutFactory(), voltageLevelLayoutFactory).run(layoutParameters);
            case null, default -> throw new PowsyblException("First argument is an instance of an unexpected class");
        }
        return new DefaultSVGWriter(sldParameters.getComponentLibrary(), sldParameters.getLayoutParameters(), sldParameters.getSvgParameters());
    }
}
