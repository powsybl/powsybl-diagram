/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.Node;

import java.util.Set;

/**
 * Represents a connected set of nodes if considering the borderNodes as disconnected:
 * for any couple of nodes in <code>nodess</code> there is at least one path connecting them together WITHOUT passing
 * through any nodes of <code>borderNodes</code>.
 * The <code>nodes</code> holds the connected set of nodes.
 * The <code>borderNodes</code> holds the nodes that are at the border of this set, that is for which 1 adjacent node
 * (at least) is in the nodeSet, and for which 1 other adjacent node (at least) is not.
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public class TopologicallyConnectedNodesSet {

    private final Set<Node> nodes;

    private final Set<Node> borderNodes;

    TopologicallyConnectedNodesSet(Set<Node> nodes, Set<Node> borderSwitchNodes) {
        this.nodes = nodes;
        this.borderNodes = borderSwitchNodes;
    }

    public Set<Node> getNodes() {
        return nodes;
    }

    public Set<Node> getBorderNodes() {
        return borderNodes;
    }
}
