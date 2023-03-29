/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.Network;
import com.powsybl.sld.Config;
import com.powsybl.sld.ConfigBuilder;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ComponentSize;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.nodes.SwitchNode;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.svg.*;
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

    LabelProviderFactory diagramLabelTestProviderFactory = new DefaultLabelProviderFactory() {

        private static final double SWITCH_DECORATOR_OFFSET = 1d;

        @Override
        public LabelProvider create(Network network, ComponentLibrary componentLibrary, LayoutParameters layoutParameters, SvgParameters svgParameters) {
            return new DefaultLabelProvider(network, componentLibrary, layoutParameters, svgParameters) {

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
            };
        }
    };

    @Before
    public void setUp() {
        network = CreateNetworksUtil.createNodeBreakerNetworkWithBranchStatus("TestNodeDecorators", "test");
        graphBuilder = new NetworkGraphBuilder(network);
    }

    @Test
    public void testBranchStatusDecorators() {

        // build substation graph
        SubstationGraph g = graphBuilder.buildSubstationGraph("S1");

        // Run horizontal substation layout
        substationGraphLayout(g);

        Config config = new ConfigBuilder(network)
                .withLayoutParameters(layoutParameters)
                .withComponentLibrary(componentLibrary)
                .withSvgParameters(svgParameters)
                .build();

        assertEquals(toString("/NodeDecoratorsBranchStatusNodeBreaker.svg"),
            toSVG(g, "/NodeDecoratorsBranchStatusNodeBreaker.svg", config));
    }

    @Test
    public void testSwitchDecorators() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(network.getVoltageLevel("VL1").getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write SVG and compare to reference
        Config config = new ConfigBuilder(network)
                .withLayoutParameters(layoutParameters)
                .withComponentLibrary(componentLibrary)
                .withSvgParameters(svgParameters)
                .withDiagramLabelProviderFactory(diagramLabelTestProviderFactory)
                .build();
        assertEquals(toString("/NodeDecoratorsSwitches.svg"),
            toSVG(g, "/NodeDecoratorsSwitches.svg", config));
    }
}
