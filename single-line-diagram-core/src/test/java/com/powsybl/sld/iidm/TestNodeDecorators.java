/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.Network;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentSize;
import com.powsybl.sld.library.ResourcesComponentLibrary;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.model.SwitchNode;
import com.powsybl.sld.svg.*;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class TestNodeDecorators extends AbstractTestCaseIidm {

    private LayoutParameters layoutParameters;

    @Override
    protected ResourcesComponentLibrary getResourcesComponentLibrary() {
        return new ResourcesComponentLibrary("/ConvergenceLibrary", "/NodeDecoratorsLibrary");
    }

    @Before
    public void setUp() {
        // Layout parameters :
        layoutParameters = new LayoutParameters()
            .setTranslateX(20)
            .setTranslateY(50)
            .setInitialXBus(0)
            .setInitialYBus(260)
            .setVerticalSpaceBus(25)
            .setHorizontalBusPadding(20)
            .setCellWidth(80)
            .setExternCellHeight(250)
            .setInternCellHeight(40)
            .setStackHeight(30)
            .setShowGrid(true)
            .setShowInternalNodes(false)
            .setScaleFactor(1)
            .setHorizontalSubstationPadding(50)
            .setVerticalSubstationPadding(50)
            .setDrawStraightWires(false)
            .setHorizontalSnakeLinePadding(30)
            .setVerticalSnakeLinePadding(30);
    }

    @Test
    public void test() {

        Graph graph = TestSVGWriter.createVoltageLevelGraph1();

        TestDiagramLabelProvider nodeDecoratorLabelProvider = new TestDiagramLabelProvider();
        assertEquals(toString("/vl1_decorated.svg"), toSVG(graph, "/vl1_decorated.svg", layoutParameters,
            nodeDecoratorLabelProvider, new DefaultDiagramStyleProvider()));

        // Same tests than before, with optimized svg :
        layoutParameters.setAvoidSVGComponentsDuplication(true);
        assertEquals(toString("/vl1_decorated_opt.svg"), toSVG(graph, "/vl1_decorated_opt.svg", layoutParameters,
            nodeDecoratorLabelProvider, new DefaultDiagramStyleProvider()));
    }

    private class TestDiagramLabelProvider extends DefaultDiagramLabelProvider {

        private static final double DECORATOR_OFFSET = 1d;

        public TestDiagramLabelProvider() {
            super(Network.create("empty", ""), componentLibrary, layoutParameters);
        }

        @Override
        public InitialValue getInitialValue(Node node) {
            InitialValue initialValue;
            if (node.getType() == Node.NodeType.BUS) {
                initialValue = new InitialValue(null, null, node.getLabel(), null, null, null);
            } else {
                initialValue = new InitialValue(Direction.UP, Direction.DOWN, "10", "20", null, null);
            }
            return initialValue;
        }

        @Override
        public List<NodeDecorator> getNodeDecorators(Node node) {
            Objects.requireNonNull(node);

            List<NodeDecorator> nodeDecorators = new LinkedList<>();
            if (node instanceof SwitchNode) {
                String componentType = "";
                switch (((SwitchNode) node).getKind()) {
                    case BREAKER:
                        componentType = "FLASH"; break;
                    case DISCONNECTOR:
                    case LOAD_BREAK_SWITCH:
                        componentType = "LOCK"; break;
                }
                nodeDecorators.add(new NodeDecorator(componentType, getSwitchDecoratorPosition((SwitchNode) node, componentType)));
            }

            return nodeDecorators;
        }

        private LabelPosition getSwitchDecoratorPosition(SwitchNode node, String decoratorType) {
            ComponentSize size = componentLibrary.getSize(node.getComponentType());
            double yShift = -size.getHeight() / 2;
            double xShift = size.getWidth() / 2 + DECORATOR_OFFSET;
            return new LabelPosition(node.getId() + "_DECORATOR", xShift, yShift, false, 0);
        }
    }
}
