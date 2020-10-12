/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.sld.svg;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ElectricalNodeInfo {
    private double v;
    private double angle;
    String color;

    public ElectricalNodeInfo(double v, double angle, String color) {
        this.v = v;
        this.angle = angle;
        this.color = color;
    }

    public double getV() {
        return v;
    }

    public double getAngle() {
        return angle;
    }

    public String getColor() {
        return color;
    }
}
