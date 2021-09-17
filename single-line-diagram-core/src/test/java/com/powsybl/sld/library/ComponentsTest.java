/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.library;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static com.powsybl.sld.library.ComponentTypeName.BREAKER;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ComponentsTest {

    @Test
    public void test() {
        String componentJSon = String.join(System.lineSeparator(),
                "{",
                "\"components\" : [ {",
                    "\"type\" : \"BREAKER\",",
                    "\"id\" : null,",
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
                "\"allowRotation\" : true,",
                "\"subComponents\" : [ {",
                    "\"name\" : \"BREAKER\",",
                    "\"fileName\" : \"breaker.svg\",",
                    "\"styleClass\" : null",
                "} ],",
                "\"styleClass\" : \"sld-breaker\"",
                "} ]",
                "}");

        Components components;
        ByteArrayInputStream is = new ByteArrayInputStream(componentJSon.getBytes(StandardCharsets.UTF_8));
        components = Components.load(is);

        assertEquals(1, components.getComponents().size());
        assertEquals("breaker.svg", components.getComponents().get(0).getMetadata().getSubComponents().get(0).getFileName());
        assertEquals(BREAKER, components.getComponents().get(0).getMetadata().getType());
        assertEquals(BREAKER, components.getComponents().get(0).getMetadata().getSubComponents().get(0).getName());
        assertEquals(18, components.getComponents().get(0).getMetadata().getSize().getWidth(), 0);
        assertEquals(19, components.getComponents().get(0).getMetadata().getSize().getHeight(), 0);
        assertEquals(2, components.getComponents().get(0).getMetadata().getAnchorPoints().size());
        assertEquals(9, components.getComponents().get(0).getMetadata().getAnchorPoints().get(0).getX(), 0);
        assertEquals(0, components.getComponents().get(0).getMetadata().getAnchorPoints().get(0).getY(), 0);
        assertEquals(AnchorOrientation.VERTICAL, components.getComponents().get(0).getMetadata().getAnchorPoints().get(0).getOrientation());
        assertEquals(9, components.getComponents().get(0).getMetadata().getAnchorPoints().get(1).getX(), 0);
        assertEquals(18, components.getComponents().get(0).getMetadata().getAnchorPoints().get(1).getY(), 0);
        assertEquals(AnchorOrientation.HORIZONTAL, components.getComponents().get(0).getMetadata().getAnchorPoints().get(1).getOrientation());
        assertTrue(components.getComponents().get(0).getMetadata().isAllowRotation());
    }
}
