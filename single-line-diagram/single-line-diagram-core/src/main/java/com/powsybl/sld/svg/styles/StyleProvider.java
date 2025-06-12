/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg.styles;

import com.powsybl.sld.library.SldComponentLibrary;
import com.powsybl.sld.model.cells.Cell;
import com.powsybl.sld.model.graphs.Graph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.BranchEdge;
import com.powsybl.sld.model.nodes.Edge;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.svg.BusInfo;
import com.powsybl.sld.svg.FeederInfo;
import com.powsybl.sld.svg.LabelProvider;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Giovanni Ferrari {@literal <giovanni.ferrari at techrain.eu>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
public interface StyleProvider {

    List<String> getEdgeStyles(Graph graph, Edge edge);

    List<String> getNodeStyles(VoltageLevelGraph graph, Node node, SldComponentLibrary componentLibrary, boolean showInternalNodes);

    List<String> getNodeDecoratorStyles(LabelProvider.NodeDecorator nodeDecorator, Node node, SldComponentLibrary componentLibrary);

    List<String> getBranchEdgeStyles(BranchEdge edge, SldComponentLibrary componentLibrary);

    List<String> getNodeSubcomponentStyles(Graph graph, Node node, String subComponentName);

    void reset();

    List<String> getCssFilenames();

    default List<URL> getCssUrls() {
        return getCssFilenames().stream()
                .map(n -> getClass().getResource("/" + n))
                .collect(Collectors.toList());
    }

    List<String> getBusStyles(String busId, VoltageLevelGraph graph);

    List<String> getBusInfoStyle(BusInfo info);

    List<String> getFeederInfoStyles(FeederInfo info);

    List<String> getCellStyles(Cell cell);
}
