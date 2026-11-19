/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg;

import com.powsybl.diagram.test.Networks;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.builders.SubstationRawBuilder;
import com.powsybl.sld.builders.VoltageLevelRawBuilder;
import com.powsybl.sld.builders.ZoneRawBuilder;
import com.powsybl.sld.iidm.AbstractTestCaseIidm;
import com.powsybl.sld.layout.SmartVoltageLevelLayoutFactory;
import com.powsybl.sld.model.cells.InternCell;
import com.powsybl.sld.model.coordinate.Orientation;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.graphs.VoltageLevelInfos;
import com.powsybl.sld.model.nodes.BusNode;
import com.powsybl.sld.model.nodes.Edge;
import com.powsybl.sld.model.nodes.EquipmentNode;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.FeederType;
import com.powsybl.sld.model.nodes.GroundDisconnectionNode;
import com.powsybl.sld.model.nodes.Internal2WTNode;
import com.powsybl.sld.model.nodes.Middle2WTNode;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.model.nodes.TeePointNode;
import com.powsybl.sld.model.nodes.feeders.BaseFeeder;
import com.powsybl.sld.svg.styles.StyleProvider;
import com.powsybl.sld.svg.styles.iidm.TopologicalStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class IdProviderTest extends AbstractTestCaseIidm {

    VoltageLevelGraph g;
    LabelProvider labelProvider;
    StyleProvider styleProvider;
    SVGLegendWriter svgLegendWriter;

    @Override
    @BeforeEach
    public void setUp() throws IOException {
        network = Networks.createNetworkWithTieLineInVoltageLevel();
        graphBuilder = new NetworkGraphBuilder(network);
        // build graph
        g = graphBuilder.buildVoltageLevelGraph("VL1");

        // Run layout
        new SmartVoltageLevelLayoutFactory(network).create(g).run(layoutParameters);

        // Providers
        labelProvider = new DefaultLabelProvider(network, componentLibrary, layoutParameters, svgParameters);
        styleProvider = new TopologicalStyleProvider(network);
        svgLegendWriter = new DefaultSVGLegendWriter(network, svgParameters);
    }

    @Test
    void testLegacyIdProvider() {
        // write SVG and compare to reference
        assertEquals(toString("/TestLegacyIdProvider.svg"), toSVG(g, "/TestLegacyIdProvider.svg", componentLibrary,
            layoutParameters, svgParameters, labelProvider, styleProvider, svgLegendWriter));

        assertEquals(toString("/TestLegacyIdProvider.json"), toMetadata(g, "/TestLegacyIdProvider.json",
            componentLibrary, layoutParameters, svgParameters, labelProvider, styleProvider, svgLegendWriter));
    }

    @Test
    void testDefaultIdProvider() {
        IdProvider idProvider = new IntIdProvider(svgParameters.getPrefixId());
        graphBuilder = new NetworkGraphBuilder(network, layoutParameters, idProvider);
        g = graphBuilder.buildVoltageLevelGraph("VL1");
        new SmartVoltageLevelLayoutFactory(network).create(g).run(layoutParameters);

        // write SVG and compare to reference
        assertEquals(toString("/TestDefaultIdProvider.svg"), toSVG(g, "/TestDefaultIdProvider.svg", componentLibrary,
            layoutParameters, svgParameters, labelProvider, styleProvider, svgLegendWriter));

        assertEquals(toString("/TestDefaultIdProvider.json"), toMetadata(g, "/TestDefaultIdProvider.json",
            componentLibrary, layoutParameters, svgParameters, labelProvider, styleProvider, svgLegendWriter));
    }

    @Test
    void testIntIdProviderCreatesDistinctIdsRegardlessOfKeys() {
        IntIdProvider intIdProvider = new IntIdProvider(svgParameters.getPrefixId());

        String equipmentId = intIdProvider.createSvgId("ALDJF_subType_AER");
        String equipmentSubtype = intIdProvider.createSvgId("ALDJF", "AER");

        assertNotEquals(equipmentId, equipmentSubtype);
        assertNotEquals(equipmentId, intIdProvider.createSvgId("ALDJF_subType_AER"));
        assertNotEquals(equipmentSubtype, intIdProvider.createSvgId("ALDJF", "AER"));
    }

    @Test
    void testLegacyIdProviderKeepsTypedKeysStable() {
        LegacyIdProvider legacyIdProvider = new LegacyIdProvider(svgParameters.getPrefixId());

        String equipmentId = legacyIdProvider.createSvgId("ALDJF_subType_AER");
        String equipmentSubtype = legacyIdProvider.createSvgId("ALDJF", "AER");

        assertEquals(equipmentId, legacyIdProvider.createSvgId("ALDJF_subType_AER"));
        assertEquals(equipmentSubtype, legacyIdProvider.createSvgId("ALDJF", "AER"));
    }

    @Test
    void testLegacyConstructorsCreateExpectedNodeAndEdgeIds() {
        // Given a node graph created with the legacy constructors
        LegacyIdProvider legacyIdProvider = new LegacyIdProvider("");
        BusNode bus1 = new BusNode("BUS1", "BUS1", false);
        BusNode bus2 = new BusNode("BUS2", "BUS2", false);

        // Then all identifiers keep the legacy format
        assertEquals(legacyIdProvider.createSvgId("BUS1"), bus1.getSvgId());
        assertEquals(legacyIdProvider.createSvgId("BUS1", "BUSBAR_SECTION"), bus1.getBusInfoSvgId());

        Edge edge = new Edge(bus1, bus2);
        assertEquals(legacyIdProvider.createSvgId("BUS1", "BUS2", "EDGE"), edge.getSvgId());
        Edge edgeInContainer = new Edge(bus1, bus2, legacyIdProvider, "VL");
        assertEquals(legacyIdProvider.createSvgId("VL", "BUS1", "BUS2"), edgeInContainer.getSvgId());

        EquipmentNode equipment = new EquipmentNode(Node.NodeType.INTERNAL, "EQUIPMENT", "Equipment",
                "EQUIPMENT", "TEST", false);
        assertEquals(legacyIdProvider.createSvgId("EQUIPMENT"), equipment.getSvgId());
        FeederNode feeder = new FeederNode("FEEDER", "Feeder", "FEEDER", "TEST", false,
                new BaseFeeder(FeederType.INJECTION), Orientation.RIGHT);
        assertEquals(legacyIdProvider.createSvgId("FEEDER"), feeder.getSvgId());
        GroundDisconnectionNode ground = new GroundDisconnectionNode("GROUND", "Ground", false, "GROUND_DISCONNECTION");
        assertEquals(legacyIdProvider.createSvgId("GROUND"), ground.getSvgId());
        Internal2WTNode transformer = new Internal2WTNode("2WT", "2WT", "2WT", "TWO_WINDINGS_TRANSFORMER");
        assertEquals(legacyIdProvider.createSvgId("2WT"), transformer.getSvgId());
        TeePointNode teePoint = new TeePointNode("TEE", "Tee", "TEE");
        assertEquals(legacyIdProvider.createSvgId("TEE"), teePoint.getSvgId());

        VoltageLevelInfos infos = new VoltageLevelInfos("VL", "VL", 400);
        Middle2WTNode middleTransformer = new Middle2WTNode("MIDDLE", "Middle", infos, infos, "TWO_WINDINGS_TRANSFORMER");
        assertEquals(legacyIdProvider.createSvgId("MIDDLE"), middleTransformer.getSvgId());
    }

    @Test
    void testLegacyCellConstructorCreatesExpectedId() {
        // Given a cell created with the legacy constructor
        LegacyIdProvider legacyIdProvider = new LegacyIdProvider("");
        BusNode bus1 = new BusNode("BUS1", "BUS1", false);
        BusNode bus2 = new BusNode("BUS2", "BUS2", false);

        // When the cell identifier is requested
        InternCell cell = new InternCell(0, List.of(bus1, bus2));

        // Then it uses the legacy identifier format
        assertEquals(legacyIdProvider.createSvgId("INTERN 0"), cell.getSvgId());
    }

    @Test
    void testLegacyRawBuildersUseLegacyProvider() {
        // Given raw builders created through their compatibility constructors
        LegacyIdProvider legacyIdProvider = new LegacyIdProvider("");
        VoltageLevelInfos infos = new VoltageLevelInfos("VL", "VL", 400);
        ZoneRawBuilder zoneBuilder = new ZoneRawBuilder(List.of("S1"));
        SubstationRawBuilder substationBuilder = new SubstationRawBuilder("S1", null);
        VoltageLevelRawBuilder voltageLevelBuilder = new VoltageLevelRawBuilder(infos, null, id -> infos);

        // Then each graph uses a legacy provider, preserving the pre-existing API behavior
        assertEquals(legacyIdProvider.getClass(), zoneBuilder.getGraph().getIdProvider().getClass());
        assertEquals(legacyIdProvider.getClass(), substationBuilder.getGraph().getIdProvider().getClass());
        assertEquals(legacyIdProvider.getClass(), voltageLevelBuilder.getGraph().getIdProvider().getClass());
    }
}
