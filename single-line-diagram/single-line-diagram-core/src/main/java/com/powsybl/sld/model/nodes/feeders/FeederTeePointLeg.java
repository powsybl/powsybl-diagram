/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.model.nodes.feeders;

import com.powsybl.sld.model.graphs.VoltageLevelInfos;
import com.powsybl.sld.model.nodes.FeederType;
import com.powsybl.sld.model.nodes.NodeSide;

/**
 * @author Giovanni Ferrari {@literal <giovani.ferrari at soft.it>}
 */
public class FeederTeePointLeg extends FeederTwLeg {
    public FeederTeePointLeg(FeederType feederType, NodeSide side, VoltageLevelInfos myVoltageLevelInfos, VoltageLevelInfos otherSideVoltageLevelInfos) {
        super(feederType, side, myVoltageLevelInfos, otherSideVoltageLevelInfos);
    }
}
