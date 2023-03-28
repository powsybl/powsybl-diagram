/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.library;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.powsybl.sld.library.ComponentTypeName.BREAKER;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ComponentsTest {

    private static final CharSequence[] CHAR_SEQUENCES = {"{",
        "\"components\" : [ {",
        "\"type\" : \"BREAKER\",",
        "\"anchorPoints\" : [ {",
        "\"x\" : 9.0,",
        "\"y\" : 0.0,",
        "\"orientation\" : \"VERTICAL\"",
        "}, {",
        "\"x\" : 9.0,",
        "\"y\" : 18.0,",
        "\"orientation\" : \"HORIZONTAL\"",
        "} ],",
        "\"size\" : {",
        "\"width\" : 18.0,",
        "\"height\" : 19.0",
        "},",
        "\"transformations\" : {},",
        "\"subComponents\" : [ {",
        "\"name\" : \"BREAKER\",",
        "\"fileName\" : \"breaker.svg\",",
        "\"styleClass\" : null",
        "} ],",
        "\"styleClass\" : \"sld-breaker\"",
        "} ]",
        "}"};

    @Test
    void test() {
        String componentJSon = String.join(System.lineSeparator(), CHAR_SEQUENCES);

        ByteArrayInputStream is = new ByteArrayInputStream(componentJSon.getBytes(StandardCharsets.UTF_8));
        Components components = Components.load(is);

        assertEquals(1, components.getComponents().size());
        assertEquals("breaker.svg", components.getComponents().get(0).getSubComponents().get(0).getFileName());
        assertEquals(BREAKER, components.getComponents().get(0).getType());
        assertEquals(BREAKER, components.getComponents().get(0).getSubComponents().get(0).getName());
        assertEquals(18, components.getComponents().get(0).getSize().getWidth(), 0);
        assertEquals(19, components.getComponents().get(0).getSize().getHeight(), 0);
        assertEquals("ComponentSize(width=18.0, height=19.0)", components.getComponents().get(0).getSize().toString());
        assertEquals(2, components.getComponents().get(0).getAnchorPoints().size());
        assertEquals(9, components.getComponents().get(0).getAnchorPoints().get(0).getX(), 0);
        assertEquals(0, components.getComponents().get(0).getAnchorPoints().get(0).getY(), 0);
        assertEquals(AnchorOrientation.VERTICAL, components.getComponents().get(0).getAnchorPoints().get(0).getOrientation());
        assertEquals(9, components.getComponents().get(0).getAnchorPoints().get(1).getX(), 0);
        assertEquals(18, components.getComponents().get(0).getAnchorPoints().get(1).getY(), 0);
        assertEquals(AnchorOrientation.HORIZONTAL, components.getComponents().get(0).getAnchorPoints().get(1).getOrientation());
        assertTrue(components.getComponents().get(0).getTransformations().isEmpty());
    }

    @Test
    void testBadLoading() {
        // Remove last character in order to raise UncheckedIOException
        String componentJSon = String.join(System.lineSeparator(), Arrays.toString(CHAR_SEQUENCES).substring(0, CHAR_SEQUENCES.length - 1));

        ByteArrayInputStream is = new ByteArrayInputStream(componentJSon.getBytes(StandardCharsets.UTF_8));
        assertThrows(UncheckedIOException.class, () -> Components.load(is));
    }
}
