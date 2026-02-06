/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EdgeMetadata extends AbstractMetadataItem {

    private static final int MAX_EDGE_INFOS_PER_SIDE = 2;

    private final String node1SvgId;
    private final String node2SvgId;
    private final String busNode1SvgId;
    private final String busNode2SvgId;
    private final String edgeType;
    private final boolean invisibleSide1;
    private final boolean invisibleSide2;
    private final List<EdgeInfoMetadata> edgeSide1;
    private final List<EdgeInfoMetadata> edgeSide2;
    private final EdgeInfoMetadata edgeInfoMiddle;

    public EdgeMetadata(@JsonProperty("svgId") String svgId,
                        @JsonProperty("equipmentId") String equipmentId,
                        @JsonProperty("node1") String node1SvgId,
                        @JsonProperty("node2") String node2SvgId,
                        @JsonProperty("busNode1") String busNode1SvgId,
                        @JsonProperty("busNode2") String busNode2SvgId,
                        @JsonProperty("type") String edgeType,
                        @JsonProperty("invisible1") boolean invisibleSide1,
                        @JsonProperty("invisible2") boolean invisibleSide2,
                        @JsonProperty("edgeSide1") List<EdgeInfoMetadata> edgeSide1,
                        @JsonProperty("edgeSide2") List<EdgeInfoMetadata> edgeSide2,
                        @JsonProperty("edgeInfoMiddle") EdgeInfoMetadata edgeInfoMiddle) {
        super(svgId, equipmentId);
        this.node1SvgId = node1SvgId;
        this.node2SvgId = node2SvgId;
        this.busNode1SvgId = busNode1SvgId;
        this.busNode2SvgId = busNode2SvgId;
        this.edgeType = edgeType;
        this.invisibleSide1 = invisibleSide1;
        this.invisibleSide2 = invisibleSide2;
        this.edgeSide1 = validateAndCopy(edgeSide1);
        this.edgeSide2 = validateAndCopy(edgeSide2);
        this.edgeInfoMiddle = edgeInfoMiddle;
    }

    private static List<EdgeInfoMetadata> validateAndCopy(List<EdgeInfoMetadata> edgeInfos) {
        if (edgeInfos == null || edgeInfos.isEmpty()) {
            return Collections.emptyList();
        }
        if (edgeInfos.size() > MAX_EDGE_INFOS_PER_SIDE) {
            throw new IllegalArgumentException("Maximum " + MAX_EDGE_INFOS_PER_SIDE + " edge infos allowed per side, but got " + edgeInfos.size());
        }
        return new ArrayList<>(edgeInfos);
    }

    @JsonProperty("node1")
    public String getNode1SvgId() {
        return node1SvgId;
    }

    @JsonProperty("node2")
    public String getNode2SvgId() {
        return node2SvgId;
    }

    @JsonProperty("busNode1")
    public String getBusNode1SvgId() {
        return busNode1SvgId;
    }

    @JsonProperty("busNode2")
    public String getBusNode2SvgId() {
        return busNode2SvgId;
    }

    @JsonProperty("type")
    public String getEdgeType() {
        return edgeType;
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @JsonProperty("invisible1")
    public boolean isInvisibleSide1() {
        return invisibleSide1;
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @JsonProperty("invisible2")
    public boolean isInvisibleSide2() {
        return invisibleSide2;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("edgeSide1")
    public List<EdgeInfoMetadata> getEdgeSide1() {
        return edgeSide1;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("edgeSide2")
    public List<EdgeInfoMetadata> getEdgeSide2() {
        return edgeSide2;
    }

    @JsonProperty("edgeInfoMiddle")
    public EdgeInfoMetadata getEdgeInfoMiddle() {
        return edgeInfoMiddle;
    }
}
