/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.layout;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.NadParameters;
import com.powsybl.nad.NetworkAreaDiagram;
import com.powsybl.nad.build.iidm.VoltageLevelFilter;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.Point;
import com.powsybl.nad.svg.LabelProvider;
import com.powsybl.nad.svg.StyleProvider;
import com.powsybl.nad.svg.SvgParameters;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.NominalVoltageStyleProvider;
import com.powsybl.nad.svg.iidm.StyleProviderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreno {@literal <zamarrenolm at aia.es>}
 */
class LayoutWithInitialPositionsTest extends AbstractTest {

    @BeforeEach
    void setup() {
        setLayoutParameters(new LayoutParameters());
        setSvgParameters(new SvgParameters()
                .setInsertNameDesc(true)
                .setSvgWidthAndHeightAdded(true)
                .setFixedWidth(800));
    }

    @Override
    protected StyleProvider getStyleProvider(Network network) {
        return new NominalVoltageStyleProvider(network);
    }

    private StyleProviderFactory getStyleProviderFactory() {
        return this::getStyleProvider;
    }

    @Override
    protected LabelProvider getLabelProvider(Network network) {
        return new DefaultLabelProvider(network, getSvgParameters());
    }

    @Test
    void testDiamond() {
        checkLayoutWithInitialPositions(LayoutNetworkFactory.createDiamond());
    }

    @Test
    void testNbVoltageLevels() {
        Network network = LayoutNetworkFactory.createDiamond();

        assertEquals(1, VoltageLevelFilter.createVoltageLevelDepthFilter(network, "A 400", 0).getNbVoltageLevels());
        assertEquals(4, VoltageLevelFilter.createVoltageLevelDepthFilter(network, "A 400", 3).getNbVoltageLevels());
        assertEquals(7, VoltageLevelFilter.createVoltageLevelDepthFilter(network, "A 400", 5).getNbVoltageLevels());
        assertEquals(1, VoltageLevelFilter.createVoltageLevelDepthFilter(network, "A 400", 0).getNbVoltageLevels());
    }

    private void checkLayoutWithInitialPositions(Network network) {
        Predicate<VoltageLevel> filter = vl -> vl.getNominalV() >= 100;

        // Perform an initial layout with only a few voltage levels of the network
        Map<String, Point> initialPositions = layoutResult(network, filter);

        // Check initial points contains an entry for all voltage levels filtered
        network.getVoltageLevelStream().filter(filter).forEach(vl -> assertTrue(initialPositions.containsKey(vl.getId())));
        // Check we have voltage levels in the network that are not filtered and thus will not have an initial positions
        assertTrue(network.getVoltageLevelStream().anyMatch(filter.negate()));
        network.getVoltageLevelStream().filter(filter.negate()).forEach(vl -> assertFalse(initialPositions.containsKey(vl.getId())));

        checkAllInitialPositionsFixed(network, initialPositions);
        checkOnlySomeInitialPositionsFixed(network, initialPositions);
    }

    private void checkAllInitialPositionsFixed(Network network, Map<String, Point> initialPositions) {
        // Perform a global layout with all the voltage levels in the network,
        // giving fixed positions for some equipment
        Map<String, Point> allPositions = layoutResult(network, initialPositions);

        // Check positions of initial layout have been preserved in global layout
        for (Map.Entry<String, Point> l : initialPositions.entrySet()) {
            String equipmentId = l.getKey();
            Point expected = l.getValue();
            Point actual = allPositions.get(equipmentId);
            assertNotNull(actual);
            assertEquals(expected.x(), actual.x(), 0);
            assertEquals(expected.y(), actual.y(), 0);
        }
    }

    private void checkOnlySomeInitialPositionsFixed(Network network, Map<String, Point> initialPositions) {
        // Perform a global layout with all the voltage levels in the network,
        // giving initial positions for some equipment,
        // and fixing the position for only some equipment
        // Only consider fixed the first one in the initial layout
        Map<String, Point> movableNodes = new HashMap<>(initialPositions);
        String firstKey = initialPositions.keySet().iterator().next();
        Map<String, Point> fixedNodes = Map.of(firstKey, movableNodes.remove(firstKey));
        Map<String, Point> allPositions = layoutResult(network, movableNodes, fixedNodes);

        // Check positions of initial layout have been preserved in global layout
        for (Map.Entry<String, Point> l : initialPositions.entrySet()) {
            String equipmentId = l.getKey();
            Point expected = l.getValue();
            Point actual = allPositions.get(equipmentId);
            assertNotNull(actual);
            if (fixedNodes.containsKey(equipmentId)) {
                assertEquals(expected.x(), actual.x(), 0);
                assertEquals(expected.y(), actual.y(), 0);
            } else {
                // We expect that the nodes with initial position but that have not been fixed have been moved
                assertTrue(expected.x() != actual.x() || expected.y() != actual.y());
            }
        }
    }

    private Map<String, Point> layoutResult(Network network, Predicate<VoltageLevel> voltageLevelFilter) {
        return layoutResult(network, Collections.emptyMap(), Collections.emptyMap(), voltageLevelFilter);
    }

    private Map<String, Point> layoutResult(Network network, Map<String, Point> movableNodesInitialPositions, Map<String, Point> fixedNodesPositions) {
        return layoutResult(network, movableNodesInitialPositions, fixedNodesPositions, VoltageLevelFilter.NO_FILTER);
    }

    private Map<String, Point> layoutResult(Network network, Map<String, Point> fixedNodesPositions) {
        return layoutResult(network, Collections.emptyMap(), fixedNodesPositions, VoltageLevelFilter.NO_FILTER);
    }

    private Map<String, Point> layoutResult(Network network,
                                            Map<String, Point> movableNodesInitialPositions,
                                            Map<String, Point> fixedNodesPositions,
                                            Predicate<VoltageLevel> voltageLevelFilter
    ) {
        LayoutFactory delegateLayoutFactory = new BasicForceLayoutFactory();
        PositionsLayoutFactory positionsLayoutFactory = new PositionsLayoutFactory(
                delegateLayoutFactory,
                movableNodesInitialPositions,
                fixedNodesPositions);
        StringWriter writer = new StringWriter();
        StringWriter metadataWriter = new StringWriter();
        NetworkAreaDiagram.draw(network, writer, metadataWriter,
                new NadParameters()
                        .setSvgParameters(getSvgParameters())
                        .setLayoutParameters(getLayoutParameters())
                        .setStyleProviderFactory(this::getStyleProvider)
                        .setLayoutFactory(positionsLayoutFactory),
                voltageLevelFilter);
        return positionsLayoutFactory.getLayoutResult().positions;
    }

    static class PositionsLayoutFactory implements LayoutFactory {
        static class LayoutResult {
            Map<String, Point> positions;
        }

        private final LayoutFactory delegateLayoutFactory;
        private final LayoutResult layoutResult = new LayoutResult();
        private final Map<String, Point> movableNodesInitialPositions;
        private final Map<String, Point> fixedNodesPositions;

        PositionsLayoutFactory(LayoutFactory delegateLayoutFactory,
                               Map<String, Point> movableNodesInitialPositions,
                               Map<String, Point> fixedNodePositions
        ) {
            this.delegateLayoutFactory = delegateLayoutFactory;
            this.movableNodesInitialPositions = movableNodesInitialPositions;
            this.fixedNodesPositions = fixedNodePositions;
        }

        public LayoutResult getLayoutResult() {
            return layoutResult;
        }

        @Override
        public Layout create() {
            final Layout delegateLayout = delegateLayoutFactory.create();
            return new Layout() {
                @Override
                public void run(Graph graph, LayoutParameters layoutParameters) {
                    if (!movableNodesInitialPositions.isEmpty()) {
                        delegateLayout.setMovableNodesInitialPositions(movableNodesInitialPositions);
                    }
                    if (!fixedNodesPositions.isEmpty()) {
                        delegateLayout.setFixedNodesPositions(fixedNodesPositions);
                    }
                    delegateLayout.run(graph, layoutParameters);
                    layoutResult.positions = graph.getNodePositions();
                }

                @Override
                public void setMovableNodesInitialPositions(Map<String, Point> movableNodesInitialPositions) {
                    throw new PowsyblException("not implemented");
                }

                @Override
                public void setFixedNodesPositions(Map<String, Point> fixedNodesPositions) {
                    throw new PowsyblException("not implemented");
                }

                @Override
                public Map<String, Point> getMovableNodesInitialPositions() {
                    throw new PowsyblException("not implemented");
                }

                @Override
                public Map<String, Point> getFixedNodesPositions() {
                    throw new PowsyblException("not implemented");
                }

                @Override
                public void setTextNodeFixedPosition(String voltageLevelId, Point topLeft, Point edgeConnection) {
                    throw new PowsyblException("not implemented");
                }
            };
        }
    }
}

