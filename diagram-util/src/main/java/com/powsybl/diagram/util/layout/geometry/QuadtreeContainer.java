/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.layout.geometry;

/**
 * A class that contains a quadtree. This is used for initialization purposes. When starting some algorithms, we create forces
 * at the start that require a quadtree object, but we don't actually have the points objects needed to create the quadtree. Instead
 * we use this to say we have a quadtree, and we will later actually create and use the quadtree
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class QuadtreeContainer {
    private Quadtree quadtree;

    public Quadtree getQuadtree() {
        return quadtree;
    }

    public void setQuadtree(Quadtree quadtree) {
        this.quadtree = quadtree;
    }
}

