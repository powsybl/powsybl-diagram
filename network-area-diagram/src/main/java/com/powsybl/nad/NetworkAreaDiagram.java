/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.nad.build.iidm.NetworkGraphBuilder;
import com.powsybl.nad.build.iidm.VoltageLevelFilter;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.svg.SvgParameters;
import com.powsybl.nad.svg.SvgWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public final class NetworkAreaDiagram {

    private NetworkAreaDiagram() {
    }

    public static void draw(Network network, Path svgFile) {
        draw(network, svgFile, new NadParameters(), VoltageLevelFilter.NO_FILTER);
    }

    public static void draw(Network network, Writer writer) {
        draw(network, writer, new NadParameters(), VoltageLevelFilter.NO_FILTER);
    }

    public static void draw(Network network, Path svgFile, String voltageLevelId, int depth) {
        draw(network, svgFile, new NadParameters(), VoltageLevelFilter.createVoltageLevelDepthFilter(network, voltageLevelId, depth));
    }

    public static void draw(Network network, Writer writer, String voltageLevelId, int depth) {
        draw(network, writer, new NadParameters(), VoltageLevelFilter.createVoltageLevelDepthFilter(network, voltageLevelId, depth));
    }

    public static void draw(Network network, Path svgFile, List<String> voltageLevelIds) {
        draw(network, svgFile, new NadParameters(), VoltageLevelFilter.createVoltageLevelsFilter(network, voltageLevelIds));
    }

    public static void draw(Network network, Writer writer, List<String> voltageLevelIds) {
        draw(network, writer, new NadParameters(), VoltageLevelFilter.createVoltageLevelsFilter(network, voltageLevelIds));
    }

    public static void draw(Network network, Path svgFile, List<String> voltageLevelIds, int depth) {
        draw(network, svgFile, new NadParameters(), VoltageLevelFilter.createVoltageLevelsDepthFilter(network, voltageLevelIds, depth));
    }

    public void draw(Network network, Writer writer, Predicate<VoltageLevel> voltageLevelFilter) {
        draw(network, writer, new NadParameters(), voltageLevelFilter);
    }

    public static void draw(Network network, Path svgFile, NadParameters param, Predicate<VoltageLevel> voltageLevelFilter) {
        genericDraw(network, svgFile, param, voltageLevelFilter);
    }

    public static void draw(Network network, Writer writer, NadParameters param, Predicate<VoltageLevel> voltageLevelFilter) {
        genericDraw(network, writer, param, voltageLevelFilter);
    }

    private static void genericDraw(Network network, Object object, NadParameters param, Predicate<VoltageLevel> voltageLevelFilter) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(object);
        Objects.requireNonNull(param);
        Objects.requireNonNull(voltageLevelFilter);

        Graph graph = new NetworkGraphBuilder(network, voltageLevelFilter, param.getIdProviderFactory().create()).buildGraph();
        param.getLayoutFactory().create().run(graph, param.getLayoutParameters());
        SvgWriter svgWriter = new SvgWriter(param.getSvgParameters(), param.getStyleProviderFactory().create(network), param.createLabelProvider(network));

        if (object instanceof Path svgFile) {
            svgWriter.writeSvg(graph, svgFile);
        } else if (object instanceof Writer writer) {
            svgWriter.writeSvg(graph, writer);
        } else {
            throw new PowsyblException("Second argument is an instance of an unexpected class");
        }
    }

    public String drawToString(Network network, SvgParameters svgParameters) {
        try (StringWriter writer = new StringWriter()) {
            NadParameters nadParameters = new NadParameters().setSvgParameters(svgParameters);
            draw(network, writer, nadParameters, VoltageLevelFilter.NO_FILTER);
            return writer.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static List<String> getDisplayedVoltageLevels(Network network, List<String> voltageLevelIds, int depth) {
        NetworkGraphBuilder builder = new NetworkGraphBuilder(network, VoltageLevelFilter.createVoltageLevelsDepthFilter(network, voltageLevelIds, depth));
        return builder.getVoltageLevels().stream()
                .map(VoltageLevel::getId)
                .sorted()
                .toList();
    }
}
