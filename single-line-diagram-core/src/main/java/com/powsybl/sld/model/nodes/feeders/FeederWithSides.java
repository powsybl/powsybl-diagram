/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.nodes.feeders;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.sld.model.graphs.VoltageLevelInfos;
import com.powsybl.sld.model.nodes.FeederType;
import com.powsybl.sld.model.nodes.NodeSide;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class FeederWithSides extends BaseFeeder {

    protected final NodeSide side;

    protected final VoltageLevelInfos myVoltageLevelInfos;
    protected final VoltageLevelInfos otherSideVoltageLevelInfos;

    public FeederWithSides(FeederType feederType, String componentType, NodeSide side, VoltageLevelInfos myVoltageLevelInfos, VoltageLevelInfos otherSideVoltageLevelInfos) {
        super(feederType, componentType);
        this.side = side;
        this.myVoltageLevelInfos = myVoltageLevelInfos;
        this.otherSideVoltageLevelInfos = otherSideVoltageLevelInfos;
    }

    public NodeSide getSide() {
        return side;
    }

// TODO: to remove : circular reference to VoltageLevelInfos in package graphs
    public VoltageLevelInfos getOtherSideVoltageLevelInfos() {
        return otherSideVoltageLevelInfos;
    }

    public VoltageLevelInfos getVoltageLevelInfos() {
        return myVoltageLevelInfos;
    }

    public void writeJsonContent(JsonGenerator generator) throws IOException {
        generator.writeStringField("side", side.name());
        if (otherSideVoltageLevelInfos != null) {
            generator.writeFieldName("otherSideVoltageLevelInfos");
            otherSideVoltageLevelInfos.writeJsonContent(generator);
        }
    }
}
