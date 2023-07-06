package com.powsybl.sld.builders;

import com.powsybl.commons.*;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.graphs.NodeFactory;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.nodes.FeederNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.powsybl.sld.model.nodes.NodeSide.*;

public class SubstationRawBuilder {

    protected static final Logger LOGGER = LoggerFactory.getLogger(SubstationRawBuilder.class);

    private static final String VL_NOT_PRESENT = "VoltageLevel(s) '%s' not found in Substation '%s'";

    SubstationGraph substationGraph;
    List<VoltageLevelRawBuilder> voltageLevelBuilders = new ArrayList<>();

    ZoneRawBuilder zBuilder;

    public SubstationRawBuilder(String id, ZoneRawBuilder parentBuilder) {
        substationGraph = SubstationGraph.create(id, parentBuilder == null ? null : parentBuilder.getGraph());
    }

    public SubstationGraph getGraph() {
        return substationGraph;
    }

    public ZoneRawBuilder getSubstationBuilder() {
        return zBuilder;
    }

    public void setZoneRawBuilder(ZoneRawBuilder zoneBuilder) {
        this.zBuilder = zoneBuilder;
    }

    public void addVlBuilder(VoltageLevelRawBuilder vlBuilder) {
        voltageLevelBuilders.add(vlBuilder);
        vlBuilder.setSubstationBuilder(this);
    }

    private boolean containsVoltageLevelRawBuilder(VoltageLevelRawBuilder vl) {
        return vl.getSubstationBuilder() == this;
    }

    public Map<VoltageLevelRawBuilder, FeederNode> createLine(String id, VoltageLevelRawBuilder vl1, VoltageLevelRawBuilder vl2, int order1, int order2,
                                                               Direction direction1, Direction direction2) {
        Map<VoltageLevelRawBuilder, FeederNode> feederLineNodes = new HashMap<>();
        FeederNode feederLineNode1 = vl1.createFeederLineNode(id, vl2.getVoltageLevelInfos().getId(), ONE, order1, direction1);
        FeederNode feederLineNode2 = vl2.createFeederLineNode(id, vl1.getVoltageLevelInfos().getId(), TWO, order2, direction2);
        feederLineNodes.put(vl1, feederLineNode1);
        feederLineNodes.put(vl2, feederLineNode2);

        if (containsVoltageLevelRawBuilder(vl1) && containsVoltageLevelRawBuilder(vl2)) {
            // All VoltageLevel must be in the same Substation
            substationGraph.addLineEdge(id, feederLineNode1, feederLineNode2);
        }
        return feederLineNodes;
    }

    public Map<VoltageLevelRawBuilder, FeederNode> createLine(String id, VoltageLevelRawBuilder vl1, VoltageLevelRawBuilder vl2) {
        return createLine(id, vl1, vl2, 0, 0, null, null);
    }

    public Map<VoltageLevelRawBuilder, FeederNode> createFeeder2WT(String id, VoltageLevelRawBuilder vl1, VoltageLevelRawBuilder vl2, int order1, int order2,
                                                                      Direction direction1, Direction direction2) {
        Map<VoltageLevelRawBuilder, FeederNode> f2WTNodes = new HashMap<>();
        FeederNode feeder2WtNode1 = vl1.createFeeder2wtLegNode(id, ONE, order1, direction1);
        FeederNode feeder2WTNode2 = vl2.createFeeder2wtLegNode(id, TWO, order2, direction2);
        f2WTNodes.put(vl1, feeder2WtNode1);
        f2WTNodes.put(vl2, feeder2WTNode2);
        if (containsVoltageLevelRawBuilder(vl1) && containsVoltageLevelRawBuilder(vl2)) {
            // All VoltageLevel must be in the same Substation
            NodeFactory.createMiddle2WTNode(substationGraph, id, id, feeder2WtNode1, feeder2WTNode2, vl1.getVoltageLevelInfos(), vl2.getVoltageLevelInfos(), false);
        } else {
            // At least one VoltageLevel is not in the same Substation (Zone graph case)
            String vls = String.join(", ", Stream.of(vl1, vl2).filter(vl -> !containsVoltageLevelRawBuilder(vl)).map(vl -> vl.getGraph().getId()).toList());
            throw new PowsyblException(String.format(VL_NOT_PRESENT, vls, substationGraph.getId()));
        }
        return f2WTNodes;
    }

    public Map<VoltageLevelRawBuilder, FeederNode> createFeeder2WT(String id, VoltageLevelRawBuilder vl1, VoltageLevelRawBuilder vl2) {
        return createFeeder2WT(id, vl1, vl2, 0, 0, null, null);
    }

    public Map<VoltageLevelRawBuilder, FeederNode> createFeeder3WT(String id, VoltageLevelRawBuilder vl1, VoltageLevelRawBuilder vl2, VoltageLevelRawBuilder vl3,
                                                                      int order1, int order2, int order3,
                                                                      Direction direction1, Direction direction2, Direction direction3) {
        Map<VoltageLevelRawBuilder, FeederNode> f3WTNodes = new HashMap<>();
        FeederNode feeder3WTNode1 = vl1.createFeeder3wtLegNode(id, ONE, order1, direction1);
        FeederNode feeder3WTNode2 = vl2.createFeeder3wtLegNode(id, TWO, order2, direction2);
        FeederNode feeder3WTNode3 = vl3.createFeeder3wtLegNode(id, THREE, order3, direction3);
        f3WTNodes.put(vl1, feeder3WTNode1);
        f3WTNodes.put(vl2, feeder3WTNode2);
        f3WTNodes.put(vl3, feeder3WTNode3);

        if (containsVoltageLevelRawBuilder(vl1) && containsVoltageLevelRawBuilder(vl2) && containsVoltageLevelRawBuilder(vl3)) {
            // All VoltageLevel must be in the same Substation
            // creation of the middle node and the edges linking the transformer leg nodes to this middle node
            NodeFactory.createMiddle3WTNode(substationGraph, id, id, feeder3WTNode1, feeder3WTNode2, feeder3WTNode3);
        } else {
            // At least one VoltageLevel is not in the same Substation (Zone graph case)
            String vls = String.join(", ", Stream.of(vl1, vl2, vl3).filter(vl -> !containsVoltageLevelRawBuilder(vl)).map(vl -> vl.getGraph().getId()).toList());
            throw new PowsyblException(String.format(VL_NOT_PRESENT, vls, substationGraph.getId()));
        }

        return f3WTNodes;
    }

    public Map<VoltageLevelRawBuilder, FeederNode> createFeeder3WT(String id, VoltageLevelRawBuilder vl1, VoltageLevelRawBuilder vl2, VoltageLevelRawBuilder vl3) {
        return createFeeder3WT(id, vl1, vl2, vl3, 0, 0, 0, null, null, null);
    }
}
