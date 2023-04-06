/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.nad.model.*;

import java.util.List;
import java.util.Optional;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public interface LabelProvider {
    Optional<EdgeInfo> getEdgeInfo(Graph graph, BranchEdge edge, BranchEdge.Side side);

    Optional<EdgeInfo> getEdgeInfo(Graph graph, ThreeWtEdge edge);

    String getLabel(Edge edge);

    String getArrowPathDIn();

    String getArrowPathDOut();

    List<String> getVoltageLevelDescription(VoltageLevelNode voltageLevelNode);

    String getBusDescription(BusNode busNode);

    List<String> getVoltageLevelDetails(VoltageLevelNode vlNode);
}
