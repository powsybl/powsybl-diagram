/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.iidm;

import com.powsybl.sld.Config;
import com.powsybl.sld.ConfigBuilder;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.library.FlatDesignLibrary;
import com.powsybl.sld.library.ResourcesComponentLibrary;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.svg.DiagramStyleProvider;
import com.powsybl.sld.util.TopologicalStyleProvider;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class TestCase11FlatDesignComponents extends AbstractTestCaseIidm {

    @Before
    public void setUp() {
        network = NetworkFactory.createTestCase11Network();
        substation = network.getSubstation("subst");
        graphBuilder = new NetworkGraphBuilder(network);
    }

    @Override
    protected ResourcesComponentLibrary getResourcesComponentLibrary() {
        return new FlatDesignLibrary();
    }

    @Override
    protected DiagramStyleProvider getDefaultDiagramStyleProvider() {
        return new TopologicalStyleProvider(network);
    }

    @Test
    public void test() {
        // build substation graph
        SubstationGraph g = graphBuilder.buildSubstationGraph(substation.getId());

        // Run horizontal substation layout
        substationGraphLayout(g);

        String filename = "/TestCase11FlatDesign.svg";
        Config config = new ConfigBuilder(network)
                .withLayoutParameters(layoutParameters)
                .withSvgParameters(svgParameters)
                .withComponentLibrary(getResourcesComponentLibrary())
                .build();
        assertEquals(toString(filename), toSVG(g, filename, config));
    }
}
