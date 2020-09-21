/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, vSeg. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public class Segment {
    private int value;
    private int span;
    private int shift;

    Segment(int value, int span, int shift) {
        this.value = value;
        this.span = span;
        this.shift = shift;
    }

    public void copy(Segment segment) {
        this.value = segment.value;
        this.span = segment.span;
        this.shift = segment.shift;
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
