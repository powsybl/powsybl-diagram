/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.AbstractTestCase;
import com.powsybl.sld.builders.RawGraphBuilder;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.graphs.Graph;
import com.powsybl.sld.model.nodes.FeederNode;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.svg.*;

import java.util.*;

import static com.powsybl.sld.library.ComponentTypeName.ARROW_ACTIVE;
import static com.powsybl.sld.library.ComponentTypeName.ARROW_REACTIVE;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public abstract class AbstractTestCaseRaw extends AbstractTestCase {
    protected RawGraphBuilder rawGraphBuilder = new RawGraphBuilder();

    protected RawDiagramLabelProvider getRawLabelProvider() {
        return new RawDiagramLabelProvider(componentLibrary, layoutParameters);
    }

    @Override
    public String toSVG(Graph graph, String filename) {
        return toSVG(graph, filename, getRawLabelProvider(), new BasicStyleProvider());
    }

    private static class RawDiagramLabelProvider extends AbstractDiagramLabelProvider {
        public RawDiagramLabelProvider(ComponentLibrary componentLibrary, LayoutParameters layoutParameters) {
            super(componentLibrary, layoutParameters);
        }

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
    }
}
