/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.nodes;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public enum NodeSide {
    ONE(1),
    TWO(2),
    THREE(3);

    int intValue;

    NodeSide(int intValue) {
        this.intValue = intValue;
    }

    public int getIntValue() {
        return intValue;
    }

}
