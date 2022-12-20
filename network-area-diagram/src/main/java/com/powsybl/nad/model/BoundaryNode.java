/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.model;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class BoundaryNode extends VoltageLevelNode {

    public BoundaryNode(String diagramId, String equipmentId, String nameOrId) {
        super(diagramId, equipmentId, nameOrId, false);
    }

}
