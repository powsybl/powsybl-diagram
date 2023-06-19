/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.nad.build.iidm.IdProvider;
import com.powsybl.nad.build.iidm.NetworkGraphBuilder;
import com.powsybl.nad.build.iidm.VoltageLevelFilter;
import com.powsybl.nad.layout.LayoutFactory;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.svg.LabelProvider;
import com.powsybl.nad.svg.StyleProvider;
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

    /* ------------------------------------------------------------------------------------------------ */
    // Network, Path and different options of filtering
    /* ------------------------------------------------------------------------------------------------ */

    public static void draw(Network network, Path svgFile) {
        draw(network, svgFile, NadParameters.defaultParameters(), VoltageLevelFilter.NO_FILTER);
    }

    public static void draw(Network network, Path svgFile, String voltageLevelId, int depth) {
        draw(network, svgFile, NadParameters.defaultParameters(), VoltageLevelFilter.createVoltageLevelDepthFilter(network, voltageLevelId, depth));
    }

    public static void draw(Network network, Path svgFile, List<String> voltageLevelIds) {
        draw(network, svgFile, NadParameters.defaultParameters(), VoltageLevelFilter.createVoltageLevelsFilter(network, voltageLevelIds));
    }

    public static void draw(Network network, Path svgFile, List<String> voltageLevelIds, int depth) {
        draw(network, svgFile, NadParameters.defaultParameters(), VoltageLevelFilter.createVoltageLevelsDepthFilter(network, voltageLevelIds, depth));
    }

    /* ---------------------------------------------------------------- */
    // Network, Path, NadParameters, Predicate
    /* ---------------------------------------------------------------- */

    public static void draw(Network network, Path svgFile, NadParameters param, Predicate<VoltageLevel> voltageLevelFilter) {

        Objects.requireNonNull(network);
        Objects.requireNonNull(svgFile);
        LayoutParameters layoutParameters = param.getLayoutParameters();
        SvgParameters svgParameters = param.getSvgParameters();
        StyleProvider styleProvider = param.getStyleProviderFactory().create(network);
        LayoutFactory layoutFactory = param.getLayoutFactory();
        IdProvider idProvider = param.getIdProviderFactory().create();
        LabelProvider labelProvider = param.createLabelProvider(network);
        Objects.requireNonNull(voltageLevelFilter);

        Graph graph = new NetworkGraphBuilder(network, voltageLevelFilter, idProvider).buildGraph();
        layoutFactory.create().run(graph, layoutParameters);
        new SvgWriter(svgParameters, styleProvider, labelProvider).writeSvg(graph, svgFile);
    }

    /* ------------------------------------------------------------------------------------------------ */
    // Network, Writer and different options of filtering
    /* ------------------------------------------------------------------------------------------------ */

    public static void draw(Network network, Writer writer) {
        draw(network, writer, NadParameters.defaultParameters(), VoltageLevelFilter.NO_FILTER);
    }

    public static void draw(Network network, Writer writer, String voltageLevelId, int depth) {
        draw(network, writer, NadParameters.defaultParameters(), VoltageLevelFilter.createVoltageLevelDepthFilter(network, voltageLevelId, depth));
    }

    public static void draw(Network network, Writer writer, List<String> voltageLevelIds) {
        draw(network, writer, NadParameters.defaultParameters(), VoltageLevelFilter.createVoltageLevelsFilter(network, voltageLevelIds));
    }

    public void draw(Network network, Writer writer, Predicate<VoltageLevel> voltageLevelFilter) {
        draw(network, writer, NadParameters.defaultParameters(), voltageLevelFilter);
    }

    /* ----------------------------------------------------------------------------------------------- */
    // Network, Writer, NadParameters, Predicate
    /* ----------------------------------------------------------------------------------------------- */

    public static void draw(Network network, Writer writer, NadParameters nadParameters, Predicate<VoltageLevel> voltageLevelFilter) {
        Graph graph = new NetworkGraphBuilder(network, voltageLevelFilter, nadParameters.getIdProviderFactory().create()).buildGraph();
        nadParameters.getLayoutFactory().create().run(graph, nadParameters.getLayoutParameters());
        new SvgWriter(nadParameters.getSvgParameters(), nadParameters.getStyleProviderFactory().create(network), nadParameters.createLabelProvider(network)).writeSvg(graph, writer);
    }

    public String drawToString(Network network, SvgParameters svgParameters) {
        try (StringWriter writer = new StringWriter()) {
            NadParameters nadParameters = NadParameters.builder().withSvgParameters(svgParameters).build();
            draw(network, writer, nadParameters, VoltageLevelFilter.NO_FILTER);
            return writer.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
