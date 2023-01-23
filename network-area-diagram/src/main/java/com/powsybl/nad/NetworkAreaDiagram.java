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
import com.powsybl.nad.build.iidm.IntIdProvider;
import com.powsybl.nad.build.iidm.NetworkGraphBuilder;
import com.powsybl.nad.build.iidm.VoltageLevelFilter;
import com.powsybl.nad.layout.BasicForceLayoutFactory;
import com.powsybl.nad.layout.LayoutFactory;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.svg.LabelProvider;
import com.powsybl.nad.svg.StyleProvider;
import com.powsybl.nad.svg.SvgParameters;
import com.powsybl.nad.svg.SvgWriter;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.TopologicalStyleProvider;

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
        draw(network, svgFile, new SvgParameters(), VoltageLevelFilter.NO_FILTER);
    }

    public static void draw(Network network, Path svgFile, String voltageLevelId, int depth) {
        draw(network, svgFile, new SvgParameters(), VoltageLevelFilter.createVoltageLevelDepthFilter(network, voltageLevelId, depth));
    }

    public static void draw(Network network, Path svgFile, List<String> voltageLevelIds) {
        draw(network, svgFile, new SvgParameters(), VoltageLevelFilter.createVoltageLevelsFilter(network, voltageLevelIds));
    }

    public static void draw(Network network, Path svgFile, List<String> voltageLevelIds, int depth) {
        draw(network, svgFile, new SvgParameters(), VoltageLevelFilter.createVoltageLevelsDepthFilter(network, voltageLevelIds, depth));
    }

    /* ------------------------------------------------------------------------------------------------ */
    // Network, Path, SvgParameters and different options of filtering
    /* ------------------------------------------------------------------------------------------------ */

    public static void draw(Network network, Path svgFile, SvgParameters svgParameters) {
        draw(network, svgFile, svgParameters, VoltageLevelFilter.NO_FILTER);
    }

    public static void draw(Network network, Path svgFile, SvgParameters svgParameters, String voltageLevelId, int depth) {
        draw(network, svgFile, svgParameters, VoltageLevelFilter.createVoltageLevelDepthFilter(network, voltageLevelId, depth));
    }

    public static void draw(Network network, Path svgFile, SvgParameters svgParameters, List<String> voltageLevelIds, int depth) {
        draw(network, svgFile, svgParameters, VoltageLevelFilter.createVoltageLevelsDepthFilter(network, voltageLevelIds, depth));
    }

    public static void draw(Network network, Path svgFile, SvgParameters svgParameters, Predicate<VoltageLevel> voltageLevelFilter) {
        draw(network, svgFile, svgParameters, new LayoutParameters(), voltageLevelFilter);
    }

    /* ------------------------------------------------------------------------------------------------ */
    // Network, Path, SvgParameters, LayoutParameters and different options of filtering
    /* ------------------------------------------------------------------------------------------------ */

    public static void draw(Network network, Path svgFile, SvgParameters svgParameters, LayoutParameters layoutParameters) {
        draw(network, svgFile, svgParameters, layoutParameters, VoltageLevelFilter.NO_FILTER);
    }

    public static void draw(Network network, Path svgFile, SvgParameters svgParameters, LayoutParameters layoutParameters, String voltageLevelId, int depth) {
        draw(network, svgFile, svgParameters, layoutParameters, VoltageLevelFilter.createVoltageLevelDepthFilter(network, voltageLevelId, depth));
    }

    public static void draw(Network network, Path svgFile, SvgParameters svgParameters, LayoutParameters layoutParameters, List<String> voltageLevelIds, int depth) {
        draw(network, svgFile, svgParameters, layoutParameters, VoltageLevelFilter.createVoltageLevelsDepthFilter(network, voltageLevelIds, depth));
    }

    public static void draw(Network network, Path svgFile, SvgParameters svgParameters, LayoutParameters layoutParameters, Predicate<VoltageLevel> voltageLevelFilter) {
        draw(network, svgFile, svgParameters, layoutParameters, new TopologicalStyleProvider(network), voltageLevelFilter);
    }

    /* ------------------------------------------------------------------------------------------------ */
    // Network, Path, SvgParameters, LayoutParameters, StyleProvider and different options of filtering
    /* ------------------------------------------------------------------------------------------------ */

    public static void draw(Network network, Path svgFile, SvgParameters svgParameters, LayoutParameters layoutParameters, StyleProvider styleProvider) {
        draw(network, svgFile, svgParameters, layoutParameters, styleProvider, VoltageLevelFilter.NO_FILTER);
    }

    public static void draw(Network network, Path svgFile, SvgParameters svgParameters, LayoutParameters layoutParameters, StyleProvider styleProvider, String voltageLevelId, int depth) {
        draw(network, svgFile, svgParameters, layoutParameters, styleProvider, VoltageLevelFilter.createVoltageLevelDepthFilter(network, voltageLevelId, depth));
    }

    public static void draw(Network network, Path svgFile, SvgParameters svgParameters, LayoutParameters layoutParameters, StyleProvider styleProvider, List<String> voltageLevelIds, int depth) {
        draw(network, svgFile, svgParameters, layoutParameters, styleProvider, VoltageLevelFilter.createVoltageLevelsDepthFilter(network, voltageLevelIds, depth));
    }

    public static void draw(Network network, Path svgFile, SvgParameters svgParameters, LayoutParameters layoutParameters,
                            StyleProvider styleProvider, Predicate<VoltageLevel> voltageLevelFilter) {
        draw(network, svgFile, svgParameters, layoutParameters, styleProvider, new DefaultLabelProvider(network, svgParameters), voltageLevelFilter);
    }

    /* --------------------------------------------------------------------------------------------------------------- */
    // Network, Path, SvgParameters, LayoutParameters, StyleProvider, LabelProvider and different options of filtering
    /* --------------------------------------------------------------------------------------------------------------- */

    public static void draw(Network network, Path svgFile, SvgParameters svgParameters, LayoutParameters layoutParameters, StyleProvider styleProvider, LabelProvider labelProvider) {
        draw(network, svgFile, svgParameters, layoutParameters, styleProvider, labelProvider, VoltageLevelFilter.NO_FILTER);
    }

    public static void draw(Network network, Path svgFile, SvgParameters svgParameters, LayoutParameters layoutParameters, StyleProvider styleProvider, LabelProvider labelProvider, String voltageLevelId, int depth) {
        draw(network, svgFile, svgParameters, layoutParameters, styleProvider, labelProvider, VoltageLevelFilter.createVoltageLevelDepthFilter(network, voltageLevelId, depth));
    }

    public static void draw(Network network, Path svgFile, SvgParameters svgParameters, LayoutParameters layoutParameters, StyleProvider styleProvider, LabelProvider labelProvider, List<String> voltageLevelIds, int depth) {
        draw(network, svgFile, svgParameters, layoutParameters, styleProvider, labelProvider, VoltageLevelFilter.createVoltageLevelsDepthFilter(network, voltageLevelIds, depth));
    }

    public static void draw(Network network, Path svgFile, SvgParameters svgParameters, LayoutParameters layoutParameters,
                            StyleProvider styleProvider, LabelProvider labelProvider, Predicate<VoltageLevel> voltageLevelFilter) {
        draw(network, svgFile, svgParameters, layoutParameters, styleProvider, labelProvider, new BasicForceLayoutFactory(), voltageLevelFilter);
    }

    /* ------------------------------------------------------------------------------------------------------------------------------ */
    // Network, Path, SvgParameters, LayoutParameters, StyleProvider, LabelProvider, LayoutFactory and different options of filtering
    /* ------------------------------------------------------------------------------------------------------------------------------ */

    public static void draw(Network network, Path svgFile, SvgParameters svgParameters, LayoutParameters layoutParameters, StyleProvider styleProvider, LabelProvider labelProvider, LayoutFactory layoutFactory) {
        draw(network, svgFile, svgParameters, layoutParameters, styleProvider, labelProvider, layoutFactory, VoltageLevelFilter.NO_FILTER);
    }

    public static void draw(Network network, Path svgFile, SvgParameters svgParameters, LayoutParameters layoutParameters, StyleProvider styleProvider, LabelProvider labelProvider, LayoutFactory layoutFactory, String voltageLevelId, int depth) {
        draw(network, svgFile, svgParameters, layoutParameters, styleProvider, labelProvider, layoutFactory, VoltageLevelFilter.createVoltageLevelDepthFilter(network, voltageLevelId, depth));
    }

    public static void draw(Network network, Path svgFile, SvgParameters svgParameters, LayoutParameters layoutParameters, StyleProvider styleProvider, LabelProvider labelProvider, LayoutFactory layoutFactory, List<String> voltageLevelIds, int depth) {
        draw(network, svgFile, svgParameters, layoutParameters, styleProvider, labelProvider, layoutFactory, VoltageLevelFilter.createVoltageLevelsDepthFilter(network, voltageLevelIds, depth));
    }

    public static void draw(Network network, Path svgFile, SvgParameters svgParameters, LayoutParameters layoutParameters, StyleProvider styleProvider, LabelProvider labelProvider, LayoutFactory layoutFactory, Predicate<VoltageLevel> voltageLevelFilter) {
        draw(network, svgFile, svgParameters, layoutParameters, styleProvider, labelProvider, layoutFactory, new IntIdProvider(), voltageLevelFilter);
    }

    /* ------------------------------------------------------------------------------------------------------------------------------ */
    // Network, Path, SvgParameters, LayoutParameters, StyleProvider, LabelProvider, LayoutFactory, IdProvider and different options of filtering
    /* ------------------------------------------------------------------------------------------------------------------------------ */

    public static void draw(Network network, Path svgFile, SvgParameters svgParameters, LayoutParameters layoutParameters, StyleProvider styleProvider, LabelProvider labelProvider, LayoutFactory layoutFactory, IdProvider idProvider) {
        draw(network, svgFile, svgParameters, layoutParameters, styleProvider, labelProvider, layoutFactory, idProvider, VoltageLevelFilter.NO_FILTER);
    }

    public static void draw(Network network, Path svgFile, SvgParameters svgParameters, LayoutParameters layoutParameters, StyleProvider styleProvider, LabelProvider labelProvider, LayoutFactory layoutFactory, IdProvider idProvider, String voltageLevelId, int depth) {
        draw(network, svgFile, svgParameters, layoutParameters, styleProvider, labelProvider, layoutFactory, idProvider, VoltageLevelFilter.createVoltageLevelDepthFilter(network, voltageLevelId, depth));
    }

    public static void draw(Network network, Path svgFile, SvgParameters svgParameters, LayoutParameters layoutParameters, StyleProvider styleProvider, LabelProvider labelProvider, LayoutFactory layoutFactory, IdProvider idProvider, List<String> voltageLevelIds, int depth) {
        draw(network, svgFile, svgParameters, layoutParameters, styleProvider, labelProvider, layoutFactory, idProvider, VoltageLevelFilter.createVoltageLevelsDepthFilter(network, voltageLevelIds, depth));
    }

    public static void draw(Network network, Path svgFile, SvgParameters svgParameters, LayoutParameters layoutParameters,
                            StyleProvider styleProvider, LabelProvider labelProvider, LayoutFactory layoutFactory, IdProvider idProvider, Predicate<VoltageLevel> voltageLevelFilter) {

        Objects.requireNonNull(network);
        Objects.requireNonNull(svgFile);
        Objects.requireNonNull(layoutParameters);
        Objects.requireNonNull(svgParameters);
        Objects.requireNonNull(styleProvider);
        Objects.requireNonNull(layoutFactory);
        Objects.requireNonNull(idProvider);
        Objects.requireNonNull(voltageLevelFilter);

        Graph graph = new NetworkGraphBuilder(network, voltageLevelFilter, idProvider).buildGraph();
        layoutFactory.create().run(graph, layoutParameters);
        new SvgWriter(svgParameters, styleProvider, labelProvider).writeSvg(graph, svgFile);
    }

    /* ------------------------------------------------------------------------------------------------ */
    // Network, Writer and different options of filtering
    /* ------------------------------------------------------------------------------------------------ */

    public static void draw(Network network, Writer writer) {
        draw(network, writer, new SvgParameters(), VoltageLevelFilter.NO_FILTER);
    }

    public static void draw(Network network, Writer writer, String voltageLevelId, int depth) {
        draw(network, writer, new SvgParameters(), VoltageLevelFilter.createVoltageLevelDepthFilter(network, voltageLevelId, depth));
    }

    public static void draw(Network network, Writer writer, List<String> voltageLevelIds) {
        draw(network, writer, new SvgParameters(), VoltageLevelFilter.createVoltageLevelsFilter(network, voltageLevelIds));
    }

    public void draw(Network network, Writer writer, Predicate<VoltageLevel> voltageLevelFilter) {
        draw(network, writer, new SvgParameters(), voltageLevelFilter);
    }

    /* ------------------------------------------------------------------------------------------------ */
    // Network, Writer, SvgParameters and different options of filtering
    /* ------------------------------------------------------------------------------------------------ */

    public static void draw(Network network, Writer writer, SvgParameters svgParameters) {
        draw(network, writer, svgParameters, VoltageLevelFilter.NO_FILTER);
    }

    public static void draw(Network network, Writer writer, SvgParameters svgParameters, String voltageLevelId, int depth) {
        draw(network, writer, svgParameters, VoltageLevelFilter.createVoltageLevelDepthFilter(network, voltageLevelId, depth));
    }

    public static void draw(Network network, Writer writer, SvgParameters svgParameters, List<String> voltageLevelIds, int depth) {
        draw(network, writer, svgParameters, VoltageLevelFilter.createVoltageLevelsDepthFilter(network, voltageLevelIds, depth));
    }

    public static void draw(Network network, Writer writer, SvgParameters svgParameters, Predicate<VoltageLevel> voltageLevelFilter) {
        draw(network, writer, svgParameters, new LayoutParameters(), voltageLevelFilter);
    }

    /* ------------------------------------------------------------------------------------------------ */
    // Network, Writer, SvgParameters, LayoutParameters and different options of filtering
    /* ------------------------------------------------------------------------------------------------ */

    public static void draw(Network network, Writer writer, SvgParameters svgParameters, LayoutParameters layoutParameters) {
        draw(network, writer, svgParameters, layoutParameters, VoltageLevelFilter.NO_FILTER);
    }

    public static void draw(Network network, Writer writer, SvgParameters svgParameters, LayoutParameters layoutParameters, String voltageLevelId, int depth) {
        draw(network, writer, svgParameters, layoutParameters, VoltageLevelFilter.createVoltageLevelDepthFilter(network, voltageLevelId, depth));
    }

    public static void draw(Network network, Writer writer, SvgParameters svgParameters, LayoutParameters layoutParameters, List<String> voltageLevelIds, int depth) {
        draw(network, writer, svgParameters, layoutParameters, VoltageLevelFilter.createVoltageLevelsDepthFilter(network, voltageLevelIds, depth));
    }

    public static void draw(Network network, Writer writer, SvgParameters svgParameters, LayoutParameters layoutParameters, Predicate<VoltageLevel> voltageLevelFilter) {
        draw(network, writer, svgParameters, layoutParameters, new TopologicalStyleProvider(network), voltageLevelFilter);
    }

    /* ------------------------------------------------------------------------------------------------ */
    // Network, Writer, SvgParameters, LayoutParameters, StyleProvider and different options of filtering
    /* ------------------------------------------------------------------------------------------------ */

    public static void draw(Network network, Writer writer, SvgParameters svgParameters, LayoutParameters layoutParameters, StyleProvider styleProvider) {
        draw(network, writer, svgParameters, layoutParameters, styleProvider, VoltageLevelFilter.NO_FILTER);
    }

    public static void draw(Network network, Writer writer, SvgParameters svgParameters, LayoutParameters layoutParameters, StyleProvider styleProvider, String voltageLevelId, int depth) {
        draw(network, writer, svgParameters, layoutParameters, styleProvider, VoltageLevelFilter.createVoltageLevelDepthFilter(network, voltageLevelId, depth));
    }

    public static void draw(Network network, Writer writer, SvgParameters svgParameters, LayoutParameters layoutParameters, StyleProvider styleProvider, List<String> voltageLevelIds, int depth) {
        draw(network, writer, svgParameters, layoutParameters, styleProvider, VoltageLevelFilter.createVoltageLevelsDepthFilter(network, voltageLevelIds, depth));
    }

    public static void draw(Network network, Writer writer, SvgParameters svgParameters, LayoutParameters layoutParameters,
                     StyleProvider styleProvider, Predicate<VoltageLevel> voltageLevelFilter) {
        draw(network, writer, svgParameters, layoutParameters, styleProvider, new DefaultLabelProvider(network, svgParameters), voltageLevelFilter);
    }

    /* --------------------------------------------------------------------------------------------------------------- */
    // Network, Writer, SvgParameters, LayoutParameters, StyleProvider, LabelProvider and different options of filtering
    /* --------------------------------------------------------------------------------------------------------------- */

    public static void draw(Network network, Writer writer, SvgParameters svgParameters, LayoutParameters layoutParameters, StyleProvider styleProvider, LabelProvider labelProvider) {
        draw(network, writer, svgParameters, layoutParameters, styleProvider, labelProvider, VoltageLevelFilter.NO_FILTER);
    }

    public static void draw(Network network, Writer writer, SvgParameters svgParameters, LayoutParameters layoutParameters, StyleProvider styleProvider, LabelProvider labelProvider, String voltageLevelId, int depth) {
        draw(network, writer, svgParameters, layoutParameters, styleProvider, labelProvider, VoltageLevelFilter.createVoltageLevelDepthFilter(network, voltageLevelId, depth));
    }

    public static void draw(Network network, Writer writer, SvgParameters svgParameters, LayoutParameters layoutParameters, StyleProvider styleProvider, LabelProvider labelProvider, List<String> voltageLevelIds, int depth) {
        draw(network, writer, svgParameters, layoutParameters, styleProvider, labelProvider, VoltageLevelFilter.createVoltageLevelsDepthFilter(network, voltageLevelIds, depth));
    }

    public static void draw(Network network, Writer writer, SvgParameters svgParameters, LayoutParameters layoutParameters,
                     StyleProvider styleProvider, LabelProvider labelProvider, Predicate<VoltageLevel> voltageLevelFilter) {
        draw(network, writer, svgParameters, layoutParameters, styleProvider, labelProvider, new BasicForceLayoutFactory(), voltageLevelFilter);
    }

    /* ------------------------------------------------------------------------------------------------------------------------------ */
    // Network, Writer, SvgParameters, LayoutParameters, StyleProvider, LabelProvider, LayoutFactory and different options of filtering
    /* ------------------------------------------------------------------------------------------------------------------------------ */

    public static void draw(Network network, Writer writer, SvgParameters svgParameters, LayoutParameters layoutParameters, StyleProvider styleProvider, LabelProvider labelProvider, LayoutFactory layoutFactory) {
        draw(network, writer, svgParameters, layoutParameters, styleProvider, labelProvider, layoutFactory, VoltageLevelFilter.NO_FILTER);
    }

    public static void draw(Network network, Writer writer, SvgParameters svgParameters, LayoutParameters layoutParameters, StyleProvider styleProvider, LabelProvider labelProvider, LayoutFactory layoutFactory, String voltageLevelId, int depth) {
        draw(network, writer, svgParameters, layoutParameters, styleProvider, labelProvider, layoutFactory, VoltageLevelFilter.createVoltageLevelDepthFilter(network, voltageLevelId, depth));
    }

    public static void draw(Network network, Writer writer, SvgParameters svgParameters, LayoutParameters layoutParameters, StyleProvider styleProvider, LabelProvider labelProvider, LayoutFactory layoutFactory, List<String> voltageLevelIds, int depth) {
        draw(network, writer, svgParameters, layoutParameters, styleProvider, labelProvider, layoutFactory, VoltageLevelFilter.createVoltageLevelsDepthFilter(network, voltageLevelIds, depth));
    }

    public static void draw(Network network, Writer writer, SvgParameters svgParameters, LayoutParameters layoutParameters,
                            StyleProvider styleProvider, LabelProvider labelProvider, LayoutFactory layoutFactory, Predicate<VoltageLevel> voltageLevelFilter) {
        draw(network, writer, svgParameters, layoutParameters, styleProvider, labelProvider, layoutFactory, new IntIdProvider(), voltageLevelFilter);
    }

    /* ------------------------------------------------------------------------------------------------------------------------------ */
    // Network, Writer, SvgParameters, LayoutParameters, StyleProvider, LabelProvider, LayoutFactory, IdProvider and different options of filtering
    /* ------------------------------------------------------------------------------------------------------------------------------ */

    public static void draw(Network network, Writer writer, SvgParameters svgParameters, LayoutParameters layoutParameters, StyleProvider styleProvider, LabelProvider labelProvider, LayoutFactory layoutFactory, IdProvider idProvider) {
        draw(network, writer, svgParameters, layoutParameters, styleProvider, labelProvider, layoutFactory, idProvider, VoltageLevelFilter.NO_FILTER);
    }

    public static void draw(Network network, Writer writer, SvgParameters svgParameters, LayoutParameters layoutParameters, StyleProvider styleProvider, LabelProvider labelProvider, LayoutFactory layoutFactory, IdProvider idProvider, String voltageLevelId, int depth) {
        draw(network, writer, svgParameters, layoutParameters, styleProvider, labelProvider, layoutFactory, idProvider, VoltageLevelFilter.createVoltageLevelDepthFilter(network, voltageLevelId, depth));
    }

    public static void draw(Network network, Writer writer, SvgParameters svgParameters, LayoutParameters layoutParameters, StyleProvider styleProvider, LabelProvider labelProvider, LayoutFactory layoutFactory, IdProvider idProvider, List<String> voltageLevelIds, int depth) {
        draw(network, writer, svgParameters, layoutParameters, styleProvider, labelProvider, layoutFactory, idProvider, VoltageLevelFilter.createVoltageLevelsDepthFilter(network, voltageLevelIds, depth));
    }

    public static void draw(Network network, Writer writer, SvgParameters svgParameters, LayoutParameters layoutParameters,
                     StyleProvider styleProvider, LabelProvider labelProvider, LayoutFactory layoutFactory, IdProvider idProvider, Predicate<VoltageLevel> voltageLevelFilter) {
        Graph graph = new NetworkGraphBuilder(network, voltageLevelFilter, idProvider).buildGraph();
        layoutFactory.create().run(graph, layoutParameters);
        new SvgWriter(svgParameters, styleProvider, labelProvider).writeSvg(graph, writer);
    }

    public String drawToString(Network network, SvgParameters svgParameters) {
        try (StringWriter writer = new StringWriter()) {
            draw(network, writer, svgParameters);
            return writer.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
