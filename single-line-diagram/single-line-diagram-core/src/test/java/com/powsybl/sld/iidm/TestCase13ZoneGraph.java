/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.iidm;

import com.powsybl.commons.*;
import com.powsybl.diagram.test.Networks;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.layout.*;
import com.powsybl.sld.layout.pathfinding.*;
import com.powsybl.sld.model.graphs.ZoneGraph;
import com.powsybl.sld.svg.SvgParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi@techrain.eu>}
 */
class TestCase13ZoneGraph extends AbstractTestCaseIidm {

    private static final String SUBSTATION_ID_1 = "Substation1";
    private static final String SUBSTATION_ID_2 = "Substation2";

    @BeforeEach
    public void setUp() {
        svgParameters.setCssLocation(SvgParameters.CssLocation.INSERTED_IN_SVG);
        network = Networks.createNetworkWithLine();
        // In order to keep same results -> can be removed later
        network.getVoltageLevelStream().forEach(vl -> vl.setNominalV(380));
    }

    @Test
    void test() {
        List<String> zone = Arrays.asList(SUBSTATION_ID_1, SUBSTATION_ID_2);
        ZoneGraph g = new NetworkGraphBuilder(network).buildZoneGraph(zone);
        // write Json and compare to reference
        assertEquals(toString("/TestCase13ZoneGraph.json"), toJson(g, "/TestCase13ZoneGraph.json"));
    }

    @Test
    void test2() {
        List<String> zone = Arrays.asList(SUBSTATION_ID_1, SUBSTATION_ID_2);
        ZoneGraph g = new NetworkGraphBuilder(network).buildZoneGraph(zone);
        // write Json and compare to reference
        assertEquals(toString("/TestCase13ZoneGraphNoCoords.json"), toJson(g, "/TestCase13ZoneGraphNoCoords.json", false));
    }

    @Test
    void testZoneGraphHWithSubstationH() {
        // build zone graph
        network = Networks.createNetworkWithManySubstations();
        List<String> zone = Arrays.asList("A", "B", "C", "D", "E");
        ZoneGraph g = new NetworkGraphBuilder(network).buildZoneGraph(zone);

        layoutParameters.setDiagrammPadding(1.0, 1.0, 1.0, 1.0);

        // Run horizontal zone layout
        new HorizontalZoneLayoutFactory().create(g, DijkstraPathFinder::new, new HorizontalSubstationLayoutFactory(), new PositionVoltageLevelLayoutFactory()).run(layoutParameters);

        assertEquals(toString("/TestCase13ZoneGraphHH.svg"), toSVG(g, "/TestCase13ZoneGraphHH.svg"));
    }

    @Test
    void testZoneGraphVWithSubstationV() {
        // build zone graph
        network = Networks.createNetworkWithManySubstations();
        List<String> zone = Arrays.asList("A", "B", "C", "D", "E");
        ZoneGraph g = new NetworkGraphBuilder(network).buildZoneGraph(zone);

        layoutParameters.setDiagrammPadding(1.0, 1.0, 1.0, 1.0);

        // Run vertical zone layout
        new VerticalZoneLayoutFactory().create(g, DijkstraPathFinder::new, new VerticalSubstationLayoutFactory(), new PositionVoltageLevelLayoutFactory()).run(layoutParameters);

        assertEquals(toString("/TestCase13ZoneGraphVV.svg"), toSVG(g, "/TestCase13ZoneGraphVV.svg"));
    }

    @Test
    void testZoneGraphVWithSubstationH() {
        // build zone graph
        network = Networks.createNetworkWithManySubstations();
        List<String> zone = Arrays.asList("A", "B", "C", "D", "E");
        ZoneGraph g = new NetworkGraphBuilder(network).buildZoneGraph(zone);

        layoutParameters.setDiagrammPadding(1.0, 1.0, 1.0, 1.0);

        // Run vertical zone layout
        new VerticalZoneLayoutFactory().create(g, DijkstraPathFinder::new, new HorizontalSubstationLayoutFactory(), new PositionVoltageLevelLayoutFactory()).run(layoutParameters);

        assertEquals(toString("/TestCase13ZoneGraphVH.svg"), toSVG(g, "/TestCase13ZoneGraphVH.svg"));
    }

    @Test
    void testZoneGraphHWithSubstationV() {
        // build zone graph
        network = Networks.createNetworkWithManySubstations();
        List<String> zone = Arrays.asList("A", "B", "C", "D", "E");
        ZoneGraph g = new NetworkGraphBuilder(network).buildZoneGraph(zone);

        layoutParameters.setDiagrammPadding(1.0, 1.0, 1.0, 1.0);

        // Run vertical zone layout
        new HorizontalZoneLayoutFactory().create(g, DijkstraPathFinder::new, new VerticalSubstationLayoutFactory(), new PositionVoltageLevelLayoutFactory()).run(layoutParameters);

        assertEquals(toString("/TestCase13ZoneGraphHV.svg"), toSVG(g, "/TestCase13ZoneGraphHV.svg"));
    }

    @Test
    void testZoneGraphMatrix2rows3cols() {
        // build zone graph
        network = Networks.createNetworkWithManySubstations();
        List<String> zone = Arrays.asList("A", "B", "C", "D", "E");
        ZoneGraph g = new NetworkGraphBuilder(network).buildZoneGraph(zone);

        layoutParameters.setDiagrammPadding(50.0, 50.0, 50.0, 50.0);
        layoutParameters.setVoltageLevelPadding(20, 60, 20, 60);

        // Run matrix zone layout
        String[][] substationsIds = {{"A", "B", "C"},
                                     {"D", "", "E"}};
        //String[][] substationsIds = {{"B", "C"}};
        new MatrixZoneLayoutFactory(substationsIds).create(g, DijkstraPathFinder::new, new HorizontalSubstationLayoutFactory(), new PositionVoltageLevelLayoutFactory()).run(layoutParameters);

        assertEquals(toString("/TestCase13ZoneGraphMatrix2x3.svg"), toSVG(g, "/TestCase13ZoneGraphMatrix2x3.svg"));
    }

    @Test
    void testZoneGraphMatrix1row5cols() {
        // build zone graph
        network = Networks.createNetworkWithManySubstations();
        List<String> zone = Arrays.asList("A", "B", "C", "D", "E");
        ZoneGraph g = new NetworkGraphBuilder(network).buildZoneGraph(zone);

        layoutParameters.setDiagrammPadding(0.0, 0.0, 0.0, 0.0);
        layoutParameters.setVoltageLevelPadding(20, 60, 20, 60);

        // Run default matrix zone layout
        String[][] matrix = {{"A", "B", "C", "D", "E"}};
        new MatrixZoneLayoutFactory(matrix).create(g, DijkstraPathFinder::new, new VerticalSubstationLayoutFactory(), new PositionVoltageLevelLayoutFactory()).run(layoutParameters);

        assertEquals(toString("/TestCase13ZoneGraphMatrix1x5.svg"), toSVG(g, "/TestCase13ZoneGraphMatrix1x5.svg"));
    }

    @Test
    void testZoneGraphMatrix1rows2cols() {
        // build zone graph
        network = Networks.createNetworkWithManySubstations();
        List<String> zone = Arrays.asList("B", "C");
        ZoneGraph g = new NetworkGraphBuilder(network).buildZoneGraph(zone);

        layoutParameters.setDiagrammPadding(0.0, 0.0, 0.0, 0.0);

        // Run matrix zone layout
        String[][] substationsIds = {{"B", "C"}};
        new MatrixZoneLayoutFactory(substationsIds).create(g, DijkstraPathFinder::new, new HorizontalSubstationLayoutFactory(), new PositionVoltageLevelLayoutFactory()).run(layoutParameters);

        assertEquals(toString("/TestCase13ZoneGraphMatrix1x2.svg"), toSVG(g, "/TestCase13ZoneGraphMatrix1x2.svg"));
    }

    @Test
    void testInvalidZoneGraphMatrix() {
        // build zone graph
        network = Networks.createNetworkWithManySubstations();
        List<String> zone = Arrays.asList("B", "C");
        ZoneGraph g = new NetworkGraphBuilder(network).buildZoneGraph(zone);

        layoutParameters.setDiagrammPadding(0.0, 0.0, 0.0, 0.0);

        // Run matrix zone layout
        String[][] substationsIds = {{"B", "A"}};

        ZoneLayoutPathFinderFactory pFinderFactory = DijkstraPathFinder::new;
        SubstationLayoutFactory sFactory = new HorizontalSubstationLayoutFactory();
        VoltageLevelLayoutFactory vFactory = new PositionVoltageLevelLayoutFactory();
        MatrixZoneLayoutFactory mFactory = new MatrixZoneLayoutFactory(substationsIds);

        PowsyblException e = assertThrows(PowsyblException.class, () -> mFactory.create(g, pFinderFactory, sFactory, vFactory));
        assertEquals("Substation 'A' was not found in zone graph 'B_C'", e.getMessage());
    }
}
