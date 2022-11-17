/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class Padding {

    private double left;
    private double top;
    private double right;
    private double bottom;

    public Padding(double left, double top, double right, double bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public Padding(double horizontal, double vertical) {
        this.left = horizontal;
        this.top = vertical;
        this.right = horizontal;
        this.bottom = vertical;
    }

    public Padding(double padding) {
        this.left = padding;
        this.top = padding;
        this.right = padding;
        this.bottom = padding;
    }

    public double getLeft() {
        return left;
    }

    public void setLeft(double left) {
        this.left = left;
    }

    public double getTop() {
        return top;
    }

    public void setTop(double top) {
        this.top = top;
    }

    public double getRight() {
        return right;
    }

    public void setRight(double right) {
        this.right = right;
    }

    public double getBottom() {
        return bottom;
    }

    public void setBottom(double bottom) {
        this.bottom = bottom;
    }
}

