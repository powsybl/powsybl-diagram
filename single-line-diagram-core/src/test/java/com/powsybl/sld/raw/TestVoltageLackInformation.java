/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.iidm.network.Network;
import com.powsybl.sld.layout.BlockOrganizer;
import com.powsybl.sld.layout.ImplicitCellDetector;
import com.powsybl.sld.layout.PositionVoltageLevelLayout;
import com.powsybl.sld.layout.positionfromextension.PositionFromExtension;
import com.powsybl.sld.model.*;
import com.powsybl.sld.svg.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Objects;
import java.util.Optional;

import static com.powsybl.sld.model.BusCell.Direction.TOP;
import static org.junit.Assert.assertEquals;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class TestVoltageLackInformation extends AbstractTestCaseRaw {

    @Before
    public void setUp() {
        int i = 0;
        com.powsybl.sld.RawGraphBuilder.VoltageLevelBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 380);
        BusNode bbs1 = vlBuilder.createBusBarSection("bbs1", 1, 1);
        BusNode bbs21 = vlBuilder.createBusBarSection("bbs21", 2, 1);
        BusNode bbs22 = vlBuilder.createBusBarSection("bbs22", 2, 2);
        FeederNode load = vlBuilder.createLoad("load", i++, TOP);
        SwitchNode d1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d1", false, false);
        SwitchNode d2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d2", false, false);
        SwitchNode b = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b", false, false);
        FictitiousNode f = vlBuilder.createFictitiousNode("f");

        vlBuilder.connectNode(bbs1, d1);
        vlBuilder.connectNode(f, d1);
        vlBuilder.connectNode(bbs21, d2);
        vlBuilder.connectNode(f, d2);
        vlBuilder.connectNode(f, b);
        vlBuilder.connectNode(b, load);

        FeederNode loadC = vlBuilder.createLoad("loadC", i++, TOP);
        SwitchNode bC = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bC", false, false);
        vlBuilder.connectNode(loadC, bC);
        FictitiousNode fC = vlBuilder.createFictitiousNode("fC");
        vlBuilder.connectNode(fC, bC);
        SwitchNode dC1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dC1", false, false);
        vlBuilder.connectNode(dC1, fC);
        vlBuilder.connectNode(dC1, bbs1);
        SwitchNode dC2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dC2", false, false);
        vlBuilder.connectNode(dC2, fC);
        vlBuilder.connectNode(dC2, bbs22);

        BusNode bbs13 = vlBuilder.createBusBarSection("bbs13", 1, 3);
        BusNode bbs23 = vlBuilder.createBusBarSection("bbs23", 2, 3);

        FictitiousNode commonFG = vlBuilder.createFictitiousNode("commonFG");

        SwitchNode dF1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "dF1", false, false);
        vlBuilder.connectNode(dF1, commonFG);
        vlBuilder.connectNode(dF1, bbs13);
        SwitchNode dF2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "dF2", false, false);
        vlBuilder.connectNode(dF2, commonFG);
        vlBuilder.connectNode(dF2, bbs23);

        FeederNode loadG = vlBuilder.createLoad("loadG", i++, TOP);
        vlBuilder.connectNode(loadG, commonFG);

        SwitchNode bSwitchCG = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bSwitchCG", false, false);
        vlBuilder.connectNode(fC, bSwitchCG);
        vlBuilder.connectNode(loadG, bSwitchCG);
    }

    @Test
    public void test() {
        layoutParameters.setAdaptCellHeightToContent(true);

        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl", true);

        new ImplicitCellDetector().detectCells(g);
        new BlockOrganizer(new PositionFromExtension(), true, true, true, true).organize(g);
        new PositionVoltageLevelLayout(g).run(layoutParameters);

        // Make DiagramLabelProvider return instance of BusInfo
        DiagramLabelProvider withBusInfoProvider = new DefaultDiagramLabelProvider(Network.create("empty", ""), componentLibrary, layoutParameters) {
            @Override
            public Optional<BusInfo> getBusInfo(BusNode node) {
                Objects.requireNonNull(node);
                return Optional.of(new BusInfo("userDefinedId"));
            }
        };

        assertEquals(toString("/TestVoltageLackInformation.svg"),
                toSVG(g, "/TestVoltageLackInformation.svg", withBusInfoProvider, new BasicStyleProvider()));
    }
}
