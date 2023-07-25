package com.powsybl.sld.builders;

import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.graphs.NodeFactory;
import com.powsybl.sld.model.graphs.ZoneGraph;
import com.powsybl.sld.model.nodes.FeederNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.powsybl.sld.model.nodes.NodeSide.*;

public class ZoneRawBuilder extends AbstractRawBuilder {

    ZoneGraph zoneGraph;
    List<SubstationRawBuilder> substationRawBuilders = new ArrayList<>();

    public ZoneRawBuilder(List<String> ids) {
        zoneGraph = ZoneGraph.create(ids);
    }

    @Override
    public ZoneGraph getGraph() {
        return zoneGraph;
    }

    public void addSubstationBuilder(SubstationRawBuilder ssBuilder) {
        substationRawBuilders.add(ssBuilder);
        ssBuilder.setZoneRawBuilder(this);
    }

    @Override
    public Map<VoltageLevelRawBuilder, FeederNode> createLine(String id, VoltageLevelRawBuilder vl1, VoltageLevelRawBuilder vl2, int order1, int order2, Direction direction1, Direction direction2) {
        Map<VoltageLevelRawBuilder, FeederNode> feederLineNodes = new HashMap<>();
        FeederNode feederLineNode1 = vl1.createFeederLineNode(id, vl2.getVoltageLevelInfos().getId(), ONE, order1, direction1);
        FeederNode feederLineNode2 = vl2.createFeederLineNode(id, vl1.getVoltageLevelInfos().getId(), TWO, order2, direction2);
        feederLineNodes.put(vl1, feederLineNode1);
        feederLineNodes.put(vl2, feederLineNode2);

        if (vl1.getSubstationBuilder() != vl2.getSubstationBuilder()) {
            // At least one VoltageLevel is not in the same Substation (Zone graph case)
            zoneGraph.addLineEdge(id, feederLineNode1, feederLineNode2);
        }
        return feederLineNodes;
    }

    @Override
    public Map<VoltageLevelRawBuilder, FeederNode> createFeeder2WT(String id, VoltageLevelRawBuilder vl1, VoltageLevelRawBuilder vl2, int order1, int order2, Direction direction1, Direction direction2) {
        Map<VoltageLevelRawBuilder, FeederNode> f2WTNodes = new HashMap<>();
        FeederNode feeder2WtNode1 = vl1.createFeeder2wtLegNode(id, ONE, order1, direction1);
        FeederNode feeder2WTNode2 = vl2.createFeeder2wtLegNode(id, TWO, order2, direction2);
        f2WTNodes.put(vl1, feeder2WtNode1);
        f2WTNodes.put(vl2, feeder2WTNode2);
        if (vl1.getSubstationBuilder() != vl2.getSubstationBuilder()) {
            // At least one VoltageLevel is not in the same Substation (Zone graph case)
            NodeFactory.createMiddle2WTNode(zoneGraph, id, id, feeder2WtNode1, feeder2WTNode2, vl1.getVoltageLevelInfos(), vl2.getVoltageLevelInfos(), false);
        }
        return f2WTNodes;
    }

    @Override
    public Map<VoltageLevelRawBuilder, FeederNode> createFeeder3WT(String id, VoltageLevelRawBuilder vl1, VoltageLevelRawBuilder vl2, VoltageLevelRawBuilder vl3, int order1, int order2, int order3, Direction direction1, Direction direction2, Direction direction3) {
        Map<VoltageLevelRawBuilder, FeederNode> f3WTNodes = new HashMap<>();
        FeederNode feeder3WTNode1 = vl1.createFeeder3wtLegNode(id, ONE, order1, direction1);
        FeederNode feeder3WTNode2 = vl2.createFeeder3wtLegNode(id, TWO, order2, direction2);
        FeederNode feeder3WTNode3 = vl3.createFeeder3wtLegNode(id, THREE, order3, direction3);
        f3WTNodes.put(vl1, feeder3WTNode1);
        f3WTNodes.put(vl2, feeder3WTNode2);
        f3WTNodes.put(vl3, feeder3WTNode3);

        if (vl1.getSubstationBuilder() != vl2.getSubstationBuilder() ||
            vl1.getSubstationBuilder() != vl3.getSubstationBuilder() ||
            vl2.getSubstationBuilder() != vl3.getSubstationBuilder()) {
            // At least one VoltageLevel is not in the same Substation (Zone graph case)
            NodeFactory.createMiddle3WTNode(zoneGraph, id, id, feeder3WTNode1, feeder3WTNode2, feeder3WTNode3);
        }
        return f3WTNodes;
    }
}
