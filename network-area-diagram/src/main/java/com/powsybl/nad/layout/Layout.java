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

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public interface Layout {
    void run(Graph graph, LayoutParameters layoutParameters);

    void setMovableNodesInitialPositions(Map<String, Point> movableNodesInitialPositions);

    void setFixedNodesPositions(Map<String, Point> fixedNodesPositions);

    Map<String, Point> getMovableNodesInitialPositions();

    Map<String, Point> getFixedNodesPositions();

    void setTextNodeFixedPosition(String voltageLevelId, Point topLeft, Point edgeConnection);
}
