/**
 Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.layout;

import com.powsybl.diagram.util.forcelayout.AbstractForceLayout;
import com.powsybl.diagram.util.forcelayout.ForceAtlas2Layout;
import com.powsybl.nad.model.Edge;
import com.powsybl.nad.model.Node;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class BasicForceAtlas2Layout extends AbstractBasicForceLayout {

    private static final int SCALE = 20;

    @Override
    protected int getScale() {
        return SCALE;
    }

    @Override
    protected AbstractForceLayout<Node, Edge> getForceLayoutAlgorithm(org.jgrapht.Graph<Node, Edge> jgraphtGraph, LayoutParameters layoutParameters) {
        // We could complete the ForceAtlas2 object created here
        // setting additional parameters received through the layout parameters
        return new ForceAtlas2Layout<>(jgraphtGraph);
    }
}
