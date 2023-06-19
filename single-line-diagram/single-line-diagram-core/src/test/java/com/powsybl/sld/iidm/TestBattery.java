/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.svg.DefaultSVGWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sophie Frasnedo <sophie.frasnedo at rte-france.com>
 */
class TestBattery extends AbstractTestCaseIidm {

    @BeforeEach
    public void setUp() throws IOException {
        network = Networks.createNetworkWithBatteries();
        graphBuilder = new NetworkGraphBuilder(network);
    }

    @Test
    void test() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph("vl1");

        // Run layout
        voltageLevelGraphLayout(g);

        // write SVG and compare to reference
        DefaultSVGWriter defaultSvgWriter = new DefaultSVGWriter(getResourcesComponentLibrary(), layoutParameters, svgParameters);

        assertEquals(toString("/TestBatteries.svg"), toSVG(g, "/TestBatteries.svg", defaultSvgWriter, getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider()));
    }
}
