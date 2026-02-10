/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.builders;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.graphs.NodeFactory;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.NodeSide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 * @author Benoit Jeanson {@literal <Benoit.Jeanson at rte-france.com>}
 */

public class SubstationRawBuilder extends AbstractRawBuilder {

    protected static final Logger LOGGER = LoggerFactory.getLogger(SubstationRawBuilder.class);

    private static final String VL_NOT_PRESENT = "VoltageLevel(s) '%s' not found in Substation '%s'";

    SubstationGraph substationGraph;
    List<VoltageLevelRawBuilder> voltageLevelBuilders = new ArrayList<>();

    ZoneRawBuilder zBuilder;

    public SubstationRawBuilder(String id, ZoneRawBuilder parentBuilder) {
        substationGraph = SubstationGraph.create(id, parentBuilder == null ? null : parentBuilder.getGraph());
    }

    @Override
    public SubstationGraph getGraph() {
        return substationGraph;
    }

    public ZoneRawBuilder getZoneBuilder() {
        return zBuilder;
    }

    public void setZoneRawBuilder(ZoneRawBuilder zoneBuilder) {
        this.zBuilder = zoneBuilder;
    }

    public void addVlBuilder(VoltageLevelRawBuilder vlBuilder) {
        voltageLevelBuilders.add(vlBuilder);
        vlBuilder.setSubstationBuilder(this);
    }

    @Override
    protected boolean containsVoltageLevelRawBuilders(VoltageLevelRawBuilder vl1, VoltageLevelRawBuilder vl2) {
        return containsVoltageLevelRawBuilder(vl1) && containsVoltageLevelRawBuilder(vl2);
    }

    public Map<VoltageLevelRawBuilder, FeederNode> createFeeder2WT(String id,
                                                                   List<VoltageLevelRawBuilder> vls,
                                                                   List<Integer> orders,
                                                                   List<Direction> directions) {
        checkInputParameters(2, vls, orders, directions);
        VoltageLevelRawBuilder vl1 = vls.get(0);
        VoltageLevelRawBuilder vl2 = vls.get(1);
        int order1 = orders.get(0);
        int order2 = orders.get(1);
        Direction direction1 = directions.get(0);
        Direction direction2 = directions.get(1);
        Map<VoltageLevelRawBuilder, FeederNode> f2WTNodes = new HashMap<>();
        FeederNode feeder2WtNode1 = vl1.createFeeder2wtLegNode(id, NodeSide.ONE, order1, direction1);
        FeederNode feeder2WTNode2 = vl2.createFeeder2wtLegNode(id, NodeSide.TWO, order2, direction2);
        f2WTNodes.put(vl1, feeder2WtNode1);
        f2WTNodes.put(vl2, feeder2WTNode2);
        if (containsVoltageLevelRawBuilder(vl1) && containsVoltageLevelRawBuilder(vl2)) {
            // All VoltageLevel must be in the same Substation
            NodeFactory.createMiddle2WTNode(substationGraph, id, id, feeder2WtNode1, feeder2WTNode2, vl1.getVoltageLevelInfos(), vl2.getVoltageLevelInfos(), false);
        } else {
            // At least one VoltageLevel is not in the same Substation (Zone graph case)
            manageErrorCase(Stream.of(vl1, vl2));
        }
        return f2WTNodes;
    }

    public Map<VoltageLevelRawBuilder, FeederNode> createFeeder2WT(String id, VoltageLevelRawBuilder vl1, VoltageLevelRawBuilder vl2) {
        return createFeeder2WT(id, List.of(vl1, vl2), List.of(0, 0), Stream.of((Direction) null, null).toList());
    }

    public Map<VoltageLevelRawBuilder, FeederNode> createFeeder3WT(String id,
                                                                   List<VoltageLevelRawBuilder> vls,
                                                                   List<Integer> orders,
                                                                   List<Direction> directions) {
        checkInputParameters(3, vls, orders, directions);
        VoltageLevelRawBuilder vl1 = vls.get(0);
        VoltageLevelRawBuilder vl2 = vls.get(1);
        VoltageLevelRawBuilder vl3 = vls.get(2);
        int order1 = orders.get(0);
        int order2 = orders.get(1);
        int order3 = orders.get(2);
        Direction direction1 = directions.get(0);
        Direction direction2 = directions.get(1);
        Direction direction3 = directions.get(2);
        Map<VoltageLevelRawBuilder, FeederNode> f3WTNodes = new HashMap<>();
        FeederNode feeder3WTNode1 = vl1.createFeeder3wtLegNode(id, NodeSide.ONE, order1, direction1);
        FeederNode feeder3WTNode2 = vl2.createFeeder3wtLegNode(id, NodeSide.TWO, order2, direction2);
        FeederNode feeder3WTNode3 = vl3.createFeeder3wtLegNode(id, NodeSide.THREE, order3, direction3);
        f3WTNodes.put(vl1, feeder3WTNode1);
        f3WTNodes.put(vl2, feeder3WTNode2);
        f3WTNodes.put(vl3, feeder3WTNode3);

        if (containsVoltageLevelRawBuilder(vl1) && containsVoltageLevelRawBuilder(vl2) && containsVoltageLevelRawBuilder(vl3)) {
            // All VoltageLevel must be in the same Substation
            // creation of the middle node and the edges linking the transformer leg nodes to this middle node
            NodeFactory.createMiddle3WTNode(substationGraph, id, id, feeder3WTNode1, feeder3WTNode2, feeder3WTNode3);
        } else {
            // At least one VoltageLevel is not in the same Substation (Zone graph case)
            manageErrorCase(Stream.of(vl1, vl2, vl3));
        }

        return f3WTNodes;
    }

    public Map<VoltageLevelRawBuilder, FeederNode> createFeeder3WT(String id, VoltageLevelRawBuilder vl1, VoltageLevelRawBuilder vl2, VoltageLevelRawBuilder vl3) {
        return createFeeder3WT(id, List.of(vl1, vl2, vl3), List.of(0, 0, 0), Stream.of((Direction) null, null, null).toList());
    }

    private void manageErrorCase(Stream<VoltageLevelRawBuilder> stream) {
        // At least one VoltageLevel is not in the same Substation (Zone graph case)
        String vls = String.join(", ", stream.filter(vl -> !containsVoltageLevelRawBuilder(vl)).map(vl -> vl.getGraph().getId()).toList());
        throw new PowsyblException(String.format(VL_NOT_PRESENT, vls, substationGraph.getId()));
    }

    private boolean containsVoltageLevelRawBuilder(VoltageLevelRawBuilder vl) {
        return vl.getSubstationBuilder() == this;
    }
}
