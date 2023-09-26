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
public class NetworkAreaDiagram {

    private final Network network;
    private final Predicate<VoltageLevel> voltageLevelFilter;

    public NetworkAreaDiagram(Network network) {
        this(network, VoltageLevelFilter.NO_FILTER);
    }

    public NetworkAreaDiagram(Network network, String voltageLevelId, int depth) {
        this(network, VoltageLevelFilter.createVoltageLevelDepthFilter(network, voltageLevelId, depth));
    }

    public NetworkAreaDiagram(Network network, List<String> voltageLevelIds) {
        this(network, VoltageLevelFilter.createVoltageLevelsFilter(network, voltageLevelIds));
    }

    public NetworkAreaDiagram(Network network, List<String> voltageLevelIds, int depth) {
        this(network, VoltageLevelFilter.createVoltageLevelsDepthFilter(network, voltageLevelIds, depth));
    }

    public NetworkAreaDiagram(Network network, Predicate<VoltageLevel> voltageLevelFilter) {
        this.network = Objects.requireNonNull(network);
        this.voltageLevelFilter = Objects.requireNonNull(voltageLevelFilter);
    }

    public Network getNetwork() {
        return network;
    }

    public void draw(Path svgFile) {
        draw(svgFile, new SvgParameters());
    }

    public void draw(Path svgFile, SvgParameters svgParameters) {
        draw(svgFile, svgParameters, new LayoutParameters());
    }

    public void draw(Path svgFile, SvgParameters svgParameters, LayoutParameters layoutParameters) {
        draw(svgFile, svgParameters, layoutParameters, new TopologicalStyleProvider(network));
    }

    public void draw(Path svgFile, SvgParameters svgParameters, LayoutParameters layoutParameters,
                                   StyleProvider styleProvider) {
        draw(svgFile, svgParameters, layoutParameters, styleProvider, new DefaultLabelProvider(network, svgParameters));
    }

    public void draw(Path svgFile, SvgParameters svgParameters, LayoutParameters layoutParameters,
                                   StyleProvider styleProvider, LabelProvider labelProvider) {
        draw(svgFile, svgParameters, layoutParameters, styleProvider, labelProvider, new BasicForceLayoutFactory());
    }

    public void draw(Path svgFile, SvgParameters svgParameters, LayoutParameters layoutParameters,
                                   StyleProvider styleProvider, LabelProvider labelProvider, LayoutFactory layoutFactory) {
        draw(svgFile, svgParameters, layoutParameters, styleProvider, labelProvider, layoutFactory, new IntIdProvider());
    }

    public void draw(Path svgFile, SvgParameters svgParameters, LayoutParameters layoutParameters,
                                   StyleProvider styleProvider, LabelProvider labelProvider, LayoutFactory layoutFactory,
                                   IdProvider idProvider) {
        Objects.requireNonNull(svgFile);
        Objects.requireNonNull(layoutParameters);
        Objects.requireNonNull(svgParameters);
        Objects.requireNonNull(styleProvider);
        Objects.requireNonNull(layoutFactory);
        Objects.requireNonNull(idProvider);

        Graph graph = new NetworkGraphBuilder(network, voltageLevelFilter, idProvider).buildGraph();
        layoutFactory.create().run(graph, layoutParameters);
        new SvgWriter(svgParameters, styleProvider, labelProvider).writeSvg(graph, svgFile);
    }

    public void draw(Writer writer) {
        draw(writer, new SvgParameters());
    }

    public void draw(Writer writer, SvgParameters svgParameters) {
        draw(writer, svgParameters, new LayoutParameters());
    }

    public void draw(Writer writer, SvgParameters svgParameters, LayoutParameters layoutParameters) {
        draw(writer, svgParameters, layoutParameters, new TopologicalStyleProvider(network));
    }

    public void draw(Writer writer, SvgParameters svgParameters, LayoutParameters layoutParameters,
                     StyleProvider styleProvider) {
        draw(writer, svgParameters, layoutParameters, styleProvider, new DefaultLabelProvider(network, svgParameters));
    }

    public void draw(Writer writer, SvgParameters svgParameters, LayoutParameters layoutParameters,
                     StyleProvider styleProvider, LabelProvider labelProvider) {
        draw(writer, svgParameters, layoutParameters, styleProvider, labelProvider, new BasicForceLayoutFactory());
    }

    public void draw(Writer writer, SvgParameters svgParameters, LayoutParameters layoutParameters,
                     StyleProvider styleProvider, LabelProvider labelProvider, LayoutFactory layoutFactory) {
        draw(writer, svgParameters, layoutParameters, styleProvider, labelProvider, layoutFactory, new IntIdProvider());
    }

    public void draw(Writer writer, SvgParameters svgParameters, LayoutParameters layoutParameters,
                     StyleProvider styleProvider, LabelProvider labelProvider, LayoutFactory layoutFactory,
                     IdProvider idProvider) {
        Graph graph = new NetworkGraphBuilder(network, voltageLevelFilter, idProvider).buildGraph();
        layoutFactory.create().run(graph, layoutParameters);
        new SvgWriter(svgParameters, styleProvider, labelProvider).writeSvg(graph, writer);
    }

    public String drawToString(SvgParameters svgParameters) {
        try (StringWriter writer = new StringWriter()) {
            draw(writer, svgParameters);
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
