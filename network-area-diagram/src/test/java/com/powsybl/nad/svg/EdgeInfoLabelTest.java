/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.diagram.test.Networks;
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

import java.util.Optional;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class EdgeInfoLabelTest extends AbstractTest {

    private String internalLabel;
    private String externalLabel;

    @BeforeEach
    void setup() {
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
            public Optional<EdgeInfo> getEdgeInfo(Graph graph, BranchEdge edge, BranchEdge.Side side) {
                return Optional.of(new EdgeInfo("test", EdgeInfo.Direction.OUT, internalLabel, externalLabel));
            }

            @Override
            public Optional<EdgeInfo> getEdgeInfo(Graph graph, ThreeWtEdge edge) {
                return Optional.of(new EdgeInfo("test", EdgeInfo.Direction.IN, internalLabel, externalLabel));
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
        Network network = Networks.createTwoVoltageLevels();
        getSvgParameters().setArrowShift(10);
        assertSvgEquals("/edge_info_missing_label.svg", network);
    }

    @Test
    void testPerpendicularLabels() {
        Network network = Networks.createTwoVoltageLevels();
        internalLabel = "int";
        externalLabel = "ext";
        getSvgParameters().setEdgeInfoAlongEdge(false)
                .setArrowShift(50)
                .setArrowLabelShift(25);
        assertSvgEquals("/edge_info_perpendicular_label.svg", network);
    }

    @Test
    void testParallelLabels() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        internalLabel = "243";
        externalLabel = "145";
        getSvgParameters().setArrowShift(60)
                .setArrowLabelShift(20);
        assertSvgEquals("/edge_info_double_labels.svg", network);
    }
}
