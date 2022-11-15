/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public abstract class AbstractNode extends AbstractIdentifiable implements Node {

    private int width;
    private int height;
    private Point position;

    protected AbstractNode(String diagramId, String equipmentId, String name) {
        super(diagramId, equipmentId, name);
        position = new Point();
        width = 0;
        height = 0;
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
        return position.getX();
    }

    @Override
    public double getY() {
        return position.getY();
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
