/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import static com.powsybl.sld.library.ComponentTypeName.ARROW_ACTIVE;
import static com.powsybl.sld.library.ComponentTypeName.ARROW_REACTIVE;

import com.powsybl.sld.AbstractTestCase;
import com.powsybl.sld.RawGraphBuilder;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.model.*;
import com.powsybl.sld.svg.*;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public abstract class AbstractTestCaseRaw extends AbstractTestCase {
    protected RawGraphBuilder rawGraphBuilder = new RawGraphBuilder();
    private LayoutParameters layoutParameters;

    protected AbstractTestCaseRaw() {
        layoutParameters = createDefaultLayoutParameters();
    }

    @Override
    protected LayoutParameters getLayoutParameters() {
        return layoutParameters;
    }

    @Override
    public void toSVG(Graph graph, String filename) {
        Stream<Node> nodeStream = getNodeStream(graph);
        toSVG(graph, filename, getLayoutParameters(), new RawDiagramLabelProvider(nodeStream), new DefaultDiagramStyleProvider());
    }

    private static Stream<Node> getNodeStream(Graph graph) { //TODO: put in Graph interface
        if (graph instanceof VoltageLevelGraph) {
            return ((VoltageLevelGraph) graph).getNodes().stream();
        } else if (graph instanceof SubstationGraph) {
            return ((SubstationGraph) graph).getNodes().stream().flatMap(g -> g.getNodes().stream());
        } else if (graph instanceof ZoneGraph) {
            return ((ZoneGraph) graph).getNodes().stream().flatMap(g -> g.getNodes().stream()).flatMap(g -> g.getNodes().stream());
        }
        throw new AssertionError();
    }

    private static class RawDiagramLabelProvider implements DiagramLabelProvider {
        private final Map<Node, List<NodeLabel>> nodeLabels;

        public RawDiagramLabelProvider(Stream<Node> nodeStream) {
            this.nodeLabels = new HashMap<>();
            LabelPosition labelPosition = new LabelPosition("default", 0, -5, true, 0);
            nodeStream.forEach(n -> {
                List<DiagramLabelProvider.NodeLabel> labels = new ArrayList<>();
                labels.add(new DiagramLabelProvider.NodeLabel(n.getLabel(), labelPosition, null));
                nodeLabels.put(n, labels);
            });
        }

        @Override
        public List<FeederInfo> getFeederInfos(FeederNode node) {
            return Arrays.asList(
                    new FeederInfo(ARROW_ACTIVE, Direction.OUT, "", "tata", null),
                    new FeederInfo(ARROW_REACTIVE, Direction.IN, "", "tutu", null));
        }

        @Override
        public List<NodeLabel> getNodeLabels(Node node) {
            List<NodeLabel> labels = nodeLabels.get(node);
            return labels != null ? labels : Collections.emptyList();
        }

        @Override
        public List<NodeDecorator> getNodeDecorators(Node node) {
            return new ArrayList<>();
        }
    }
}
