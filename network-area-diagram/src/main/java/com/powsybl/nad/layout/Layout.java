/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.layout;

import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.Point;

import java.util.Map;
import java.util.Set;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public interface Layout {
    LayoutResult run(Graph graph, LayoutParameters layoutParameters);

    void setInitialNodePositions(Map<String, Point> initialNodePositions);

    void setNodesWithFixedPosition(Set<String> nodesWithFixedPosition);

    Map<String, Point> getInitialNodePositions();

    Set<String> getNodesWithFixedPosition();
}
