package com.powsybl.sld.builders;

import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.graphs.NodeFactory;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.graphs.VoltageLevelInfos;
import com.powsybl.sld.model.nodes.*;

import java.util.function.Function;

public class VoltageLevelRawBuilder {

    private final VoltageLevelInfos voltageLevelInfos;
    private final VoltageLevelGraph voltageLevelGraph;
    private final Function<String, VoltageLevelInfos> getVoltageLevelInfosFromId;

    private SubstationRawBuilder substationBuilder;

    public VoltageLevelRawBuilder(VoltageLevelInfos voltageLevelInfos, SubstationRawBuilder parentBuilder, Function<String, VoltageLevelInfos> getVoltageLevelInfosFromId) {
        this.voltageLevelInfos = voltageLevelInfos;
        this.voltageLevelGraph = VoltageLevelGraph.create(voltageLevelInfos, parentBuilder == null ? null : parentBuilder.getGraph());
        this.getVoltageLevelInfosFromId = getVoltageLevelInfosFromId;
    }

    public VoltageLevelGraph getGraph() {
        return voltageLevelGraph;
    }

    public SubstationRawBuilder getSubstationBuilder() {
        return substationBuilder;
    }

    public void setSubstationBuilder(SubstationRawBuilder substationBuilder) {
        this.substationBuilder = substationBuilder;
    }

    public BusNode createBusBarSection(String id, int busbarIndex, int sectionIndex) {
        BusNode busNode = NodeFactory.createBusNode(voltageLevelGraph, id, id);
        busNode.setBusBarIndexSectionIndex(busbarIndex, sectionIndex);
        return busNode;
    }

    public BusNode createBusBarSection(String id) {
        return NodeFactory.createBusNode(voltageLevelGraph, id, id);
    }

    public SwitchNode createSwitchNode(SwitchNode.SwitchKind sk, String id, boolean fictitious, boolean open) {
        return NodeFactory.createSwitchNode(voltageLevelGraph, id, id, sk.name(), fictitious, sk, open);
    }

    public SwitchNode createSwitchNode(SwitchNode.SwitchKind sk, String id, boolean fictitious, boolean open, Integer order, Direction direction) {
        SwitchNode sw = NodeFactory.createSwitchNode(voltageLevelGraph, id, id, sk.name(), fictitious, sk, open);
        if (direction != null || order != null) {
            addExtension(sw, order, direction);
        }
        return sw;
    }

    public void connectNode(Node node1, Node node2) {
        voltageLevelGraph.addEdge(node1, node2);
    }

    public FictitiousNode createFictitiousNode(int id) {
        return NodeFactory.createInternalNode(voltageLevelGraph, id);
    }

    public FictitiousNode createFictitiousNode(String id) {
        return NodeFactory.createInternalNode(voltageLevelGraph, id);
    }

    public void addExtension(Node fn, Integer order, Direction direction) {
        if (order != null) {
            fn.setOrder(order);
        }
        fn.setDirection(direction == null ? Direction.UNDEFINED : direction);
    }

    private void commonFeederSetting(FeederNode node, String id, int order, Direction direction) {
        node.setLabel(id);
        if (direction != null) {
            addExtension(node, order, direction);
        }
    }

    public FeederNode createLoad(String id) {
        return createLoad(id, 0, null);
    }

    public FeederNode createLoad(String id, int order, Direction direction) {
        FeederNode fn = NodeFactory.createLoad(voltageLevelGraph, id, id);
        commonFeederSetting(fn, id, order, direction);
        return fn;
    }

    public FeederNode createGenerator(String id) {
        return createGenerator(id, 0, null);
    }

    public FeederNode createGenerator(String id, int order, Direction direction) {
        FeederNode fn = NodeFactory.createGenerator(voltageLevelGraph, id, id);
        commonFeederSetting(fn, id, order, direction);
        return fn;
    }

    public FeederLineNode createFeederLineNode(String id, String otherVlId, FeederWithSideNode.Side side, int order, Direction direction) {
        FeederLineNode fln = NodeFactory.createFeederLineNode(voltageLevelGraph, id + "_" + side, id, id, side, getVoltageLevelInfosFromId.apply(otherVlId));
        commonFeederSetting(fln, id, order, direction);
        return fln;
    }

    public Feeder2WTNode createFeeder2WTNode(String id, String otherVlId, FeederWithSideNode.Side side,
                                             int order, Direction direction) {
        Feeder2WTNode f2WTe = NodeFactory.createFeeder2WTNode(voltageLevelGraph, id + "_" + side, id, id, side, getVoltageLevelInfosFromId.apply(otherVlId));
        commonFeederSetting(f2WTe, id, order, direction);
        return f2WTe;
    }

    public Feeder2WTLegNode createFeeder2wtLegNode(String id, FeederWithSideNode.Side side,
                                                   int order, Direction direction) {
        Feeder2WTLegNode f2WTe = NodeFactory.createFeeder2WTLegNode(voltageLevelGraph, id + "_" + side, id, id, side);
        commonFeederSetting(f2WTe, id, order, direction);
        return f2WTe;
    }

    public Feeder3WTLegNode createFeeder3wtLegNode(String id, FeederWithSideNode.Side side, int order, Direction direction) {
        Feeder3WTLegNode f3WTe = NodeFactory.createFeeder3WTLegNodeForSubstationDiagram(voltageLevelGraph, id + "_" + side, id, id, side);
        commonFeederSetting(f3WTe, id + side.getIntValue(), order, direction);
        return f3WTe;
    }

    public VoltageLevelInfos getVoltageLevelInfos() {
        return voltageLevelInfos;
    }
}
