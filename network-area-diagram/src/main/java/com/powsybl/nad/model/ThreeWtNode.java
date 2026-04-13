/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

import com.powsybl.nad.build.iidm.IdProvider;

import java.util.Collections;
import java.util.List;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class ThreeWtNode extends AbstractNode {

    private List<String> styleClasses = Collections.emptyList();

    public ThreeWtNode(IdProvider idProvider, String equipmentId, String nameOrId) {
        super(idProvider.createSvgId(equipmentId), equipmentId, nameOrId, false);
    }

    @Override
    public List<String> getStyleClasses() {
        return styleClasses;
    }

    public void setStyleClasses(List<String> styleClasses) {
        this.styleClasses = styleClasses;
    }
}
