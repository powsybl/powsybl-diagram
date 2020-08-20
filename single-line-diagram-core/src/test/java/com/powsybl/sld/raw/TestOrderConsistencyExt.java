/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.RawGraphBuilder;
import com.powsybl.sld.layout.BlockOrganizer;
import com.powsybl.sld.layout.ImplicitCellDetector;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.PositionVoltageLevelLayout;
import com.powsybl.sld.layout.positionbyclustering.PositionByClustering;
import com.powsybl.sld.layout.positionfromextension.PositionFromExtension;
import com.powsybl.sld.model.*;
import com.powsybl.sld.svg.DefaultDiagramStyleProvider;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestOrderConsistencyExt extends AbstractTestOrderConsistency {

    @Test
    public void test() {
        Graph g1 = rawGraphBuilder.buildVoltageLevelGraph("vl1", false, true);
        new ImplicitCellDetector().detectCells(g1);
        new BlockOrganizer(new PositionFromExtension()).organize(g1);
        new PositionVoltageLevelLayout(g1).run(layoutParameters);
        assertEquals(toString("/orderConsistencyExt1.json"), toJson(g1, "/orderConsistencyExt1.json"));

        Graph g2 = rawGraphBuilder.buildVoltageLevelGraph("vl2", false, true);
        new ImplicitCellDetector().detectCells(g2);
        new BlockOrganizer(new PositionFromExtension()).organize(g2);
        new PositionVoltageLevelLayout(g2).run(layoutParameters);

        assertEquals(toString("/orderConsistencyExt2.json"), toJson(g2, "/orderConsistencyExt2.json"));
    }
}