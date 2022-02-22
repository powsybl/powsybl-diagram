/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.BranchEdge;
import com.powsybl.sld.model.nodes.Edge;
import com.powsybl.sld.model.nodes.Node;

import java.net.URL;
import java.util.List;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public interface DiagramStyleProvider {

    List<String> getSvgWireStyles(Graph graph, Edge edge, boolean highlightLineState);

    List<String> getSvgNodeStyles(VoltageLevelGraph graph, Node node, ComponentLibrary componentLibrary, boolean showInternalNodes);

    List<String> getSvgNodeDecoratorStyles(DiagramLabelProvider.NodeDecorator nodeDecorator, Node node, ComponentLibrary componentLibrary);

    List<String> getZoneLineStyles(BranchEdge edge, ComponentLibrary componentLibrary);

    List<String> getSvgNodeSubcomponentStyles(Graph graph, Node node, String subComponentName);

    void reset();

    List<String> getCssFilenames();

    List<URL> getCssUrls();

    List<String> getBusStyles(String busId, VoltageLevelGraph graph);
}
