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
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.layout.*;
import com.powsybl.sld.model.graphs.*;
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

    private static Identifiable<?> getIdentifiable(Network network, String id) {
        Identifiable<?> identifiable = network.getIdentifiable(id);
        if (identifiable == null) {
            throw new PowsyblException("Network element '" + id + "' not found");
        }
        return identifiable;
    }

    /* draw functions with file */

    public static void draw(Network network, String id, String svgFile) {
        draw(network, id, Path.of(svgFile));
    }

    public static void draw(Network network, String id, Path svgFile) {
        draw(network, id, svgFile, new SingleLineDiagramConfigurationAdder(network).add());
    }

    public static void draw(Network network, String id, Path svgFile, SingleLineDiagramConfiguration singleLineDiagramConfiguration) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(id);

        Identifiable<?> identifiable = getIdentifiable(network, id);

        if (identifiable.getType() == VOLTAGE_LEVEL) {
            drawVoltageLevel(network, id, svgFile, singleLineDiagramConfiguration);
        } else if (identifiable.getType() == SUBSTATION) {
            drawSubstation(network, id, svgFile, singleLineDiagramConfiguration);
        } else {
            throw new PowsyblException("Given id '" + id + "' is not a substation or voltage level id in given network '" + network.getId() + "'");
        }
    }

    public static void drawVoltageLevel(Network network, String id, String svgFile) {
        drawVoltageLevel(network, id, Path.of(svgFile));
    }

    public static void drawVoltageLevel(Network network, String voltageLevelId, Path svgFile) {
        drawVoltageLevel(network, voltageLevelId, svgFile, new SingleLineDiagramConfigurationAdder(network).add());
    }

    private static void drawVoltageLevel(Network network, String voltageLevelId, Path svgFile, SingleLineDiagramConfiguration singleLineDiagramConfiguration) {
        LayoutParameters layoutParameters = singleLineDiagramConfiguration.getLayoutParameters();
        VoltageLevelLayoutFactory voltageLevelLayoutFactory = singleLineDiagramConfiguration.getVoltageLevelLayoutFactory();

        Objects.requireNonNull(voltageLevelLayoutFactory);

        VoltageLevelGraph voltageLevelGraph = new NetworkGraphBuilder(network).buildVoltageLevelGraph(voltageLevelId);
        voltageLevelLayoutFactory.create(voltageLevelGraph).run(layoutParameters);
        draw(voltageLevelGraph, svgFile, singleLineDiagramConfiguration);
    }

    public static void drawSubstation(Network network, String id, String svgFile) {
        drawSubstation(network, id, Path.of(svgFile));
    }

    public static void drawSubstation(Network network, String id, Path svgFile) {
        drawSubstation(network, id, svgFile, new SingleLineDiagramConfigurationAdder(network).add());
    }

    private static void drawSubstation(Network network, String substationId, Path svgFile, SingleLineDiagramConfiguration singleLineDiagramConfiguration) {

        LayoutParameters layoutParameters = singleLineDiagramConfiguration.getLayoutParameters();
        VoltageLevelLayoutFactory voltageLevelLayoutFactory = singleLineDiagramConfiguration.getVoltageLevelLayoutFactory();
        SubstationLayoutFactory substationLayoutFactory = singleLineDiagramConfiguration.getSubstationLayoutFactory();

        Objects.requireNonNull(substationLayoutFactory);
        Objects.requireNonNull(voltageLevelLayoutFactory);

        SubstationGraph substationGraph = new NetworkGraphBuilder(network).buildSubstationGraph(substationId);
        substationLayoutFactory.create(substationGraph, voltageLevelLayoutFactory).run(layoutParameters);
        draw(substationGraph, svgFile, singleLineDiagramConfiguration);
    }

    public static void draw(Graph graph, Path svgFile, SingleLineDiagramConfiguration singleLineDiagramConfiguration) {
        Objects.requireNonNull(svgFile);

        Path dir = svgFile.toAbsolutePath().getParent();
        String svgFileName = svgFile.getFileName().toString();
        if (!svgFileName.endsWith(".svg")) {
            svgFileName = svgFileName + ".svg";
        }
        try (Writer writerForSvg = Files.newBufferedWriter(svgFile, StandardCharsets.UTF_8);
             Writer metadataWriter = Files.newBufferedWriter(dir.resolve(svgFileName.replace(".svg", "_metadata.json")), StandardCharsets.UTF_8)) {
            draw(graph, writerForSvg, metadataWriter, singleLineDiagramConfiguration);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /* draw functions with writer */

    public static void draw(Network network, String id, Writer writerForSvg, Writer metadataWriter) {
        draw(network, id, writerForSvg, metadataWriter, new SingleLineDiagramConfigurationAdder(network).add());
    }

    public static void draw(Network network, String id, Writer writerForSvg, Writer metadataWriter, SingleLineDiagramConfiguration singleLineDiagramConfiguration) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(id);

        Identifiable<?> identifiable = getIdentifiable(network, id);
        if (identifiable.getType() == VOLTAGE_LEVEL) {
            drawVoltageLevel(network, id, writerForSvg, metadataWriter, singleLineDiagramConfiguration);
        } else if (identifiable.getType() == SUBSTATION) {
            drawSubstation(network, id, writerForSvg, metadataWriter, singleLineDiagramConfiguration);
        } else {
            throw new PowsyblException("Given id '" + id + "' is not a substation or voltage level id in given network '" + network.getId() + "'");
        }
    }

    public static void drawVoltageLevel(Network network, String voltageLevelId, Writer writerForSvg, Writer metadataWriter, SingleLineDiagramConfiguration singleLineDiagramConfiguration) {
        LayoutParameters layoutParameters = singleLineDiagramConfiguration.getLayoutParameters();
        VoltageLevelLayoutFactory voltageLevelLayoutFactory = singleLineDiagramConfiguration.getVoltageLevelLayoutFactory();

        Objects.requireNonNull(voltageLevelLayoutFactory);

        VoltageLevelGraph voltageLevelGraph = new NetworkGraphBuilder(network).buildVoltageLevelGraph(voltageLevelId);
        voltageLevelLayoutFactory.create(voltageLevelGraph).run(layoutParameters);
        draw(voltageLevelGraph, writerForSvg, metadataWriter, singleLineDiagramConfiguration);
    }

    public static void drawSubstation(Network network, String substationId, Writer writerForSvg, Writer metadataWriter, SingleLineDiagramConfiguration singleLineDiagramConfiguration) {
        LayoutParameters layoutParameters = singleLineDiagramConfiguration.getLayoutParameters();
        VoltageLevelLayoutFactory voltageLevelLayoutFactory = singleLineDiagramConfiguration.getVoltageLevelLayoutFactory();
        SubstationLayoutFactory substationLayoutFactory = singleLineDiagramConfiguration.getSubstationLayoutFactory();

        Objects.requireNonNull(substationLayoutFactory);
        Objects.requireNonNull(voltageLevelLayoutFactory);

        SubstationGraph substationGraph = new NetworkGraphBuilder(network).buildSubstationGraph(substationId);
        substationLayoutFactory.create(substationGraph, voltageLevelLayoutFactory).run(layoutParameters);
        draw(substationGraph, writerForSvg, metadataWriter, singleLineDiagramConfiguration);
    }

    public static void draw(Graph graph, Writer writerForSvg, Writer metadataWriter,
                            SingleLineDiagramConfiguration singleLineDiagramConfiguration) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(writerForSvg);
        Objects.requireNonNull(metadataWriter);

        DiagramLabelProvider diagramLabelProvider = singleLineDiagramConfiguration.getDiagramLabelProvider();
        DiagramStyleProvider diagramStyleProvider = singleLineDiagramConfiguration.getDiagramStyleProvider();
        SvgParameters svgParameters = singleLineDiagramConfiguration.getSvgParameters();

        Objects.requireNonNull(diagramLabelProvider);
        Objects.requireNonNull(diagramStyleProvider);
        Objects.requireNonNull(svgParameters);

        DefaultSVGWriter svgWriter = new DefaultSVGWriter(singleLineDiagramConfiguration.getComponentLibrary(), singleLineDiagramConfiguration.getLayoutParameters());

        LOGGER.info("Writing SVG and JSON metadata files...");

        // write SVG file
        GraphMetadata metadata = svgWriter.write(svgParameters.getPrefixId(), graph, diagramLabelProvider, diagramStyleProvider, writerForSvg);

        // write metadata JSON file
        metadata.writeJson(metadataWriter);
    }
}
