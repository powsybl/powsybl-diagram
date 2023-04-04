/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

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
import static com.powsybl.sld.model.nodes.Node.NodeType.BUS;
import static com.powsybl.sld.model.nodes.Node.NodeType.FEEDER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
class TestSerialBlock extends AbstractTestCaseIidm {

    @BeforeEach
    public void setUp() {
        Network network = Network.create("testCase1", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = createSubstation(network, "s", "s", Country.FR);
        vl = createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380, 10);
        createBusBarSection(vl, "bbs", "bbs", 0, 1, 1);
        createLoad(vl, "la", "la", "la", 10, ConnectablePosition.Direction.TOP, 2, 10, 10);
        createSwitch(vl, "ba", "ba", SwitchKind.BREAKER, false, false, false, 2, 1);
        createSwitch(vl, "da", "da", SwitchKind.DISCONNECTOR, false, false, false, 1, 0);
    }

    @Test
    void test() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        // layout
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
        assertTrue(sb.getSubBlocks().get(2).isEmbeddingNodeType(FEEDER));

        assertEquals("bbs", sb.getSubBlocks().get(0).getStartingNode().getId());
        assertEquals("INTERNAL_vl_da-ba", sb.getSubBlocks().get(0).getEndingNode().getId());
        assertEquals("INTERNAL_vl_da-ba", sb.getSubBlocks().get(1).getStartingNode().getId());
        assertEquals("INTERNAL_vl_la", sb.getSubBlocks().get(1).getEndingNode().getId());
        assertEquals("INTERNAL_vl_la", sb.getSubBlocks().get(2).getStartingNode().getId());
        assertEquals("la", sb.getSubBlocks().get(2).getEndingNode().getId());

        sb.sizing();
        assertEquals(0, sb.getPosition().get(H));
        assertEquals(0, sb.getPosition().get(V));
        assertEquals(2, sb.getPosition().getSpan(H));
        assertEquals(4, sb.getPosition().getSpan(V));

        assertEquals(0, sb.getLowerBlock().getPosition().get(H));
        assertEquals(0, sb.getLowerBlock().getPosition().get(V));
        assertEquals(2, sb.getLowerBlock().getPosition().getSpan(H));
        assertEquals(0, sb.getLowerBlock().getPosition().getSpan(V));

        assertEquals(0, sb.getSubBlocks().get(1).getPosition().get(H));
        assertEquals(0, sb.getSubBlocks().get(1).getPosition().get(V));
        assertEquals(2, sb.getSubBlocks().get(1).getPosition().getSpan(H));
        assertEquals(4, sb.getSubBlocks().get(1).getPosition().getSpan(V));

        assertEquals(0, sb.getUpperBlock().getPosition().get(H));
        assertEquals(4, sb.getUpperBlock().getPosition().get(V));
        assertEquals(2, sb.getUpperBlock().getPosition().getSpan(H));
        assertEquals(0, sb.getUpperBlock().getPosition().getSpan(V));

        sb.getCoord().set(X, 10);
        sb.getCoord().set(Y, 20);
        sb.getCoord().setSpan(X, 100);
        sb.getCoord().setSpan(Y, 200);
        CalculateCoordBlockVisitor ccbv = CalculateCoordBlockVisitor.create(layoutParameters, new LayoutContext(0., 0., 0., null));
        sb.getPosition().setOrientation(Orientation.LEFT);
        sb.accept(ccbv);

        assertEquals(10, sb.getLowerBlock().getCoord().get(X), 0);
        assertEquals(20, sb.getLowerBlock().getCoord().get(Y), 0);
        assertEquals(100, sb.getLowerBlock().getCoord().getSpan(X), 0);
        assertEquals(200, sb.getLowerBlock().getCoord().getSpan(Y), 0);

        assertEquals(10, sb.getSubBlocks().get(1).getCoord().get(X), 0);
        assertEquals(20, sb.getSubBlocks().get(1).getCoord().get(Y), 0);
        assertEquals(100, sb.getSubBlocks().get(1).getCoord().getSpan(X), 0);
        assertEquals(200, sb.getSubBlocks().get(1).getCoord().getSpan(Y), 0);

        assertEquals(10, sb.getUpperBlock().getCoord().get(X), 0);
        assertEquals(20, sb.getUpperBlock().getCoord().get(Y), 0);
        assertEquals(100, sb.getUpperBlock().getCoord().getSpan(X), 0);
        assertEquals(200, sb.getUpperBlock().getCoord().getSpan(Y), 0);

        sb.reverseBlock();

        // LegPrimaryBlock is NOT reversed (bus is always the starting node)
        assertEquals("INTERNAL_vl_da-ba", sb.getSubBlocks().get(1).getEndingNode().getId());
        assertEquals("bbs", sb.getSubBlocks().get(2).getStartingNode().getId());

        // BodyPrimaryBlock is reversed
        assertEquals("INTERNAL_vl_da-ba", sb.getSubBlocks().get(1).getEndingNode().getId());
        assertEquals("INTERNAL_vl_la", sb.getSubBlocks().get(1).getStartingNode().getId());

        // FeederPrimaryBlock is NOT reversed (feeder is always the ending node)
        assertEquals("la", sb.getSubBlocks().get(0).getEndingNode().getId());
        assertEquals("INTERNAL_vl_la", sb.getSubBlocks().get(0).getStartingNode().getId());
    }
}
