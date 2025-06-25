/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.serde.NetworkSerDe;
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.build.iidm.VoltageLevelFilter;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.model.BranchEdge;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.ThreeWtEdge;
import com.powsybl.nad.svg.EdgeInfo.Direction;
import com.powsybl.nad.svg.SvgParameters.EdgeInfoEnum;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.NominalVoltageStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class EdgeInfoMultiLabelTest extends AbstractTest {

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
            public List<EdgeInfo> getEdgeInfo(Graph graph, BranchEdge edge, BranchEdge.Side side) {
                return getEdgeInfos(EdgeInfo.Direction.OUT);
            }

            @Override
            public List<EdgeInfo> getEdgeInfo(Graph graph, ThreeWtEdge edge) {
                return getEdgeInfos(EdgeInfo.Direction.IN);
            }

            @Override
            public String getArrowPathDIn() { // larger arrow
                return "M-2 -1 H2 L0 1z";
            }

            @Override
            public String getArrowPathDOut() { // thinner arrow
                return "M-0.5 1 H0.5 L0 -1z";
            }

            private List<EdgeInfo> getEdgeInfos(Direction direction) {
                List<EdgeInfo> edgeInfos = new ArrayList<>();
                for (EdgeInfoEnum infoType : getSvgParameters().getEdgeInfoDisplayed()) {
                    String label = switch (infoType) {
                        case CURRENT -> EdgeInfo.CURRENT;
                        case ACTIVE_POWER -> EdgeInfo.ACTIVE_POWER;
                        case REACTIVE_POWER -> EdgeInfo.REACTIVE_POWER;
                    };
                    externalLabel = switch (infoType) {
                        case CURRENT -> "100 A";
                        case ACTIVE_POWER -> "200 MW";
                        case REACTIVE_POWER -> "-300 MVAR";
                    };
                    edgeInfos.add(new EdgeInfo(label, direction, internalLabel, externalLabel));
                }
                return edgeInfos;
            }
        };
    }

    @Test
    void testMultiplePerpendicularLabels() {
        Network network = Networks.createTwoVoltageLevels();
        internalLabel = null;
        externalLabel = "ext";
        EdgeInfoEnum[] infos = {EdgeInfoEnum.CURRENT, EdgeInfoEnum.ACTIVE_POWER, EdgeInfoEnum.REACTIVE_POWER};
        getSvgParameters().setEdgeInfoAlongEdge(false)
                .setEdgeInfoDisplayed(infos)
                .setArrowShift(20)
                .setArrowLabelShift(35);
        assertSvgEquals("/edge_info_perpendicular_label.svg", network);
    }

    @Test
    void testMultiplePerpendicularLabels2() {
        Network network = NetworkSerDe.read(getClass().getResourceAsStream("/IEEE_24_bus.xiidm"));
        internalLabel = null;
        externalLabel = "ext";
        EdgeInfoEnum[] infos = {EdgeInfoEnum.CURRENT, EdgeInfoEnum.ACTIVE_POWER, EdgeInfoEnum.REACTIVE_POWER};
        getSvgParameters().setEdgeInfoAlongEdge(false)
                .setEdgeInfoDisplayed(infos)
                .setArrowShift(30)
                .setArrowLabelShift(35);
        assertSvgEquals("/edge_info_perpendicular_label.svg", network);
    }

}
