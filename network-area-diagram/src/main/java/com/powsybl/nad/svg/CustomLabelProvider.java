/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg;

import com.powsybl.nad.model.BranchEdge;
import com.powsybl.nad.model.ThreeWtEdge;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
    public static final String INFO_TYPE = "Custom";
    final Map<String, BranchLabels> branchLabels;
    final Map<String, ThreeWtLabels> threeWtLabels;
    final Map<String, InjectionLabels> injectionLabels;
    final Map<String, VoltageLevelLegend> vlLegends;

    public record BranchLabels(String side1Internal, String side1External,
                               String middle1, String middle2,
                               String side2Internal, String side2External,
                               EdgeInfo.Direction arrow1, EdgeInfo.Direction arrowMiddle, EdgeInfo.Direction arrow2) {
    }

    public record ThreeWtLabels(String side1Internal, String side1External,
                                String side2Internal, String side2External,
                                String side3Internal, String side3External,
                                EdgeInfo.Direction arrow1, EdgeInfo.Direction arrow2, EdgeInfo.Direction arrow3) {
    }

    public record InjectionLabels(String labelInternal, String labelExternal, EdgeInfo.Direction arrow) {
    }

    public CustomLabelProvider(Map<String, BranchLabels> branchLabels, Map<String, ThreeWtLabels> threeWtLabels, Map<String, InjectionLabels> injectionLabels,
                               Map<String, VoltageLevelLegend> vlLegends) {
        this.branchLabels = Objects.requireNonNull(branchLabels);
        this.threeWtLabels = Objects.requireNonNull(threeWtLabels);
        this.injectionLabels = Objects.requireNonNull(injectionLabels);
        this.vlLegends = Objects.requireNonNull(vlLegends);
    }

    @Override
    public Optional<EdgeInfo> getBranchEdgeInfo(String branchId, BranchEdge.Side side, String branchType) {
        BranchLabels bl = this.branchLabels.get(branchId);
        return Optional.ofNullable(bl).map(bl1 -> getEdgeInfo(bl1, side));
    }

    private EdgeInfo getEdgeInfo(BranchLabels bl, BranchEdge.Side side) {
        return switch (side) {
            case ONE -> new EdgeInfo(INFO_TYPE, INFO_TYPE, bl.arrow1, bl.side1Internal, bl.side1External);
            case TWO -> new EdgeInfo(INFO_TYPE, INFO_TYPE, bl.arrow2, bl.side2Internal, bl.side2External);
        };
    }

    @Override
    public Optional<EdgeInfo> getThreeWindingTransformerEdgeInfo(String threeWindingTransformerId, ThreeWtEdge.Side side) {
        ThreeWtLabels threeWtLabels1 = threeWtLabels.get(threeWindingTransformerId);
        return Optional.ofNullable(threeWtLabels1).map(lbl -> getEdgeInfo(side, lbl));
    }

    private EdgeInfo getEdgeInfo(ThreeWtEdge.Side edgeSide, ThreeWtLabels labels) {
        return switch (edgeSide) {
            case ONE -> new EdgeInfo(INFO_TYPE, INFO_TYPE, labels.arrow1, labels.side1Internal, labels.side1External);
            case TWO -> new EdgeInfo(INFO_TYPE, INFO_TYPE, labels.arrow2, labels.side2Internal, labels.side2External);
            case THREE -> new EdgeInfo(INFO_TYPE, INFO_TYPE, labels.arrow3, labels.side3Internal, labels.side3External);
        };
    }

    @Override
    public Optional<EdgeInfo> getInjectionEdgeInfo(String injectionId) {
        InjectionLabels injectionLabel = injectionLabels.get(injectionId);
        return Optional.ofNullable(injectionLabel)
            .map(lbl -> new EdgeInfo(INFO_TYPE, INFO_TYPE, lbl.arrow, injectionLabel.labelInternal, lbl.labelExternal));
    }

    @Override
    public String getBranchLabel(String branchId) {
        BranchLabels bl = branchLabels.get(branchId);
        if (bl == null) {
            return null;
        }
        return (bl.middle2 != null) ? bl.middle2 : bl.middle1;
    }

    @Override
    public Optional<EdgeInfo> getBranchEdgeInfo(String branchId, String branchType) {
        BranchLabels bl = branchLabels.get(branchId);
        return Optional.of(new EdgeInfo(INFO_TYPE, INFO_TYPE, bl.arrowMiddle, bl.middle1, bl.middle2));
    }

    @Override
    public VoltageLevelLegend getVoltageLevelLegend(String voltageLevelId) {
        return vlLegends.getOrDefault(voltageLevelId, new VoltageLevelLegend(Collections.emptyList(), Collections.emptyList(), Collections.emptyMap()));
    }
}
