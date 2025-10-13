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

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EdgeMetadata extends AbstractMetadataItem {

    private final String node1SvgId;
    private final String node2SvgId;
    private final String busNode1SvgId;
    private final String busNode2SvgId;
    private final String edgeType;
    private final EdgeInfoMetadata edgeInfo1;
    private final EdgeInfoMetadata edgeInfo2;

    public EdgeMetadata(@JsonProperty("svgId") String svgId,
                        @JsonProperty("equipmentId") String equipmentId,
                        @JsonProperty("node1") String node1SvgId,
                        @JsonProperty("node2") String node2SvgId,
                        @JsonProperty("busNode1") String busNode1SvgId,
                        @JsonProperty("busNode2") String busNode2SvgId,
                        @JsonProperty("type") String edgeType,
                        @JsonProperty("edgeInfo1") EdgeInfoMetadata edgeInfo1,
                        @JsonProperty("edgeInfo2") EdgeInfoMetadata edgeInfo2) {
        super(svgId, equipmentId);
        this.node1SvgId = node1SvgId;
        this.node2SvgId = node2SvgId;
        this.busNode1SvgId = busNode1SvgId;
        this.busNode2SvgId = busNode2SvgId;
        this.edgeType = edgeType;
        this.edgeInfo1 = edgeInfo1;
        this.edgeInfo2 = edgeInfo2;
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

    @JsonProperty("edgeInfo1")
    public EdgeInfoMetadata getEdgeInfo1() {
        return edgeInfo1;
    }

    @JsonProperty("edgeInfo2")
    public EdgeInfoMetadata getEdgeInfo2() {
        return edgeInfo2;
    }
}
