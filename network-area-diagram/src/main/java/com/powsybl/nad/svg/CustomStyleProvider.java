/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg;

import com.powsybl.nad.model.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Enables the customization of the style of NAD elements: Bus nodes, branch-edges and three-winding-transformers edges.
 *
 * <p>
 * NAD elements'style data is defined in the CustomStyleProvider constructor's map parameters.
 *
 * <p>
 * The busNodesStyles map is indexed by the bus ID and defines the style for the bus nodes.
 * In the map, a node style is declared in a BusNodeStyles record: fill, edge and edgeWidth are the fill color, the edge color and the edge size for the node, respectively.
 *
 * <p>
 * The edgesStyles map is indexed by the branch ID and defines the style for the edges.
 * In the map, the edge style is declared in a EdgeStyles record: edge1, width1 and dash1 are the color, the size and a dash pattern for the first half edge, respectively.
 * Edge2, width2 and dash2 are the color, the size and a dash pattern for the second half edge.
 *
 * <p>
 * The threeWtsStyles map is index by the three-winding-transformer ID and defines the style for the transformer’s legs.
 * In the map, the style is declared in a ThreeWtStyles record: edge1, width1, dash1, edge2, width2 and dash2, edge3, width23 and dash3,
 * are the color, the size and a dash pattern for the three legs of the transformer.
 *
 * <p>
 * Note that the edge size is a string, it can be specified in pixel (e.g, 4px).
 * A dash pattern is a string with a sequence of comma and/or white space separated lengths and percentages, that specify the lengths of alternating dashes and gaps in the edge.
 * Elements that do not have a style specified in the parameters will be displayed with a default style.
 *
 * @author Christian Biasuzzi {@literal <christian.biasuzzi at soft.it>}
 */
public class CustomStyleProvider extends AbstractStyleProvider {

    final Map<String, BusNodeStyles> busNodesStyles;
    final Map<String, EdgeStyles> edgesStyles;
    final Map<String, ThreeWtStyles> threeWtsStyles;

    public record BusNodeStyles(String fill, String edge, String edgeWidth) {
    }

    public record EdgeStyles(String edge1, String width1, String dash1, String edge2, String width2,
                             String dash2) {
    }

    public record ThreeWtStyles(String edge1, String width1, String dash1, String edge2, String width2,
                                String dash2, String edge3, String width3, String dash3) {
    }

    private record EdgeStyle(String stroke, String strokeWidth, String dash) {
    }

    public CustomStyleProvider(Map<String, BusNodeStyles> busNodesStyles, Map<String, EdgeStyles> edgesStyles,
                               Map<String, ThreeWtStyles> threeWtsStyles) {
        this.busNodesStyles = Objects.requireNonNull(busNodesStyles);
        this.edgesStyles = Objects.requireNonNull(edgesStyles);
        this.threeWtsStyles = Objects.requireNonNull(threeWtsStyles);
    }

    @Override
    public List<String> getCssFilenames() {
        return Collections.singletonList("customStyle.css");
    }

    @Override
    public String getBusNodeStyle(BusNode busNode) {
        BusNodeStyles style = busNodesStyles.get(busNode.getEquipmentId());
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

    private EdgeStyle getEdgeStyle(EdgeStyles styles, BranchEdge.Side side) {
        return (side == BranchEdge.Side.ONE)
                ? new EdgeStyle(styles.edge1(), styles.width1(), styles.dash1())
                : new EdgeStyle(styles.edge2(), styles.width2(), styles.dash2());
    }

    private String formatEdgeStyle(EdgeStyle lineStyle) {
        return Stream.of(
                        Optional.ofNullable(lineStyle.stroke()).map(stroke -> String.format("stroke:%s;", stroke)).orElse(null),
                        Optional.ofNullable(lineStyle.strokeWidth()).map(width -> String.format("stroke-width:%s;", width)).orElse(null),
                        Optional.ofNullable(lineStyle.dash()).map(dash -> String.format("stroke-dasharray:%s;", dash)).orElse(null)
                )
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
    }

    private EdgeStyle getThreeWtStyle(ThreeWtStyles styles, ThreeWtEdge.Side side) {
        return switch (side) {
            case ONE -> new EdgeStyle(styles.edge1(), styles.width1(), styles.dash1());
            case TWO -> new EdgeStyle(styles.edge2(), styles.width2(), styles.dash2());
            case THREE -> new EdgeStyle(styles.edge3(), styles.width3(), styles.dash3());
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

    @Override
    public List<String> getEdgeInfoStyleClasses(EdgeInfo info, String externalInfoType) {
        List<String> styles = new LinkedList<>();
        info.getDirection().ifPresent(direction -> styles.add(
                CLASSES_PREFIX + (direction == EdgeInfo.Direction.OUT ? "state-out" : "state-in")));
        return styles;
    }

    @Override
    public List<String> getHighlightNodeStyleClasses(Node node) {
        return List.of();
    }

    @Override
    public List<String> getHighlightSideEdgeStyleClasses(BranchEdge edge, BranchEdge.Side side) {
        return List.of();
    }

    @Override
    public List<String> getHighlightThreeWtEdgStyleClasses(ThreeWtEdge edge) {
        return List.of();
    }

    @Override
    protected boolean isDisconnected(ThreeWtEdge threeWtEdge) {
        return false;
    }

    @Override
    protected boolean isDisconnected(BranchEdge branchEdge) {
        return false;
    }

    @Override
    protected boolean isDisconnected(BranchEdge edge, BranchEdge.Side side) {
        return false;
    }

    @Override
    protected boolean isDisconnected(Injection injection) {
        return false;
    }

    @Override
    protected Optional<String> getBaseVoltageStyle(Edge edge) {
        return Optional.empty();
    }

    @Override
    protected Optional<String> getBaseVoltageStyle(BranchEdge edge, BranchEdge.Side side) {
        return Optional.empty();
    }

    @Override
    protected Optional<String> getBaseVoltageStyle(ThreeWtEdge threeWtEdge) {
        return Optional.empty();
    }
}
