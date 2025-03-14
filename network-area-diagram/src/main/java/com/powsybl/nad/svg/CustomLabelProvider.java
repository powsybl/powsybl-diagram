/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg;

import com.powsybl.nad.model.*;
import com.powsybl.nad.utils.svg.SvgUtils;

import java.util.*;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi at soft.it>}
 */
public class CustomLabelProvider implements LabelProvider {
    final Map<String, CustomBranchLabels> branchLabels;
    final Map<String, CustomThreeWtLabels> threeWtLabels;
    final Map<String, String> busDescriptions;
    final Map<String, List<String>> vlDescriptions;
    final Map<String, List<String>> vlDetails;

    public record CustomBranchLabels(String side1, String middle, String side2, EdgeInfo.Direction arrow1, EdgeInfo.Direction arrow2) {
    }

    public record CustomThreeWtLabels(String side1, String side2, String side3, EdgeInfo.Direction arrow1, EdgeInfo.Direction arrow2, EdgeInfo.Direction arrow3) {
    }

    public CustomLabelProvider(Map<String, CustomBranchLabels> branchLabels, Map<String, CustomThreeWtLabels> threeWtLabels,
                               Map<String, String> busDescriptions, Map<String, List<String>> vlDescriptions, Map<String, List<String>> vlDetails) {
        this.branchLabels = Objects.requireNonNull(branchLabels);
        this.threeWtLabels = Objects.requireNonNull(threeWtLabels);
        this.busDescriptions = Objects.requireNonNull(busDescriptions);
        this.vlDescriptions = Objects.requireNonNull(vlDescriptions);
        this.vlDetails = Objects.requireNonNull(vlDetails);
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
    public Optional<EdgeInfo> getEdgeInfo(Graph graph, ThreeWtEdge edge) {
        CustomThreeWtLabels threeWtLabels1 = threeWtLabels.get(edge.getEquipmentId());
        ThreeWtEdge.Side edgeSide = edge.getSide();
        String labelSide = null;
        EdgeInfo.Direction arrowDirection = null;
        if (threeWtLabels1 != null) {
            switch (edgeSide) {
                case ONE -> {
                    labelSide = threeWtLabels1.side1;
                    arrowDirection = threeWtLabels1.arrow1;
                }
                case TWO -> {
                    labelSide = threeWtLabels1.side2;
                    arrowDirection = threeWtLabels1.arrow2;
                }
                case THREE -> {
                    labelSide = threeWtLabels1.side3;
                    arrowDirection = threeWtLabels1.arrow3;
                }
            }
        }
        return Optional.of(new EdgeInfo("Custom", arrowDirection, null, labelSide));
    }

    @Override
    public String getLabel(Edge edge) {
        CustomBranchLabels bl = branchLabels.get(edge.getEquipmentId());
        return (bl != null) ? bl.middle : null;
    }

    @Override
    public String getBusDescription(BusNode busNode) {
        return busDescriptions.get(busNode.getEquipmentId());
    }

    @Override
    public List<String> getVoltageLevelDescription(VoltageLevelNode voltageLevelNode) {
        return vlDescriptions.getOrDefault(voltageLevelNode.getEquipmentId(), Collections.emptyList());
    }

    @Override
    public List<String> getVoltageLevelDetails(VoltageLevelNode vlNode) {
        return vlDetails.getOrDefault(vlNode.getEquipmentId(), Collections.emptyList());
    }

    @Override
    public String getArrowPathDIn() {
        return SvgUtils.ARROW_PATH_DIN;
    }

    @Override
    public String getArrowPathDOut() {
        return SvgUtils.ARROW_PATH_DOUT;
    }
}
