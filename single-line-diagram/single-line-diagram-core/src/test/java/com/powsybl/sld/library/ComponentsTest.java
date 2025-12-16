/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.library;

import com.powsybl.diagram.components.ComponentsLoader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.powsybl.sld.library.SldComponentTypeName.BREAKER;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
class ComponentsTest {

    private static final CharSequence[] CHAR_SEQUENCES = {
        "[ {",
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
        "} ]"};

    @Test
    void test() {
        String componentJSon = String.join(System.lineSeparator(), CHAR_SEQUENCES);

        ByteArrayInputStream is = new ByteArrayInputStream(componentJSon.getBytes(StandardCharsets.UTF_8));
        var components = new ComponentsLoader<>(SldComponent.class).load(is);

        assertEquals(1, components.size());
        assertEquals("breaker.svg", components.get(0).getSubComponents().get(0).fileName());
        assertEquals(BREAKER, components.get(0).getType());
        assertEquals(BREAKER, components.get(0).getSubComponents().get(0).name());
        assertEquals(18, components.get(0).getSize().width(), 0);
        assertEquals(19, components.get(0).getSize().height(), 0);
        assertEquals("ComponentSize(width=18.0, height=19.0)", components.get(0).getSize().toString());
        assertEquals(2, components.get(0).getAnchorPoints().size());
        assertEquals(9, components.get(0).getAnchorPoints().get(0).getX(), 0);
        assertEquals(0, components.get(0).getAnchorPoints().get(0).getY(), 0);
        assertEquals(AnchorOrientation.VERTICAL, components.get(0).getAnchorPoints().get(0).getOrientation());
        assertEquals(9, components.get(0).getAnchorPoints().get(1).getX(), 0);
        assertEquals(18, components.get(0).getAnchorPoints().get(1).getY(), 0);
        assertEquals(AnchorOrientation.HORIZONTAL, components.get(0).getAnchorPoints().get(1).getOrientation());
        assertTrue(components.get(0).getTransformations().isEmpty());
    }

    @Test
    void testBadLoading() {
        // Remove last character in order to raise UncheckedIOException
        String componentJSon = String.join(System.lineSeparator(), Arrays.toString(CHAR_SEQUENCES).substring(0, CHAR_SEQUENCES.length - 1));

        ByteArrayInputStream is = new ByteArrayInputStream(componentJSon.getBytes(StandardCharsets.UTF_8));
        ComponentsLoader<SldComponent> loader = new ComponentsLoader<>(SldComponent.class);
        assertThrows(UncheckedIOException.class, () -> loader.load(is));
    }
}
