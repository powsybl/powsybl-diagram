/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.sld.SingleLineDiagramConfiguration;
import com.powsybl.sld.SingleLineDiagramConfigurationBuilder;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.library.FlatDesignLibrary;
import com.powsybl.sld.library.ResourcesComponentLibrary;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class TestComplexParallelLegsInternalPst extends AbstractTestCaseIidm {

    @Before
    public void setUp() throws IOException {
        network = CreateNetworksUtil.createNetworkWithInternalPst();
        graphBuilder = new NetworkGraphBuilder(network);
    }

    @Override
    protected ResourcesComponentLibrary getResourcesComponentLibrary() {
        return new FlatDesignLibrary();
    }

    @Test
    public void test() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph("vl1");

        // Run layout
        voltageLevelGraphLayout(g);

        // write SVG and compare to reference
        String filename = "/TestComplexParallelLegsInternalPst.svg";
        SingleLineDiagramConfiguration singleLineDiagramConfiguration = new SingleLineDiagramConfigurationBuilder(network)
                .withLayoutParameters(layoutParameters)
                .withComponentLibrary(getResourcesComponentLibrary())
                .build();
        assertEquals(toString(filename), toSVG(g, filename, singleLineDiagramConfiguration));
    }
}
