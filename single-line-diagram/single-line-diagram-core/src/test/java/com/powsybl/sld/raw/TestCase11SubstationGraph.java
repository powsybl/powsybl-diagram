/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.commons.*;
import com.powsybl.sld.layout.PositionVoltageLevelLayoutFactory;
import com.powsybl.sld.layout.VerticalSubstationLayoutFactory;
import com.powsybl.sld.model.graphs.SubstationGraph;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/*
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
class TestCase11SubstationGraph extends AbstractTestCaseRaw {

    @BeforeEach
    public void setUp() {
        // Nothing to do
    }

    @Test
    void testH() {
        RawGraphBuilderUtils.createRawBuilderWithTwoSubstations(rawGraphBuilder, null, false, false, false);
        SubstationGraph g = rawGraphBuilder.buildSubstationGraph("subst");
        substationGraphLayout(g);
        assertEquals(toString("/TestCase11SubstationGraphHRaw.json"), toJson(g, "/TestCase11SubstationGraphHRaw.json"));
    }

    @Test
    void testV() {
        RawGraphBuilderUtils.createRawBuilderWithTwoSubstations(rawGraphBuilder, null, false, false, false);
        SubstationGraph g = rawGraphBuilder.buildSubstationGraph("subst");
        new VerticalSubstationLayoutFactory().create(g, new PositionVoltageLevelLayoutFactory()).run(layoutParameters);
        assertEquals(toString("/TestCase11SubstationGraphVRaw.json"), toJson(g, "/TestCase11SubstationGraphVRaw.json"));
    }

    @Test
    void testH2() {
        RawGraphBuilderUtils.createRawBuilderWithTwoSubstations(rawGraphBuilder, null, false, false, false);
        SubstationGraph g = rawGraphBuilder.buildSubstationGraph("subst2");
        substationGraphLayout(g);
        assertEquals(toString("/TestCase11SubstationGraphHRaw2.json"), toJson(g, "/TestCase11SubstationGraphHRaw2.json"));
    }

    @Test
    void testV2() {
        RawGraphBuilderUtils.createRawBuilderWithTwoSubstations(rawGraphBuilder, null, false, false, false);
        SubstationGraph g = rawGraphBuilder.buildSubstationGraph("subst2");
        new VerticalSubstationLayoutFactory().create(g, new PositionVoltageLevelLayoutFactory()).run(layoutParameters);
        assertEquals(toString("/TestCase11SubstationGraphVRaw2.json"), toJson(g, "/TestCase11SubstationGraphVRaw2.json"));
    }

    @Test
    void testH2WithLines() {
        RawGraphBuilderUtils.createRawBuilderWithTwoSubstations(rawGraphBuilder, null, true, false, false);

        SubstationGraph g = rawGraphBuilder.buildSubstationGraph("subst2");
        substationGraphLayout(g);
        assertEquals(toString("/TestCase11SubstationGraphHRaw2WithLines.json"), toJson(g, "/TestCase11SubstationGraphHRaw2WithLines.json"));
    }

    @Test
    void testV2WithLines() {
        RawGraphBuilderUtils.createRawBuilderWithTwoSubstations(rawGraphBuilder, null, true, false, false);
        SubstationGraph g = rawGraphBuilder.buildSubstationGraph("subst2");
        new VerticalSubstationLayoutFactory().create(g, new PositionVoltageLevelLayoutFactory()).run(layoutParameters);
        assertEquals(toString("/TestCase11SubstationGraphVRaw2WithLines.json"), toJson(g, "/TestCase11SubstationGraphVRaw2WithLines.json"));
    }

    @Test
    void testH2With2wts() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> RawGraphBuilderUtils.createRawBuilderWithTwoSubstations(rawGraphBuilder, null, false, true, false));
        assertEquals("VoltageLevel(s) 'vlSubst2' not found in Substation 'subst'", e.getMessage());
    }

    @Test
    void testH2With3wts() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> RawGraphBuilderUtils.createRawBuilderWithTwoSubstations(rawGraphBuilder, null, false, false, true));
        assertEquals("VoltageLevel(s) 'vlSubst2' not found in Substation 'subst'", e.getMessage());
    }
}
