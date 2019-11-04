/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.iidm.extensions.BusbarSectionPosition;
import com.powsybl.sld.layout.BlockOrganizer;
import com.powsybl.sld.layout.ImplicitCellDetector;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.PositionVoltageLevelLayout;
import com.powsybl.sld.model.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * <pre>
 *     b
 *    / \
 *   |   |
 * -d1---|---- bbs1
 * -----d2---- bbs2
 *
 * </pre>
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TestCase3Coupling extends AbstractTestCase {

    @Before
    public void setUp() {
        network = Network.create("testCase1", "test");
        graphBuilder = new NetworkGraphBuilder(network);

        Substation s = network.newSubstation()
                .setId("s")
                .setCountry(Country.FR)
                .add();

        vl = s.newVoltageLevel()
                .setId("vl")
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .setNominalV(400)
                .add();
        VoltageLevel.NodeBreakerView view = vl.getNodeBreakerView()
                .setNodeCount(10);

        BusbarSection bbs1 = view.newBusbarSection()
                .setId("bbs1")
                .setNode(0)
                .add();
        bbs1.addExtension(BusbarSectionPosition.class, new BusbarSectionPosition(bbs1, 1, 1));

        view.newDisconnector()
                .setId("d1")
                .setNode1(0)
                .setNode2(1)
                .add();

        view.newBreaker()
                .setId("b")
                .setNode1(1)
                .setNode2(2)
                .add();

        view.newDisconnector()
                .setId("d2")
                .setNode1(2)
                .setNode2(3)
                .add();

        BusbarSection bbs2 = view.newBusbarSection()
                .setId("bbs2")
                .setNode(3)
                .add();
        bbs2.addExtension(BusbarSectionPosition.class, new BusbarSectionPosition(bbs2, 2, 1));

    }

    @Test
    public void test() {
        // build graph
        Graph g = graphBuilder.buildVoltageLevelGraph(vl.getId(), false, true, false);

        // assert graph structure
        assertEquals(7, g.getNodes().size());

        // detect cells
        new ImplicitCellDetector().detectCells(g);

        // assert cells
        assertEquals(1, g.getCells().size());
        Cell cell = g.getCells().iterator().next();
        assertEquals(Cell.CellType.INTERN, cell.getType());
        assertEquals(7, cell.getNodes().size());
        assertEquals(2, ((BusCell) cell).getBusNodes().size());
        assertEquals("INTERN[FICT_vl_d1Fictif, FICT_vl_d2Fictif, b, bbs1, bbs2, d1, d2]", cell.getFullId());

        // build blocks
        new BlockOrganizer().organize(g);

        // assert blocks and nodes rotation
        assertEquals(2, ((BusCell) cell).getPrimaryLegBlocks().size());
        assertNotNull(cell.getRootBlock());
        assertTrue(cell.getRootBlock() instanceof SerialBlock);
        SerialBlock bp = (SerialBlock) cell.getRootBlock();
        assertEquals(new Position(-1, -1, 0, 0, false, Orientation.VERTICAL), bp.getPosition());
        assertEquals("bbs1", bp.getStartingNode().getId());
        assertEquals("FICT_vl_d2Fictif", bp.getEndingNode().getId());
        assertEquals(3, bp.getSubBlocks().size());

        assertTrue(bp.getSubBlocks().get(0) instanceof LegPrimaryBlock);

        LegPrimaryBlock bc = (LegPrimaryBlock) bp.getSubBlocks().get(0);
        assertEquals(new Position(0, 0, 1, 0, false, Orientation.VERTICAL), bc.getPosition());

        assertTrue(bp.getSubBlocks().get(1) instanceof BodyPrimaryBlock);
        BodyPrimaryBlock bpyl = (BodyPrimaryBlock) bp.getSubBlocks().get(1);
        assertEquals(Node.NodeType.FICTITIOUS, bpyl.getStartingNode().getType());
        assertEquals(new Position(0, 1, 0, 0, false, Orientation.VERTICAL), bpyl.getPosition());

        assertTrue(bp.getSubBlocks().get(2) instanceof LegPrimaryBlock);
        LegPrimaryBlock bpyu = (LegPrimaryBlock) bp.getSubBlocks().get(2);
        assertEquals(Node.NodeType.SWITCH, bpyu.getNodes().get(1).getType());
        assertEquals(new Position(1, 2, 1, 0, false, Orientation.VERTICAL), bpyu.getPosition());

        // calculate coordinates
        LayoutParameters layoutParameters = new LayoutParameters()
                .setTranslateX(20)
                .setTranslateY(50)
                .setInitialXBus(0)
                .setInitialYBus(260)
                .setVerticalSpaceBus(25)
                .setHorizontalBusPadding(20)
                .setCellWidth(50)
                .setExternCellHeight(250)
                .setInternCellHeight(40)
                .setStackHeight(30)
                .setShowGrid(true)
                .setShowInternalNodes(true)
                .setScaleFactor(1)
                .setHorizontalSubstationPadding(50)
                .setVerticalSubstationPadding(50);

        new PositionVoltageLevelLayout(g).run(layoutParameters);

        // assert coordinate
        assertEquals(10, g.getNodes().get(0).getX(), 0);
        assertEquals(260, g.getNodes().get(0).getY(), 0);
        assertFalse(g.getNodes().get(0).isRotated());

        assertEquals(10, g.getNodes().get(1).getX(), 0);
        assertEquals(285, g.getNodes().get(1).getY(), 0);
        assertFalse(g.getNodes().get(1).isRotated());

        assertEquals(25, g.getNodes().get(2).getX(), 0);
        assertEquals(260, g.getNodes().get(2).getY(), 0);
        assertFalse(g.getNodes().get(2).isRotated());

        assertEquals(50, g.getNodes().get(3).getX(), 0);
        assertEquals(220, g.getNodes().get(3).getY(), 0);
        assertTrue(g.getNodes().get(3).isRotated());

        assertEquals(75, g.getNodes().get(4).getX(), 0);
        assertEquals(285, g.getNodes().get(4).getY(), 0);
        assertFalse(g.getNodes().get(4).isRotated());

        assertEquals(25, g.getNodes().get(5).getX(), 0);
        assertEquals(220, g.getNodes().get(5).getY(), 0);
        assertTrue(g.getNodes().get(5).isRotated());

        assertEquals(75, g.getNodes().get(6).getX(), 0);
        assertEquals(220, g.getNodes().get(6).getY(), 0);
        assertTrue(g.getNodes().get(6).isRotated());

        // write SVG and compare to reference
        compareSvg(g, layoutParameters, "/TestCase3Coupling.svg");
    }
}
