/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.builders;

import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.graphs.BaseGraph;
import com.powsybl.sld.model.nodes.FeederNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.powsybl.sld.model.nodes.NodeSide.*;

/**
 * @author Thomas Adam <tadam at neverhack.com>
 */
public abstract class AbstractRawBuilder implements BaseRawBuilder {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractRawBuilder.class);

    protected abstract BaseGraph getGraph();

    protected abstract boolean containsVoltageLevelRawBuilders(VoltageLevelRawBuilder vl1, VoltageLevelRawBuilder vl2);

    @Override
    public Map<VoltageLevelRawBuilder, FeederNode> createLine(String id, VoltageLevelRawBuilder vl1, VoltageLevelRawBuilder vl2, int order1, int order2,
                                                              Direction direction1, Direction direction2) {
        Map<VoltageLevelRawBuilder, FeederNode> feederLineNodes = new HashMap<>();
        FeederNode feederLineNode1 = vl1.createFeederLineNode(id, vl2.getVoltageLevelInfos().getId(), ONE, order1, direction1);
        FeederNode feederLineNode2 = vl2.createFeederLineNode(id, vl1.getVoltageLevelInfos().getId(), TWO, order2, direction2);
        feederLineNodes.put(vl1, feederLineNode1);
        feederLineNodes.put(vl2, feederLineNode2);

        if (containsVoltageLevelRawBuilders(vl1, vl2)) {
            // All VoltageLevel must be in the same Substation
            getGraph().addLineEdge(id, feederLineNode1, feederLineNode2);
        }
        return feederLineNodes;
    }

    @Override
    public Map<VoltageLevelRawBuilder, FeederNode> createLine(String id, VoltageLevelRawBuilder vl1, VoltageLevelRawBuilder vl2) {
        return createLine(id, vl1, vl2, 0, 0, null, null);
    }
}
