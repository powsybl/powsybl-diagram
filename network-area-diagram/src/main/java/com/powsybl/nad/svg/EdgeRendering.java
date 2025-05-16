/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import java.util.List;
import java.util.Map;

import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.Point;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public interface EdgeRendering {
    void run(Graph graph, SvgParameters svgParameters);

    void setBentLinesPoints(Map<String, List<Point>> bentLinesPoints);
}
