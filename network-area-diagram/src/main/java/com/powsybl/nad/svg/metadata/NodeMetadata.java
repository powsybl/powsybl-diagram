/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.nad.model.Point;

import java.util.List;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NodeMetadata extends AbstractMetadataItem {

    private final double x;
    private final double y;
    private final boolean fictitious;
    private final boolean invisible;
    private final String legendSvgId;
    private final String legendEdgeSvgId;
    private final List<String> legendHeader;
    private final List<String> legendFooter;

    public NodeMetadata(@JsonProperty("svgId") String svgId,
                        @JsonProperty("equipmentId") String equipmentId,
                        @JsonProperty("x") double x,
                        @JsonProperty("y") double y,
                        @JsonProperty("fictitious") boolean fictitious,
                        @JsonProperty("visible") boolean invisible,
                        @JsonProperty("legendSvgId") String legendSvgId,
                        @JsonProperty("legendEdgeSvgId") String legendEdgeSvgId,
                        @JsonProperty("legendHeader") List<String> legendHeader,
                        @JsonProperty("legendFooter") List<String> legendFooter) {
        super(svgId, equipmentId);
        this.x = x;
        this.y = y;
        this.fictitious = fictitious;
        this.invisible = invisible;
        this.legendSvgId = legendSvgId;
        this.legendEdgeSvgId = legendEdgeSvgId;
        this.legendHeader = legendHeader;
        this.legendFooter = legendFooter;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @JsonProperty("fictitious")
    public boolean isFictitious() {
        return fictitious;
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @JsonProperty("invisible")
    public boolean isInvisible() {
        return invisible;
    }

    @JsonIgnore
    public Point getPosition() {
        return new Point(x, y);
    }

    @JsonProperty("legendSvgId")
    public String getLegendSvgId() {
        return legendSvgId;
    }

    @JsonProperty("legendEdgeSvgId")
    public String getLegendEdgeSvgId() {
        return legendEdgeSvgId;
    }

    @JsonProperty("legendHeader")
    public List<String> getLegendHeader() {
        return legendHeader;
    }

    @JsonProperty("legendFooter")
    public List<String> getLegendFooter() {
        return legendFooter;
    }
}
