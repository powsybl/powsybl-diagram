/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.nodes.feeders;

import com.powsybl.sld.model.graphs.VoltageLevelInfos;
import com.powsybl.sld.model.nodes.FeederType;
import com.powsybl.sld.model.nodes.NodeSide;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class FeederTwLeg extends FeederWithSides {

    public FeederTwLeg(FeederType feederType, String componentType, NodeSide side, VoltageLevelInfos myVoltageLevelInfos, VoltageLevelInfos otherSideVoltageLevelInfos) {
        super(feederType, componentType, side, myVoltageLevelInfos, otherSideVoltageLevelInfos);
    }

    @Override
    public VoltageLevelInfos getVoltageLevelInfos() {
        // we consider this node represent the other side of the transformer so voltage level infos is the other
        // side one
        if (otherSideVoltageLevelInfos != null) {
            return otherSideVoltageLevelInfos;
        }
        return super.getVoltageLevelInfos();
    }
}
