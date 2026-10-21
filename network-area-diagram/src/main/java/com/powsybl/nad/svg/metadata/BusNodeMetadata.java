/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class BusNodeMetadata extends AbstractMetadataItem {

    private final int nbNeighbours;
    private final int index;
    private final String vlNodeId;
    private final String legend;
    private final List<String> classes;
    private final String style;

    public BusNodeMetadata(@JsonProperty("svgId") String svgId,
                           @JsonProperty("equipmentId") String equipmentId,
                           @JsonProperty("nbNeighbours") int nbNeighbours,
                           @JsonProperty("index") int index,
                           @JsonProperty("vlNode") String vlNodeId,
                           @JsonProperty("legend") String legend,
                           @JsonProperty("classes") List<String> classes,
                           @JsonProperty("style") String style) {
        super(svgId, equipmentId);
        this.nbNeighbours = nbNeighbours;
        this.index = index;
        this.vlNodeId = vlNodeId;
        this.legend = legend;
        this.classes = classes;
        this.style = style;
    }

    @JsonProperty("nbNeighbours")
    public int getNbNeighbours() {
        return nbNeighbours;
    }

    @JsonProperty("index")
    public int getIndex() {
        return index;
    }

    @JsonProperty("vlNode")
    public String getVlNodeId() {
        return vlNodeId;
    }

    @JsonProperty("legend")
    public String getLegend() {
        return legend;
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @JsonProperty("classes")
    public List<String> getClasses() {
        return classes;
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @JsonProperty("style")
    public String getStyle() {
        return style;
    }
}
