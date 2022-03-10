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
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import com.powsybl.sld.model.BusCell;
import com.powsybl.sld.model.FeederNode;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.model.VoltageLevelGraph;
import com.powsybl.sld.svg.BasicStyleProvider;
import com.powsybl.sld.svg.DefaultDiagramLabelProvider;
import com.powsybl.sld.svg.DiagramLabelProvider;
import com.powsybl.sld.svg.FeederInfo;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.powsybl.sld.library.ComponentTypeName.ARROW_ACTIVE;
import static com.powsybl.sld.library.ComponentTypeName.ARROW_REACTIVE;
import static org.junit.Assert.assertEquals;

/**
 * <PRE>
 * l
 * |
 * b
 * |
 * d
 * |
 * ------ bbs
 * </PRE>
 *
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class TestCase14UpToNFeederInfos extends AbstractTestCaseIidm {

    private DiagramLabelProvider manyFeederInfoProvider;

    @Before
    public void setUp() {
        network = Network.create("testCase14", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = createSubstation(network, "s", "s", Country.FR);
        vl = createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380, 10);
        createBusBarSection(vl, "bbs", "bbs", 0, 1, 1);
        createLoad(vl, "l", "l", "l", 0, ConnectablePosition.Direction.TOP, 2, 10, 10);
        createSwitch(vl, "d", "d", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        createSwitch(vl, "b", "b", SwitchKind.BREAKER, false, false, false, 1, 2);

        // many feeder values provider example for the test :
        //
        manyFeederInfoProvider = new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters) {

            @Override
            public List<FeederInfo> getFeederInfos(FeederNode node) {
                List<FeederInfo> feederInfos = Arrays.asList(
                        new FeederInfo(ARROW_ACTIVE, Direction.OUT, null, "10", null),
                        new FeederInfo(ARROW_REACTIVE, Direction.IN, null, "20", null),
                        new FeederInfo(ARROW_REACTIVE, Direction.IN, null, "30", null),
                        new FeederInfo(ARROW_ACTIVE, null, null, "40", null), // Not displayed
                        new FeederInfo(ARROW_ACTIVE, Direction.OUT, null, "50", null));
                boolean feederArrowSymmetry = node.getDirection() == BusCell.Direction.TOP || layoutParameters.isFeederInfoSymmetry();
                if (!feederArrowSymmetry) {
                    Collections.reverse(feederInfos);
                }
                return feederInfos;
            }

            @Override
            public List<DiagramLabelProvider.NodeDecorator> getNodeDecorators(Node node) {
                return new ArrayList<>();
            }
        };
    }

    @Test
    public void test() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId(), true);

        layoutParameters.setSpaceForFeederInfos(100)
                .setFeederInfosIntraMargin(5);

        // Run layout
        voltageLevelGraphLayout(g);

        // write SVG and compare to reference
        assertEquals(toString("/TestCase14UpToNFeederInfos.svg"), toSVG(g, "/TestCase14UpToNFeederInfos.svg", manyFeederInfoProvider, new BasicStyleProvider()));
    }
}
