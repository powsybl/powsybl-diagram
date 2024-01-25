/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.layout.pathfinding.*;
import com.powsybl.sld.model.graphs.*;
import com.powsybl.sld.model.nodes.*;

import java.util.*;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public abstract class AbstractZoneLayout extends AbstractBaseLayout<ZoneGraph> {
    protected SubstationLayoutFactory sLayoutFactory;
    protected VoltageLevelLayoutFactory vLayoutFactory;
    protected Map<BaseGraph, AbstractLayout<SubstationGraph>> layoutBySubstation;
    protected PathFinder pathFinder;

    protected AbstractZoneLayout(ZoneGraph graph, SubstationLayoutFactory sLayoutFactory, VoltageLevelLayoutFactory vLayoutFactory) {
        super(graph);
        this.sLayoutFactory = Objects.requireNonNull(sLayoutFactory);
        this.vLayoutFactory = Objects.requireNonNull(vLayoutFactory);
        this.layoutBySubstation = new HashMap<>();
        for (SubstationGraph subGraph : getGraph().getSubstations()) {
            Layout sLayout = sLayoutFactory.create(subGraph, vLayoutFactory);
            layoutBySubstation.put(subGraph, (AbstractLayout<SubstationGraph>) sLayout);
        }
    }

    @Override
    public void run(LayoutParameters layoutParameters) {
        pathFinder = new PathFinderFactory().create(layoutParameters.getZoneLayoutPathFinder());

        // Calculate all the coordinates for the substation graphs in the zone graph
        calculateCoordSubstations(layoutParameters);

        // Calculate all the coordinates for the middle nodes and the snake lines between the substation graphs
        manageSnakeLines(layoutParameters);
    }

    protected abstract void calculateCoordSubstations(LayoutParameters layoutParameters);

    protected void move(BaseGraph subGraph, double dx, double dy) {
        for (VoltageLevelGraph vlGraph : subGraph.getVoltageLevels()) {
            vlGraph.setCoord(vlGraph.getX() + dx, vlGraph.getY() + dy);
            vlGraph.getLineEdges().forEach(s -> s.shiftSnakeLine(dx, dy));
        }
        subGraph.getMultiTermNodes().forEach(node -> {
            node.setCoordinates(node.getX() + dx, node.getY() + dy);
            node.getAdjacentEdges().forEach(edge -> {
                if (edge instanceof BranchEdge branch) {
                    branch.shiftSnakeLine(dx, dy);
                }
            });
        });
        subGraph.getLineEdges().forEach(s -> s.shiftSnakeLine(dx, dy));
    }
}
