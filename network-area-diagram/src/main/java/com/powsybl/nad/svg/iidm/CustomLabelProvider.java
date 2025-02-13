/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg.iidm;

import com.powsybl.iidm.network.Network;
import com.powsybl.nad.model.*;
import com.powsybl.nad.svg.EdgeInfo;
import com.powsybl.nad.svg.SvgParameters;

import java.util.*;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi at soft.it>}
 */
public class CustomLabelProvider extends DefaultLabelProvider {
    final Map<String, CustomBranchLabels> branchLabels;

    public record CustomBranchLabels(String side1, String middle, String side2, EdgeInfo.Direction arrow1, EdgeInfo.Direction arrow2) {
    }

    public CustomLabelProvider(Network network, SvgParameters svgParameters, Map<String, CustomBranchLabels> branchLabels) {
        super(network, svgParameters);
        this.branchLabels = Objects.requireNonNull(branchLabels);
    }

    @Override
    public Optional<EdgeInfo> getEdgeInfo(Graph graph, BranchEdge edge, BranchEdge.Side side) {
        CustomBranchLabels customBranchLabels = branchLabels.get(edge.getEquipmentId());
        String label = null;
        EdgeInfo.Direction arrowDirection = null;
        if (customBranchLabels != null) {
            label = side == BranchEdge.Side.ONE ? customBranchLabels.side1 : customBranchLabels.side2;
            arrowDirection = side == BranchEdge.Side.ONE ? customBranchLabels.arrow1 : customBranchLabels.arrow2;
        }
        return Optional.of(new EdgeInfo("Custom", arrowDirection, null, label));
    }

    @Override
    public String getLabel(Edge edge) {
        CustomBranchLabels bl = branchLabels.get(edge.getEquipmentId());
        return (bl != null) ? bl.middle : null;
    }
}
