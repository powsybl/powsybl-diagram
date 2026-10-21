/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.coordinate;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public enum Orientation {
    UP,
    DOWN,
    LEFT,
    RIGHT,
    MIDDLE,
    UNDEFINED;

    public boolean isVertical() {
        return this == UP || this == DOWN;
    }

    public boolean isHorizontal() {
        return this == LEFT || this == RIGHT;
    }

    public int progressionSign() {
        return switch (this) {
            case UP, LEFT -> -1;
            case DOWN, RIGHT -> 1;
            default -> 0;
        };
    }

    public Orientation opposite() {
        return switch (this) {
            case UP -> DOWN;
            case DOWN -> UP;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
            case MIDDLE -> MIDDLE;
            case UNDEFINED -> UNDEFINED;
        };
    }

    public double toRotationAngle() {
        return switch (this) {
            case DOWN -> 180.0;
            case LEFT -> 270.0;
            case RIGHT -> 90.0;
            default -> 0;
        };
    }

}
