/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.powsybl.sld.library.ComponentTypeName;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class FeederLegNode extends FeederWithSideNode {

    private final VoltageLevelInfos voltageLevelInfos;

    public FeederLegNode(String id, String name, String equipmentId, Graph graph, Side side) {
        this(id, name, equipmentId, graph, side, null);
    }

    public FeederLegNode(String id, String name, String equipmentId, Graph graph, Side side, VoltageLevelInfos voltageLevelInfos) {
        super(id, name, equipmentId, ComponentTypeName.LINE, false, graph, side);
        this.voltageLevelInfos = voltageLevelInfos;
    }

    public VoltageLevelInfos getVoltageLevelInfos() {
        if (voltageLevelInfos != null) {
            return voltageLevelInfos;
        }
        return super.getVoltageLevelInfos();
    }
}
