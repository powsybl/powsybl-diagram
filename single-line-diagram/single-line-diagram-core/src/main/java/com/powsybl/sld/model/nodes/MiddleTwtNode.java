/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.nodes;

import com.powsybl.sld.model.coordinate.Point;

import java.util.List;

/**
 * Node for transformer which are displayed between voltage levels in multi voltage level diagrams
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public interface MiddleTwtNode extends Node {
    /**
     * Set the transformer orientation of the node based on the given snake lines
     * @param snakelines the snake lines
     */
    void setOrientationFromSnakeLines(List<List<Point>> snakelines);
}
