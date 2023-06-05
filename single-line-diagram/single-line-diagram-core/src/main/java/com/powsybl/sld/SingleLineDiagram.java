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
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.SubstationLayoutFactory;
import com.powsybl.sld.layout.VoltageLevelLayoutFactory;
import com.powsybl.sld.model.graphs.Graph;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
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
        draw(network, id, svgFile, new ConfigBuilder().build());
    }

    public static void draw(Network network, String id, Path svgFile, Config config) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(id);

        Identifiable<?> identifiable = getIdentifiable(network, id);

        if (identifiable.getType() == VOLTAGE_LEVEL) {
            drawVoltageLevel(network, id, svgFile, config);
        } else if (identifiable.getType() == SUBSTATION) {
            drawSubstation(network, id, svgFile, config);
        } else {
            throw new PowsyblException("Given id '" + id + "' is not a substation or voltage level id in given network '" + network.getId() + "'");
        }
    }

    public static void drawVoltageLevel(Network network, String id, String svgFile) {
        drawVoltageLevel(network, id, Path.of(svgFile));
    }

    public static void drawVoltageLevel(Network network, String voltageLevelId, Path svgFile) {
        drawVoltageLevel(network, voltageLevelId, svgFile, new ConfigBuilder().build());
    }

    private static void drawVoltageLevel(Network network, String voltageLevelId, Path svgFile, Config config) {
        LayoutParameters layoutParameters = config.getLayoutParameters();
        VoltageLevelLayoutFactory voltageLevelLayoutFactory = config.getVoltageLevelLayoutFactory();

        Objects.requireNonNull(voltageLevelLayoutFactory);

        VoltageLevelGraph voltageLevelGraph = new NetworkGraphBuilder(network).buildVoltageLevelGraph(voltageLevelId);
        voltageLevelLayoutFactory.create(voltageLevelGraph).run(layoutParameters);
        DefaultSVGWriter svgWriter = new DefaultSVGWriter(config.getComponentLibrary(), config.getLayoutParameters(), config.getSvgParameters());
        draw(voltageLevelGraph, svgFile, svgWriter, config.createLabelProvider(network), config.createStyleProvider(network), config.getSvgParameters().getPrefixId());
    }

    public static void drawSubstation(Network network, String id, String svgFile) {
        drawSubstation(network, id, Path.of(svgFile));
    }

    public static void drawSubstation(Network network, String id, Path svgFile) {
        drawSubstation(network, id, svgFile, new ConfigBuilder().build());
    }

    private static void drawSubstation(Network network, String substationId, Path svgFile, Config config) {

        LayoutParameters layoutParameters = config.getLayoutParameters();
        VoltageLevelLayoutFactory voltageLevelLayoutFactory = config.getVoltageLevelLayoutFactory();
        SubstationLayoutFactory substationLayoutFactory = config.getSubstationLayoutFactory();

        Objects.requireNonNull(substationLayoutFactory);
        Objects.requireNonNull(voltageLevelLayoutFactory);

        SubstationGraph substationGraph = new NetworkGraphBuilder(network).buildSubstationGraph(substationId);
        substationLayoutFactory.create(substationGraph, voltageLevelLayoutFactory).run(layoutParameters);
        DefaultSVGWriter svgWriter = new DefaultSVGWriter(config.getComponentLibrary(), config.getLayoutParameters(), config.getSvgParameters());
        draw(substationGraph, svgFile, svgWriter, config.createLabelProvider(network), config.createStyleProvider(network), config.getSvgParameters().getPrefixId());
    }

    public static void draw(Graph graph, Path svgFile, DefaultSVGWriter svgWriter, LabelProvider labelProvider, StyleProvider styleProvider, String prefixId) {
        Objects.requireNonNull(svgFile);

        Path dir = svgFile.toAbsolutePath().getParent();
        String svgFileName = svgFile.getFileName().toString();
        if (!svgFileName.endsWith(".svg")) {
            svgFileName = svgFileName + ".svg";
        }
        try (Writer writerForSvg = Files.newBufferedWriter(svgFile, StandardCharsets.UTF_8);
             Writer metadataWriter = Files.newBufferedWriter(dir.resolve(svgFileName.replace(".svg", "_metadata.json")), StandardCharsets.UTF_8)) {
            draw(graph, writerForSvg, metadataWriter, svgWriter, labelProvider, styleProvider, prefixId);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /* draw functions with writer */

    public static void draw(Network network, String id, Writer writerForSvg, Writer metadataWriter) {
        draw(network, id, writerForSvg, metadataWriter, new ConfigBuilder().build());
    }

    public static void draw(Network network, String id, Writer writerForSvg, Writer metadataWriter, Config config) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(id);

        Identifiable<?> identifiable = getIdentifiable(network, id);
        if (identifiable.getType() == VOLTAGE_LEVEL) {
            drawVoltageLevel(network, id, writerForSvg, metadataWriter, config);
        } else if (identifiable.getType() == SUBSTATION) {
            drawSubstation(network, id, writerForSvg, metadataWriter, config);
        } else {
            throw new PowsyblException("Given id '" + id + "' is not a substation or voltage level id in given network '" + network.getId() + "'");
        }
    }

    public static void drawVoltageLevel(Network network, String voltageLevelId, Writer writerForSvg, Writer metadataWriter, Config config) {
        LayoutParameters layoutParameters = config.getLayoutParameters();
        VoltageLevelLayoutFactory voltageLevelLayoutFactory = config.getVoltageLevelLayoutFactory();

        Objects.requireNonNull(voltageLevelLayoutFactory);

        VoltageLevelGraph voltageLevelGraph = new NetworkGraphBuilder(network).buildVoltageLevelGraph(voltageLevelId);
        voltageLevelLayoutFactory.create(voltageLevelGraph).run(layoutParameters);
        DefaultSVGWriter svgWriter = new DefaultSVGWriter(config.getComponentLibrary(), config.getLayoutParameters(), config.getSvgParameters());
        draw(voltageLevelGraph, writerForSvg, metadataWriter, svgWriter, config.createLabelProvider(network), config.createStyleProvider(network), config.getSvgParameters().getPrefixId());
    }

    public static void drawSubstation(Network network, String substationId, Writer writerForSvg, Writer metadataWriter, Config config) {
        LayoutParameters layoutParameters = config.getLayoutParameters();
        VoltageLevelLayoutFactory voltageLevelLayoutFactory = config.getVoltageLevelLayoutFactory();
        SubstationLayoutFactory substationLayoutFactory = config.getSubstationLayoutFactory();

        Objects.requireNonNull(substationLayoutFactory);
        Objects.requireNonNull(voltageLevelLayoutFactory);

        SubstationGraph substationGraph = new NetworkGraphBuilder(network).buildSubstationGraph(substationId);
        substationLayoutFactory.create(substationGraph, voltageLevelLayoutFactory).run(layoutParameters);
        DefaultSVGWriter svgWriter = new DefaultSVGWriter(config.getComponentLibrary(), config.getLayoutParameters(), config.getSvgParameters());
        draw(substationGraph, writerForSvg, metadataWriter, svgWriter, config.createLabelProvider(network), config.createStyleProvider(network), config.getSvgParameters().getPrefixId());
    }

    public static void draw(Graph graph, Writer writerForSvg, Writer metadataWriter, SVGWriter svgWriter, LabelProvider labelProvider, StyleProvider styleProvider, String prefixId) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(writerForSvg);
        Objects.requireNonNull(metadataWriter);
        Objects.requireNonNull(labelProvider);
        Objects.requireNonNull(styleProvider);

        LOGGER.info("Writing SVG and JSON metadata files...");

        // write SVG file
        GraphMetadata metadata = svgWriter.write(prefixId, graph, labelProvider, styleProvider, writerForSvg);

        // write metadata JSON file
        metadata.writeJson(metadataWriter);
    }
}
