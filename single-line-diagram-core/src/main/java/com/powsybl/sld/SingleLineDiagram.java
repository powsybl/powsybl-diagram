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

    public static void draw(Network network, String id, Path svgFile) {
        draw(network, id, svgFile, new ConvergenceComponentLibrary());
    }

    public static void draw(Network network, String id, Path svgFile, ComponentLibrary componentLibrary) {
        draw(network, id, svgFile, componentLibrary, new LayoutParameters());
    }

    public static void draw(Network network, String id, Path svgFile, ComponentLibrary componentLibrary, LayoutParameters layoutParameters) {
        draw(network, id, svgFile, componentLibrary, layoutParameters,
                new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters),
                new DefaultDiagramStyleProvider(),
                "");
    }

    public static void draw(Network network, String id, Path svgFile, ComponentLibrary componentLibrary, LayoutParameters layoutParameters,
                            DiagramLabelProvider initProvider, DiagramStyleProvider styleProvider, String prefixId) {
        draw(network, id, svgFile, componentLibrary, layoutParameters,
                new HorizontalSubstationLayoutFactory(), new SmartVoltageLevelLayoutFactory(network),
                initProvider, styleProvider, prefixId);
    }

    public static void draw(Network network, String id, Path svgFile, ComponentLibrary componentLibrary, LayoutParameters layoutParameters,
                            SubstationLayoutFactory sLayoutFactory, VoltageLevelLayoutFactory vLayoutFactory,
                            DiagramLabelProvider initProvider, DiagramStyleProvider styleProvider, String prefixId) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(id);

        Identifiable<?> identifiable = network.getIdentifiable(id);
        if (identifiable.getType() == VOLTAGE_LEVEL) {
            drawVoltageLevel(network, id, svgFile, componentLibrary, layoutParameters, vLayoutFactory, initProvider, styleProvider, prefixId);
        } else if (identifiable.getType() == SUBSTATION) {
            drawSubstation(network, id, svgFile, componentLibrary, layoutParameters, sLayoutFactory, vLayoutFactory, initProvider, styleProvider, prefixId);
        } else {
            throw new PowsyblException("Given id '" + id + "' is not a substation or voltage level id in given network");
        }
    }

    public static void drawVoltageLevel(Network network, String id, Path svgFile) {
        drawVoltageLevel(network, id, svgFile, new ConvergenceComponentLibrary());
    }

    public static void drawVoltageLevel(Network network, String id, Path svgFile, ComponentLibrary componentLibrary) {
        drawVoltageLevel(network, id, svgFile, componentLibrary, new LayoutParameters());
    }

    public static void drawVoltageLevel(Network network, String id, Path svgFile, ComponentLibrary componentLibrary, LayoutParameters layoutParameters) {
        drawVoltageLevel(network, id, svgFile, componentLibrary, layoutParameters,
                new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters),
                new DefaultDiagramStyleProvider(),
                "");
    }

    public static void drawVoltageLevel(Network network, String id, Path svgFile, ComponentLibrary componentLibrary, LayoutParameters layoutParameters,
                                        DiagramLabelProvider initProvider, DiagramStyleProvider styleProvider, String prefixId) {
        drawVoltageLevel(network, id, svgFile, componentLibrary, layoutParameters, new SmartVoltageLevelLayoutFactory(network), initProvider, styleProvider, prefixId);
    }

    private static void drawVoltageLevel(Network network, String voltageLevelId, Path svgFile, ComponentLibrary componentLibrary, LayoutParameters layoutParameters,
                                         VoltageLevelLayoutFactory vLayoutFactory, DiagramLabelProvider initProvider, DiagramStyleProvider styleProvider, String prefixId) {
        Objects.requireNonNull(vLayoutFactory);

        VoltageLevelGraph voltageLevelGraph = new NetworkGraphBuilder(network).buildVoltageLevelGraph(voltageLevelId, true);
        vLayoutFactory.create(voltageLevelGraph).run(layoutParameters);
        draw(voltageLevelGraph, svgFile, componentLibrary, layoutParameters, initProvider, styleProvider, prefixId);
    }

    public static void drawSubstation(Network network, String id, Path svgFile) {
        drawSubstation(network, id, svgFile, new ConvergenceComponentLibrary());
    }

    public static void drawSubstation(Network network, String id, Path svgFile, ComponentLibrary componentLibrary) {
        drawSubstation(network, id, svgFile, componentLibrary, new LayoutParameters());
    }

    public static void drawSubstation(Network network, String id, Path svgFile, ComponentLibrary componentLibrary, LayoutParameters layoutParameters) {
        drawSubstation(network, id, svgFile, componentLibrary, layoutParameters,
                new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters),
                new DefaultDiagramStyleProvider(),
                "");
    }

    public static void drawSubstation(Network network, String id, Path svgFile, ComponentLibrary componentLibrary, LayoutParameters layoutParameters,
                                      DiagramLabelProvider initProvider, DiagramStyleProvider styleProvider, String prefixId) {
        drawSubstation(network, id, svgFile, componentLibrary, layoutParameters,
                new HorizontalSubstationLayoutFactory(), new SmartVoltageLevelLayoutFactory(network),
                initProvider, styleProvider, prefixId);
    }

    private static void drawSubstation(Network network, String substationId, Path svgFile, ComponentLibrary componentLibrary, LayoutParameters layoutParameters,
                                       SubstationLayoutFactory sLayoutFactory, VoltageLevelLayoutFactory vLayoutFactory,
                                       DiagramLabelProvider initProvider, DiagramStyleProvider styleProvider, String prefixId) {
        Objects.requireNonNull(sLayoutFactory);
        Objects.requireNonNull(vLayoutFactory);

        SubstationGraph substationGraph = new NetworkGraphBuilder(network).buildSubstationGraph(substationId);
        sLayoutFactory.create(substationGraph, vLayoutFactory).run(layoutParameters);
        draw(substationGraph, svgFile, componentLibrary, layoutParameters, initProvider, styleProvider, prefixId);
    }

    public static void draw(Graph graph, Path svgFile, ComponentLibrary componentLibrary, LayoutParameters layoutParameters,
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
