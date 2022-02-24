/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.Network;
import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.library.ComponentSize;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.nodes.SwitchNode;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.svg.DefaultDiagramLabelProvider;
import com.powsybl.sld.svg.LabelPosition;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public class TestNodeDecoratorsNodeBreaker extends AbstractTestCaseIidm {

    @Before
    public void setUp() {
        layoutParameters.setShowInternalNodes(false);
        network = CreateNetworksUtil.createNodeBreakerNetworkWithBranchStatus("TestNodeDecorators", "test");
        graphBuilder = new NetworkGraphBuilder(network);
    }

    @Test
    public void testBranchStatusDecorators() {

        // build substation graph
        SubstationGraph g = graphBuilder.buildOrphanSubstationGraph("S1");

        // Run horizontal substation layout
        substationGraphLayout(g);

        assertEquals(toString("/NodeDecoratorsBranchStatusNodeBreaker.svg"),
            toSVG(g, "/NodeDecoratorsBranchStatusNodeBreaker.svg", getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider()));
    }

    @Test
    public void testSwitchDecorators() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildOrphanVoltageLevelGraph(network.getVoltageLevel("VL1").getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write SVG and compare to reference
        assertEquals(toString("/NodeDecoratorsSwitches.svg"),
            toSVG(g, "/NodeDecoratorsSwitches.svg", new TestDiagramLabelProvider(network), getDefaultDiagramStyleProvider()));
    }

    private class TestDiagramLabelProvider extends DefaultDiagramLabelProvider {

        private static final double SWITCH_DECORATOR_OFFSET = 1d;

        public TestDiagramLabelProvider(Network network) {
            super(network, componentLibrary, layoutParameters);
        }

        @Override
        public List<NodeDecorator> getNodeDecorators(Node node, Direction direction) {
            Objects.requireNonNull(node);
            if (node instanceof SwitchNode) {
                return Collections.singletonList(new NodeDecorator("LOCK", getSwitchDecoratorPosition((SwitchNode) node)));
            }
            return Collections.emptyList();
        }

        private LabelPosition getSwitchDecoratorPosition(SwitchNode node) {
            ComponentSize size = componentLibrary.getSize(node.getComponentType());
            double yShift = -size.getHeight() / 2;
            double xShift = size.getWidth() / 2 + SWITCH_DECORATOR_OFFSET;
            return new LabelPosition("DECORATOR", xShift, yShift, false, 0);
        }
    }
}
