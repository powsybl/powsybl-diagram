/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.library;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.sld.model.coordinate.Orientation;
import com.powsybl.sld.model.coordinate.Point;

import java.util.Objects;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class AnchorPoint extends Point {

    private final AnchorOrientation orientation;

    /**
     * Constructor
     *
     * @param x    abscissa
     * @param y    ordinate
     * @param orientation connection orientation
     */
    @JsonCreator
    public AnchorPoint(@JsonProperty("x") double x,
                       @JsonProperty("y") double y,
                       @JsonProperty("orientation") AnchorOrientation orientation) {
        super(x, y);
        this.orientation = Objects.requireNonNull(orientation);
    }

    public AnchorOrientation getOrientation() {
        return orientation;
    }

    /**
     * Apply transformation on anchorPoints
     */
    public AnchorPoint transformAnchorPoint(Orientation nodeOrientation, Component.Transformation nodeTransformation) {
        if (nodeTransformation == Component.Transformation.ROTATION) {
            if (nodeOrientation.isHorizontal()) {
                switch (this.orientation) {
                    case VERTICAL:
                        return new AnchorPoint(getY(), getX(), AnchorOrientation.HORIZONTAL);
                    case HORIZONTAL:
                        return new AnchorPoint(getY(), getX(), AnchorOrientation.VERTICAL);
                    case NONE:
                        return this;
                    default:
                        throw new AssertionError("Unknown anchor orientation " + orientation);
                }
            } else if (nodeOrientation == Orientation.DOWN) {
                return new AnchorPoint(-getX(), -getY(), this.orientation);
            } else {
                return new AnchorPoint(getX(), getY(), orientation);
            }
        } else if (nodeTransformation == Component.Transformation.FLIP) {
            if (nodeOrientation == Orientation.DOWN) {
                return new AnchorPoint(getX(), -getY(), this.orientation);
            }
        }
        return this;
    }

    @Override
    public String toString() {
        return "AnchorPoint(x=" + getX() + ", y=" + getY() + ", orientation=" + orientation + ")";
    }
}
