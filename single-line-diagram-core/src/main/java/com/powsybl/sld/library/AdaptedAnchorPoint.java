/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.library;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AdaptedAnchorPoint {

    private double x;

    private double y;

    private AnchorOrientation orientation;

    /**
     * Constructor
     *
     * @param x    abscissa
     * @param y    ordinate
     * @param orientation connection orientation
     */
    @JsonCreator
    public AdaptedAnchorPoint(@JsonProperty("x") double x,
                              @JsonProperty("y") double y,
                              @JsonProperty("orientation") AnchorOrientation orientation) {
        this.x = x;
        this.y = y;
        this.orientation = Objects.requireNonNull(orientation);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public AnchorOrientation getOrientation() {
        return orientation;
    }

    public void setOrientation(AnchorOrientation orientation) {
        this.orientation = orientation;
    }

    @Override
    public String toString() {
        return "AnchorPoint(x=" + x + ", y=" + y + ", orientation=" + orientation + ")";
    }
}
