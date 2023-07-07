/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.graphs.ZoneGraph;

import java.util.Objects;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public abstract class AbstractZoneLayout extends AbstractBaseLayout {

    private final ZoneGraph graph;
    protected SubstationLayoutFactory sLayoutFactory;
    protected VoltageLevelLayoutFactory vLayoutFactory;

    protected AbstractZoneLayout(ZoneGraph graph, SubstationLayoutFactory sLayoutFactory, VoltageLevelLayoutFactory vLayoutFactory) {
        this.graph = graph;
        this.sLayoutFactory = Objects.requireNonNull(sLayoutFactory);
        this.vLayoutFactory = Objects.requireNonNull(vLayoutFactory);
    }

    public ZoneGraph getGraph() {
        return graph;
    }

    @Override
    public void run(LayoutParameters layoutParameters) {
        // Calculate all the coordinates for the substation graphs in the zone graph
        calculateCoordSubstations(layoutParameters);

        // Calculate all the coordinates for the middle nodes and the snake lines between the substation graphs
        manageSnakeLines(layoutParameters);
    }

    protected abstract void calculateCoordSubstations(LayoutParameters layoutParameters);

    protected void manageAllSnakeLines(LayoutParameters layoutParameters) {
        getGraph().getVoltageLevels().forEach(g -> manageSnakeLines(g, layoutParameters));
        getGraph().getSubstations().forEach(g -> manageSnakeLines(g, layoutParameters));
        manageSnakeLines(getGraph(), layoutParameters);
    }
}
