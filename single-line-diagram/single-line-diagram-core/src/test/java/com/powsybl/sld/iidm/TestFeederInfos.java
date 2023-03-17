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
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.svg.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.powsybl.sld.library.ComponentTypeName.*;
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
public class TestFeederInfos extends AbstractTestCaseIidm {

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
    }

    @Test
    public void testManyFeederInfos() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        layoutParameters.setSpaceForFeederInfos(100)
                .setFeederInfosIntraMargin(5)
                .setPowerValuePrecision(3);

        // Run layout
        voltageLevelGraphLayout(g);

        // many feeder values provider example for the test :
        DiagramLabelProvider manyFeederInfoProvider = new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters) {

            @Override
            public List<FeederInfo> getFeederInfos(FeederNode node) {
                List<FeederInfo> feederInfos = Arrays.asList(
                        new DirectionalFeederInfo(ARROW_ACTIVE, 1000.967543, valueFormatter::formatPower, null),
                        new DirectionalFeederInfo(ARROW_REACTIVE, Double.NaN, valueFormatter::formatPower, null),
                        new DirectionalFeederInfo(ARROW_REACTIVE, LabelDirection.IN, null, "3000", null),
                        new DirectionalFeederInfo(ARROW_ACTIVE, LabelDirection.OUT, null, "40", null), // Not displayed
                        new DirectionalFeederInfo(ARROW_ACTIVE, LabelDirection.OUT, null, "50", null),
                        new DirectionalFeederInfo(ARROW_CURRENT, 100, valueFormatter::formatCurrent, null));
                boolean feederArrowSymmetry = node.getDirection() == Direction.TOP || layoutParameters.isFeederInfoSymmetry();
                if (!feederArrowSymmetry) {
                    Collections.reverse(feederInfos);
                }
                return feederInfos;
            }

            @Override
            public List<DiagramLabelProvider.NodeDecorator> getNodeDecorators(Node node, Direction direction) {
                return new ArrayList<>();
            }
        };
        // write SVG and compare to reference
        assertEquals(toString("/TestFeederInfos.svg"), toSVG(g, "/TestFeederInfos.svg", manyFeederInfoProvider, new BasicStyleProvider()));
    }

    @Test
    public void testFrenchFormatting() {
        // Add power values to the load
        network.getLoad("l").getTerminal().setP(1200.29);
        network.getLoad("l").getTerminal().setQ(-1);

        layoutParameters.setLanguageTag("fr").setPowerValuePrecision(1);

        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        // Run layout
        voltageLevelGraphLayout(g);

        // write SVG and compare to reference
        assertEquals(toString("/TestFormattingFeederInfos.svg"), toSVG(g, "/TestFormattingFeederInfos.svg"));
    }
}
