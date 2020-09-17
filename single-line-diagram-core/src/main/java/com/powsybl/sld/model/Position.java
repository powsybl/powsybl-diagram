/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, vSeg. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class Position {

    enum Dimension {
        H, V
    }

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

        public void mergeEnvelop(Stream<Segment> segStream) {
/*
            int resShift = segments.stream().mapToInt(Segment::getShift).min().orElse(0);
            this.setShift(resShift);
            this.setSpan(segments.stream()
                    .mapToInt(seg -> seg.getSpan() + getShift()).max().orElse(0) - resShift);
*/
            List<Segment> segments = segStream.collect(Collectors.toList());
            setSpan(segments.stream().mapToInt(Segment::getSpan).max().orElse(0));
            segments.forEach(seg -> seg.setValue(0));
        }

        void glue(Stream<Segment> segStream) {
            List<Segment> segments = segStream.collect(Collectors.toList());
            setSpan(segments.stream().mapToInt(Segment::getSpan).sum());
            int cumulSpan = 0;
            for (Segment seg : segments) {
                seg.setValue(cumulSpan);
                cumulSpan += seg.getSpan();
            }
        }
    }

    private Map<Dimension, Segment> dim2Seg = new EnumMap<>(Dimension.class);

    private Orientation orientation;

    public Position(int h, int v, int hSpan, int vSpan, Orientation orientation) {
        dim2Seg.put(Dimension.H, new Segment(h, hSpan, 0));
        dim2Seg.put(Dimension.V, new Segment(v, vSpan, 0));
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
        return dim2Seg.get(Dimension.H).getValue();
    }

    public Position setH(int h) {
        dim2Seg.get(Dimension.H).setValue(h);
        return this;
    }

    public int getV() {
        return dim2Seg.get(Dimension.V).getValue();
    }

    public Position setV(int v) {
        dim2Seg.get(Dimension.V).setValue(v);
        return this;
    }

    public Position setHV(int h, int v) {
        setH(h);
        setV(v);
        return this;
    }

    public Segment getSegment(Dimension dimension) {
        return dim2Seg.get(dimension);
    }

    public int getHSpan() {
        return dim2Seg.get(Dimension.H).getSpan();
    }

    public Position setHSpan(int hSpan) {
        dim2Seg.get(Dimension.H).setSpan(hSpan);
        return this;
    }

    public int getVSpan() {
        return dim2Seg.get(Dimension.V).getSpan();
    }

    public Position setVSpan(int vSpan) {
        dim2Seg.get(Dimension.V).setSpan(vSpan);
        return this;
    }

    public Segment getHSeg() {
        return dim2Seg.get(Dimension.H);
    }

    public void setHSeg(Segment hSeg) {
        dim2Seg.put(Dimension.H, hSeg);
    }

    public Segment getVSeg() {
        return dim2Seg.get(Dimension.V);
    }

    public void setVSeg(Segment vSeg) {
        dim2Seg.put(Dimension.V, vSeg);
    }

    @Override
    public String toString() {
        return "h=" + getH() + " v=" + getV() + " hSpan=" + getHSpan() + " vSpan=" + getVSpan() + ", " + orientation;
    }

    public void writeJsonContent(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeNumberField("h", getH());
        generator.writeNumberField("v", getV());
        generator.writeNumberField("hSpan", getHSpan());
        generator.writeNumberField("vSpan", getVSpan());
        if (orientation != null) {
            generator.writeStringField("orientation", orientation.name());
        }
        generator.writeEndObject();
    }
}
