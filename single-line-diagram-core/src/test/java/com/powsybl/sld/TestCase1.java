/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.iidm.extensions.BusbarSectionPosition;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import com.powsybl.sld.layout.BlockOrganizer;
import com.powsybl.sld.layout.ImplicitCellDetector;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.PositionVoltageLevelLayout;
import com.powsybl.sld.model.*;
import org.junit.Before;
import org.junit.Test;

import static com.powsybl.sld.library.ComponentTypeName.*;
import static org.junit.Assert.*;

/**
 * <PRE>
 * l
 * |
 * b
 * |
 * d
 * |
 * ------ bbs
 * </PRE>
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TestCase1 extends AbstractTestCase {

    @Before
    public void setUp() {
        network = Network.create("testCase1", "test");
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
        BusbarSection bbs = view.newBusbarSection()
                .setId("bbs")
                .setNode(0)
                .add();
        bbs.addExtension(BusbarSectionPosition.class, new BusbarSectionPosition(bbs, 1, 1));
        Load l = vl.newLoad()
                .setId("l")
                .setNode(2)
                .setP0(10)
                .setQ0(10)
                .add();
        l.addExtension(ConnectablePosition.class, new ConnectablePosition<>(l, new ConnectablePosition
                .Feeder("l", 0, ConnectablePosition.Direction.TOP), null, null, null));
        view.newDisconnector()
                .setId("d")
                .setNode1(0)
                .setNode2(1)
                .add();
        view.newBreaker()
                .setId("b")
                .setNode1(1)
                .setNode2(2)
                .add();
    }

    @Test
    public void test() {
        // build graph
        Graph g = Graph.create(vl);

        // assert graph structure
        assertEquals(5, g.getNodes().size());

        assertEquals(Node.NodeType.BUS, g.getNodes().get(0).getType());
        assertEquals(Node.NodeType.FEEDER, g.getNodes().get(1).getType());
        assertEquals(Node.NodeType.FICTITIOUS, g.getNodes().get(2).getType());
        assertEquals(Node.NodeType.SWITCH, g.getNodes().get(3).getType());
        assertEquals(Node.NodeType.SWITCH, g.getNodes().get(4).getType());

        assertEquals("bbs", g.getNodes().get(0).getId());
        assertEquals("l", g.getNodes().get(1).getId());
        assertEquals("FICT_vl_1", g.getNodes().get(2).getId());
        assertEquals("d", g.getNodes().get(3).getId());
        assertEquals("b", g.getNodes().get(4).getId());

        assertEquals(BUSBAR_SECTION, g.getNodes().get(0).getComponentType());
        assertEquals(LOAD, g.getNodes().get(1).getComponentType());
        assertEquals(NODE, g.getNodes().get(2).getComponentType());
        assertEquals(DISCONNECTOR, g.getNodes().get(3).getComponentType());
        assertEquals(BREAKER, g.getNodes().get(4).getComponentType());

        assertEquals(1, g.getNodes().get(0).getAdjacentNodes().size());
        assertEquals(1, g.getNodes().get(1).getAdjacentNodes().size());
        assertEquals(2, g.getNodes().get(2).getAdjacentNodes().size());
        assertEquals(2, g.getNodes().get(3).getAdjacentNodes().size());
        assertEquals(2, g.getNodes().get(4).getAdjacentNodes().size());

        assertFalse(g.getNodes().get(0).isRotated());
        assertFalse(g.getNodes().get(1).isRotated());
        assertFalse(g.getNodes().get(2).isRotated());
        assertFalse(g.getNodes().get(3).isRotated());
        assertFalse(g.getNodes().get(4).isRotated());

        assertNull(g.getNodes().get(0).getCell());
        assertNull(g.getNodes().get(1).getCell());
        assertNull(g.getNodes().get(2).getCell());
        assertNull(g.getNodes().get(3).getCell());
        assertNull(g.getNodes().get(4).getCell());

        assertEquals(-1, g.getNodes().get(0).getX(), 0);
        assertEquals(-1, g.getNodes().get(0).getY(), 0);
        assertEquals(-1, g.getNodes().get(1).getX(), 0);
        assertEquals(-1, g.getNodes().get(1).getY(), 0);
        assertEquals(-1, g.getNodes().get(2).getX(), 0);
        assertEquals(-1, g.getNodes().get(2).getY(), 0);
        assertEquals(-1, g.getNodes().get(3).getX(), 0);
        assertEquals(-1, g.getNodes().get(3).getY(), 0);
        assertEquals(-1, g.getNodes().get(4).getX(), 0);
        assertEquals(-1, g.getNodes().get(4).getY(), 0);

        assertEquals(4, g.getEdges().size());
        assertEquals("bbs", g.getEdges().get(0).getNode1().getId());
        assertEquals("d", g.getEdges().get(0).getNode2().getId());
        assertEquals("d", g.getEdges().get(1).getNode1().getId());
        assertEquals("FICT_vl_1", g.getEdges().get(1).getNode2().getId());
        assertEquals("FICT_vl_1", g.getEdges().get(2).getNode1().getId());
        assertEquals("b", g.getEdges().get(2).getNode2().getId());
        assertEquals("b", g.getEdges().get(3).getNode1().getId());
        assertEquals("l", g.getEdges().get(3).getNode2().getId());

        assertTrue(g.getCells().isEmpty());

        // detect cells
        new ImplicitCellDetector().detectCells(g);

        // assert cells
        assertEquals(1, g.getCells().size());
        ExternCell externCell = (ExternCell) g.getCells().iterator().next();
        assertEquals(Cell.CellType.EXTERN, externCell.getType());
        assertEquals(-1, externCell.getOrder());
        assertEquals(5, externCell.getNodes().size());
        assertTrue(externCell.getPrimaryLegBlocks().isEmpty());
        assertEquals(1, externCell.getBusNodes().size());
        assertEquals("bbs", externCell.getBusNodes().get(0).getId());
        assertNull(externCell.getRootBlock());
//        assertTrue(externCell.getAbstractCellBridgingWith().isEmpty()); //TODO
        assertEquals("EXTERN[FICT_vl_dFictif, b, bbs, d, l]", externCell.getFullId());
        assertEquals(new Position(0, 0), externCell.getMaxBusPosition());

        new BlockOrganizer().organize(g);

        // assert blocks and nodes rotation
        assertNotNull(externCell.getRootBlock());
        assertTrue(externCell.getRootBlock() instanceof SerialBlock);
        SerialBlock bc = (SerialBlock) externCell.getRootBlock();
        assertEquals(new Position(0, 0, 1, 2, false, Orientation.VERTICAL), bc.getPosition());
        assertEquals("bbs", bc.getStartingNode().getId());
        assertEquals("l", bc.getEndingNode().getId());
        assertEquals(1, externCell.getPrimaryLegBlocks().size());

        assertTrue(bc.getUpperBlock() instanceof BodyPrimaryBlock);
        BodyPrimaryBlock ub = (BodyPrimaryBlock) bc.getUpperBlock();
        assertEquals(new Position(0, 0, 1, 2, false, Orientation.VERTICAL), ub.getPosition());
        assertEquals("FICT_vl_dFictif", ub.getStartingNode().getId());
        assertEquals("l", ub.getEndingNode().getId());
        assertTrue(ub.getStackableBlocks().isEmpty());

        assertTrue(bc.getLowerBlock() instanceof LegPrimaryBlock);
        LegPrimaryBlock lb = (LegPrimaryBlock) bc.getLowerBlock();
        assertEquals(new Position(0, 0, 1, 0, false, Orientation.VERTICAL), lb.getPosition());
        assertEquals("bbs", lb.getStartingNode().getId());
        assertEquals("FICT_vl_dFictif", lb.getEndingNode().getId());
        assertTrue(lb.getStackableBlocks().isEmpty());

        assertFalse(g.getNodes().get(0).isRotated());
        assertFalse(g.getNodes().get(1).isRotated());
        assertFalse(g.getNodes().get(2).isRotated());
        assertFalse(g.getNodes().get(3).isRotated());
        assertFalse(g.getNodes().get(4).isRotated());

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
                .setVerticalSubstationPadding(50)
                .setArrowDistance(20);

        new PositionVoltageLevelLayout(g).run(layoutParameters);

        // assert coordinate
        assertEquals(10, g.getNodes().get(0).getX(), 0);
        assertEquals(260, g.getNodes().get(0).getY(), 0);
        assertEquals(25, g.getNodes().get(1).getX(), 0);
        assertEquals(-20, g.getNodes().get(1).getY(), 0);
        assertEquals(25, g.getNodes().get(2).getX(), 0);
        assertEquals(260, g.getNodes().get(2).getY(), 0);
        assertEquals(25, g.getNodes().get(3).getX(), 0);
        assertEquals(105, g.getNodes().get(3).getY(), 0);
        assertEquals(25, g.getNodes().get(4).getX(), 0);
        assertEquals(230, g.getNodes().get(4).getY(), 0);

        // write SVG and compare to reference
        compareSvg(g, layoutParameters, "/TestCase1.svg");
    }
}
