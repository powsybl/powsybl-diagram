/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.model;

import com.powsybl.nad.build.iidm.IdProvider;

import java.util.Collections;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class BoundaryBusNode extends BusNode {

    public BoundaryBusNode(IdProvider idProvider, String equipmentId) {
        super(idProvider.createSvgId(equipmentId), equipmentId, Collections.emptyList(), "");
    }

}
