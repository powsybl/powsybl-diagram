/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.layout.PositionVoltageLevelLayoutFactory;
import com.powsybl.sld.layout.VerticalSubstationLayoutFactory;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.model.VoltageLevelGraph;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public class TestInternalBranchesNodeBreaker extends AbstractTestCaseIidm {

    @Before
    public void setUp() {
        network = CreateNetworksUtil.createNodeBreakerNetworkWithInternalBranches("TestInternalBranchesNodeBreaker", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = network.getSubstation("S1");
    }

    @Test
    public void testVLGraph() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildOrphanVoltageLevelGraph(network.getVoltageLevel("VL1").getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write SVG and compare to reference
        assertEquals(toString("/InternalBranchesNodeBreaker.svg"),
                toSVG(g, "/InternalBranchesNodeBreaker.svg", getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider()));

    }

    @Test
    public void testSubstationGraphH() {
        // build substation graph
        SubstationGraph g = graphBuilder.buildOrphanSubstationGraph(substation.getId());

        // Run horizontal substation layout
        substationGraphLayout(g);

        assertEquals(toString("/InternalBranchesNodeBreakerH.json"), toJson(g, "/InternalBranchesNodeBreakerH.json"));
    }

    @Test
    public void testSubstationGraphV() {
        // build substation graph
        SubstationGraph g = graphBuilder.buildOrphanSubstationGraph(substation.getId());

        // Run vertical substation layout
        new VerticalSubstationLayoutFactory().create(g, new PositionVoltageLevelLayoutFactory()).run(layoutParameters);

        assertEquals(toString("/InternalBranchesNodeBreakerV.json"), toJson(g, "/InternalBranchesNodeBreakerV.json"));
    }
}
