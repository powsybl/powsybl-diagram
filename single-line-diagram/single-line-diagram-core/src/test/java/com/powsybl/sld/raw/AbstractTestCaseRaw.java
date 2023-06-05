/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.iidm.network.Network;
import com.powsybl.sld.AbstractTestCase;
import com.powsybl.sld.ConfigBuilder;
import com.powsybl.sld.builders.RawGraphBuilder;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.graphs.Graph;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.svg.*;
import com.powsybl.sld.svg.styles.BasicStyleProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.powsybl.sld.library.ComponentTypeName.ARROW_ACTIVE;
import static com.powsybl.sld.library.ComponentTypeName.ARROW_REACTIVE;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public abstract class AbstractTestCaseRaw extends AbstractTestCase {
    protected RawGraphBuilder rawGraphBuilder = new RawGraphBuilder();

    @Override
    public String toSVG(Graph graph, String filename) {
        LabelProvider labelProvider = labelRawProviderFactory.create(Network.create("empty", ""), componentLibrary, layoutParameters, svgParameters);
        DefaultSVGWriter defaultSVGWriter = new DefaultSVGWriter(componentLibrary, layoutParameters, svgParameters);
        return toSVG(graph, filename, defaultSVGWriter, labelProvider, new BasicStyleProvider(), svgParameters.getPrefixId());
    }

    private final ConfigBuilder.LabelProviderFactory labelRawProviderFactory = new DefaultLabelProviderFactory() {

        @Override
        public LabelProvider create(Network network, ComponentLibrary componentLibrary, LayoutParameters layoutParameters, SvgParameters svgParameters) {
            return new DefaultLabelProvider(Network.create("empty", ""), componentLibrary, layoutParameters, svgParameters) {

                @Override
                public List<FeederInfo> getFeederInfos(FeederNode node) {
                    return Arrays.asList(
                            new DirectionalFeederInfo(ARROW_ACTIVE, LabelDirection.OUT, "", "tata", null),
                            new DirectionalFeederInfo(ARROW_REACTIVE, LabelDirection.IN, "", "tutu", null));
                }

                @Override
                public List<NodeDecorator> getNodeDecorators(Node node, Direction direction) {
                    return new ArrayList<>();
                }
            };
        }
    };

    public ConfigBuilder.LabelProviderFactory getLabelRawProviderFactory() {
        return labelRawProviderFactory;
    }
}
