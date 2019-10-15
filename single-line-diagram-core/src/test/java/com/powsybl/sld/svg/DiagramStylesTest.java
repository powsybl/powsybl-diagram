package com.powsybl.sld.svg;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DiagramStylesTest {

    @Test
    public void test() {
        String input = "ab_cd.ef gh";
        String escaped = DiagramStyles.escapeId(input);
        assertEquals(input, DiagramStyles.unescapeId(escaped));
    }

    @Test
    public void test2() {
        String input = "_c";
        String escaped = DiagramStyles.escapeClassName(input);
        assertEquals("_95_c", escaped);
    }
}
