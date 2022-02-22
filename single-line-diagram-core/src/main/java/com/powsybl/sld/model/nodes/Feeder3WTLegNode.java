/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.nodes;

import com.powsybl.sld.library.ComponentTypeName;
import com.powsybl.sld.model.VoltageLevelInfos;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class Feeder3WTLegNode extends FeederTwtLegNode {

    public Feeder3WTLegNode(String id, String name, String equipmentId, Side side, VoltageLevelInfos myVoltageLevelInfos, VoltageLevelInfos otherSideVoltageLevelInfos) {
        super(id, name, equipmentId, ComponentTypeName.THREE_WINDINGS_TRANSFORMER_LEG, side, myVoltageLevelInfos, otherSideVoltageLevelInfos,
                FeederType.THREE_WINDINGS_TRANSFORMER_LEG);
    }
}
