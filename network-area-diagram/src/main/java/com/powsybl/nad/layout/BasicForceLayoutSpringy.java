/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.layout;

import com.powsybl.diagram.util.forcelayout.AbstractForceLayout;
import com.powsybl.diagram.util.forcelayout.ForceLayoutSpringy;
import com.powsybl.nad.model.Edge;
import com.powsybl.nad.model.Node;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class BasicForceLayoutSpringy extends AbstractBasicForceLayout {

    private static final int SCALE = 100;

    @Override
    protected int getScale() {
        return SCALE;
    }

    @Override
    protected AbstractForceLayout<Node, Edge> getForceLayoutAlgorithm(org.jgrapht.Graph<Node, Edge> jgraphtGraph, LayoutParameters layoutParameters) {
        ForceLayoutSpringy<Node, Edge> forceLayout = new ForceLayoutSpringy<>(jgraphtGraph);
        forceLayout.setSpringRepulsionFactor(layoutParameters.getSpringRepulsionFactorForceLayout());
        return forceLayout;
    }

}
