/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg;

import com.powsybl.iidm.network.Network;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.nodes.EquipmentNode;
import com.powsybl.sld.model.nodes.Node;

import java.util.List;
import java.util.Optional;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class DebugDiagramLabelProvider extends DefaultDiagramLabelProvider {

    public DebugDiagramLabelProvider(Network network, ComponentLibrary componentLibrary, LayoutParameters layoutParameters) {
        super(network, componentLibrary, layoutParameters);
    }

    @Override
    public List<NodeLabel> getNodeLabels(Node node, Direction direction) {
        String debugLabel;
        if (node instanceof EquipmentNode) {
            EquipmentNode eqNode = (EquipmentNode) node;
            debugLabel = Optional.ofNullable(node.getLabel().orElse(layoutParameters.isUseName() ? eqNode.getName() : eqNode.getEquipmentId()))
                    .orElse(node.getId());
        } else {
            debugLabel = node.getId();
        }
        return List.of(new NodeLabel(debugLabel, getFeederLabelPosition(node, direction)));
    }
}
