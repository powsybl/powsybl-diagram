/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.coordinate;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.powsybl.sld.model.coordinate.Coord.Dimension.*;

/**
 * class use to store relatives coordinates of a nodeBus
 *
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class Coord {

    public static class Segment {
        private double value;
        private double span;

        Segment(double value, double span) {
            this.value = value;
            this.span = span;
        }

        public void copy(Segment segment) {
            this.value = segment.value;
            this.span = segment.span;
        }

        public void replicateMe(Stream<Segment> segments) {
            segments.forEach(seg -> seg.copy(this));
        }

        public double getValue() {
            return value;
        }

        public double getSpan() {
            return span;
        }
    }

    public enum Dimension {
        X, Y
    }

    private final Map<Dimension, Segment> dim2seg = new EnumMap<>(Dimension.class);

    public Coord(double x, double y) {
        dim2seg.put(X, new Segment(x, 0));
        dim2seg.put(Y, new Segment(y, 0));
    }

    public double get(Dimension dimension) {
        return dim2seg.get(dimension).getValue();
    }

    public double getSpan(Dimension dimension) {
        return dim2seg.get(dimension).getSpan();
    }

    public Segment getSegment(Dimension dimension) {
        return dim2seg.get(dimension);
    }

    public void set(Dimension dimension, double value, double span) {
        dim2seg.put(dimension, new Segment(value, span));
    }

    @Override
    public String toString() {
        return "Coord(x=" + get(X) + ", y=" + get(Y) + ", xSpan=" + getSpan(X) + ", ySpan=" + getSpan(Y) + ")";
    }

    public void writeJsonContent(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeNumberField("x", get(X));
        generator.writeNumberField("y", get(Y));
        generator.writeNumberField("xSpan", getSpan(X));
        generator.writeNumberField("ySpan", getSpan(Y));
        generator.writeEndObject();
    }
}

