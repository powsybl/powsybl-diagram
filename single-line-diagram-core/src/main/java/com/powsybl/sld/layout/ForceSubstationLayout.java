/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.model.TwtEdge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.graph.impl.EdgeImpl;
import org.gephi.graph.impl.GraphModelImpl;
import org.gephi.graph.impl.NodeImpl;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2Builder;

import java.util.Objects;
import java.util.Random;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ForceSubstationLayout implements SubstationLayout {

    private final SubstationGraph substationGraph;

    private final VoltageLevelLayoutFactory voltageLevelLayoutFactory;

    private final Random random = new Random();

    public ForceSubstationLayout(SubstationGraph substationGraph, VoltageLevelLayoutFactory voltageLevelLayoutFactory) {
        this.substationGraph = Objects.requireNonNull(substationGraph);
        this.voltageLevelLayoutFactory = Objects.requireNonNull(voltageLevelLayoutFactory);
    }

    @Override
    public void run(LayoutParameters layoutParameters) {
        ForceAtlas2 forceAtlas2 = new ForceAtlas2Builder()
                .buildLayout();
        GraphModel graphModel = new GraphModelImpl();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        for (Graph voltageLevelGraph : substationGraph.getNodes()) {
            NodeImpl n = new NodeImpl(voltageLevelGraph.getVoltageLevelId());
            n.setPosition(random.nextFloat() * 1000, random.nextFloat() * 1000);
            undirectedGraph.addNode(n);
        }
        for (TwtEdge edge : substationGraph.getEdges()) {
            NodeImpl node1 = (NodeImpl) undirectedGraph.getNode(edge.getNode1().getGraph().getVoltageLevelId());
            NodeImpl node2 = (NodeImpl) undirectedGraph.getNode(edge.getNode2().getGraph().getVoltageLevelId());
            undirectedGraph.addEdge(new EdgeImpl(edge.toString(), node1, node2, 0, 1, false));
        }
        forceAtlas2.setGraphModel(graphModel);
        forceAtlas2.resetPropertiesValues();
        forceAtlas2.setAdjustSizes(true);
        forceAtlas2.setOutboundAttractionDistribution(false);
        forceAtlas2.setEdgeWeightInfluence(1.5d);
        forceAtlas2.setGravity(10d);
        forceAtlas2.setJitterTolerance(.02);
        forceAtlas2.setScalingRatio(15.0);
        forceAtlas2.initAlgo();
        int maxSteps = 1000;

        for (int i = 0; i < maxSteps && forceAtlas2.canAlgo(); i++) {
            forceAtlas2.goAlgo();
        }
        forceAtlas2.endAlgo();

        for (Graph voltageLevelGraph : substationGraph.getNodes()) {
            org.gephi.graph.api.Node n = undirectedGraph.getNode(voltageLevelGraph.getVoltageLevelId());
            System.out.println(n.x() + " " + n.y());
            voltageLevelGraph.setX(n.x()* 50);
            voltageLevelGraph.setY(n.y() * 50);

            voltageLevelLayoutFactory.create(voltageLevelGraph)
                .run(layoutParameters);
        }
    }
}
