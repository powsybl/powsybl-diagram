/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.nad.build.iidm.NetworkGraphBuilder;
import com.powsybl.nad.build.iidm.VoltageLevelFilter;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.svg.SvgParameters;
import com.powsybl.nad.svg.SvgWriter;
import com.powsybl.nad.svg.metadata.DiagramMetadata;
import org.apache.commons.io.output.NullWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public final class NetworkAreaDiagram {

    private NetworkAreaDiagram() {
    }

    public static void draw(Network network, Path svgFile) {
        draw(network, svgFile, new NadParameters(), VoltageLevelFilter.NO_FILTER);
    }

    public static void draw(Network network, Writer writer, Writer metadataWriter) {
        draw(network, writer, metadataWriter, new NadParameters(), VoltageLevelFilter.NO_FILTER);
    }

    public static void draw(Network network, Path svgFile, String voltageLevelId, int depth) {
        draw(network, svgFile, new NadParameters(), VoltageLevelFilter.createVoltageLevelDepthFilter(network, voltageLevelId, depth));
    }

    public static void draw(Network network, Writer writer, Writer metadataWriter, String voltageLevelId, int depth) {
        draw(network, writer, metadataWriter, new NadParameters(), VoltageLevelFilter.createVoltageLevelDepthFilter(network, voltageLevelId, depth));
    }

    public static void draw(Network network, Path svgFile, List<String> voltageLevelIds) {
        draw(network, svgFile, new NadParameters(), VoltageLevelFilter.createVoltageLevelsFilter(network, voltageLevelIds));
    }

    public static void draw(Network network, Writer writer, Writer metadataWriter, List<String> voltageLevelIds) {
        draw(network, writer, metadataWriter, new NadParameters(), VoltageLevelFilter.createVoltageLevelsFilter(network, voltageLevelIds));
    }

    public static void draw(Network network, Path svgFile, List<String> voltageLevelIds, int depth) {
        draw(network, svgFile, new NadParameters(), VoltageLevelFilter.createVoltageLevelsDepthFilter(network, voltageLevelIds, depth));
    }

    public static void draw(Network network, Writer writer, Writer metadataWriter, Predicate<VoltageLevel> voltageLevelFilter) {
        draw(network, writer, metadataWriter, new NadParameters(), voltageLevelFilter);
    }

    public static void draw(Network network, Path svgFile, NadParameters param, Predicate<VoltageLevel> voltageLevelFilter) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(svgFile);
        Objects.requireNonNull(param);

        Graph graph = getLayoutResult(network, param, voltageLevelFilter);
        createSvgWriter(network, param).writeSvg(graph, svgFile);
        createMetadata(graph, param).writeJson(getMetadataPath(svgFile));
    }

    public static void draw(Network network, Writer writer, Writer metadataWriter, NadParameters param, Predicate<VoltageLevel> voltageLevelFilter) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(writer);
        Objects.requireNonNull(metadataWriter);
        Objects.requireNonNull(param);

        Graph graph = getLayoutResult(network, param, voltageLevelFilter);
        createSvgWriter(network, param).writeSvg(graph, writer);
        createMetadata(graph, param).writeJson(metadataWriter);
    }

    private static DiagramMetadata createMetadata(Graph graph, NadParameters param) {
        return new DiagramMetadata(param.getLayoutParameters(), param.getSvgParameters()).addMetadata(graph);
    }

    private static Graph getLayoutResult(Network network, NadParameters param, Predicate<VoltageLevel> voltageLevelFilter) {
        Objects.requireNonNull(voltageLevelFilter);
        var networkGraphBuilder = new NetworkGraphBuilder(network, voltageLevelFilter, param.getLayoutParameters(), param.getIdProviderFactory().create());
        var graph = networkGraphBuilder.buildGraph();
        param.getLayoutFactory().create().run(graph, param.getLayoutParameters());
        return graph;
    }

    private static SvgWriter createSvgWriter(Network network, NadParameters param) {
        return new SvgWriter(param.getSvgParameters(), param.getStyleProviderFactory().create(network),
                param.createLabelProvider(network), param.getComponentLibrary(), param.getEdgeRouting());
    }

    private static Path getMetadataPath(Path svgPath) {
        Path dir = svgPath.toAbsolutePath().getParent();
        String svgFileName = svgPath.getFileName().toString();
        if (!svgFileName.endsWith(".svg")) {
            svgFileName = svgFileName + ".svg";
        }
        return dir.resolve(svgFileName.replace(".svg", "_metadata.json"));
    }

    public static String drawToString(Network network, SvgParameters svgParameters) {
        try (StringWriter writer = new StringWriter()) {
            NadParameters nadParameters = new NadParameters().setSvgParameters(svgParameters);
            draw(network, writer, NullWriter.INSTANCE, nadParameters, VoltageLevelFilter.NO_FILTER);
            return writer.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static List<String> getDisplayedVoltageLevels(Network network, List<String> voltageLevelIds, int depth) {
        NetworkGraphBuilder builder = new NetworkGraphBuilder(network, VoltageLevelFilter.createVoltageLevelsDepthFilter(network, voltageLevelIds, depth), new LayoutParameters());
        return builder.getVoltageLevels().stream()
                .map(VoltageLevel::getId)
                .sorted()
                .toList();
    }
}
