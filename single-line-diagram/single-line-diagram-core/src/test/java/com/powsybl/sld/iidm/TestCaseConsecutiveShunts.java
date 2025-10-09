/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.Network;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.svg.DefaultLabelProvider;
import com.powsybl.sld.svg.LabelPosition;
import com.powsybl.sld.svg.LabelProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class TestCaseConsecutiveShunts extends AbstractTestCaseIidm {

    LabelProvider labelProvider;

    @BeforeEach
    public void setUp() {
        svgParameters.setShowInternalNodes(true);
        network = Network.read("consecutive_shunts.xiidm", getClass().getResourceAsStream("/consecutive_shunts.xiidm"));
        vl = network.getVoltageLevel("AU");
        graphBuilder = new NetworkGraphBuilder(network);

        labelProvider = new DefaultLabelProvider(network, componentLibrary, layoutParameters, svgParameters) {

            @Override
            public List<NodeLabel> getNodeLabels(Node node, Direction direction) {
                return node.isFictitious()
                        ? node.getId().matches("INTERNAL_AU_\\d*") ? Collections.singletonList(new NodeLabel(node.getId().replace("INTERNAL_AU_", ""), new LabelPosition("NW", 4, -1, true, 0))) : Collections.emptyList()
                        : Collections.singletonList(new NodeLabel(node.getId(), new LabelPosition("NW", 4, -1, true, 0)));
            }
        };

    }

    @Test
    void test() throws IOException {

        // build voltage level 1 graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId());

        voltageLevelGraphLayout(g);

        // write SVG and compare to reference
        assertEquals(toString("/consecutive_shunts.svg"), toSVG(g, "/consecutive_shunts.svg", componentLibrary, layoutParameters, svgParameters, labelProvider, getDefaultDiagramStyleProvider()));
    }

}
