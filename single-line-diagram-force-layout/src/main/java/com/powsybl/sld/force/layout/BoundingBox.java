/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.force.layout;

/**
 * @author Mathilde Grapin <mathilde.grapin at rte-france.com>
 */
public class BoundingBox {
    private Vector topRight;
    private Vector bottomLeft;

    public BoundingBox(Vector topRight, Vector bottomLeft) {
        this.topRight = topRight;
        this.bottomLeft = bottomLeft;
    }

    public Vector getTopRight() {
        return topRight;
    }

    public Vector getBottomLeft() {
        return bottomLeft;
    }

    public double getHeight() {
        return topRight.getY() - bottomLeft.getY();
    }

    public double getWidth() {
        return topRight.getX() - bottomLeft.getX();
    }
}
