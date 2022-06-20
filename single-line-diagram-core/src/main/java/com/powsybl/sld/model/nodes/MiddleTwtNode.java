/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.nodes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.sld.model.graphs.VoltageLevelInfos;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class MiddleTwtNode extends FictitiousNode {
    protected final VoltageLevelInfos[] voltageLevelInfosLeg;

    protected MiddleTwtNode(String id, String name, VoltageLevelInfos[] voltageLevelInfosLeg, String componentType) {
        super(id, name, id, componentType);
        this.voltageLevelInfosLeg = voltageLevelInfosLeg;
    }

    public VoltageLevelInfos getVoltageLevelInfos() {
        return null; // there is not a unique voltage level infos for a middle point so we consider this is undefined
    }

    public VoltageLevelInfos getVoltageLevelInfos(NodeSide side) {
        Objects.requireNonNull(side);
        int sideIntValue = side.getIntValue();
        if (sideIntValue > voltageLevelInfosLeg.length) {
            throw new IllegalStateException();
        }
        return voltageLevelInfosLeg[sideIntValue - 1];
    }

    @Override
    protected void writeJsonContent(JsonGenerator generator, boolean includeCoordinates) throws IOException {
        super.writeJsonContent(generator, includeCoordinates);
        int side = 1;
        for (VoltageLevelInfos voltageLevelInfos : voltageLevelInfosLeg) {
            generator.writeFieldName("voltageLevelInfosLeg" + (side++));
            voltageLevelInfos.writeJsonContent(generator);
        }
    }
}
