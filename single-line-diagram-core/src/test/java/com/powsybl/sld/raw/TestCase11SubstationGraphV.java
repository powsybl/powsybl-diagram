/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.layout.PositionVoltageLevelLayoutFactory;
import com.powsybl.sld.layout.VerticalSubstationLayoutFactory;
import com.powsybl.sld.model.SubstationGraph;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */

public class TestCase11SubstationGraphV extends TestCase11SubstationGraph {

    @Test
    public void test() {
        SubstationGraph g = rawGraphBuilder.buildSubstationGraph("subst", false);
        new VerticalSubstationLayoutFactory().create(g, new PositionVoltageLevelLayoutFactory()).run(layoutParameters);
        assertEquals(toJson(g, "/TestCase11SubstationGraphVertical.json"), toString("/TestCase11SubstationGraphVertical.json"));
    }
}
