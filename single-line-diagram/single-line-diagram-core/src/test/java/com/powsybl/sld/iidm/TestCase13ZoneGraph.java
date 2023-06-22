/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.*;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.layout.*;
import com.powsybl.sld.model.graphs.ZoneGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
class TestCase13ZoneGraph extends AbstractTestCaseIidm {

    private static final String SUBSTATION_ID_1 = "Substation1";
    private static final String SUBSTATION_ID_2 = "Substation2";

    @BeforeEach
    public void setUp() {
        layoutParameters.setCssLocation(LayoutParameters.CssLocation.INSERTED_IN_SVG);
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
    void testHorizontal() {
        // build zone graph
        network = Networks.createNetworkWithManySubstations();
        List<String> zone = Arrays.asList("A", "B", "C", "D", "E");
        ZoneGraph g = new NetworkGraphBuilder(network).buildZoneGraph(zone);

        // Run horizontal zone layout
        new HorizontalZoneLayoutFactory().create(g, new HorizontalSubstationLayoutFactory(), new PositionVoltageLevelLayoutFactory()).run(layoutParameters);

        assertEquals(toString("/TestCase13ZoneGraphH.svg"), toSVG(g, "/TestCase13ZoneGraphH.svg"));
    }

    @Test
    void testVertical() {
        // build zone graph
        network = Networks.createNetworkWithManySubstations();
        List<String> zone = Arrays.asList("A", "B", "C", "D", "E");
        ZoneGraph g = new NetworkGraphBuilder(network).buildZoneGraph(zone);

        // Run horizontal zone layout
        new VerticalZoneLayoutFactory().create(g, new VerticalSubstationLayoutFactory(), new PositionVoltageLevelLayoutFactory()).run(layoutParameters);

        assertEquals(toString("/TestCase13ZoneGraphV.svg"), toSVG(g, "/TestCase13ZoneGraphV.svg"));
    }
}
