/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg.iidm;

import com.powsybl.iidm.network.Network;
import com.powsybl.nad.model.BranchEdge;
import com.powsybl.nad.model.BusNode;
import com.powsybl.nad.model.ThreeWtEdge;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi at soft.it>}
 */
public class CustomStyleProvider extends TopologicalStyleProvider {

    final Map<String, CustomBusNodeStyles> busNodesStyles;
    final Map<String, CustomEdgeStyles> edgesStyles;
    final Map<String, CustomThreeWtStyles> threeWtsStyles;

    public record CustomBusNodeStyles(String fill, String edge, String edgeWidth) {
    }

    public record CustomEdgeStyles(String edge1, String width1, String dash1, String edge2, String width2,
                                   String dash2) {
    }

    public record CustomThreeWtStyles(String edge1, String width1, String dash1, String edge2, String width2,
                                      String dash2, String edge3, String width3, String dash3) {
    }

    private record CustomEdgeStyle(String stroke, String strokeWidth, String dash) {
    }

    public CustomStyleProvider(Network network, Map<String, CustomBusNodeStyles> busNodesStyles, Map<String, CustomEdgeStyles> edgesStyles,
                               Map<String, CustomThreeWtStyles> threeWtsStyles) {
        super(network);
        this.busNodesStyles = Objects.requireNonNull(busNodesStyles);
        this.edgesStyles = Objects.requireNonNull(edgesStyles);
        this.threeWtsStyles = Objects.requireNonNull(threeWtsStyles);
    }

    @Override
    public String getBusNodeStyle(BusNode busNode) {
        CustomBusNodeStyles style = busNodesStyles.get(busNode.getEquipmentId());
        if (style != null) {
            List<String> parts = new ArrayList<>();
            if (style.fill() != null) {
                parts.add(String.format("background:%s; fill:%s;", style.fill(), style.fill()));
            }
            if (style.edge() != null) {
                parts.add(String.format("stroke:%s;", style.edge()));
                parts.add(String.format("border: solid %s %s;", style.edge(), style.edgeWidth() != null ? style.edgeWidth() : "1px"));
            }
            if (style.edgeWidth() != null) {
                parts.add(String.format("stroke-width:%s;", style.edgeWidth()));
            }
            return parts.isEmpty() ? null : String.join(" ", parts);
        }
        return null;
    }

    private CustomEdgeStyle getEdgeStyle(CustomEdgeStyles styles, BranchEdge.Side side) {
        return (side == BranchEdge.Side.ONE)
                ? new CustomEdgeStyle(styles.edge1(), styles.width1(), styles.dash1())
                : new CustomEdgeStyle(styles.edge2(), styles.width2(), styles.dash2());
    }

    private String formatEdgeStyle(CustomEdgeStyle lineStyle) {
        return Stream.of(
                        Optional.ofNullable(lineStyle.stroke()).map(stroke -> String.format("stroke:%s;", stroke)).orElse(null),
                        Optional.ofNullable(lineStyle.strokeWidth()).map(width -> String.format("stroke-width:%s;", width)).orElse(null),
                        Optional.ofNullable(lineStyle.dash()).map(dash -> String.format("stroke-dasharray:%s;", dash)).orElse(null)
                )
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
    }

    private CustomEdgeStyle getThreeWtStyle(CustomThreeWtStyles styles, ThreeWtEdge.Side side) {
        return switch (side) {
            case ONE -> new CustomEdgeStyle(styles.edge1(), styles.width1(), styles.dash1());
            case TWO -> new CustomEdgeStyle(styles.edge2(), styles.width2(), styles.dash2());
            case THREE -> new CustomEdgeStyle(styles.edge3(), styles.width3(), styles.dash3());
        };
    }

    @Override
    public String getSideEdgeStyle(BranchEdge edge, BranchEdge.Side side) {
        return Optional.ofNullable(edgesStyles.get(edge.getEquipmentId()))
                .map(styles -> formatEdgeStyle(getEdgeStyle(styles, side)))
                .orElse(null);
    }

    @Override
    public String getThreeWtEdgeStyle(ThreeWtEdge threeWtEdge) {
        ThreeWtEdge.Side side = threeWtEdge.getSide();
        return Optional.ofNullable(threeWtsStyles.get(threeWtEdge.getEquipmentId()))
                .map(styles -> formatEdgeStyle(getThreeWtStyle(styles, side)))
                .orElse(null);
    }
}
