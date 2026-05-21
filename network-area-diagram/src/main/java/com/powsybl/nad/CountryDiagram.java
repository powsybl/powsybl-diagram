/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad;

import com.powsybl.iidm.network.Network;
import com.powsybl.nad.build.iidm.DefaultCountryStyleProvider;
import com.powsybl.nad.build.iidm.CountryGraphBuilder;
import com.powsybl.nad.build.iidm.DefaultCountryLabelProvider;
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
import java.util.Objects;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public final class CountryDiagram {

    private CountryDiagram() {
    }

    public static void draw(Network network, Path svgFile) {
        draw(network, svgFile, new NadParameters());
    }

    public static void draw(Network network, Writer writer, Writer metadataWriter) {
        draw(network, writer, metadataWriter, new NadParameters());
    }

    public static void draw(Network network, Path svgFile, NadParameters param) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(svgFile);
        Objects.requireNonNull(param);

        Graph graph = getLayoutResult(network, param);
        createSvgWriter(network, param).writeSvg(graph, svgFile);
        createMetadata(graph, param).writeJson(getMetadataPath(svgFile));
    }

    public static void draw(Network network, Writer writer, Writer metadataWriter, NadParameters param) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(writer);
        Objects.requireNonNull(metadataWriter);
        Objects.requireNonNull(param);

        Graph graph = getLayoutResult(network, param);
        createSvgWriter(network, param).writeSvg(graph, writer);
        createMetadata(graph, param).writeJson(metadataWriter);
    }

    private static DiagramMetadata createMetadata(Graph graph, NadParameters param) {
        return new DiagramMetadata(param.getLayoutParameters(), param.getSvgParameters()).addMetadata(graph);
    }

    private static Graph getLayoutResult(Network network, NadParameters param) {
        var builder = new CountryGraphBuilder(network, param.getIdProviderFactory().create(), new DefaultCountryLabelProvider());
        var graph = builder.buildGraph();
        param.getLayoutFactory().create().run(graph, param.getLayoutParameters());
        return graph;
    }

    private static SvgWriter createSvgWriter(Network network, NadParameters param) {
        return new SvgWriter(param.getSvgParameters(), new DefaultCountryStyleProvider(network),
                param.getComponentLibrary(), param.getEdgeRouting());
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
            draw(network, writer, NullWriter.INSTANCE, nadParameters);
            return writer.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
