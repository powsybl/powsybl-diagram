/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.model;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class Injection extends AbstractIdentifiable {

    public enum Type {
        LOAD,
        SHUNT_COMPENSATOR,
        DANGLING_LINE,
        STATIC_VAR_COMPENSATOR,
        HVDC_CONVERTER_STATION,
        GENERATOR,
        GROUND,
        BATTERY
    }

    private final Type type;
    private final double p;
    private final double q;
    private double angle;

    public Injection(String diagramId, String equipmentId, String nameOrId, Type type, double p, double q) {
        super(diagramId, equipmentId, nameOrId);
        this.type = type;
        this.p = p;
        this.q = q;
    }

    public Type getType() {
        return type;
    }

    public double getP() {
        return p;
    }

    public double getQ() {
        return q;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }
}
