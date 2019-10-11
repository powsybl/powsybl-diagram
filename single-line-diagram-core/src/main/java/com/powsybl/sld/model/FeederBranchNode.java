/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.powsybl.iidm.network.VoltageLevel;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class FeederBranchNode extends FeederNode {

    private VoltageLevel vlOtherSide;

    protected FeederBranchNode(String id, String name, String componentType, boolean fictitious, Graph graph, VoltageLevel vlOtherSide) {
        super(id, name, componentType, fictitious, graph);
        this.vlOtherSide = vlOtherSide;
    }

    public VoltageLevel getVlOtherSide() {
        return vlOtherSide;
    }
}
