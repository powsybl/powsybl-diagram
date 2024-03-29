/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.layout.*;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.layout.CalculateCoordBlockVisitor;
import com.powsybl.sld.layout.LayoutContext;
import com.powsybl.sld.model.blocks.Block;
import com.powsybl.sld.model.blocks.BodyParallelBlock;
import com.powsybl.sld.model.blocks.LegPrimaryBlock;
import com.powsybl.sld.model.blocks.SerialBlock;
import com.powsybl.sld.model.cells.Cell;
import com.powsybl.sld.model.coordinate.Orientation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.powsybl.sld.model.coordinate.Coord.Dimension.X;
import static com.powsybl.sld.model.coordinate.Coord.Dimension.Y;
import static com.powsybl.sld.model.coordinate.Position.Dimension.H;
import static com.powsybl.sld.model.coordinate.Position.Dimension.V;
import static com.powsybl.sld.model.nodes.Node.NodeType.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
class TestSerialParallelBlock extends AbstractTestCaseIidm {

    @BeforeEach
    public void setUp() {
        Network network = Network.create("testCase1", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = Networks.createSubstation(network, "s", "s", Country.FR);
        vl = Networks.createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380);
        Networks.createBusBarSection(vl, "bbs", "bbs", 0, 1, 1);
        Networks.createSwitch(vl, "da", "da", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        Networks.createSwitch(vl, "ba", "ba", SwitchKind.BREAKER, false, false, false, 1, 2);
        Networks.createSwitch(vl, "ba1", "ba1", SwitchKind.BREAKER, false, false, false, 2, 4);
        Networks.createSwitch(vl, "ba2", "ba2", SwitchKind.BREAKER, false, false, false, 2, 5);
        Networks.createLoad(vl, "la1", "la1", "la1", 10, ConnectablePosition.Direction.TOP, 4, 10, 10);
        Networks.createLoad(vl, "la2", "la2", "la2", 10, ConnectablePosition.Direction.TOP, 5, 10, 10);
    }

    @Test
    void test() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        // detect cells
        new PositionVoltageLevelLayoutFactory().create(g).run(layoutParameters);

        assertEquals(1, g.getCellStream().count());
        Optional<Cell> oCell = g.getCellStream().findFirst();
        assertTrue(oCell.isPresent());
        Cell cell = oCell.get();
        assertEquals(Block.Type.SERIAL, cell.getRootBlock().getType());
        SerialBlock sb = (SerialBlock) cell.getRootBlock();
        assertTrue(sb.isEmbeddingNodeType(BUS));
        assertTrue(sb.isEmbeddingNodeType(FEEDER));
        assertTrue(sb.getLowerBlock().isEmbeddingNodeType(BUS));
        assertTrue(sb.getUpperBlock().isEmbeddingNodeType(FEEDER));
        assertTrue(sb.getSubBlocks().get(0).isEmbeddingNodeType(BUS));
        assertTrue(sb.getSubBlocks().get(1).isEmbeddingNodeType(SWITCH));

        assertSame(Block.Type.LEGPRIMARY, sb.getLowerBlock().getType());
        LegPrimaryBlock subSB = (LegPrimaryBlock) sb.getLowerBlock();
        assertSame(Block.Type.BODYPARALLEL, sb.getUpperBlock().getType());
        BodyParallelBlock subPB = (BodyParallelBlock) sb.getUpperBlock();

        assertEquals("bbs", sb.getStartingNode().getId());
        assertEquals("bbs", subSB.getStartingNode().getId());
        assertEquals("INTERNAL_vl_2", subPB.getSubBlocks().get(0).getStartingNode().getId());

        sb.sizing();
        assertEquals(0, sb.getPosition().get(H));
        assertEquals(0, sb.getPosition().get(V));
        assertEquals(4, sb.getPosition().getSpan(H));
        assertEquals(8, sb.getPosition().getSpan(V));

        assertEquals(0, subSB.getPosition().get(H));
        assertEquals(0, subSB.getPosition().get(V));
        assertEquals(2, subSB.getPosition().getSpan(H));
        assertEquals(0, subSB.getPosition().getSpan(V));

        assertEquals(0, subPB.getSubBlocks().get(0).getPosition().get(H));
        assertEquals(0, subPB.getSubBlocks().get(0).getPosition().get(V));
        assertEquals(2, subPB.getSubBlocks().get(0).getPosition().getSpan(H));
        assertEquals(4, subPB.getSubBlocks().get(0).getPosition().getSpan(V));

        assertEquals(2, subPB.getSubBlocks().get(1).getPosition().get(H));
        assertEquals(0, subPB.getSubBlocks().get(1).getPosition().get(V));
        assertEquals(2, subPB.getSubBlocks().get(1).getPosition().getSpan(H));
        assertEquals(4, subPB.getSubBlocks().get(1).getPosition().getSpan(V));

        sb.getCoord().set(X, 10, 100);
        sb.getCoord().set(Y, 20, 200);
        CalculateCoordBlockVisitor ccbv = CalculateCoordBlockVisitor.create(layoutParameters, new LayoutContext(0., 0., 0., null, false, false, false));
        sb.getPosition().setOrientation(Orientation.LEFT);
        sb.accept(ccbv);

        assertEquals(10, sb.getCoord().get(X), 0);
        assertEquals(20, sb.getCoord().get(Y), 0);
        assertEquals(100, sb.getCoord().getSpan(X), 0);
        assertEquals(200, sb.getCoord().getSpan(Y), 0);

        assertEquals(35, subSB.getCoord().get(X), 0);
        assertEquals(20, subSB.getCoord().get(Y), 0);
        assertEquals(50, subSB.getCoord().getSpan(X), 0);
        assertEquals(200, subSB.getCoord().getSpan(Y), 0);

        assertEquals(-15, subPB.getSubBlocks().get(0).getCoord().get(X), 0);
        assertEquals(20, subPB.getSubBlocks().get(0).getCoord().get(Y), 0);
        assertEquals(50, subPB.getSubBlocks().get(0).getCoord().getSpan(X), 0);
        assertEquals(200, subPB.getSubBlocks().get(0).getCoord().getSpan(Y), 0);

        assertEquals(35, subPB.getSubBlocks().get(1).getCoord().get(X), 0);
        assertEquals(20, subPB.getSubBlocks().get(1).getCoord().get(Y), 0);
        assertEquals(50, subPB.getSubBlocks().get(1).getCoord().getSpan(X), 0);
        assertEquals(200, subPB.getSubBlocks().get(1).getCoord().getSpan(Y), 0);

        sb.reverseBlock();

        assertEquals("INTERNAL_vl_da-ba", sb.getEndingNode().getId());
        assertEquals("INTERNAL_vl_da-ba", subSB.getEndingNode().getId());
        assertEquals("INTERNAL_vl_2", subPB.getSubBlocks().get(1).getEndingNode().getId());
    }
}
