/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.nad.model.BranchEdge;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.ThreeWtEdge;
import com.powsybl.nad.model.VoltageLevelNode;

import java.util.List;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public interface LabelProvider {
    List<EdgeInfo> getEdgeInfos(Graph graph, BranchEdge edge, BranchEdge.Side side);

    List<EdgeInfo> getEdgeInfos(Graph graph, ThreeWtEdge edge);

    String getArrowPathDIn();

    String getArrowPathDOut();

    List<String> getVoltageLevelDescription(VoltageLevelNode voltageLevelNode);
}
