/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.nodes;

import java.util.Objects;

import com.powsybl.sld.model.graphs.VoltageLevelInfos;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class Middle2WTNode extends MiddleTwtNode {

    public Middle2WTNode(String id, String name, VoltageLevelInfos voltageLevelInfosLeg1, VoltageLevelInfos voltageLevelInfosLeg2, String componentType) {
        super(id, name,
            new VoltageLevelInfos[]{Objects.requireNonNull(voltageLevelInfosLeg1), Objects.requireNonNull(voltageLevelInfosLeg2) },
            componentType);
    }
}
