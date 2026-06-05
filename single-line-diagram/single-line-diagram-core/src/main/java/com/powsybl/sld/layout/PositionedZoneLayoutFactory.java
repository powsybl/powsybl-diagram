/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout;

import java.util.List;

import org.jgrapht.alg.util.Pair;

import com.powsybl.sld.layout.pathfinding.*;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.graphs.*;

/**
 * @author Frédéric Sabot {@literal <frederic.sabot at haulogy.net>}
 */
public class PositionedZoneLayoutFactory implements ZoneLayoutFactory {

    private final List<Pair<String, Point>> positions;

    public PositionedZoneLayoutFactory(List<Pair<String, Point>> positions) {
        this.positions = positions;
    }

    @Override
    public Layout create(ZoneGraph graph, ZoneLayoutPathFinderFactory pathFinderFactory,
                         SubstationLayoutFactory sLayoutFactory, VoltageLevelLayoutFactory vLayoutFactory) {
        return new PositionedZoneLayout(graph, positions, pathFinderFactory, sLayoutFactory, vLayoutFactory);
    }

}
