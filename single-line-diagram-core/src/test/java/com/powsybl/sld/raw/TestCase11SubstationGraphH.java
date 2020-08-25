/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.layout.HorizontalSubstationLayoutFactory;
import com.powsybl.sld.layout.PositionVoltageLevelLayoutFactory;
import com.powsybl.sld.model.SubstationGraph;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */

public class TestCase11SubstationGraphH extends TestCase11SubstationGraph {

    @Test
    public void test() {
        SubstationGraph g = rawGraphBuilder.buildSubstationGraph("subst", false);
        new HorizontalSubstationLayoutFactory().create(g, new PositionVoltageLevelLayoutFactory()).run(layoutParameters);
        assertEquals(toString("/TestCase11SubstationGraphHorizontal.json"), toJson(g, "/TestCase11SubstationGraphHorizontal.json"));
    }
}
