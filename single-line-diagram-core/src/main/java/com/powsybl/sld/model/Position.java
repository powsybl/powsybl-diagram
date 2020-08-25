/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class Position {

    private int h;
    private int v;

    private int hSpan;
    private int vSpan;

    private Orientation orientation;

    public Position(int h, int v, int hSpan, int vSpan, Orientation orientation) {
        this.h = h;
        this.v = v;
        this.hSpan = hSpan;
        this.vSpan = vSpan;
        this.orientation = orientation;
    }

    public Position(int h, int v) {
        this(h, v, 0, 0, null);
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public int getH() {
        return h;
    }

    public Position setH(int h) {
        this.h = h;
        return this;
    }

    public int getV() {
        return v;
    }

    public Position setV(int v) {
        this.v = v;
        return this;
    }

    public Position setHV(int h, int v) {
        setH(h);
        setV(v);
        return this;
    }

    public int getHSpan() {
        return hSpan;
    }

    public Position setHSpan(int hSpan) {
        this.hSpan = hSpan;
        return this;
    }

    public int getVSpan() {
        return vSpan;
    }

    public Position setVSpan(int vSpan) {
        this.vSpan = vSpan;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(h, v, hSpan, vSpan, orientation);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Position) {
            Position other = (Position) o;
            return other.h == h
                    && other.v == v
                    && other.hSpan == hSpan
                    && other.vSpan == vSpan
                    && other.orientation == orientation;
        }
        return false;
    }

    @Override
    public String toString() {
        return "h=" + h + " v=" + v + " hSpan=" + hSpan + " vSpan=" + vSpan + ", " + orientation;
    }

    public void writeJsonContent(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeNumberField("h", h);
        generator.writeNumberField("v", v);
        generator.writeNumberField("hSpan", hSpan);
        generator.writeNumberField("vSpan", vSpan);
        if (orientation != null) {
            generator.writeStringField("orientation", orientation.name());
        }
        generator.writeEndObject();
    }
}
