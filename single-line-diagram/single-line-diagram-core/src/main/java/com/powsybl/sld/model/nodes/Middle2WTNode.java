/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.nodes;

import com.powsybl.sld.model.coordinate.Orientation;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.graphs.VoltageLevelInfos;

import java.util.List;
import java.util.Objects;

/**
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
public class Middle2WTNode extends MiddleTwtNode {

    public Middle2WTNode(String id, String name, VoltageLevelInfos voltageLevelInfosLeg1, VoltageLevelInfos voltageLevelInfosLeg2, String componentType) {
        super(id, name,
            new VoltageLevelInfos[]{Objects.requireNonNull(voltageLevelInfosLeg1), Objects.requireNonNull(voltageLevelInfosLeg2) },
            componentType);
    }

    public void setOrientationFromSnakeLines(List<List<Point>> snakelines) {
        List<Point> pol1 = snakelines.get(0);
        List<Point> pol2 = snakelines.get(1);

        // Orientation.LEFT example:
        // coord1 o-----OO-----o coord2
        Point coord1 = pol1.get(pol1.size() - 2); // point linked to winding1
        Point coord2 = pol2.get(pol2.size() - 2); // point linked to winding2

        if (coord1.getX() == coord2.getX()) {
            setOrientation(coord2.getY() > coord1.getY() ? Orientation.DOWN : Orientation.UP);
        } else {
            setOrientation(coord1.getX() < coord2.getX() ? Orientation.RIGHT : Orientation.LEFT);
        }
    }
}
