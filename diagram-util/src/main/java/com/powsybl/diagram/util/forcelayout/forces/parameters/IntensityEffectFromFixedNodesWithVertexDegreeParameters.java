/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout.forces.forceparameter;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
// this name is clearly way too long...
public class IntensityEffectFromFixedNodesWithVertexDegreeParameters extends IntensityEffectFromFixedNodesParameters {
    // Note : it is important that the order of the vertex in this array corresponds to the order in which they are accessed when iterating over the graph or the points
    private final int[] degreeOfVertex;

    public IntensityEffectFromFixedNodesWithVertexDegreeParameters(
            double forceIntensity,
            boolean effectFromFixedNodes,
            int[] degreeOfVertex
    ) {
        super(forceIntensity, effectFromFixedNodes);
        this.degreeOfVertex = degreeOfVertex;
    }

    public int[] getDegreeOfVertex() {
        return degreeOfVertex;
    }
}

