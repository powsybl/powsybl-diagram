/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class FeederBranchNode extends FeederNode {

    private final String vIdOtherSide;

    private final double nominalVOtherSide;

    protected FeederBranchNode(String id, String name, String componentType,
                               boolean fictitious, Graph graph,
                               String vIdOtherSide, double nominalVOtherSide) {
        super(id, name, componentType, fictitious, graph);
        this.vIdOtherSide = vIdOtherSide;
        this.nominalVOtherSide = nominalVOtherSide;
    }

    public String getVIdOtherSide() {
        return vIdOtherSide;
    }

    public double getNominalVOtherSide() {
        return nominalVOtherSide;
    }
}
