/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.graphs.ZoneGraph;

import java.util.Objects;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public abstract class AbstractZoneLayout extends AbstractBaseLayout<ZoneGraph> {
    protected SubstationLayoutFactory sLayoutFactory;
    protected VoltageLevelLayoutFactory vLayoutFactory;

    protected AbstractZoneLayout(ZoneGraph graph, SubstationLayoutFactory sLayoutFactory, VoltageLevelLayoutFactory vLayoutFactory) {
        super(graph);
        this.sLayoutFactory = Objects.requireNonNull(sLayoutFactory);
        this.vLayoutFactory = Objects.requireNonNull(vLayoutFactory);
    }

    @Override
    public void run(LayoutParameters layoutParameters) {
        // Calculate all the coordinates for the substation graphs in the zone graph
        calculateCoordSubstations(layoutParameters);

        // Calculate all the coordinates for the middle nodes and the snake lines between the substation graphs
        manageSnakeLines(layoutParameters);
    }

    protected abstract void calculateCoordSubstations(LayoutParameters layoutParameters);

    protected void move(SubstationGraph subGraph, double dx, double dy) {
        for (VoltageLevelGraph vlGraph : subGraph.getVoltageLevels()) {
            vlGraph.setCoord(vlGraph.getX() + dx, vlGraph.getY() + dy);
        }
    }
}
