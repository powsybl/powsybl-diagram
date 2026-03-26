/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.commons.PowsyblException;
import com.powsybl.diagram.test.Networks;
import com.powsybl.iidm.network.Network;
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.build.iidm.IntIdProvider;
import com.powsybl.nad.build.iidm.NetworkGraphBuilder;
import com.powsybl.nad.build.iidm.VoltageLevelFilter;
import com.powsybl.nad.layout.BasicForceLayout;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.model.BranchEdge;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.ThreeWtEdge;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.NominalVoltageStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringWriter;
import java.util.Optional;

/**
 * @author Giovanni Ferrari {@literal <giovanni.ferrari at soft.it>}
 */
class EdgeInfoSvgComponentTest extends AbstractTest {

    private String internalLabel;
    private String externalLabel;
    private String side1Label;
    private String side2Label;

    @BeforeEach
    void setup() {
        setLayoutParameters(new LayoutParameters());
        setSvgParameters(new SvgParameters()
                .setSvgWidthAndHeightAdded(true)
                .setFixedWidth(800)
                .setArrowPathIn("M-20 -10 H20 L0 10z")
                .setArrowPathOut("M-5 10 H5 L0 -10z"));
    }

    @Override
    protected StyleProvider getStyleProvider(Network network) {
        return new NominalVoltageStyleProvider(network);
    }

    @Override
    protected LabelProvider getLabelProvider(Network network) {
        return new DefaultLabelProvider(network, getSvgParameters()) {

            @Override
            public Optional<EdgeInfo> getBranchEdgeInfo(String branchId, BranchEdge.Side side, String branchType) {
                return Optional.of(new EdgeInfo("test", "test", EdgeInfo.Direction.OUT, internalLabel, externalLabel, "FLASH"));
            }

            @Override
            public Optional<EdgeInfo> getBranchEdgeInfo(String branchId, String branchType) {
                return Optional.of(new EdgeInfo("test", "test", 1, side1Label, side2Label, "LOCK"));
            }

            @Override
            public Optional<EdgeInfo> getThreeWindingTransformerEdgeInfo(String threeWindingTransformerId, ThreeWtEdge.Side side) {
                return Optional.of(new EdgeInfo("test", "test", EdgeInfo.Direction.IN, internalLabel, externalLabel));
            }
        };
    }

    @Test
    void testSvgComponentEdgeInfo() {
        Network network = Networks.createTwoVoltageLevels();
        internalLabel = "int";
        externalLabel = "ext";
        side1Label = "side1";
        side2Label = "side2";
        getSvgParameters().setEdgeInfoAlongEdge(false)
                .setArrowShift(50)
                .setArrowLabelShift(25);
        assertSvgEquals("/edge_info_components.svg", network);
    }

    @Test
    void testUnknownEdgeInfoCopmponent() {
        Network network = Networks.createTwoVoltageLevels();
        LabelProvider mylabelProvider = new DefaultLabelProvider(network, getSvgParameters()) {

            @Override
            public Optional<EdgeInfo> getBranchEdgeInfo(String branchId, BranchEdge.Side side, String branchType) {
                return Optional.of(new EdgeInfo("test", "test", EdgeInfo.Direction.OUT, internalLabel, externalLabel, "UNKNOWN"));
            }

            @Override
            public Optional<EdgeInfo> getBranchEdgeInfo(String branchId, String branchType) {
                return Optional.of(new EdgeInfo("test", "test", EdgeInfo.Direction.OUT, side1Label, side2Label, "UNKNOWN"));
            }

            @Override
            public Optional<EdgeInfo> getThreeWindingTransformerEdgeInfo(String threeWindingTransformerId, ThreeWtEdge.Side side) {
                return Optional.of(new EdgeInfo("test", "test", EdgeInfo.Direction.IN, internalLabel, externalLabel));
            }
        };
        Graph graph = new NetworkGraphBuilder(network, VoltageLevelFilter.NO_FILTER, mylabelProvider, getLayoutParameters(), new IntIdProvider()).buildGraph();
        BasicForceLayout layout = new BasicForceLayout();
        layout.run(graph, getLayoutParameters());
        StringWriter writer = new StringWriter();
        SvgWriter svgWriter = new SvgWriter(getSvgParameters(), getStyleProvider(network), getComponentLibrary(), getEdgeRouting());
        PowsyblException e = assertThrows(PowsyblException.class, () -> svgWriter.writeSvg(graph, writer));
        assertTrue(e.getMessage().contains("Cannot find size for component of type UNKNOWN"));
    }

}
