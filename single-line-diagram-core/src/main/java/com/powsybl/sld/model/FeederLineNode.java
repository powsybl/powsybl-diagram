/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.VoltageLevel;

import java.util.Objects;

import static com.powsybl.sld.library.ComponentTypeName.LINE;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class FeederLineNode extends FeederBranchNode {
    protected FeederLineNode(String id, String name, String componentType, boolean fictitious, Graph graph, VoltageLevel vlOtherSide) {
        super(id, name, componentType, fictitious, graph, vlOtherSide);
    }

    public static FeederNode create(Graph graph, Line line, Branch.Side side) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(line);

        String id = line.getId() + "_" + side.name();
        String name = line.getName() + "_" + side.name();
        Branch.Side otherSide = side == Branch.Side.ONE ? Branch.Side.TWO : Branch.Side.ONE;
        VoltageLevel vlOtherSide = line.getTerminal(otherSide).getVoltageLevel();
        return new FeederLineNode(id, name, LINE, false, graph, vlOtherSide);
    }
}
