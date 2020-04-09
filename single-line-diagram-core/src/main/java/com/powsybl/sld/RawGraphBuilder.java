/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

import com.powsybl.sld.library.ComponentTypeName;
import com.powsybl.sld.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.powsybl.sld.model.FeederNode.Side.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

public class RawGraphBuilder implements GraphBuilder {

    private final Map<String, VoltageLevelBuilder> vlBuilders = new TreeMap<>();
    private final Map<String, SubstationBuilder> ssBuilders = new TreeMap<>();

    public VoltageLevelBuilder createVoltageLevelBuilder(VoltageLevelInfos voltageLevelInfos, boolean forVoltageLevelDiagram) {
        VoltageLevelBuilder vlBuilder = new VoltageLevelBuilder(voltageLevelInfos, forVoltageLevelDiagram);
        vlBuilders.put(voltageLevelInfos.getId(), vlBuilder);
        return vlBuilder;
    }

    public VoltageLevelBuilder createVoltageLevelBuilder(String vlId, double vlNominalV, boolean forVoltageLevelDiagram) {
        return createVoltageLevelBuilder(new VoltageLevelInfos(vlId, vlId, vlNominalV), forVoltageLevelDiagram);
    }

    public VoltageLevelBuilder createVoltageLevelBuilder(VoltageLevelInfos voltageLevelInfos) {
        return createVoltageLevelBuilder(voltageLevelInfos, true);
    }

    public VoltageLevelBuilder createVoltageLevelBuilder(String vlId, double vlNominalV) {
        return createVoltageLevelBuilder(new VoltageLevelInfos(vlId, vlId, vlNominalV));
    }

    public VoltageLevelInfos getVoltageLevelInfosFromId(String id) {
        if (vlBuilders.containsKey(id)) {
            return vlBuilders.get(id).voltageLevelInfos;
        }
        return new VoltageLevelInfos("OTHER", "OTHER", 0);
    }

    class VoltageLevelBuilder {

        private VoltageLevelInfos voltageLevelInfos;
        private Graph graph;

        private SubstationBuilder substationBuilder;

        public VoltageLevelBuilder(VoltageLevelInfos voltageLevelInfos, boolean forVoltageLevelDiagram) {
            this.voltageLevelInfos = voltageLevelInfos;
            graph = Graph.create(voltageLevelInfos, false, forVoltageLevelDiagram);
        }

        public Graph getGraph() {
            return graph;
        }

        public SubstationBuilder getSubstationBuilder() {
            return substationBuilder;
        }

        public void setSubstationBuilder(SubstationBuilder substationBuilder) {
            this.substationBuilder = substationBuilder;
        }

        public BusNode createBusBarSection(String id, int busbarIndex, int sectionIndex) {
            BusNode busNode = BusNode.create(graph, id, id);
            graph.addNode(busNode);
            busNode.setStructuralPosition(new Position(sectionIndex, busbarIndex, 1, 0, false, null));
            return busNode;
        }

        public BusNode createBusBarSection(String id) {
            BusNode busNode = BusNode.create(graph, id, id);
            graph.addNode(busNode);
            return busNode;
        }

        public SwitchNode createSwitchNode(SwitchNode.SwitchKind sk, String id, boolean fictitious, boolean open) {
            SwitchNode sw = new SwitchNode(id, id, sk.name(), fictitious, graph, sk, open);
            graph.addNode(sw);
            return sw;
        }

        public void connectNode(Node node1, Node node2) {
            graph.addEdge(node1, node2);
        }

        public FictitiousNode createFictitiousNode(String id) {
            FictitiousNode fictitiousNode = new FictitiousNode(graph, id);
            graph.addNode(fictitiousNode);
            return fictitiousNode;
        }

        public void addExtension(FeederNode fn, int order, BusCell.Direction direction) {
            fn.setOrder(order);
            fn.setDirection(direction);
        }

        private void commonFeederSetting(FeederNode node, String id, int order, BusCell.Direction direction) {
            node.setLabel(id);
            graph.addNode(node);
            if (direction != null) {
                addExtension(node, order, direction);
            }
        }

        public FeederNode createFeederNode(String id, String componentTypeName, boolean fictitious, int order, BusCell.Direction direction) {
            FeederNode fn = new FeederNode(id, id, id, componentTypeName, fictitious, graph);
            commonFeederSetting(fn, id, order, direction);
            return fn;
        }

        public FeederNode createFeederNode(String id, String componentTypeName, boolean fictitious) {
            FeederNode fn = createFeederNode(id, componentTypeName, fictitious);
            commonFeederSetting(fn, id, 0, null);
            return fn;
        }

        public FeederNode createLoad(String id) {
            return createFeederNode(id, ComponentTypeName.LOAD, false);
        }

        public FeederNode createLoad(String id, int order, BusCell.Direction direction) {
            return createFeederNode(id, ComponentTypeName.LOAD, false, order, direction);
        }

        public FeederNode createGenerator(String id) {
            return createFeederNode(id, ComponentTypeName.GENERATOR, false);
        }

        public FeederNode createGenerator(String id, int order, BusCell.Direction direction) {
            return createFeederNode(id, ComponentTypeName.GENERATOR, false, order, direction);
        }

        public FeederLineNode createFeederLineNode(String id, String otherVlId, FeederNode.Side side, int order, BusCell.Direction direction) {
            String name = id + "_" + side;
            FeederLineNode fln = new FeederLineNode(name, id, id, ComponentTypeName.LINE, false, graph, side,
                    getVoltageLevelInfosFromId(otherVlId));
            commonFeederSetting(fln, id, order, direction);
            return fln;
        }

        public Feeder2WTNode createFeeder2WTNode(String id, String otherVlId, FeederNode.Side side,
                                                 int order, BusCell.Direction direction) {
            String name = id + "_" + side;
            Feeder2WTNode f2WTe = new Feeder2WTNode(name, id, id, ComponentTypeName.TWO_WINDINGS_TRANSFORMER, false,
                    graph, side, getVoltageLevelInfosFromId(otherVlId));
            commonFeederSetting(f2WTe, id, order, direction);
            return f2WTe;
        }

        public Feeder3WTNode createFeeder3WTNode(String id, String transformerId, FeederNode.Side side,
                                                 int order, BusCell.Direction direction) {
            String name = id + "_" + side;
            Feeder3WTNode f3WTe = new Feeder3WTNode(name, id, id,
                    ComponentTypeName.THREE_WINDINGS_TRANSFORMER, false, graph, transformerId, side);
            commonFeederSetting(f3WTe, id + side.getIntValue(), order, direction);
            return f3WTe;
        }
    }

    public SubstationBuilder createSubstationBuilder(String id) {
        SubstationBuilder ssb = new SubstationBuilder(id);
        ssBuilders.put(id, ssb);
        return ssb;
    }

    public class SubstationBuilder {

        SubstationGraph ssGraph;

        public SubstationBuilder(String id) {
            ssGraph = SubstationGraph.create(id);
        }

        public SubstationGraph getSsGraph() {
            return ssGraph;
        }

        public void addVlBuilder(VoltageLevelBuilder vlBuilder) {
            ssGraph.addNode(vlBuilder.getGraph());
            vlBuilder.setSubstationBuilder(this);
        }

        public Map<VoltageLevelBuilder, FeederLineNode> createLine(String id, VoltageLevelBuilder vl1, VoltageLevelBuilder vl2, int order1, int order2,
                                                                   BusCell.Direction direction1, BusCell.Direction direction2) {
            Map<VoltageLevelBuilder, FeederLineNode> feederLineNodes = new HashMap<>();
            FeederLineNode feederLineNode1 = vl1.createFeederLineNode(id, vl2.voltageLevelInfos.getId(), ONE, order1, direction1);
            FeederLineNode feederLineNode2 = vl2.createFeederLineNode(id, vl1.voltageLevelInfos.getId(), TWO, order2, direction2);
            feederLineNodes.put(vl1, feederLineNode1);
            feederLineNodes.put(vl2, feederLineNode2);
            ssGraph.addEdge(ComponentTypeName.LINE, feederLineNode1, feederLineNode2);
            return feederLineNodes;
        }

        public Map<VoltageLevelBuilder, FeederLineNode> createLine(String id, VoltageLevelBuilder vl1, VoltageLevelBuilder vl2) {
            return createLine(id, vl1, vl2, 0, 0, null, null);
        }

        public Map<VoltageLevelBuilder, Feeder2WTNode> createFeeder2WT(String id, VoltageLevelBuilder vl1, VoltageLevelBuilder vl2, int order1, int order2,
                                                                       BusCell.Direction direction1, BusCell.Direction direction2) {
            Map<VoltageLevelBuilder, Feeder2WTNode> f2WTNodes = new HashMap<>();
            Feeder2WTNode feeder2WTNode1 = vl1.createFeeder2WTNode(id, vl2.voltageLevelInfos.getId(), ONE, order1, direction1);
            Feeder2WTNode feeder2WTNode2 = vl2.createFeeder2WTNode(id, vl1.voltageLevelInfos.getId(), TWO, order2, direction2);
            f2WTNodes.put(vl1, feeder2WTNode1);
            f2WTNodes.put(vl2, feeder2WTNode2);
            ssGraph.addEdge(ComponentTypeName.TWO_WINDINGS_TRANSFORMER, feeder2WTNode1, feeder2WTNode2);
            return f2WTNodes;
        }

        public Map<VoltageLevelBuilder, Feeder2WTNode> createFeeder2WT(String id, VoltageLevelBuilder vl1, VoltageLevelBuilder vl2) {
            return createFeeder2WT(id, vl1, vl2, 0, 0, null, null);
        }

        public Map<VoltageLevelBuilder, Feeder3WTNode> createFeeder3WT(String id, VoltageLevelBuilder vl1, VoltageLevelBuilder vl2, VoltageLevelBuilder vl3,
                                                                       int order1, int order2, int order3,
                                                                       BusCell.Direction direction1, BusCell.Direction direction2, BusCell.Direction direction3) {
            Map<VoltageLevelBuilder, Feeder3WTNode> f3WTNodes = new HashMap<>();
            Feeder3WTNode feeder3WTNode1 = vl1.createFeeder3WTNode(id, id, ONE, order1, direction1);
            Feeder3WTNode feeder3WTNode2 = vl2.createFeeder3WTNode(id, id, TWO, order2, direction2);
            Feeder3WTNode feeder3WTNode3 = vl3.createFeeder3WTNode(id, id, THREE, order3, direction3);
            f3WTNodes.put(vl1, feeder3WTNode1);
            f3WTNodes.put(vl2, feeder3WTNode2);
            f3WTNodes.put(vl3, feeder3WTNode3);
            ssGraph.addEdge(ComponentTypeName.THREE_WINDINGS_TRANSFORMER, feeder3WTNode1, feeder3WTNode2, feeder3WTNode3);
            return f3WTNodes;
        }

        public Map<VoltageLevelBuilder, Feeder3WTNode> createFeeder3WT(String id, VoltageLevelBuilder vl1, VoltageLevelBuilder vl2, VoltageLevelBuilder vl3) {
            return createFeeder3WT(id, vl1, vl2, vl3, 0, 0, 0, null, null, null);
        }
    }

    public Graph buildVoltageLevelGraph(String id,
                                        boolean useName,
                                        boolean forVoltageLevelDiagram) {
        return vlBuilders.get(id).getGraph();
    }

    public SubstationGraph buildSubstationGraph(String id,
                                                boolean useName) {
        return ssBuilders.get(id).getSsGraph();
    }

    //TODO: buildZoneGraph
    public ZoneGraph buildZoneGraph(List<String> substationIds, boolean useName) {
        return null;
    }
}
