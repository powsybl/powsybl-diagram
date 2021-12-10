/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.sld.layout.*;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ConvergenceComponentLibrary;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.model.VoltageLevelGraph;
import com.powsybl.sld.svg.*;
import com.powsybl.sld.util.TopologicalStyleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static com.powsybl.iidm.network.IdentifiableType.SUBSTATION;
import static com.powsybl.iidm.network.IdentifiableType.VOLTAGE_LEVEL;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public final class SingleLineDiagram {

    private static final Logger LOGGER = LoggerFactory.getLogger(SingleLineDiagram.class);

    private SingleLineDiagram() {
    }

    public static void draw(Network network, String id, String svgFile) {
        draw(network, id, Path.of(svgFile));
    }

    public static void draw(Network network, String id, Path svgFile) {
        draw(network, id, svgFile, new LayoutParameters());
    }

    public static void draw(Network network, String id, Path svgFile, LayoutParameters layoutParameters) {
        draw(network, id, svgFile, layoutParameters, new ConvergenceComponentLibrary());
    }

    public static void draw(Network network, String id, Path svgFile, LayoutParameters layoutParameters, ComponentLibrary componentLibrary) {
        draw(network, id, svgFile, layoutParameters, componentLibrary,
                new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters),
                new TopologicalStyleProvider(network),
                "");
    }

    public static void draw(Network network, String id, Path svgFile, LayoutParameters layoutParameters, ComponentLibrary componentLibrary,
                            DiagramLabelProvider initProvider, DiagramStyleProvider styleProvider, String prefixId) {
        draw(network, id, svgFile, layoutParameters, componentLibrary,
                new HorizontalSubstationLayoutFactory(), new SmartVoltageLevelLayoutFactory(network),
                initProvider, styleProvider, prefixId);
    }

    public static void draw(Network network, String id, Path svgFile, LayoutParameters layoutParameters, ComponentLibrary componentLibrary,
                            SubstationLayoutFactory sLayoutFactory, VoltageLevelLayoutFactory vLayoutFactory,
                            DiagramLabelProvider initProvider, DiagramStyleProvider styleProvider, String prefixId) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(id);

        Identifiable<?> identifiable = network.getIdentifiable(id);
        if (identifiable.getType() == VOLTAGE_LEVEL) {
            drawVoltageLevel(network, id, svgFile, layoutParameters, componentLibrary, vLayoutFactory, initProvider, styleProvider, prefixId);
        } else if (identifiable.getType() == SUBSTATION) {
            drawSubstation(network, id, svgFile, layoutParameters, componentLibrary, sLayoutFactory, vLayoutFactory, initProvider, styleProvider, prefixId);
        } else {
            throw new PowsyblException("Given id '" + id + "' is not a substation or voltage level id in given network");
        }
    }

    public static void drawVoltageLevel(Network network, String id, Path svgFile) {
        drawVoltageLevel(network, id, svgFile, new LayoutParameters());
    }

    public static void drawVoltageLevel(Network network, String id, Path svgFile, LayoutParameters layoutParameters) {
        drawVoltageLevel(network, id, svgFile, layoutParameters, new ConvergenceComponentLibrary());
    }

    public static void drawVoltageLevel(Network network, String id, Path svgFile, LayoutParameters layoutParameters, ComponentLibrary componentLibrary) {
        drawVoltageLevel(network, id, svgFile, layoutParameters, componentLibrary,
                new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters),
                new TopologicalStyleProvider(network),
                "");
    }

    public static void drawVoltageLevel(Network network, String id, Path svgFile, LayoutParameters layoutParameters, ComponentLibrary componentLibrary,
                                        DiagramLabelProvider initProvider, DiagramStyleProvider styleProvider, String prefixId) {
        drawVoltageLevel(network, id, svgFile, layoutParameters, componentLibrary, new SmartVoltageLevelLayoutFactory(network), initProvider, styleProvider, prefixId);
    }

    private static void drawVoltageLevel(Network network, String voltageLevelId, Path svgFile, LayoutParameters layoutParameters, ComponentLibrary componentLibrary,
                                         VoltageLevelLayoutFactory vLayoutFactory, DiagramLabelProvider initProvider, DiagramStyleProvider styleProvider, String prefixId) {
        Objects.requireNonNull(vLayoutFactory);

        VoltageLevelGraph voltageLevelGraph = new NetworkGraphBuilder(network).buildVoltageLevelGraph(voltageLevelId, true);
        vLayoutFactory.create(voltageLevelGraph).run(layoutParameters);
        draw(voltageLevelGraph, svgFile, layoutParameters, componentLibrary, initProvider, styleProvider, prefixId);
    }

    public static void drawSubstation(Network network, String id, Path svgFile) {
        drawSubstation(network, id, svgFile, new LayoutParameters());
    }

    public static void drawSubstation(Network network, String id, Path svgFile, LayoutParameters layoutParameters) {
        drawSubstation(network, id, svgFile, layoutParameters, new ConvergenceComponentLibrary());
    }

    public static void drawSubstation(Network network, String id, Path svgFile, LayoutParameters layoutParameters, ComponentLibrary componentLibrary) {
        drawSubstation(network, id, svgFile, layoutParameters, componentLibrary,
                        new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters),
                        new TopologicalStyleProvider(network),
                        "");
    }

    public static void drawSubstation(Network network, String id, Path svgFile, LayoutParameters layoutParameters, ComponentLibrary componentLibrary,
                                      DiagramLabelProvider initProvider, DiagramStyleProvider styleProvider, String prefixId) {
        drawSubstation(network, id, svgFile, layoutParameters, componentLibrary,
                        new HorizontalSubstationLayoutFactory(), new SmartVoltageLevelLayoutFactory(network),
                        initProvider, styleProvider, prefixId);
    }

    private static void drawSubstation(Network network, String substationId, Path svgFile, LayoutParameters layoutParameters, ComponentLibrary componentLibrary,
                                       SubstationLayoutFactory sLayoutFactory, VoltageLevelLayoutFactory vLayoutFactory,
                                       DiagramLabelProvider initProvider, DiagramStyleProvider styleProvider, String prefixId) {
        Objects.requireNonNull(sLayoutFactory);
        Objects.requireNonNull(vLayoutFactory);

        SubstationGraph substationGraph = new NetworkGraphBuilder(network).buildSubstationGraph(substationId);
        sLayoutFactory.create(substationGraph, vLayoutFactory).run(layoutParameters);
        draw(substationGraph, svgFile, layoutParameters, componentLibrary, initProvider, styleProvider, prefixId);
    }

    public static void draw(Graph graph, Path svgFile, LayoutParameters layoutParameters, ComponentLibrary componentLibrary,
                            DiagramLabelProvider initProvider, DiagramStyleProvider styleProvider, String prefixId) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(componentLibrary);
        Objects.requireNonNull(layoutParameters);
        Objects.requireNonNull(initProvider);
        Objects.requireNonNull(styleProvider);
        Objects.requireNonNull(prefixId);

        Path dir = svgFile.toAbsolutePath().getParent();
        String svgFileName = svgFile.getFileName().toString();
        if (!svgFileName.endsWith(".svg")) {
            svgFileName = svgFileName + ".svg";
        }
        try (Writer svgWriter = Files.newBufferedWriter(svgFile, StandardCharsets.UTF_8);
             Writer metadataWriter = Files.newBufferedWriter(dir.resolve(svgFileName.replace(".svg", "_metadata.json")), StandardCharsets.UTF_8)) {
            SVGWriter writer = new DefaultSVGWriter(componentLibrary, layoutParameters);
            draw(graph, writer, initProvider, styleProvider, svgWriter, metadataWriter, prefixId);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void draw(Network network, String id, Writer svgWriter, Writer metadataWriter) {
        draw(network, id, svgWriter, metadataWriter, new LayoutParameters());
    }

    public static void draw(Network network, String id, Writer svgWriter, Writer metadataWriter, LayoutParameters layoutParameters) {
        draw(network, id, svgWriter, metadataWriter, layoutParameters, new ConvergenceComponentLibrary());
    }

    public static void draw(Network network, String id, Writer svgWriter, Writer metadataWriter, LayoutParameters layoutParameters, ComponentLibrary componentLibrary) {
        draw(network, id, svgWriter, metadataWriter, layoutParameters, componentLibrary,
                new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters),
                new TopologicalStyleProvider(network),
                "");
    }

    public static void draw(Network network, String id, Writer svgWriter, Writer metadataWriter, LayoutParameters layoutParameters, ComponentLibrary componentLibrary,
                            DiagramLabelProvider initProvider, DiagramStyleProvider styleProvider, String prefixId) {
        draw(network, id, svgWriter, metadataWriter, layoutParameters, componentLibrary,
                new HorizontalSubstationLayoutFactory(), new SmartVoltageLevelLayoutFactory(network),
                initProvider, styleProvider, prefixId);
    }

    public static void draw(Network network, String id, Writer svgWriter, Writer metadataWriter, LayoutParameters layoutParameters, ComponentLibrary componentLibrary,
                            SubstationLayoutFactory sLayoutFactory, VoltageLevelLayoutFactory vLayoutFactory,
                            DiagramLabelProvider initProvider, DiagramStyleProvider styleProvider, String prefixId) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(id);

        Identifiable<?> identifiable = network.getIdentifiable(id);
        if (identifiable.getType() == VOLTAGE_LEVEL) {
            drawVoltageLevel(network, id, svgWriter, metadataWriter, layoutParameters, componentLibrary, vLayoutFactory, initProvider, styleProvider, prefixId);
        } else if (identifiable.getType() == SUBSTATION) {
            drawSubstation(network, id, svgWriter, metadataWriter, layoutParameters, componentLibrary, sLayoutFactory, vLayoutFactory, initProvider, styleProvider, prefixId);
        } else {
            throw new PowsyblException("Given id '" + id + "' is not a substation or voltage level id in given network");
        }
    }

    public static void drawVoltageLevel(Network network, String id, Writer svgWriter, Writer metadataWriter) {
        drawVoltageLevel(network, id, svgWriter, metadataWriter, new LayoutParameters());
    }

    public static void drawVoltageLevel(Network network, String id, Writer svgWriter, Writer metadataWriter, LayoutParameters layoutParameters) {
        drawVoltageLevel(network, id, svgWriter, metadataWriter, layoutParameters, new ConvergenceComponentLibrary());
    }

    public static void drawVoltageLevel(Network network, String id, Writer svgWriter, Writer metadataWriter, LayoutParameters layoutParameters, ComponentLibrary componentLibrary) {
        drawVoltageLevel(network, id, svgWriter, metadataWriter, layoutParameters, componentLibrary,
                new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters),
                new TopologicalStyleProvider(network),
                "");
    }

    public static void drawVoltageLevel(Network network, String id, Writer svgWriter, Writer metadataWriter, LayoutParameters layoutParameters, ComponentLibrary componentLibrary,
                                        DiagramLabelProvider initProvider, DiagramStyleProvider styleProvider, String prefixId) {
        drawVoltageLevel(network, id, svgWriter, metadataWriter, layoutParameters, componentLibrary, new SmartVoltageLevelLayoutFactory(network), initProvider, styleProvider, prefixId);
    }

    private static void drawVoltageLevel(Network network, String voltageLevelId, Writer svgWriter, Writer metadataWriter, LayoutParameters layoutParameters, ComponentLibrary componentLibrary,
                                         VoltageLevelLayoutFactory vLayoutFactory, DiagramLabelProvider initProvider, DiagramStyleProvider styleProvider, String prefixId) {
        Objects.requireNonNull(vLayoutFactory);

        VoltageLevelGraph voltageLevelGraph = new NetworkGraphBuilder(network).buildVoltageLevelGraph(voltageLevelId, true);
        vLayoutFactory.create(voltageLevelGraph).run(layoutParameters);
        draw(voltageLevelGraph, svgWriter, metadataWriter, layoutParameters, componentLibrary, initProvider, styleProvider, prefixId);
    }

    public static void drawSubstation(Network network, String id, Writer svgWriter, Writer metadataWriter) {
        drawSubstation(network, id, svgWriter, metadataWriter, new LayoutParameters());
    }

    public static void drawSubstation(Network network, String id, Writer svgWriter, Writer metadataWriter, LayoutParameters layoutParameters) {
        drawSubstation(network, id, svgWriter, metadataWriter, layoutParameters, new ConvergenceComponentLibrary());
    }

    public static void drawSubstation(Network network, String id, Writer svgWriter, Writer metadataWriter, LayoutParameters layoutParameters, ComponentLibrary componentLibrary) {
        drawSubstation(network, id, svgWriter, metadataWriter, layoutParameters, componentLibrary,
                new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters),
                new TopologicalStyleProvider(network),
                "");
    }

    public static void drawSubstation(Network network, String id, Writer svgWriter, Writer metadataWriter, LayoutParameters layoutParameters, ComponentLibrary componentLibrary,
                                      DiagramLabelProvider initProvider, DiagramStyleProvider styleProvider, String prefixId) {
        drawSubstation(network, id, svgWriter, metadataWriter, layoutParameters, componentLibrary,
                new HorizontalSubstationLayoutFactory(), new SmartVoltageLevelLayoutFactory(network),
                initProvider, styleProvider, prefixId);
    }

    private static void drawSubstation(Network network, String substationId, Writer svgWriter, Writer metadataWriter, LayoutParameters layoutParameters, ComponentLibrary componentLibrary,
                                       SubstationLayoutFactory sLayoutFactory, VoltageLevelLayoutFactory vLayoutFactory,
                                       DiagramLabelProvider initProvider, DiagramStyleProvider styleProvider, String prefixId) {
        Objects.requireNonNull(sLayoutFactory);
        Objects.requireNonNull(vLayoutFactory);

        SubstationGraph substationGraph = new NetworkGraphBuilder(network).buildSubstationGraph(substationId);
        sLayoutFactory.create(substationGraph, vLayoutFactory).run(layoutParameters);
        draw(substationGraph, svgWriter, metadataWriter, layoutParameters, componentLibrary, initProvider, styleProvider, prefixId);
    }

    public static void draw(Graph graph, Writer svgWriter, Writer metadataWriter, LayoutParameters layoutParameters, ComponentLibrary componentLibrary,
                            DiagramLabelProvider initProvider, DiagramStyleProvider styleProvider, String prefixId) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(componentLibrary);
        Objects.requireNonNull(layoutParameters);
        Objects.requireNonNull(initProvider);
        Objects.requireNonNull(styleProvider);
        Objects.requireNonNull(prefixId);

        draw(graph, new DefaultSVGWriter(componentLibrary, layoutParameters),
                initProvider, styleProvider, svgWriter, metadataWriter, prefixId);
    }

    public static void draw(Graph graph, SVGWriter writer, DiagramLabelProvider initProvider, DiagramStyleProvider styleProvider, Writer svgWriter, Writer metadataWriter, String prefixId) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(writer);
        Objects.requireNonNull(initProvider);
        Objects.requireNonNull(styleProvider);
        Objects.requireNonNull(prefixId);
        Objects.requireNonNull(svgWriter);
        Objects.requireNonNull(metadataWriter);

        LOGGER.info("Writing SVG and JSON metadata files...");

        // write SVG file
        GraphMetadata metadata = writer.write(prefixId, graph, initProvider, styleProvider, svgWriter);

        // write metadata JSON file
        metadata.writeJson(metadataWriter);
    }
}
