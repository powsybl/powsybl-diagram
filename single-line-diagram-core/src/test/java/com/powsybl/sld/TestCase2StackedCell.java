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

import static com.powsybl.sld.library.ComponentTypeName.NODE;
import static org.junit.Assert.*;

/**
 * <pre>
 *     l
 *     |
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
public class TestCase2StackedCell extends AbstractTestCase {

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
        BusbarSection bbs1 = view.newBusbarSection()
                .setId("bbs1")
                .setNode(0)
                .add();
        bbs1.addExtension(BusbarSectionPosition.class, new BusbarSectionPosition(bbs1, 1, 1));
        BusbarSection bbs2 = view.newBusbarSection()
                .setId("bbs2")
                .setNode(1)
                .add();
        bbs2.addExtension(BusbarSectionPosition.class, new BusbarSectionPosition(bbs2, 2, 1));
        Load l = vl.newLoad()
                .setId("l")
                .setNode(3)
                .setP0(10)
                .setQ0(10)
                .add();
        l.addExtension(ConnectablePosition.class, new ConnectablePosition<>(l, new ConnectablePosition
                .Feeder("l", 0, ConnectablePosition.Direction.TOP), null, null, null));
        view.newDisconnector()
                .setId("d1")
                .setNode1(0)
                .setNode2(2)
                .add();
        view.newDisconnector()
                .setId("d2")
                .setNode1(1)
                .setNode2(2)
                .add();
        view.newBreaker()
                .setId("b")
                .setNode1(2)
                .setNode2(3)
                .add();
    }

    @Test
    public void test() {
        // build graph
        Graph g = Graph.create(vl);

        // assert graph structure
        assertEquals(7, g.getNodes().size());
        assertEquals(Node.NodeType.FICTITIOUS, g.getNodes().get(3).getType());
        assertEquals("FICT_vl_2", g.getNodes().get(3).getId());
        assertEquals(NODE, g.getNodes().get(3).getComponentType());

        assertEquals(6, g.getEdges().size());
        assertEquals("d1", g.getEdges().get(1).getNode1().getId());
        assertEquals("FICT_vl_2", g.getEdges().get(1).getNode2().getId());
        assertEquals("d2", g.getEdges().get(3).getNode1().getId());
        assertEquals("FICT_vl_2", g.getEdges().get(3).getNode2().getId());
        assertEquals("FICT_vl_2", g.getEdges().get(4).getNode1().getId());
        assertEquals("b", g.getEdges().get(4).getNode2().getId());

        // detect cells
        new ImplicitCellDetector().detectCells(g);

        // assert cells
        assertEquals(1, g.getCells().size());
        Cell cell = g.getCells().iterator().next();
        assertEquals(Cell.CellType.EXTERN, cell.getType());
        assertEquals(-1, ((ExternCell) cell).getOrder());
        assertEquals(7, cell.getNodes().size());
        assertEquals(2, ((BusCell) cell).getBusNodes().size());
        assertEquals("EXTERN[FICT_vl_2, b, bbs1, bbs2, d1, d2, l]", cell.getFullId());

        // build blocks
        new BlockOrganizer().organize(g);

        // assert blocks and nodes rotation
        assertEquals(2, ((BusCell) cell).getPrimaryLegBlocks().size());
        assertNotNull(cell.getRootBlock());
        assertTrue(cell.getRootBlock() instanceof SerialBlock);
        SerialBlock bc = (SerialBlock) cell.getRootBlock();
        assertEquals(new Coord(-1, -1), bc.getCoord());
        assertEquals(new Position(0, 0, 1, 2, false, Orientation.VERTICAL), bc.getPosition());
        assertEquals("bbs1", bc.getStartingNode().getId());
        assertEquals("l", bc.getEndingNode().getId());

        assertTrue(bc.getUpperBlock() instanceof BodyPrimaryBlock);
        BodyPrimaryBlock bpy = (BodyPrimaryBlock) bc.getUpperBlock();
        assertEquals(bc, bpy.getParentBlock());
        assertEquals(new Coord(-1, -1), bpy.getCoord());
        assertEquals(new Position(0, 0, 1, 2, false, Orientation.VERTICAL), bpy.getPosition());
        assertEquals("FICT_vl_2", bpy.getStartingNode().getId());
        assertEquals("l", bpy.getEndingNode().getId());
        assertTrue(bpy.getStackableBlocks().isEmpty());

        assertTrue(bc.getLowerBlock() instanceof LegParralelBlock);
        LegParralelBlock bpl = (LegParralelBlock) bc.getLowerBlock();
        assertEquals(bc, bpl.getParentBlock());
        assertEquals(new Position(0, 0, 1, 0, false, Orientation.VERTICAL), bpl.getPosition());
        assertEquals(new Coord(-1, -1), bpl.getCoord());
        assertEquals("bbs1", bpl.getStartingNode().getId());
        assertEquals("FICT_vl_2", bpl.getEndingNode().getId());
        assertEquals(2, bpl.getSubBlocks().size());

        assertTrue(bpl.getSubBlocks().get(0) instanceof LegPrimaryBlock);
        LegPrimaryBlock bpy1 = (LegPrimaryBlock) bpl.getSubBlocks().get(0);
        assertEquals(new Position(0, 0, 0, 0, false, Orientation.VERTICAL), bpy1.getPosition());
        assertEquals(new Coord(-1, -1), bpy1.getCoord());
        assertEquals("FICT_vl_2", bpy1.getEndingNode().getId());
        assertEquals("bbs1", bpy1.getStartingNode().getId());
        assertEquals(1, bpy1.getStackableBlocks().size());

        assertTrue(bpl.getSubBlocks().get(1) instanceof LegPrimaryBlock);
        LegPrimaryBlock bpy2 = (LegPrimaryBlock) bpl.getSubBlocks().get(1);
        assertEquals(new Position(0, 0, 0, 0, false, Orientation.VERTICAL), bpy2.getPosition());
        assertEquals(new Coord(-1, -1), bpy2.getCoord());
        assertEquals("FICT_vl_2", bpy2.getEndingNode().getId());
        assertEquals("bbs2", bpy2.getStartingNode().getId());
        assertEquals(1, bpy2.getStackableBlocks().size());

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
        assertEquals(10, g.getNodes().get(1).getX(), 0);
        assertEquals(285, g.getNodes().get(1).getY(), 0);
        assertEquals(25, g.getNodes().get(2).getX(), 0);
        assertEquals(-20, g.getNodes().get(2).getY(), 0);
        assertEquals(25, g.getNodes().get(3).getX(), 0);
        assertEquals(230, g.getNodes().get(3).getY(), 0);
        assertEquals(25, g.getNodes().get(4).getX(), 0);
        assertEquals(260, g.getNodes().get(4).getY(), 0);
        assertEquals(25, g.getNodes().get(5).getX(), 0);
        assertEquals(285, g.getNodes().get(5).getY(), 0);
        assertEquals(25, g.getNodes().get(6).getX(), 0);
        assertEquals(105, g.getNodes().get(6).getY(), 0);

        // write SVG and compare to reference
        compareSvg(g, layoutParameters, "/TestCase2StackedCell.svg");
    }
}
