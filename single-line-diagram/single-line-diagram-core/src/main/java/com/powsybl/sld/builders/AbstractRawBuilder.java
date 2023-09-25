/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.builders;

import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.graphs.BaseGraph;
import com.powsybl.sld.model.graphs.NodeFactory;
import com.powsybl.sld.model.nodes.FeederNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.*;

import static com.powsybl.sld.model.nodes.NodeSide.*;

/**
 * @author Thomas Adam <tadam at neverhack.com>
 */
public abstract class AbstractRawBuilder implements BaseRawBuilder {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractRawBuilder.class);

    private static final String REQUIRED_N_VOLTAGELEVEL_RAW_BUILDER = "Expected '%d' VoltageLevelRawBuilders but only '%d' found";

    private static final String REQUIRED_N_ORDER = "Expected '%d' node orders but only '%d' found";

    private static final String REQUIRED_N_DIRECTION = "Expected '%d' node directions but only '%d' found";

    protected abstract BaseGraph getGraph();

    protected abstract boolean containsVoltageLevelRawBuilders(VoltageLevelRawBuilder vl1, VoltageLevelRawBuilder vl2);

    @Override
    public Map<VoltageLevelRawBuilder, FeederNode> createLine(String id,
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
        Map<VoltageLevelRawBuilder, FeederNode> feederLineNodes = new HashMap<>();
        FeederNode feederLineNode1 = vl1.createFeederLineNode(id, vl2.getVoltageLevelInfos().getId(), ONE, order1, direction1);
        FeederNode feederLineNode2 = vl2.createFeederLineNode(id, vl1.getVoltageLevelInfos().getId(), TWO, order2, direction2);
        feederLineNodes.put(vl1, feederLineNode1);
        feederLineNodes.put(vl2, feederLineNode2);

        if (containsVoltageLevelRawBuilders(vl1, vl2)) {
            // All VoltageLevel must be in the same Substation or the same Zone
            getGraph().addLineEdge(id, feederLineNode1, feederLineNode2);
        }
        return feederLineNodes;
    }

    @Override
    public Map<VoltageLevelRawBuilder, FeederNode> createLine(String id, VoltageLevelRawBuilder vl1, VoltageLevelRawBuilder vl2) {
        return createLine(id, List.of(vl1, vl2), List.of(0, 0), Stream.of((Direction) null, null).toList());
    }

    @Override
    public Map<VoltageLevelRawBuilder, FeederNode> createHdvcLine(String id,
                                                                  HvdcConverterStation.HvdcType type,
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
        Map<VoltageLevelRawBuilder, FeederNode> feederLineNodes = new HashMap<>();

        FeederNode feederLccNode1;
        FeederNode feederLccNode2;
        switch (type) {
            case LCC: {
                feederLccNode1 = NodeFactory.createLccConverterStation(vl1.getGraph(), id + "_" + ONE, id, id, ONE, vl2.getVoltageLevelInfos());
                feederLccNode2 = NodeFactory.createLccConverterStation(vl2.getGraph(), id + "_" + TWO, id, id, TWO, vl1.getVoltageLevelInfos());
                break;
            }
            case VSC: {
                feederLccNode1 = NodeFactory.createVscConverterStation(vl1.getGraph(), id + "_" + ONE, id, id, ONE, vl2.getVoltageLevelInfos());
                feederLccNode2 = NodeFactory.createVscConverterStation(vl2.getGraph(), id + "_" + TWO, id, id, TWO, vl1.getVoltageLevelInfos());
                break;
            }
            default: {
                throw new AssertionError();
            }
        }
        feederLccNode1.setLabel(id);
        feederLccNode1.setOrder(order1);
        feederLccNode1.setDirection(direction1 == null ? Direction.UNDEFINED : direction1);
        feederLccNode2.setLabel(id);
        feederLccNode2.setOrder(order2);
        feederLccNode2.setDirection(direction1 == null ? Direction.UNDEFINED : direction2);

        feederLineNodes.put(vl1, feederLccNode1);
        feederLineNodes.put(vl2, feederLccNode2);

        if (containsVoltageLevelRawBuilders(vl1, vl2)) {
            // All VoltageLevel must be in the same Substation or the same Zone
            getGraph().addLineEdge(id, feederLccNode1, feederLccNode2);
        }

        return feederLineNodes;
    }

    @Override
    public Map<VoltageLevelRawBuilder, FeederNode> createHdvcLine(String id, HvdcConverterStation.HvdcType type, VoltageLevelRawBuilder vl1, VoltageLevelRawBuilder vl2) {
        return createHdvcLine(id, type, List.of(vl1, vl2), List.of(0, 0), Stream.of((Direction) null, null).toList());
    }

    protected void checkInputParameters(int expectedSize,
                                        List<VoltageLevelRawBuilder> vls,
                                        List<Integer> orders,
                                        List<Direction> directions) {
        if (vls.size() != expectedSize) {
            throw new IllegalArgumentException(String.format(REQUIRED_N_VOLTAGELEVEL_RAW_BUILDER, expectedSize, vls.size()));
        }
        if (orders.size() != expectedSize) {
            throw new IllegalArgumentException(String.format(REQUIRED_N_ORDER, expectedSize, orders.size()));
        }
        if (directions.size() != expectedSize) {
            throw new IllegalArgumentException(String.format(REQUIRED_N_DIRECTION, expectedSize, directions.size()));
        }
    }
}
