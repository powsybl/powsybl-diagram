package com.powsybl.sld.builders;

import com.powsybl.sld.model.*;

import java.util.function.Function;

public class VoltageLevelRawBuilder {

    private final VoltageLevelInfos voltageLevelInfos;
    private final VoltageLevelGraph voltageLevelGraph;
    private final Function<String, VoltageLevelInfos> getVoltageLevelInfosFromId;

    private SubstationRawBuilder substationBuilder;

    public VoltageLevelRawBuilder(VoltageLevelInfos voltageLevelInfos, boolean forVoltageLevelDiagram, Function<String, VoltageLevelInfos> getVoltageLevelInfosFromId) {
        this.voltageLevelInfos = voltageLevelInfos;
        this.voltageLevelGraph = VoltageLevelGraph.create(voltageLevelInfos, forVoltageLevelDiagram);
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
        BusNode busNode = BusNode.create(voltageLevelGraph, id, id);
        voltageLevelGraph.addNode(busNode);
        busNode.setBusBarIndexSectionIndex(busbarIndex, sectionIndex);
        return busNode;
    }

    public BusNode createBusBarSection(String id) {
        BusNode busNode = BusNode.create(voltageLevelGraph, id, id);
        voltageLevelGraph.addNode(busNode);
        return busNode;
    }

    public SwitchNode createSwitchNode(SwitchNode.SwitchKind sk, String id, boolean fictitious, boolean open) {
        SwitchNode sw = new SwitchNode(id, id, sk.name(), fictitious, voltageLevelGraph, sk, open);
        voltageLevelGraph.addNode(sw);
        return sw;
    }

    public SwitchNode createSwitchNode(SwitchNode.SwitchKind sk, String id, boolean fictitious, boolean open, Integer order, BusCell.Direction direction) {
        SwitchNode sw = new SwitchNode(id, id, sk.name(), fictitious, voltageLevelGraph, sk, open);
        voltageLevelGraph.addNode(sw);
        if (direction != null || order != null) {
            addExtension(sw, order, direction);
        }
        return sw;
    }

    public void connectNode(Node node1, Node node2) {
        voltageLevelGraph.addEdge(node1, node2);
    }

    public FictitiousNode createFictitiousNode(int id) {
        InternalNode fictitiousNode = new InternalNode(id, voltageLevelGraph);
        voltageLevelGraph.addNode(fictitiousNode);
        return fictitiousNode;
    }

    public FictitiousNode createFictitiousNode(String id) {
        InternalNode fictitiousNode = new InternalNode(id, voltageLevelGraph);
        voltageLevelGraph.addNode(fictitiousNode);
        return fictitiousNode;
    }

    public void addExtension(Node fn, Integer order, BusCell.Direction direction) {
        if (order != null) {
            fn.setOrder(order);
        }
        fn.setDirection(direction == null ? BusCell.Direction.UNDEFINED : direction);
    }

    private void commonFeederSetting(FeederNode node, String id, int order, BusCell.Direction direction) {
        node.setLabel(id);
        voltageLevelGraph.addNode(node);
        if (direction != null) {
            addExtension(node, order, direction);
        }
    }

    public FeederNode createLoad(String id) {
        return createLoad(id, 0, null);
    }

    public FeederNode createLoad(String id, int order, BusCell.Direction direction) {
        FeederInjectionNode fn = FeederInjectionNode.createLoad(voltageLevelGraph, id, id);
        commonFeederSetting(fn, id, order, direction);
        return fn;
    }

    public FeederNode createGenerator(String id) {
        return createGenerator(id, 0, null);
    }

    public FeederNode createGenerator(String id, int order, BusCell.Direction direction) {
        FeederInjectionNode fn = FeederInjectionNode.createGenerator(voltageLevelGraph, id, id);
        commonFeederSetting(fn, id, order, direction);
        return fn;
    }

    public FeederLineNode createFeederLineNode(String id, String otherVlId, FeederWithSideNode.Side side, int order, BusCell.Direction direction) {
        FeederLineNode fln = FeederLineNode.create(voltageLevelGraph, id + "_" + side, id, id, side, getVoltageLevelInfosFromId.apply(otherVlId));
        commonFeederSetting(fln, id, order, direction);
        return fln;
    }

    public Feeder2WTNode createFeeder2WTNode(String id, String otherVlId, FeederWithSideNode.Side side,
                                             int order, BusCell.Direction direction) {
        Feeder2WTNode f2WTe = Feeder2WTNode.create(voltageLevelGraph, id + "_" + side, id, id, side, getVoltageLevelInfosFromId.apply(otherVlId));
        commonFeederSetting(f2WTe, id, order, direction);
        return f2WTe;
    }

    public Feeder2WTLegNode createFeeder2wtLegNode(String id, FeederWithSideNode.Side side,
                                                   int order, BusCell.Direction direction) {
        Feeder2WTLegNode f2WTe = Feeder2WTLegNode.create(voltageLevelGraph, id + "_" + side, id, id, side);
        commonFeederSetting(f2WTe, id, order, direction);
        return f2WTe;
    }

    public Feeder3WTLegNode createFeeder3wtLegNode(String id, FeederWithSideNode.Side side, int order, BusCell.Direction direction) {
        Feeder3WTLegNode f3WTe = Feeder3WTLegNode.createForSubstationDiagram(voltageLevelGraph, id + "_" + side, id, id, side);
        commonFeederSetting(f3WTe, id + side.getIntValue(), order, direction);
        return f3WTe;
    }

    public VoltageLevelInfos getVoltageLevelInfos() {
        return voltageLevelInfos;
    }
}
