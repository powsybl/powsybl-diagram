/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.library.ComponentSize;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.model.nodes.SwitchNode;
import com.powsybl.sld.svg.DefaultLabelProvider;
import com.powsybl.sld.svg.DefaultSVGWriter;
import com.powsybl.sld.svg.LabelPosition;
import com.powsybl.sld.svg.LabelProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
class TestNodeDecoratorsNodeBreaker extends AbstractTestCaseIidm {

    @BeforeEach
    public void setUp() {
        network = Networks.createNodeBreakerNetworkWithBranchStatus("TestNodeDecorators", "test");
        graphBuilder = new NetworkGraphBuilder(network);
    }

    @Test
    void testBranchStatusDecorators() {

        // build substation graph
        SubstationGraph g = graphBuilder.buildSubstationGraph("S1");

        // Run horizontal substation layout
        substationGraphLayout(g);

        DefaultSVGWriter defaultSVGWriter = new DefaultSVGWriter(componentLibrary, layoutParameters, svgParameters);
        assertEquals(toString("/NodeDecoratorsBranchStatusNodeBreaker.svg"),
                toSVG(g, "/NodeDecoratorsBranchStatusNodeBreaker.svg", defaultSVGWriter, getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider()));
    }

    @Test
    void testSwitchDecorators() {

        LabelProvider labelTestProvider = new DefaultLabelProvider(network, componentLibrary, layoutParameters, svgParameters) {

            private static final double SWITCH_DECORATOR_OFFSET = 1d;

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

        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(network.getVoltageLevel("VL1").getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write SVG and compare to reference
        DefaultSVGWriter defaultSVGWriter = new DefaultSVGWriter(componentLibrary, layoutParameters, svgParameters);
        assertEquals(toString("/NodeDecoratorsSwitches.svg"),
                toSVG(g, "/NodeDecoratorsSwitches.svg", defaultSVGWriter, labelTestProvider, getDefaultDiagramStyleProvider()));
    }

}
