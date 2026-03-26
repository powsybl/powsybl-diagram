/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.raw;

import com.powsybl.iidm.network.Network;
import com.powsybl.sld.AbstractTestCase;
import com.powsybl.sld.builders.RawGraphBuilder;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.graphs.Graph;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.svg.DefaultLabelProvider;
import com.powsybl.sld.svg.ValueFeederInfo;
import com.powsybl.sld.svg.FeederInfo;
import com.powsybl.sld.svg.LabelProvider;
import com.powsybl.sld.svg.*;
import com.powsybl.sld.svg.styles.BasicStyleProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.powsybl.sld.library.SldComponentTypeName.ARROW_ACTIVE;
import static com.powsybl.sld.library.SldComponentTypeName.ARROW_REACTIVE;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 */
public abstract class AbstractTestCaseRaw extends AbstractTestCase {
    protected RawGraphBuilder rawGraphBuilder = new RawGraphBuilder();

    @Override
    public String toSVG(Graph graph, String filename) {
        return toSVG(graph, filename, componentLibrary, layoutParameters, svgParameters, labelRawProvider, new BasicStyleProvider(), legendRawProvider);
    }

    @Override
    public String toMetadata(Graph graph, String filename) {
        return toMetadata(graph, filename, componentLibrary, layoutParameters, svgParameters, labelRawProvider, new BasicStyleProvider(), legendRawProvider);
    }

    private final SVGLegendWriter legendRawProvider = new DefaultSVGLegendWriter(Network.create("empty", ""), svgParameters);

    private final LabelProvider labelRawProvider = new DefaultLabelProvider(Network.create("empty", ""), componentLibrary, layoutParameters, svgParameters) {

        @Override
        public List<FeederInfo> getFeederInfos(FeederNode node) {
            return Arrays.asList(
                    new ValueFeederInfo(ARROW_ACTIVE, LabelDirection.OUT, "", "tata", null),
                    new ValueFeederInfo(ARROW_REACTIVE, LabelDirection.IN, "", "tutu", null));
        }

        @Override
        public List<NodeDecorator> getNodeDecorators(Node node, Direction direction) {
            return new ArrayList<>();
        }
    };

    public LabelProvider getLabelRawProvider() {
        return labelRawProvider;
    }

    public SVGLegendWriter getLegendRawProvider() {
        return legendRawProvider;
    }
}
