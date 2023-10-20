/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout.pathfinding;

import java.util.*;

/**
 * @author Thomas Adam <tadam at neverhack.com>
 */
public abstract class AbstractPathFinder implements PathFinder {
    @Override
    public List<com.powsybl.sld.model.coordinate.Point> toSnakeLine(List<Point> path) {
        // Change class of Point
        final List<com.powsybl.sld.model.coordinate.Point> snakeLine = new ArrayList<>();
        path.forEach(p -> snakeLine.add(new com.powsybl.sld.model.coordinate.Point(p.x(), p.y())));
        return snakeLine;
    }
}
