/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg.styles;

import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.model.cells.Cell;
import com.powsybl.sld.model.graphs.Graph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.BranchEdge;
import com.powsybl.sld.model.nodes.Edge;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.svg.BusInfo;
import com.powsybl.sld.svg.DiagramLabelProvider;
import com.powsybl.sld.svg.FeederInfo;

import java.net.URL;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class StyleProvidersList implements StyleProvider {

    private final List<StyleProvider> styleProviders;

    public StyleProvidersList(StyleProvider... styleProviders) {
        this(List.of(styleProviders));
    }

    public <E> StyleProvidersList(List<StyleProvider> styleProviders) {
        this.styleProviders = styleProviders;
    }

    @Override
    public List<String> getEdgeStyles(Graph graph, Edge edge) {
        return concatenateLists(sp -> sp.getEdgeStyles(graph, edge));
    }

    @Override
    public List<String> getNodeStyles(VoltageLevelGraph graph, Node node, ComponentLibrary componentLibrary, boolean showInternalNodes) {
        return concatenateLists(sp -> sp.getNodeStyles(graph, node, componentLibrary, showInternalNodes));
    }

    @Override
    public List<String> getNodeDecoratorStyles(DiagramLabelProvider.NodeDecorator nodeDecorator, Node node, ComponentLibrary componentLibrary) {
        return concatenateLists(sp -> sp.getNodeDecoratorStyles(nodeDecorator, node, componentLibrary));
    }

    @Override
    public List<String> getBranchEdgeStyles(BranchEdge edge, ComponentLibrary componentLibrary) {
        return concatenateLists(sp -> sp.getBranchEdgeStyles(edge, componentLibrary));
    }

    @Override
    public List<String> getNodeSubcomponentStyles(Graph graph, Node node, String subComponentName) {
        return concatenateLists(sp -> sp.getNodeSubcomponentStyles(graph, node, subComponentName));
    }

    @Override
    public void reset() {
        styleProviders.forEach(StyleProvider::reset);
    }

    @Override
    public List<String> getCssFilenames() {
        return concatenateLists(StyleProvider::getCssFilenames);
    }

    @Override
    public List<URL> getCssUrls() {
        return concatenateLists(StyleProvider::getCssUrls);
    }

    @Override
    public List<String> getCellStyles(Cell cell) {
        return concatenateLists(sp -> sp.getCellStyles(cell));
    }

    @Override
    public List<String> getBusStyles(String busId, VoltageLevelGraph graph) {
        return concatenateLists(sp -> sp.getBusStyles(busId, graph));
    }

    @Override
    public List<String> getBusInfoStyle(BusInfo info) {
        return concatenateLists(sp -> sp.getBusInfoStyle(info));
    }

    @Override
    public List<String> getFeederInfoStyles(FeederInfo info) {
        return concatenateLists(sp -> sp.getFeederInfoStyles(info));
    }

    private <T> List<T> concatenateLists(Function<StyleProvider, List<T>> stylesGetter) {
        return styleProviders.stream()
                .map(stylesGetter)
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());
    }
}
