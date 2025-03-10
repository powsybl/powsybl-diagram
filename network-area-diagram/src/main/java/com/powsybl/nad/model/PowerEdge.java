/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

import java.util.Arrays;
import java.util.List;

public class PowerEdge extends AbstractEdge {

    private Point[] points;

    public PowerEdge(String diagramId, String equipmentId, String nae, String type) {
        super(diagramId, null, null, type);
    }

    public void setPoints(Point... points) {
        this.points = points;
    }

    public List<Point> getPoints() {
        return Arrays.asList(points);
    }
}
