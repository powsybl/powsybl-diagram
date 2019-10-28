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
import com.powsybl.sld.model.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public class TestSerialBlock extends AbstractTestCase {

    @Before
    public void setUp() {
        Network network = Network.create("testCase1", "test");
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

        BusbarSection bbs11 = view.newBusbarSection()
                .setId("bbs")
                .setNode(0)
                .add();
        bbs11.addExtension(BusbarSectionPosition.class, new BusbarSectionPosition(bbs11, 1, 1));

        Load la = vl.newLoad()
                .setId("la")
                .setNode(2)
                .setP0(10)
                .setQ0(10)
                .add();
        la.addExtension(ConnectablePosition.class, new ConnectablePosition<>(la, new ConnectablePosition
                .Feeder("la", 10, ConnectablePosition.Direction.TOP), null, null, null));

        view.newBreaker()
                .setId("ba")
                .setNode1(2)
                .setNode2(1)
                .add();

        view.newDisconnector()
                .setId("da")
                .setNode1(1)
                .setNode2(0)
                .add();
    }

    @Test
    public void test() {
        // build graph
        Graph g = graphBuilder.buildVoltageLevelGraph(vl.getId(), false, true, false);
        // detect cells
        new ImplicitCellDetector().detectCells(g);
        new BlockOrganizer().organize(g);

        assertEquals(1, g.getCells().size());
        Cell cell = g.getCells().iterator().next();
        assertEquals(Block.Type.SERIAL, cell.getRootBlock().getType());
        SerialBlock sb = (SerialBlock) cell.getRootBlock();
        assertTrue(sb.isEmbedingNodeType(Node.NodeType.BUS));
        assertTrue(sb.isEmbedingNodeType(Node.NodeType.FEEDER));
        assertTrue(sb.getLowerBlock().isEmbedingNodeType(Node.NodeType.BUS));
        assertTrue(sb.getUpperBlock().isEmbedingNodeType(Node.NodeType.FEEDER));
        assertTrue(sb.getSubBlocks().get(0).isEmbedingNodeType(Node.NodeType.BUS));
        assertTrue(sb.getSubBlocks().get(1).isEmbedingNodeType(Node.NodeType.FEEDER));

        assertEquals("bbs", sb.getSubBlocks().get(0).getStartingNode().getId());
        assertEquals("FICT_vl_daFictif", sb.getSubBlocks().get(0).getEndingNode().getId());
        assertEquals("FICT_vl_daFictif", sb.getSubBlocks().get(1).getStartingNode().getId());
        assertEquals("la", sb.getSubBlocks().get(1).getEndingNode().getId());

        sb.sizing();
        assertEquals(0, sb.getPosition().getH());
        assertEquals(0, sb.getPosition().getV());
        assertEquals(1, sb.getPosition().getHSpan());
        assertEquals(2, sb.getPosition().getVSpan());

        assertEquals(0, sb.getLowerBlock().getPosition().getH());
        assertEquals(0, sb.getLowerBlock().getPosition().getV());
        assertEquals(1, sb.getLowerBlock().getPosition().getHSpan());
        assertEquals(0, sb.getLowerBlock().getPosition().getVSpan());

        assertEquals(0, sb.getUpperBlock().getPosition().getH());
        assertEquals(0, sb.getUpperBlock().getPosition().getV());
        assertEquals(1, sb.getUpperBlock().getPosition().getHSpan());
        assertEquals(2, sb.getUpperBlock().getPosition().getVSpan());

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

        sb.setX(10);
        sb.setY(20);
        sb.setXSpan(100);
        sb.setYSpan(200);
        sb.coordHorizontalCase(layoutParameters);

        assertEquals(10, sb.getLowerBlock().getCoord().getX(), 0);
        assertEquals(20, sb.getLowerBlock().getCoord().getY(), 0);
        assertEquals(100, sb.getLowerBlock().getCoord().getXSpan(), 0);
        assertEquals(200, sb.getLowerBlock().getCoord().getYSpan(), 0);

        assertEquals(10, sb.getUpperBlock().getCoord().getX(), 0);
        assertEquals(20, sb.getUpperBlock().getCoord().getY(), 0);
        assertEquals(100, sb.getUpperBlock().getCoord().getXSpan(), 0);
        assertEquals(200, sb.getUpperBlock().getCoord().getYSpan(), 0);

        sb.reverseBlock();

        assertEquals("FICT_vl_daFictif", sb.getSubBlocks().get(1).getEndingNode().getId());
        assertEquals("bbs", sb.getSubBlocks().get(1).getStartingNode().getId());
        assertEquals("FICT_vl_daFictif", sb.getSubBlocks().get(0).getEndingNode().getId());
        assertEquals("la", sb.getSubBlocks().get(0).getStartingNode().getId());
    }
}
