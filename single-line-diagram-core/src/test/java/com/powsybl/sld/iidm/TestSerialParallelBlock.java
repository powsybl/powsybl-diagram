/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import com.powsybl.sld.layout.BlockOrganizer;
import com.powsybl.sld.layout.ImplicitCellDetector;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.model.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class TestSerialParallelBlock extends AbstractTestCaseIidm {

    @Before
    public void setUp() {
        Network network = Network.create("testCase1", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = createSubstation(network, "s", "s", Country.FR);
        vl = createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 400, 10);
        createBusBarSection(vl, "bbs", "bbs", 0, 1, 1);
        createSwitch(vl, "da", "da", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        createSwitch(vl, "ba", "ba", SwitchKind.BREAKER, false, false, false, 1, 2);
        createSwitch(vl, "ba1", "ba1", SwitchKind.BREAKER, false, false, false, 2, 4);
        createSwitch(vl, "ba2", "ba2", SwitchKind.BREAKER, false, false, false, 2, 5);
        createLoad(vl, "la1", "la1", "la1", 10, ConnectablePosition.Direction.TOP, 4, 10, 10);
        createLoad(vl, "la2", "la2", "la2", 10, ConnectablePosition.Direction.TOP, 5, 10, 10);
    }

    @Test
    public void test() {
        // build graph
        Graph g = graphBuilder.buildVoltageLevelGraph(vl.getId(), false, true);

        // detect cells
        new ImplicitCellDetector().detectCells(g);
        new BlockOrganizer().organize(g);

        assertEquals(1, g.getCells().size());
        Cell cell = g.getCells().iterator().next();
        assertEquals(Block.Type.SERIAL, cell.getRootBlock().getType());
        SerialBlock sb = (SerialBlock) cell.getRootBlock();
        assertTrue(sb.isEmbeddingNodeType(Node.NodeType.BUS));
        assertTrue(sb.isEmbeddingNodeType(Node.NodeType.FEEDER));
        assertTrue(sb.getLowerBlock().isEmbeddingNodeType(Node.NodeType.BUS));
        assertTrue(sb.getUpperBlock().isEmbeddingNodeType(Node.NodeType.FEEDER));
        assertTrue(sb.getSubBlocks().get(0).isEmbeddingNodeType(Node.NodeType.BUS));
        assertTrue(sb.getSubBlocks().get(1).isEmbeddingNodeType(Node.NodeType.SWITCH));

        assertSame(Block.Type.LEGPRIMARY, sb.getLowerBlock().getType());
        LegPrimaryBlock subSB = (LegPrimaryBlock) sb.getLowerBlock();
        assertSame(Block.Type.BODYPARALLEL, sb.getUpperBlock().getType());
        BodyParallelBlock subPB = (BodyParallelBlock) sb.getUpperBlock();

        assertEquals("bbs", sb.getStartingNode().getId());
        assertEquals("bbs", subSB.getStartingNode().getId());
        assertEquals("FICT_vl_2", subPB.getSubBlocks().get(0).getStartingNode().getId());

        sb.sizing();
        assertEquals(0, sb.getPosition().getH());
        assertEquals(0, sb.getPosition().getV());
        assertEquals(2, sb.getPosition().getHSpan());
        assertEquals(4, sb.getPosition().getVSpan());

        assertEquals(0, subSB.getPosition().getH());
        assertEquals(0, subSB.getPosition().getV());
        assertEquals(1, subSB.getPosition().getHSpan());
        assertEquals(0, subSB.getPosition().getVSpan());

        assertEquals(0, subPB.getSubBlocks().get(0).getPosition().getH());
        assertEquals(0, subPB.getSubBlocks().get(0).getPosition().getV());
        assertEquals(1, subPB.getSubBlocks().get(0).getPosition().getHSpan());
        assertEquals(2, subPB.getSubBlocks().get(0).getPosition().getVSpan());

        assertEquals(1, subPB.getSubBlocks().get(1).getPosition().getH());
        assertEquals(0, subPB.getSubBlocks().get(1).getPosition().getV());
        assertEquals(1, subPB.getSubBlocks().get(1).getPosition().getHSpan());
        assertEquals(2, subPB.getSubBlocks().get(1).getPosition().getVSpan());

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

        assertEquals(10, sb.getCoord().getX(), 0);
        assertEquals(20, sb.getCoord().getY(), 0);
        assertEquals(100, sb.getCoord().getXSpan(), 0);
        assertEquals(200, sb.getCoord().getYSpan(), 0);

        assertEquals(-15, subSB.getCoord().getX(), 0);
        assertEquals(20, subSB.getCoord().getY(), 0);
        assertEquals(50, subSB.getCoord().getXSpan(), 0);
        assertEquals(200, subSB.getCoord().getYSpan(), 0);

        assertEquals(-15, subPB.getSubBlocks().get(0).getCoord().getX(), 0);
        assertEquals(20, subPB.getSubBlocks().get(0).getCoord().getY(), 0);
        assertEquals(50, subPB.getSubBlocks().get(0).getCoord().getXSpan(), 0);
        assertEquals(200, subPB.getSubBlocks().get(0).getCoord().getYSpan(), 0);

        assertEquals(35, subPB.getSubBlocks().get(1).getCoord().getX(), 0);
        assertEquals(20, subPB.getSubBlocks().get(1).getCoord().getY(), 0);
        assertEquals(50, subPB.getSubBlocks().get(1).getCoord().getXSpan(), 0);
        assertEquals(200, subPB.getSubBlocks().get(1).getCoord().getYSpan(), 0);

        sb.reverseBlock();

        assertEquals("FICT_vl_daFictif", sb.getEndingNode().getId());
        assertEquals("FICT_vl_daFictif", subSB.getEndingNode().getId());
        assertEquals("FICT_vl_2", subPB.getSubBlocks().get(1).getEndingNode().getId());
    }
}
