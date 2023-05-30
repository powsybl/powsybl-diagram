/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.model.nodes;

import com.powsybl.sld.model.coordinate.Orientation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class Internal2WTNode extends EquipmentNode {

    Map<String, Boolean> connectionToBus;

    public Internal2WTNode(String id, String nameOrId, String componentType, Map<String, Boolean> connectionToBus) {
        super(NodeType.INTERNAL, id, nameOrId, id, componentType, false);
        this.connectionToBus = new HashMap<>(connectionToBus);
    }

    @Override
    public void setOrientationFromBlock(Orientation blockOrientation, List<Node> blockNodes) {
        Node node0 = getAdjacentNodes().get(0);
        Node node1 = getAdjacentNodes().get(1);
        for (Node blockNode : blockNodes) {
            if (blockNode == node0 || blockNode == node1) {
                setOrientation(blockNode == node0 ? blockOrientation : blockOrientation.opposite());
                break;
            }
        }
    }

    public boolean connectedToBus(String busId) {
        return connectionToBus.get(busId);
    }
}
