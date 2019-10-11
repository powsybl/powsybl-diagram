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
public class TestCase1inverted extends AbstractTestCase {

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
        Load l = vl.newLoad()
                .setId("l")
                .setNode(0)
                .setP0(10)
                .setQ0(10)
                .add();
        l.addExtension(ConnectablePosition.class, new ConnectablePosition<>(l, new ConnectablePosition
                .Feeder("l", 0, ConnectablePosition.Direction.TOP), null, null, null));
        view.newDisconnector()
                .setId("d")
                .setNode1(2)
                .setNode2(1)
                .add();
        view.newBreaker()
                .setId("b")
                .setNode1(1)
                .setNode2(0)
                .add();
        BusbarSection bbs = view.newBusbarSection()
                .setId("bbs")
                .setNode(2)
                .add();
        bbs.addExtension(BusbarSectionPosition.class, new BusbarSectionPosition(bbs, 1, 1));
    }

    @Test
    public void test() {
        // build graph
        Graph g = Graph.create(vl);

        // assert graph structure
        assertEquals(5, g.getNodes().size());

        assertEquals("l", g.getNodes().get(0).getId());
        assertEquals("bbs", g.getNodes().get(1).getId());
        assertEquals("FICT_vl_1", g.getNodes().get(2).getId());
        assertEquals("d", g.getNodes().get(3).getId());
        assertEquals("b", g.getNodes().get(4).getId());

        // detect cells
        new ImplicitCellDetector().detectCells(g);

        // assert cells
        assertEquals(1, g.getCells().size());
        Cell cell = g.getCells().iterator().next();
        assertEquals(Cell.CellType.EXTERN, cell.getType());
        assertEquals(5, cell.getNodes().size());
        assertTrue(((BusCell) cell).getPrimaryLegBlocks().isEmpty());
        assertEquals(1, ((BusCell) cell).getBusNodes().size());
        assertEquals("bbs", ((BusCell) cell).getBusNodes().get(0).getId());
        assertNull(cell.getRootBlock());
        assertEquals("EXTERN[FICT_vl_dFictif, b, bbs, d, l]", cell.getFullId());
        assertEquals(new Position(0, 0), ((BusCell) cell).getMaxBusPosition());

        // build blocks
        new BlockOrganizer().organize(g);

        // assert blocks and nodes rotation
        assertNotNull(cell.getRootBlock());
        assertTrue(cell.getRootBlock() instanceof SerialBlock);
        SerialBlock bc = (SerialBlock) cell.getRootBlock();
        assertEquals(new Position(0, 0, 1, 2, false, Orientation.VERTICAL), bc.getPosition());
        assertEquals("bbs", bc.getStartingNode().getId());
        assertEquals("l", bc.getEndingNode().getId());
        assertEquals(1, ((BusCell) cell).getPrimaryLegBlocks().size());

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
        assertEquals(25, g.getNodes().get(0).getX(), 0);
        assertEquals(-20, g.getNodes().get(0).getY(), 0);
        assertEquals(10, g.getNodes().get(1).getX(), 0);
        assertEquals(260, g.getNodes().get(1).getY(), 0);
        assertEquals(25, g.getNodes().get(2).getX(), 0);
        assertEquals(260, g.getNodes().get(2).getY(), 0);
        assertEquals(25, g.getNodes().get(3).getX(), 0);
        assertEquals(105, g.getNodes().get(3).getY(), 0);
        assertEquals(25, g.getNodes().get(4).getX(), 0);
        assertEquals(230, g.getNodes().get(4).getY(), 0);

        // write SVG and compare to reference
        compareSvg(g, layoutParameters, "/TestCase1inverted.svg");
    }
}
