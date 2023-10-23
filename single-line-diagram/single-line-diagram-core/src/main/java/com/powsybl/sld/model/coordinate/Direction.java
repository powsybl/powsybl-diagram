
/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.coordinate;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 */
public enum Direction {
    TOP, BOTTOM, MIDDLE, UNDEFINED;

    public Orientation toOrientation() {
        switch (this) {
            case TOP:
                return Orientation.UP;
            case BOTTOM:
                return Orientation.DOWN;
            case MIDDLE:
                return Orientation.MIDDLE;
            default:
                return Orientation.UNDEFINED;
        }
    }
}
