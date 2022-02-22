/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class FeederTwtLegNode extends FeederWithSideNode {
    protected FeederTwtLegNode(String id, String name, String equipmentId, String componentType,
                               Side side, VoltageLevelInfos myVoltageLevelInfos, VoltageLevelInfos otherSideVoltageLevelInfos,
                               FeederType feederType) {
        super(id, name, equipmentId, componentType, side, myVoltageLevelInfos, otherSideVoltageLevelInfos, feederType);
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

