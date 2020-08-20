/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.layout.BlockOrganizer;
import com.powsybl.sld.layout.ImplicitCellDetector;
import com.powsybl.sld.layout.PositionVoltageLevelLayout;
import com.powsybl.sld.layout.positionbyclustering.PositionByClustering;
import com.powsybl.sld.model.*;
import com.powsybl.sld.svg.DefaultDiagramStyleProvider;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestOrderConsistencyClust extends AbstractTestOrderConsistency {

    @Test
    public void test() {
        Graph g1 = rawGraphBuilder.buildVoltageLevelGraph("vl1", false, true);
        new ImplicitCellDetector().detectCells(g1);
        new BlockOrganizer(new PositionByClustering()).organize(g1);
        new PositionVoltageLevelLayout(g1).run(layoutParameters);
        assertEquals(toString("/orderConsistencyClust1.json"), toJson(g1, "/orderConsistencyClust1.json"));

        Graph g2 = rawGraphBuilder.buildVoltageLevelGraph("vl2", false, true);
        new ImplicitCellDetector().detectCells(g2);
        new BlockOrganizer(new PositionByClustering()).organize(g2);
        new PositionVoltageLevelLayout(g2).run(layoutParameters);

        assertEquals(toString("/orderConsistencyClust2.json"), toJson(g2, "/orderConsistencyClust2.json"));
    }

}
