package com.powsybl.sld.builders;

import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.graphs.NodeFactory;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.nodes.Feeder2WTLegNode;
import com.powsybl.sld.model.nodes.Feeder3WTLegNode;
import com.powsybl.sld.model.nodes.FeederLineNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.powsybl.sld.model.nodes.FeederWithSideNode.Side.*;

public class SubstationRawBuilder {

    SubstationGraph substationGraph;
    List<VoltageLevelRawBuilder> voltageLevelBuilders = new ArrayList<>();

    public SubstationRawBuilder(String id) {
        substationGraph = SubstationGraph.create(id);
    }

    public SubstationGraph getGraph() {
        return substationGraph;
    }

    public void addVlBuilder(VoltageLevelRawBuilder vlBuilder) {
        voltageLevelBuilders.add(vlBuilder);
        vlBuilder.setSubstationBuilder(this);
    }

    public Map<VoltageLevelRawBuilder, FeederLineNode> createLine(String id, VoltageLevelRawBuilder vl1, VoltageLevelRawBuilder vl2, int order1, int order2,
                                                               Direction direction1, Direction direction2) {
        Map<VoltageLevelRawBuilder, FeederLineNode> feederLineNodes = new HashMap<>();
        FeederLineNode feederLineNode1 = vl1.createFeederLineNode(id, vl2.getVoltageLevelInfos().getId(), ONE, order1, direction1);
        FeederLineNode feederLineNode2 = vl2.createFeederLineNode(id, vl1.getVoltageLevelInfos().getId(), TWO, order2, direction2);
        feederLineNodes.put(vl1, feederLineNode1);
        feederLineNodes.put(vl2, feederLineNode2);
        substationGraph.addLineEdge(id, feederLineNode1, feederLineNode2);
        return feederLineNodes;
    }

    public Map<VoltageLevelRawBuilder, FeederLineNode> createLine(String id, VoltageLevelRawBuilder vl1, VoltageLevelRawBuilder vl2) {
        return createLine(id, vl1, vl2, 0, 0, null, null);
    }

    public Map<VoltageLevelRawBuilder, Feeder2WTLegNode> createFeeder2WT(String id, VoltageLevelRawBuilder vl1, VoltageLevelRawBuilder vl2, int order1, int order2,
                                                                      Direction direction1, Direction direction2) {
        Map<VoltageLevelRawBuilder, Feeder2WTLegNode> f2WTNodes = new HashMap<>();
        Feeder2WTLegNode feeder2WtNode1 = vl1.createFeeder2wtLegNode(id, ONE, order1, direction1);
        Feeder2WTLegNode feeder2WTNode2 = vl2.createFeeder2wtLegNode(id, TWO, order2, direction2);
        f2WTNodes.put(vl1, feeder2WtNode1);
        f2WTNodes.put(vl2, feeder2WTNode2);
        NodeFactory.createMiddle2WTNode(substationGraph, id, id, feeder2WtNode1, feeder2WTNode2, vl1.getVoltageLevelInfos(), vl2.getVoltageLevelInfos(), false);
        return f2WTNodes;
    }

    public Map<VoltageLevelRawBuilder, Feeder2WTLegNode> createFeeder2WT(String id, VoltageLevelRawBuilder vl1, VoltageLevelRawBuilder vl2) {
        return createFeeder2WT(id, vl1, vl2, 0, 0, null, null);
    }

    public Map<VoltageLevelRawBuilder, Feeder3WTLegNode> createFeeder3WT(String id, VoltageLevelRawBuilder vl1, VoltageLevelRawBuilder vl2, VoltageLevelRawBuilder vl3,
                                                                      int order1, int order2, int order3,
                                                                      Direction direction1, Direction direction2, Direction direction3) {
        Map<VoltageLevelRawBuilder, Feeder3WTLegNode> f3WTNodes = new HashMap<>();
        Feeder3WTLegNode feeder3WTNode1 = vl1.createFeeder3wtLegNode(id, ONE, order1, direction1);
        Feeder3WTLegNode feeder3WTNode2 = vl2.createFeeder3wtLegNode(id, TWO, order2, direction2);
        Feeder3WTLegNode feeder3WTNode3 = vl3.createFeeder3wtLegNode(id, THREE, order3, direction3);
        f3WTNodes.put(vl1, feeder3WTNode1);
        f3WTNodes.put(vl2, feeder3WTNode2);
        f3WTNodes.put(vl3, feeder3WTNode3);

        // creation of the middle node and the edges linking the transformer leg nodes to this middle node
        NodeFactory.createMiddle3WTNode(substationGraph, id, id,  feeder3WTNode1, feeder3WTNode2, feeder3WTNode3,
                vl1.getVoltageLevelInfos(), vl2.getVoltageLevelInfos(), vl3.getVoltageLevelInfos(), false);

        return f3WTNodes;
    }

    public Map<VoltageLevelRawBuilder, Feeder3WTLegNode> createFeeder3WT(String id, VoltageLevelRawBuilder vl1, VoltageLevelRawBuilder vl2, VoltageLevelRawBuilder vl3) {
        return createFeeder3WT(id, vl1, vl2, vl3, 0, 0, 0, null, null, null);
    }
}
