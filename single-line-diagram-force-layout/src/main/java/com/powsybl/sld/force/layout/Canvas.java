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
public class Canvas {
    private int width;
    private int height;

    public Canvas(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Vector toScreen(BoundingBox boundingBox, Vector position) {
        Vector size = boundingBox.getTopRight().subtract(boundingBox.getBottomLeft());

        double screenX = position.subtract(boundingBox.getBottomLeft()).divide(size.getX()).getX() * width;
        double screenY = position.subtract(boundingBox.getBottomLeft()).divide(size.getY()).getY() * height;

        return new Vector(screenX, screenY);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
