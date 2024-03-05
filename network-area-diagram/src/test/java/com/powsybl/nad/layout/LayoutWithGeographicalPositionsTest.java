/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.layout;

import com.powsybl.ieeecdf.converter.IeeeCdfNetworkFactory;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.impl.extensions.SubstationPositionAdderImplProvider;
import com.powsybl.nad.build.iidm.NetworkGraphBuilder;
import com.powsybl.nad.build.iidm.VoltageLevelFilter;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.Point;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
class LayoutWithGeographicalPositionsTest {

    @Test
    void layoutWithGeographicalPositionsTest() {
        Network network = IeeeCdfNetworkFactory.create9();
        new SubstationPositionAdderImplProvider().newAdder(network.getSubstation("S1")).withCoordinate(new Coordinate(2d, 3d)).add();
        new SubstationPositionAdderImplProvider().newAdder(network.getSubstation("S2")).withCoordinate(new Coordinate(4d, 5d)).add();
        new SubstationPositionAdderImplProvider().newAdder(network.getSubstation("S3")).withCoordinate(new Coordinate(6d, 7d)).add();
        new SubstationPositionAdderImplProvider().newAdder(network.getSubstation("S5")).withCoordinate(new Coordinate(8d, 9d)).add();
        new SubstationPositionAdderImplProvider().newAdder(network.getSubstation("S6")).withCoordinate(new Coordinate(10d, 11d)).add();

        Graph graph = new NetworkGraphBuilder(network, VoltageLevelFilter.NO_FILTER).buildGraph();
        Layout forceLayout = new GeographicalLayoutFactory(network).create();
        forceLayout.run(graph, new LayoutParameters());
        Map<String, Point> actual = graph.getNodePositions();

        assertEquals(300, actual.get("VL1").getX());
        assertEquals(200, actual.get("VL1").getY());
        assertEquals(500, actual.get("VL2").getX());
        assertEquals(400, actual.get("VL2").getY());
    }
}

