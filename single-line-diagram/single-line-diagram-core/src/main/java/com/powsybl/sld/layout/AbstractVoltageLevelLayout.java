/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.nodes.Node;
import org.jgrapht.alg.util.Pair;

import java.util.List;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public abstract class AbstractVoltageLevelLayout extends AbstractLayout {

    private final VoltageLevelGraph graph;
    protected final InfosNbSnakeLinesHorizontal infosNbSnakeLines;

    protected AbstractVoltageLevelLayout(VoltageLevelGraph graph) {
        this.graph = graph;
        this.infosNbSnakeLines = InfosNbSnakeLinesHorizontal.create(graph);
    }

    public VoltageLevelGraph getGraph() {
        return graph;
    }

    @Override
    protected List<Point> calculatePolylineSnakeLine(LayoutParameters layoutParam, Pair<Node, Node> nodes, boolean increment) {
        double yMin = getGraph().getY();
        double yMax = getGraph().getY() + getGraph().getInnerHeight(layoutParam.getVerticalSpaceBus());
        return calculatePolylineSnakeLineForHorizontalLayout(getGraph(), layoutParam, nodes, increment, infosNbSnakeLines, yMin, yMax);
    }

    @Override
    public void manageSnakeLines(LayoutParameters layoutParameters) {
        if (getGraph().isForVoltageLevelDiagram()) {
            manageSnakeLines(getGraph(), layoutParameters);
        }
    }
}
