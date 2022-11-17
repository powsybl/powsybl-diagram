/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.layout;

import com.powsybl.nad.model.Edge;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.Node;
import com.powsybl.nad.model.Point;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class BasicFixedLayout extends AbstractLayout {

    @Override
    protected void nodesLayout(Graph graph, LayoutParameters layoutParameters) {
        org.jgrapht.Graph<Node, Edge> jgraphtGraph = graph.getJgraphtGraph(layoutParameters.isTextNodesForceLayout());

        jgraphtGraph.vertexSet().forEach(node -> {
            Point p = getInitialNodePositions().get(node.getEquipmentId());
            if (p != null) {
                node.setPosition(p.getX(), p.getY());
            }
        });

        if (!layoutParameters.isTextNodesForceLayout()) {
            graph.getTextEdgesMap().values().forEach(nodePair -> fixedTextNodeLayout(nodePair, layoutParameters));
        }
    }
}
