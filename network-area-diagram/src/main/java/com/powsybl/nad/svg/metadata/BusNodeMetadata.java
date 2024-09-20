/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BusNodeMetadata extends AbstractMetadataItem {

    private final int nbNeighbours;
    private final int index;
    private final String vlNodeId;

    public BusNodeMetadata(@JsonProperty("svgId") String svgId,
                           @JsonProperty("equipmentId") String equipmentId,
                           @JsonProperty("nbNeighbours") int nbNeighbours,
                           @JsonProperty("index") int index,
                           @JsonProperty("vlNode") String vlNodeId) {
        super(svgId, equipmentId);
        this.nbNeighbours = nbNeighbours;
        this.index = index;
        this.vlNodeId = vlNodeId;
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
}
