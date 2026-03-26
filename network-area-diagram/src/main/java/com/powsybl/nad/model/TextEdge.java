/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

import java.util.Arrays;
import java.util.List;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class TextEdge extends AbstractEdge {

    public static final String TEXT_EDGE = "TextEdge";

    private Point[] points;

    public TextEdge(String svgId) {
        super(svgId, null, null, TEXT_EDGE);
    }

    public void setPoints(Point... points) {
        this.points = points;
    }

    public List<Point> getPoints() {
        return Arrays.asList(points);
    }
}
