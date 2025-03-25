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
 * Enables the configuration of content displayed in the NAD for branches and three-winding-transformers (labels, arrows direction),
 * and VL info boxes (voltage levels and buses labels).
 * <p>
 * Customizations are defined in the constructor's map parameters.
 *
 * <p>
 * The branchLabels map defines what will be displayed on the branches, and it is indexed by branch ID.
 * The custom content is declared via the BranchLabels record: side1 and side2 are the labels displayed on the edges of a branch,
 * while middle is the label displayed halfway along the branch.
 * Arrow1 and arrow2 determine the direction of the arrows displayed on the edges.
 *
 * <p>
 * The threeWtLabels map defines what will be displayed on the three-winding-transformers legs, and it is indexed by the equipment ID.
 * The custom content is declared via the ThreeWtLabels record: side1, side2, and side3 are the labels to be displayed on the respective transformer's legs.
 * Similarly, arrow1, arrow2, and arrow3 determine the direction of the arrows displayed on the respective transformer's legs.
 *
 * <p>
 * The busDescriptions map is indexed by the ID of the bus (in the bus view) and allows to set a bus's label, displayed in the VL info box central section.
 *
 * <p>
 * VlDescriptions and vlDetails maps, indexed by the voltage level ID, define the VL related content found in the VL info boxes.
 * VlDescriptions data will be displayed in the VL info box top section, while vlDetails will be displayed in the bottom section.
 * For each ID, the maps contain a list of strings that will be displayed sequentially, following the implicit order of the list.
 *
 * @author Christian Biasuzzi {@literal <christian.biasuzzi at soft.it>}
 */
public class CustomLabelProvider implements LabelProvider {
    final Map<String, BranchLabels> branchLabels;
    final Map<String, ThreeWtLabels> threeWtLabels;
    final Map<String, String> busDescriptions;
    final Map<String, List<String>> vlDescriptions;
    final Map<String, List<String>> vlDetails;

    public record BranchLabels(String side1, String middle, String side2, EdgeInfo.Direction arrow1, EdgeInfo.Direction arrow2) {
    }

    public record ThreeWtLabels(String side1, String side2, String side3, EdgeInfo.Direction arrow1, EdgeInfo.Direction arrow2, EdgeInfo.Direction arrow3) {
    }

    public CustomLabelProvider(Map<String, BranchLabels> branchLabels, Map<String, ThreeWtLabels> threeWtLabels,
                               Map<String, String> busDescriptions, Map<String, List<String>> vlDescriptions, Map<String, List<String>> vlDetails) {
        this.branchLabels = Objects.requireNonNull(branchLabels);
        this.threeWtLabels = Objects.requireNonNull(threeWtLabels);
        this.busDescriptions = Objects.requireNonNull(busDescriptions);
        this.vlDescriptions = Objects.requireNonNull(vlDescriptions);
        this.vlDetails = Objects.requireNonNull(vlDetails);
    }

    @Override
    public Optional<EdgeInfo> getEdgeInfo(Graph graph, BranchEdge edge, BranchEdge.Side side) {
        BranchLabels bl = this.branchLabels.get(edge.getEquipmentId());
        String label = null;
        EdgeInfo.Direction arrowDirection = null;
        if (bl != null) {
            label = side == BranchEdge.Side.ONE ? bl.side1 : bl.side2;
            arrowDirection = side == BranchEdge.Side.ONE ? bl.arrow1 : bl.arrow2;
        }
        return Optional.of(new EdgeInfo("Custom", arrowDirection, null, label));
    }

    @Override
    public Optional<EdgeInfo> getEdgeInfo(Graph graph, ThreeWtEdge edge) {
        ThreeWtLabels threeWtLabels1 = threeWtLabels.get(edge.getEquipmentId());
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
        BranchLabels bl = branchLabels.get(edge.getEquipmentId());
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
