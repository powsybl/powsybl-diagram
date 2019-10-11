/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.library;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import static com.powsybl.sld.library.ComponentTypeName.BREAKER;
import static org.junit.Assert.assertEquals;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ComponentsTest {

    @Test
    public void test() {
        String componentXml = String.join(System.lineSeparator(),
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>",
                "<components>",
                "    <component>",
                "        <fileName>breaker.svg</fileName>",
                "        <metadata type=\"BREAKER\">",
                "            <size width=\"18\" height=\"19\" />",
                "            <anchorPoint x=\"9\" y=\"0\" orientation=\"VERTICAL\"/>",
                "            <anchorPoint x=\"9\" y=\"18\" orientation=\"HORIZONTAL\"/>",
                "        </metadata>",
                "    </component>",
                "</components>");
        Components components;
        try (ByteArrayInputStream is = new ByteArrayInputStream(componentXml.getBytes(StandardCharsets.UTF_8))) {
            components = Components.load(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        assertEquals(1, components.getComponents().size());
        assertEquals("breaker.svg", components.getComponents().get(0).getFileName());
        assertEquals(BREAKER, components.getComponents().get(0).getMetadata().getType());
        assertEquals(18, components.getComponents().get(0).getMetadata().getSize().getWidth(), 0);
        assertEquals(19, components.getComponents().get(0).getMetadata().getSize().getHeight(), 0);
        assertEquals(2, components.getComponents().get(0).getMetadata().getAnchorPoints().size());
        assertEquals(9, components.getComponents().get(0).getMetadata().getAnchorPoints().get(0).getX(), 0);
        assertEquals(0, components.getComponents().get(0).getMetadata().getAnchorPoints().get(0).getY(), 0);
        assertEquals(AnchorOrientation.VERTICAL, components.getComponents().get(0).getMetadata().getAnchorPoints().get(0).getOrientation());
        assertEquals(9, components.getComponents().get(0).getMetadata().getAnchorPoints().get(1).getX(), 0);
        assertEquals(18, components.getComponents().get(0).getMetadata().getAnchorPoints().get(1).getY(), 0);
        assertEquals(AnchorOrientation.HORIZONTAL, components.getComponents().get(0).getMetadata().getAnchorPoints().get(1).getOrientation());
    }
}
