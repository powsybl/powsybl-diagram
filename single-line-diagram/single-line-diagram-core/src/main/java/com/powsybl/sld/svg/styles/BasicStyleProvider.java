/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg.styles;

import com.powsybl.sld.model.graphs.Graph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.Edge;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.svg.BusInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.powsybl.sld.svg.styles.StyleClassConstants.NODE_INFOS;
import static com.powsybl.sld.svg.styles.StyleClassConstants.WIRE_STYLE_CLASS;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class BasicStyleProvider extends AbstractStyleProvider {

    @Override
    public List<String> getEdgeStyles(Graph graph, Edge edge) {
        return List.of(WIRE_STYLE_CLASS);
    }

    @Override
    public List<String> getNodeSubcomponentStyles(Graph graph, Node node, String subComponentName) {
        return new ArrayList<>();
    }

    @Override
    public void reset() {
        // Nothing to reset for this implementation
    }

    @Override
    public List<String> getCssFilenames() {
        return List.of("tautologies.css");
    }

    @Override
    public List<String> getBusStyles(String busId, VoltageLevelGraph graph) {
        return Collections.singletonList(NODE_INFOS);
    }

    @Override
    public List<String> getBusInfoStyle(BusInfo info) {
        return Collections.emptyList();
    }
}
