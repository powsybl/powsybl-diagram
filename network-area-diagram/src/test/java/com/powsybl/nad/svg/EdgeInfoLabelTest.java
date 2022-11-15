/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.model.BranchEdge;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.ThreeWtEdge;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.NominalVoltageStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
class EdgeInfoLabelTest extends AbstractTest {

    private String internalLabel;
    private String externalLabel;

    @BeforeEach
    public void setup() {
        setLayoutParameters(new LayoutParameters());
        setSvgParameters(new SvgParameters()
                .setSvgWidthAndHeightAdded(true)
                .setFixedWidth(800));
    }

    @Override
    protected StyleProvider getStyleProvider(Network network) {
        return new NominalVoltageStyleProvider(network);
    }

    @Override
    protected LabelProvider getLabelProvider(Network network) {
        return new DefaultLabelProvider(network, getSvgParameters()) {
            @Override
            public List<EdgeInfo> getEdgeInfos(Graph graph, BranchEdge edge, BranchEdge.Side side) {
                return Collections.singletonList(new EdgeInfo("test", EdgeInfo.Direction.OUT, internalLabel, externalLabel));
            }

            @Override
            public List<EdgeInfo> getEdgeInfos(Graph graph, ThreeWtEdge edge) {
                return Collections.singletonList(new EdgeInfo("test", EdgeInfo.Direction.IN, internalLabel, externalLabel));
            }

            @Override
            public String getArrowPathDIn() { // larger arrow
                return "M-2 -1 H2 L0 1z";
            }

            @Override
            public String getArrowPathDOut() { // thinner arrow
                return "M-0.5 1 H0.5 L0 -1z";
            }
        };
    }

    @Test
    void testMissingLabels() {
        Network network = NetworkTestFactory.createTwoVoltageLevels();
        getSvgParameters().setArrowShift(10);
        assertEquals(toString("/edge_info_missing_label.svg"), generateSvgString(network, "/edge_info_missing_label.svg"));
    }

    @Test
    void testPerpendicularLabels() {
        Network network = NetworkTestFactory.createTwoVoltageLevels();
        internalLabel = "int";
        externalLabel = "ext";
        getSvgParameters().setEdgeInfoAlongEdge(false)
                .setArrowShift(50)
                .setArrowLabelShift(25);
        assertEquals(toString("/edge_info_perpendicular_label.svg"), generateSvgString(network, "/edge_info_perpendicular_label.svg"));
    }

    @Test
    void testParallelLabels() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        internalLabel = "243";
        externalLabel = "145";
        getSvgParameters().setArrowShift(60)
                .setArrowLabelShift(20);
        assertEquals(toString("/edge_info_double_labels.svg"), generateSvgString(network, "/edge_info_double_labels.svg"));
    }
}
