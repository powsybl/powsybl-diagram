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
public class ThreeWtEdgeMetadata extends AbstractMetadataItem {

    private final String vlNodeSvgId;
    private final String threeWtNodeSvgId;
    private final String busNodeSvgId;
    private final String edgeType;
    private final EdgeInfoMetadata edgeInfo;

    public ThreeWtEdgeMetadata(@JsonProperty("svgId") String svgId,
                               @JsonProperty("equipmentId") String equipmentId,
                               @JsonProperty("threeWtNode") String threeWtNodeSvgId,
                               @JsonProperty("vlNode") String vlNodeSvgId,
                               @JsonProperty("busNode") String busNodeSvgId,
                               @JsonProperty("type") String edgeType,
                               @JsonProperty("edgeInfo") EdgeInfoMetadata edgeInfo) {
        super(svgId, equipmentId);
        this.vlNodeSvgId = vlNodeSvgId;
        this.threeWtNodeSvgId = threeWtNodeSvgId;
        this.busNodeSvgId = busNodeSvgId;
        this.edgeType = edgeType;
        this.edgeInfo = edgeInfo;
    }

    @JsonProperty("threeWtNode")
    public String getThreeWtNodeSvgId() {
        return threeWtNodeSvgId;
    }

    @JsonProperty("vlNode")
    public String getVlNodeSvgId() {
        return vlNodeSvgId;
    }

    @JsonProperty("busNode")
    public String getBusNodeSvgId() {
        return busNodeSvgId;
    }

    @JsonProperty("type")
    public String getEdgeType() {
        return edgeType;
    }

    @JsonProperty("edgeInfo")
    public EdgeInfoMetadata getEdgeInfo() {
        return edgeInfo;
    }
}
