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

    private final String busId;

    private final double v;

    private final double angle;

    private final String userDefinedId;

    public ElectricalNodeInfo(String busId, double v, double angle) {
        this(busId, v, angle, null);
    }

    public ElectricalNodeInfo(String busId, double v, double angle, String userDefinedId) {
        this.busId = busId;
        this.v = v;
        this.angle = angle;
        this.userDefinedId = userDefinedId;
    }

    public double getV() {
        return v;
    }

    public double getAngle() {
        return angle;
    }

    public String getUserDefinedId() {
        return userDefinedId;
    }

    public String getBusId() {
        return busId;
    }
}
