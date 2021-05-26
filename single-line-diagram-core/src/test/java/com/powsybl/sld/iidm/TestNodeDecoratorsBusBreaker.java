/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.layout.HorizontalSubstationLayoutFactory;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.PositionVoltageLevelLayoutFactory;
import com.powsybl.sld.model.SubstationGraph;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public class TestNodeDecoratorsBusBreaker extends AbstractTestCaseIidm {

    @Override
    protected LayoutParameters getLayoutParameters() {
        return createDefaultLayoutParameters().setShowInternalNodes(false);
    }

    @Before
    public void setUp() {
        network = CreateNetworksUtil.createBusBreakerNetworkWithBranchStatus("TestNodeDecorators", "test");
        graphBuilder = new NetworkGraphBuilder(network);
    }

    @Test
    public void testBranchStatusDecorators() {
        // build substation graph
        SubstationGraph g = graphBuilder.buildSubstationGraph("S1", true);

        new HorizontalSubstationLayoutFactory().create(g, new PositionVoltageLevelLayoutFactory()).run(getLayoutParameters());
        assertEquals(toString("/NodeDecoratorsBranchStatusBusBreaker.svg"),
            toSVG(g, "/NodeDecoratorsBranchStatusBusBreaker.svg", getLayoutParameters(), getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider()));
    }
}
