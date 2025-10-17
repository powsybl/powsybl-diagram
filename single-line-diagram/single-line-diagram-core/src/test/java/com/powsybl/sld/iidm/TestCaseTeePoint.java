/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.Line;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.svg.styles.iidm.TopologicalStyleProvider;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Giovanni Ferrari {@literal <giovani.ferrari at soft.it>}
 */
class TestCaseTeePoint extends AbstractTestCaseIidm {

    @Override
    public void setUp() throws IOException {
        // no common setup
    }

    @Test
    void testTeePointTopological() {
        network = Networks.createTeePointNodeBreakerNetwork();

        Line line2 = network.getLine("L2");
        line2.getTerminal1().setP(10d);
        line2.getTerminal1().setQ(-10d);
        Line line3 = network.getLine("L3");
        line3.getTerminal1().setP(20d);
        line3.getTerminal1().setQ(20d);

        // build graph
        VoltageLevelGraph g = new NetworkGraphBuilder(network).buildVoltageLevelGraph("vl1");

        // Run layout
        voltageLevelGraphLayout(g);

        // write Json and compare to reference
        assertEquals(toString("/TestCaseTeePointTopological.svg"),
                toSVG(g, "/TestCaseTeePointTopological.svg", componentLibrary, layoutParameters, svgParameters, getDefaultDiagramLabelProvider(), new TopologicalStyleProvider(network, true)));
    }

}
