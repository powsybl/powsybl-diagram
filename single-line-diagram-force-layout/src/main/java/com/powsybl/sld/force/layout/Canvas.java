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
    private final int width;
    private final int height;

    public Canvas(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Vector toScreen(BoundingBox boundingBox, Vector position) {
        double screenX = (position.getX() - boundingBox.getLeft()) / boundingBox.getWidth() * width;
        double screenY = (position.getY() - boundingBox.getTop()) / boundingBox.getHeight() * height;
        return new Vector(screenX, screenY);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
