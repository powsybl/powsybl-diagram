/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.model.nodes;

import com.powsybl.sld.model.coordinate.Orientation;
import com.powsybl.sld.svg.IdProvider;

import java.util.List;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class Internal2WTNode extends EquipmentNode {
    public Internal2WTNode(String id, String nameOrId, String equipmentId, String componentType) {
        this(id, nameOrId, equipmentId, componentType, new com.powsybl.sld.svg.LegacyIdProvider(""));
    }

    public Internal2WTNode(String id, String nameOrId, String equipmentId, String componentType, IdProvider idProvider) {
        super(NodeType.INTERNAL, id, nameOrId, equipmentId, componentType, false, idProvider);
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
}
