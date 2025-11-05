/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public abstract class AbstractNode extends AbstractIdentifiable implements Node {

    private int width;
    private int height;
    private Point position;
    private final boolean fictitious;

    protected AbstractNode(String svgId, String equipmentId, String name, boolean fictitious) {
        super(svgId, equipmentId, name);
        position = new Point();
        width = 0;
        height = 0;
        this.fictitious = fictitious;
    }

    @Override
    public void setPosition(Point position) {
        this.position = position;
    }

    @Override
    public void setPosition(double x, double y) {
        position = new Point(x, y);
    }

    @Override
    public Point getPosition() {
        return position;
    }

    @Override
    public double getX() {
        return position.x();
    }

    @Override
    public double getY() {
        return position.y();
    }

    @Override
    public boolean isFictitious() {
        return fictitious;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
