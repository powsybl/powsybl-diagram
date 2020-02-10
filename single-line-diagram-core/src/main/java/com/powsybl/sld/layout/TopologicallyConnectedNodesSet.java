/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;


import com.powsybl.sld.model.Node;
import com.powsybl.sld.model.SwitchNode;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a set of nodes for which for any couple of nodes exists at least one path being connected together AND
 * having no open SwitchNode. The nodeSet holds the connected set of nodes. borderSwitchNodesSet holds the SwitchNode
 * that are open and for which 1 adjacent node is in the nodeSet, and the other is not.

 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

public class TopologicallyConnectedNodesSet {

    private Set<Node> nodesSet;
    private Set<SwitchNode> borderSwitchNodesSet;

    TopologicallyConnectedNodesSet() {
        nodesSet = new HashSet();
        borderSwitchNodesSet = new HashSet();
    }

    TopologicallyConnectedNodesSet(Collection<Node> nodes, Collection<SwitchNode> borderSwitchNodes) {
        nodesSet = new HashSet(nodes);
        borderSwitchNodesSet = new HashSet(borderSwitchNodes);
    }

    public Set getNodesSet() {
        return new HashSet(nodesSet);
    }

    public void setNodesSet(Set nodesSet) {
        this.nodesSet = nodesSet;
    }

    public Set<SwitchNode> getBorderSwitchNodesSet() {
        return new HashSet<>(borderSwitchNodesSet);
    }

    public void setBorderSwitchNodesSet(Set<SwitchNode> borderSwitchNodesSet) {
        this.borderSwitchNodesSet = borderSwitchNodesSet;
    }

}
