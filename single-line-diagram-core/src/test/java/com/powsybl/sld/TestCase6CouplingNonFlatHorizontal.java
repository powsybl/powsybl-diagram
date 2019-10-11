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

import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * <pre>
 *              b
 *           /     \
 *          |       |
 * bbs1.1 -d1- ds1 -|-- bbs1.2
 * bbs2.1 ---- ds2 -d2- bbs2.2
 *
 * </pre>
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TestCase6CouplingNonFlatHorizontal extends AbstractTestCase {

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

        BusbarSection bbs11 = view.newBusbarSection()
                .setId("bbs1.1")
                .setNode(0)
                .add();
        bbs11.addExtension(BusbarSectionPosition.class, new BusbarSectionPosition(bbs11, 1, 1));

        BusbarSection bbs12 = view.newBusbarSection()
                .setId("bbs1.2")
                .setNode(1)
                .add();
        bbs12.addExtension(BusbarSectionPosition.class, new BusbarSectionPosition(bbs12, 1, 2));

        BusbarSection bbs21 = view.newBusbarSection()
                .setId("bbs2.1")
                .setNode(2)
                .add();
        bbs21.addExtension(BusbarSectionPosition.class, new BusbarSectionPosition(bbs21, 2, 1));

        BusbarSection bbs22 = view.newBusbarSection()
                .setId("bbs2.2")
                .setNode(3)
                .add();
        bbs22.addExtension(BusbarSectionPosition.class, new BusbarSectionPosition(bbs22, 2, 2));

        view.newDisconnector()
                .setId("d1")
                .setNode1(0)
                .setNode2(4)
                .add();

        view.newBreaker()
                .setId("b")
                .setNode1(4)
                .setNode2(5)
                .add();

        view.newDisconnector()
                .setId("d2")
                .setNode1(5)
                .setNode2(3)
                .add();

        view.newDisconnector()
                .setId("ds1")
                .setNode1(0)
                .setNode2(1)
                .add();

        view.newDisconnector()
                .setId("ds2")
                .setNode1(2)
                .setNode2(3)
                .add();
    }

    @Test
    public void test() {
        // build graph
        Graph g = Graph.create(vl);

        // assert graph structure
        assertEquals(11, g.getNodes().size());

        // detect cells
        new ImplicitCellDetector().detectCells(g);

        assertEquals(11, g.getNodes().size());

        // assert cells
        assertEquals(3, g.getCells().size());
        Iterator<Cell> it = g.getCells().iterator();
        Cell cellB = it.next();
        assertEquals("INTERN[FICT_vl_d1Fictif, FICT_vl_d2Fictif, b, bbs1.1, bbs2.2, d1, d2]", cellB.getFullId());
        Cell cellD1 = it.next();
        assertEquals("INTERN[bbs1.1, bbs1.2, ds1]", cellD1.getFullId());
        Cell cellD2 = it.next();
        assertEquals("INTERN[bbs2.1, bbs2.2, ds2]", cellD2.getFullId());

        // build blocks
        new BlockOrganizer().organize(g);

        // assert blocks and nodes rotation
        assertEquals(2, ((BusCell) cellB).getPrimaryLegBlocks().size());
        assertNotNull(cellB.getRootBlock());
        assertTrue(cellB.getRootBlock() instanceof SerialBlock);
        SerialBlock bp = (SerialBlock) cellB.getRootBlock();
        assertEquals(new Position(-1, -1, 0, 0, false, Orientation.VERTICAL), bp.getPosition());

        assertTrue(bp.getSubBlocks().get(0) instanceof LegPrimaryBlock);

        LegPrimaryBlock bc = (LegPrimaryBlock) bp.getSubBlocks().get(0);
        assertEquals(new Position(0, 0, 1, 0, false, Orientation.VERTICAL), bc.getPosition());

        assertTrue(bp.getSubBlocks().get(1) instanceof BodyPrimaryBlock);
        BodyPrimaryBlock bpyl = (BodyPrimaryBlock) bp.getSubBlocks().get(1);
        assertEquals(new Position(0, 1, 0, 0, false, Orientation.VERTICAL), bpyl.getPosition());

        assertTrue(bp.getSubBlocks().get(2) instanceof LegPrimaryBlock);
        LegPrimaryBlock bpyu = (LegPrimaryBlock) bp.getSubBlocks().get(2);
        assertEquals(new Position(2, 2, 1, 0, false, Orientation.VERTICAL), bpyu.getPosition());

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

        assertEquals(110, g.getNodes().get(3).getX(), 0);
        assertEquals(285, g.getNodes().get(3).getY(), 0);
        assertFalse(g.getNodes().get(3).isRotated());

        assertEquals(50, g.getNodes().get(5).getX(), 0);
        assertEquals(220, g.getNodes().get(5).getY(), 0);
        assertTrue(g.getNodes().get(5).isRotated());

        assertEquals(25, g.getNodes().get(9).getX(), 0);
        assertEquals(220, g.getNodes().get(9).getY(), 0);
        assertTrue(g.getNodes().get(9).isRotated());

        assertEquals(125, g.getNodes().get(10).getX(), 0);
        assertEquals(220, g.getNodes().get(10).getY(), 0);
        assertTrue(g.getNodes().get(10).isRotated());

        assertEquals(75, g.getNodes().get(7).getX(), 0);
        assertEquals(260, g.getNodes().get(7).getY(), 0);
        assertTrue(g.getNodes().get(7).isRotated());

        assertEquals(75, g.getNodes().get(8).getX(), 0);
        assertEquals(285, g.getNodes().get(8).getY(), 0);
        assertTrue(g.getNodes().get(8).isRotated());

        // write SVG and compare to reference
        compareSvg(g, layoutParameters, "/TestCase6CouplingNonFlatHorizontal.svg");
    }
}
