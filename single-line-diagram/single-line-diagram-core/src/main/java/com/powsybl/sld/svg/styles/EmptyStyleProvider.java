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

import java.util.Collections;
import java.util.List;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class EmptyStyleProvider implements StyleProvider {

    @Override
    public List<String> getEdgeStyles(Graph graph, Edge edge) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getNodeStyles(VoltageLevelGraph graph, Node node, ComponentLibrary componentLibrary, boolean showInternalNodes) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getNodeDecoratorStyles(DiagramLabelProvider.NodeDecorator nodeDecorator, Node node, ComponentLibrary componentLibrary) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getBranchEdgeStyles(BranchEdge edge, ComponentLibrary componentLibrary) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getNodeSubcomponentStyles(Graph graph, Node node, String subComponentName) {
        return Collections.emptyList();
    }

    @Override
    public void reset() {
        // Nothing to do
    }

    @Override
    public List<String> getCssFilenames() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getBusStyles(String busId, VoltageLevelGraph graph) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getBusInfoStyle(BusInfo info) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getFeederInfoStyles(FeederInfo info) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getCellStyles(Cell cell) {
        return Collections.emptyList();
    }
}
