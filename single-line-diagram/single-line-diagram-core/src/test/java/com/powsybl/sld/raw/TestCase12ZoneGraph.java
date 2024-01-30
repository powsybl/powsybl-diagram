/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.builders.ZoneRawBuilder;
import com.powsybl.sld.layout.*;
import com.powsybl.sld.model.graphs.ZoneGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
class TestCase12ZoneGraph extends AbstractTestCaseRaw {

    private static final String[] ZONE_ID = new String[]{"subst", "subst2"};

    @BeforeEach
    public void setUp() {
        ZoneRawBuilder zb = rawGraphBuilder.createZoneBuilder(List.of(ZONE_ID));
        RawGraphBuilderUtils.createRawBuilderWithTwoSubstations(rawGraphBuilder, zb, false, false, false);
    }

    @Test
    void testZoneHAndSubstationH() {
        ZoneGraph g = rawGraphBuilder.buildZoneGraph(List.of(ZONE_ID));

        layoutParameters.setDiagrammPadding(1.0, 1.0, 1.0, 1.0);

        // Run horizontal zone layout
        new HorizontalZoneLayoutFactory().create(g, new HorizontalSubstationLayoutFactory(), new PositionVoltageLevelLayoutFactory()).run(layoutParameters);

        assertEquals(toString("/TestCase12ZoneGraphHHRaw.json"), toJson(g, "/TestCase12ZoneGraphHHRaw.json"));
    }

    @Test
    void testZoneVAndSubstationV() {
        ZoneGraph g = rawGraphBuilder.buildZoneGraph(List.of(ZONE_ID));

        layoutParameters.setDiagrammPadding(1.0, 1.0, 1.0, 1.0);

        // Run vertical zone layout
        new VerticalZoneLayoutFactory().create(g, new VerticalSubstationLayoutFactory(), new PositionVoltageLevelLayoutFactory()).run(layoutParameters);

        assertEquals(toString("/TestCase12ZoneGraphVVRaw.json"), toJson(g, "/TestCase12ZoneGraphVVRaw.json"));
    }

    @Test
    void testZoneVAndSubstationH() {
        ZoneGraph g = rawGraphBuilder.buildZoneGraph(List.of(ZONE_ID));

        layoutParameters.setDiagrammPadding(1.0, 1.0, 1.0, 1.0);

        // Run vertical zone layout
        new VerticalZoneLayoutFactory().create(g, new HorizontalSubstationLayoutFactory(), new PositionVoltageLevelLayoutFactory()).run(layoutParameters);

        assertEquals(toString("/TestCase12ZoneGraphVHRaw.json"), toJson(g, "/TestCase12ZoneGraphVHRaw.json"));
    }

    @Test
    void testZoneHAndSubstationV() {
        ZoneGraph g = rawGraphBuilder.buildZoneGraph(List.of(ZONE_ID));

        layoutParameters.setDiagrammPadding(1.0, 1.0, 1.0, 1.0);

        // Run horizontal zone layout
        new HorizontalZoneLayoutFactory().create(g, new VerticalSubstationLayoutFactory(), new PositionVoltageLevelLayoutFactory()).run(layoutParameters);

        assertEquals(toString("/TestCase12ZoneGraphHVRaw.json"), toJson(g, "/TestCase12ZoneGraphHVRaw.json"));
    }
}
