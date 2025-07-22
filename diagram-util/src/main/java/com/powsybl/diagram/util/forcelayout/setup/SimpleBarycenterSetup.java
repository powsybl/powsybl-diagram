/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout.setup;

import com.powsybl.diagram.util.forcelayout.geometry.LayoutContext;
import com.powsybl.diagram.util.forcelayout.geometry.Point;
import com.powsybl.diagram.util.forcelayout.geometry.Vector2D;

import java.util.Random;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class SimpleBarycenterSetup<V, E> extends SimpleSetup<V, E> {
    public SimpleBarycenterSetup(Random random) {
        super(random);
    }

    public SimpleBarycenterSetup() {
        super(new Random(DEFAULT_SEED));
    }

    @Override
    public void run(LayoutContext<V, E> layoutContext) {
        int nbUnknownPositions = layoutContext.getSimpleGraph().vertexSet().size() - layoutContext.getInitialPoints().size();

        // Initialize the missing positions by use the default random number generator.
        // Apply a scale depending on the number of unknown positions to have an expected mean distance remain around the same value.
        // The positions are around the center of given initial positions.
        this.scale = Math.sqrt(nbUnknownPositions) * 5;
        Vector2D initialPointsCenter = new Vector2D();
        if (!layoutContext.getInitialPoints().isEmpty()) {
            layoutContext.getInitialPoints().values().stream()
                    .map(Point::getPosition)
                    .forEach(initialPointsCenter::add);
            initialPointsCenter.divideBy(layoutContext.getInitialPoints().size());
        }
        this.center = initialPointsCenter;
        super.run(layoutContext);
    }
}
