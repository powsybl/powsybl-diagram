/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.model;

import com.powsybl.nad.build.iidm.IdProvider;

import java.util.List;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class BoundaryNode extends VoltageLevelNode {

    public BoundaryNode(IdProvider idProvider, String equipmentId, String nameOrId) {
        super(idProvider.createSvgId(equipmentId), equipmentId, nameOrId, false, true, List.of(),
                null, null, List.of(), List.of());
    }

}
