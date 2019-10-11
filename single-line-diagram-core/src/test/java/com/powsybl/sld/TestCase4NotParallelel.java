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

import java.util.Iterator;

import static org.junit.Assert.*;


/**
 * <pre>
 *            la                        gc
 *             |                        |
 *            ba                        bc
 *           /  \                       |
 *          |    |                      |
 * bbs1.1 -da1---|--- ss1 --db1--------dc- bbs1.2
 * bbs2.1 ------da2----------|---db2------
 *                           |    |
 *                            \  /
 *                             bb
 *                              |
 *                             lb
 *
 * </pre>
 * <p>
 * the branch c is to cover the merging part of SubSections class (and use of generator)
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TestCase4NotParallelel extends AbstractTestCase {

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

        Load la = vl.newLoad()
                .setId("la")
                .setNode(3)
                .setP0(10)
                .setQ0(10)
                .add();
        la.addExtension(ConnectablePosition.class, new ConnectablePosition<>(la, new ConnectablePosition
                .Feeder("la", 10, ConnectablePosition.Direction.TOP), null, null, null));

        view.newBreaker()
                .setId("ba")
                .setNode1(3)
                .setNode2(4)
                .add();

        view.newDisconnector()
                .setId("da1")
                .setNode1(4)
                .setNode2(0)
                .add();

        view.newDisconnector()
                .setId("da2")
                .setNode1(4)
                .setNode2(2)
                .add();

        Load lb = vl.newLoad()
                .setId("lb")
                .setNode(5)
                .setP0(10)
                .setQ0(10)
                .add();
        lb.addExtension(ConnectablePosition.class, new ConnectablePosition<>(lb, new ConnectablePosition
                .Feeder("lb", 20, ConnectablePosition.Direction.BOTTOM), null, null, null));

        view.newBreaker()
                .setId("bb")
                .setNode1(5)
                .setNode2(6)
                .add();

        view.newDisconnector()
                .setId("db1")
                .setNode1(6)
                .setNode2(1)
                .add();

        view.newDisconnector()
                .setId("db2")
                .setNode1(6)
                .setNode2(2)
                .add();
        view.newDisconnector()
                .setId("ss1")
                .setNode1(1)
                .setNode2(0)
                .add();

        Generator gc = vl.newGenerator()
                .setId("gc")
                .setNode(7)
                .setMinP(0)
                .setMaxP(20)
                .setVoltageRegulatorOn(false)
                .setTargetP(10)
                .setTargetQ(10)
                .add();
        gc.addExtension(ConnectablePosition.class, new ConnectablePosition<>(gc, new ConnectablePosition
                .Feeder("gc", 30, ConnectablePosition.Direction.TOP), null, null, null));

        view.newBreaker()
                .setId("bc")
                .setNode1(7)
                .setNode2(8)
                .add();

        view.newDisconnector()
                .setId("dc1")
                .setNode1(8)
                .setNode2(1)
                .add();
    }

    @Test
    public void test() {
        // build graph
        Graph g = Graph.create(vl);

        // assert graph structure
        assertEquals(18, g.getNodes().size());

        // detect cells
        new ImplicitCellDetector().detectCells(g);

        // assert cells
        assertEquals(4, g.getCells().size());
        Iterator<Cell> it = g.getCells().iterator();
        Cell cell = it.next();
        assertEquals(Cell.CellType.INTERN, cell.getType());
        assertEquals(3, cell.getNodes().size());
        assertEquals(2, ((BusCell) cell).getBusNodes().size());
        assertEquals("INTERN[bbs1.1, bbs1.2, ss1]", cell.getFullId());

        // build blocks
        new BlockOrganizer().organize(g);

        // assert blocks and nodes rotation
        assertEquals(2, ((BusCell) cell).getPrimaryLegBlocks().size());
        assertNotNull(cell.getRootBlock());
        assertTrue(cell.getRootBlock() instanceof SerialBlock);
        assertEquals(new Position(-1, -1, 0, 0, false, Orientation.HORIZONTAL), cell.getRootBlock().getPosition());

        cell = it.next();
        assertTrue(cell.getRootBlock() instanceof SerialBlock);
        assertEquals(BusCell.Direction.TOP, ((BusCell) cell).getDirection());
        SerialBlock bc = (SerialBlock) cell.getRootBlock();
        assertEquals(new Position(0, 0, 1, 2, false, Orientation.VERTICAL), bc.getPosition());

        BodyPrimaryBlock byu = (BodyPrimaryBlock) bc.getUpperBlock();
        assertEquals(new Position(0, 0, 1, 2, false, Orientation.VERTICAL), byu.getPosition());

        LegParralelBlock bpl = (LegParralelBlock) bc.getLowerBlock();
        assertEquals(new Position(0, 0, 1, 0, false, Orientation.VERTICAL), bpl.getPosition());

        cell = it.next();
        assertTrue(cell.getRootBlock() instanceof SerialBlock);
        assertEquals(BusCell.Direction.BOTTOM, ((BusCell) cell).getDirection());
        bc = (SerialBlock) cell.getRootBlock();
        assertEquals(new Position(2, 0, 1, 2, false, Orientation.VERTICAL), bc.getPosition());

        byu = (BodyPrimaryBlock) bc.getUpperBlock();
        assertEquals(new Position(0, 0, 1, 2, false, Orientation.VERTICAL), byu.getPosition());

        bpl = (LegParralelBlock) bc.getLowerBlock();
        assertEquals(new Position(0, 0, 1, 0, false, Orientation.VERTICAL), bpl.getPosition());

        cell = it.next();
        assertTrue(cell.getRootBlock() instanceof SerialBlock);
        assertEquals(BusCell.Direction.TOP, ((BusCell) cell).getDirection());
        SerialBlock bc3 = (SerialBlock) cell.getRootBlock();
        assertEquals(new Position(3, 0, 1, 2, false, Orientation.VERTICAL), bc3.getPosition());

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

        // write SVG and compare to reference
        compareSvg(g, layoutParameters, "/TestCase4NotParallelel.svg");
    }
}
