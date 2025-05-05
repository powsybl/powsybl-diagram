/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.diagram.util.forcelayout.layouts;

import com.powsybl.diagram.util.forcelayout.forces.Force;
import com.powsybl.diagram.util.forcelayout.forces.GravityForceSimple;
import com.powsybl.diagram.util.forcelayout.forces.SpringForce;
import com.powsybl.diagram.util.forcelayout.forces.forceparameter.ForceParameter;
import com.powsybl.diagram.util.forcelayout.forces.forceparameter.IntensityParameter;
import com.powsybl.diagram.util.forcelayout.forces.forceparameter.SpringContainer;
import com.powsybl.diagram.util.forcelayout.forces.forceparameter.SpringParameter;
import com.powsybl.diagram.util.forcelayout.geometry.ForceGraph;
import com.powsybl.diagram.util.forcelayout.geometry.Point;
import com.powsybl.diagram.util.forcelayout.layouts.layoutsparameters.SpringyParameters;
import org.jgrapht.Graph;

import java.util.*;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class SpringyLayout<V, E> extends AbstractLayoutAlgorithm<V, E> {
    private static final double DEFAULT_STIFFNESS = 100.0;

    private final SpringyParameters layoutParameters;
    private final IntensityParameter gravityForceParameter;
    private SpringContainer<E> springContainer;

    public SpringyLayout(SpringyParameters layoutParameters) {
        super(new ArrayList<Force<V, E, ? extends ForceParameter>>(List.of(
                new GravityForceSimple<>(),
                new SpringForce<>()
        )));
        this.layoutParameters = layoutParameters;
        this.gravityForceParameter = new IntensityParameter(layoutParameters.getRepulsion() / 200);
    }

    private void initializeSprings(ForceGraph<V, E> forceGraph) {
        Map<E, SpringParameter> springs = new HashMap<>();
        Graph<V, E> graph = forceGraph.getGraph();
        for (E edge : forceGraph.getGraph().edgeSet()) {
            V edgeSource = graph.getEdgeSource(edge);
            V edgeTarget = graph.getEdgeTarget(edge);
            if (forceGraph.getFixedPoints().containsKey(edgeSource) && forceGraph.getFixedPoints().containsKey(edgeTarget)) {
                continue;
            }
            Point pointSource = Objects.requireNonNullElseGet(forceGraph.getMovingPoints().get(edgeSource), () -> forceGraph.getInitialPoints().get(edgeSource));
            Point pointTarget = Objects.requireNonNullElseGet(forceGraph.getMovingPoints().get(edgeTarget), () -> forceGraph.getInitialPoints().get(edgeTarget));
            if (pointSource != pointTarget) { // no use in force layout to add loops
                springs.put(edge, new SpringParameter(DEFAULT_STIFFNESS, graph.getEdgeWeight(edge)));
            }
        }
        this.springContainer = new SpringContainer<>(springs);
    }

    @Override
    public void calculateLayout(ForceGraph<V, E> graph) {
        initializeSprings(graph);
        // do the loop on the nodes and forces
        // think of keeping two graph for before and after on each step
    }
}
