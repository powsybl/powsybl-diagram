/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */

class TestCaseExternCellOnTwoSections extends AbstractTestCaseIidm {

    @BeforeEach
    public void setUp() {
    }

    @Test
    void testSimpleExternCellOnTwoSections() {
        network = Networks.createSimpleExternCellOnTwoSections();
        VoltageLevelGraph g = new NetworkGraphBuilder(network).buildVoltageLevelGraph("vl");
        voltageLevelGraphLayout(g);
        assertEquals(toString("/TestCaseSimpleExternCellOnTwoSections.svg"), toSVG(g, "/TestCaseSimpleExternCellOnTwoSections.svg"));
    }

    @Test
    void testComplexExternCellOnTwoSections() {
        network = Networks.createComplexExternCellOnTwoSections();
        VoltageLevelGraph g = new NetworkGraphBuilder(network).buildVoltageLevelGraph("vl");
        voltageLevelGraphLayout(g);
        assertEquals(toString("/TestCaseComplexExternCellOnTwoSections.svg"), toSVG(g, "/TestCaseComplexExternCellOnTwoSections.svg"));
    }
}
