/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.svg.styles.iidm.TopologicalStyleProvider;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
class TestCaseFictitiousBus extends AbstractTestCaseIidm {

    @Override
    public void setUp() throws IOException {
        // no common setup
    }

    @Test
    void testBasic() {
        network = Networks.createTeePointNetwork();

        // build graph
        VoltageLevelGraph g = new NetworkGraphBuilder(network).buildVoltageLevelGraph("vl");

        // Run layout
        voltageLevelGraphLayout(g);

        // write Json and compare to reference
        assertEquals(toString("/TestCaseFictitiousBus.svg"),
                toSVG(g, "/TestCaseFictitiousBus.svg", componentLibrary, layoutParameters, svgParameters, getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider()));
    }

    @Test
    void testTopological() {
        network = Networks.createTeePointNetwork();

        // build graph
        VoltageLevelGraph g = new NetworkGraphBuilder(network).buildVoltageLevelGraph("vl");

        // Run layout
        voltageLevelGraphLayout(g);

        // write Json and compare to reference
        assertEquals(toString("/TestCaseFictitiousBusTopological.svg"),
                toSVG(g, "/TestCaseFictitiousBusTopological.svg", componentLibrary, layoutParameters, svgParameters, getDefaultDiagramLabelProvider(), new TopologicalStyleProvider(network)));
    }

    @Test
    void testDanglingLoad() {
        network = Networks.createDanglingLoadNetwork();

        // build graph
        VoltageLevelGraph g = new NetworkGraphBuilder(network).buildVoltageLevelGraph("vl");

        // Run layout
        voltageLevelGraphLayout(g);

        // write Json and compare to reference
        assertEquals(toString("/TestCaseFictitiousBusDanglingLoad.svg"),
                toSVG(g, "/TestCaseFictitiousBusDanglingLoad.svg", componentLibrary, layoutParameters, svgParameters, getDefaultDiagramLabelProvider(), new TopologicalStyleProvider(network)));
    }
}
