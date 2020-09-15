/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.List;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class Position {

    public class Segment {
        private int value;
        private int span;
        private int shift;

        Segment(int value, int span, int shift) {
            this.value = value;
            this.span = span;
            this.shift = shift;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public int getSpan() {
            return span;
        }

        public void setSpan(int span) {
            this.span = span;
        }

        public int getShift() {
            return shift;
        }

        public void setShift(int shift) {
            this.shift = shift;
        }

        void alignAndMerge(List<Segment> segments) {
            segments.forEach(seg -> seg.setValue(this.getValue()));
            int resShift = segments.stream().mapToInt(Segment::getShift).min().orElse(0);
            this.setShift(resShift);
            this.setSpan(segments.stream()
                    .mapToInt(seg -> seg.getSpan() + getShift()).max().orElse(0) - resShift);
        }

        void putAsideAndMerge(List<Segment> segments) {
            
        }
    }

    private Segment h;
    private Segment v;

    private Orientation orientation;

    public Position(int h, int v, int hSpan, int vSpan, Orientation orientation) {
        this.h = new Segment(h, hSpan, 0);
        this.v = new Segment(v, vSpan, 0);
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
        return h.getValue();
    }

    public Position setH(int h) {
        this.h.setValue(h);
        return this;
    }

    public int getV() {
        return v.getValue();
    }

    public Position setV(int v) {
        this.v.setValue(v);
        return this;
    }

    public Position setHV(int h, int v) {
        this.h.setValue(h);
        this.v.setValue(v);
        return this;
    }

    public int getHSpan() {
        return h.getSpan();
    }

    public Position setHSpan(int hSpan) {
        this.h.setSpan(hSpan);
        return this;
    }

    public int getVSpan() {
        return this.v.getSpan();
    }

    public Position setVSpan(int vSpan) {
        this.v.setSpan(vSpan);
        return this;
    }

    @Override
    public String toString() {
        return "h=" + h.getValue() + " v=" + v.getValue() + " hSpan=" + h.getSpan() + " vSpan=" + v.getSpan() + ", " + orientation;
    }

    public void writeJsonContent(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeNumberField("h", h.getValue());
        generator.writeNumberField("v", v.getValue());
        generator.writeNumberField("hSpan", h.getSpan());
        generator.writeNumberField("vSpan", v.getSpan());
        if (orientation != null) {
            generator.writeStringField("orientation", orientation.name());
        }
        generator.writeEndObject();
    }
}
