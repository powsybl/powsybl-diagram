/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.layout;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.NetworkAreaDiagram;
import com.powsybl.nad.build.iidm.VoltageLevelFilter;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.Point;
import com.powsybl.nad.svg.LabelProvider;
import com.powsybl.nad.svg.StyleProvider;
import com.powsybl.nad.svg.SvgParameters;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.NominalVoltageStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreno <zamarrenolm at aia.es>
 */
class LayoutWithInitialPositionsTest extends AbstractTest {

    @BeforeEach
    public void setup() {
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

    @Override
    protected LabelProvider getLabelProvider(Network network) {
        return new DefaultLabelProvider(network, getSvgParameters());
    }

    @Test
    void testDiamond() {
        checkLayoutWithInitialPositions(LayoutNetworkFactory.createDiamond());
    }

    private void checkLayoutWithInitialPositions(Network network) {
        Predicate<VoltageLevel> filter = vl -> vl.getNominalV() >= 100;

        // Perform an initial layout with only a few voltage levels of the network
        NetworkAreaDiagram initialDiagram = new NetworkAreaDiagram(network, filter);
        Map<String, Point> initialPositions = layoutResult(initialDiagram);

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
        NetworkAreaDiagram completeNetworkDiagram = new NetworkAreaDiagram(network, VoltageLevelFilter.NO_FILTER);
        Map<String, Point> allPositions = layoutResult(completeNetworkDiagram, initialPositions);

        // Check positions of initial layout have been preserved in global layout
        for (Map.Entry<String, Point> l : initialPositions.entrySet()) {
            String equipmentId = l.getKey();
            Point expected = l.getValue();
            Point actual = allPositions.get(equipmentId);
            assertNotNull(actual);
            assertEquals(expected.getX(), actual.getX());
            assertEquals(expected.getY(), actual.getY());
        }
    }

    private void checkOnlySomeInitialPositionsFixed(Network network, Map<String, Point> initialPositions) {
        // Perform a global layout with all the voltage levels in the network,
        // giving initial positions for some equipment,
        // and fixing the position for only some equipment
        NetworkAreaDiagram completeNetworkDiagram = new NetworkAreaDiagram(network, VoltageLevelFilter.NO_FILTER);
        // Only consider fixed the first one in the initial layout
        Set<String> fixedNodes = Set.of(initialPositions.keySet().iterator().next());
        Map<String, Point> allPositions = layoutResult(completeNetworkDiagram, initialPositions, fixedNodes);

        // Check positions of initial layout have been preserved in global layout
        for (Map.Entry<String, Point> l : initialPositions.entrySet()) {
            String equipmentId = l.getKey();
            Point expected = l.getValue();
            Point actual = allPositions.get(equipmentId);
            assertNotNull(actual);
            if (fixedNodes.contains(equipmentId)) {
                assertEquals(expected.getX(), actual.getX());
                assertEquals(expected.getY(), actual.getY());
            } else {
                // We expect that the nodes with initial position but that have not been fixed have been moved
                assertTrue(expected.getX() != actual.getX() || expected.getY() != actual.getY());
            }
        }
    }

    private Map<String, Point> layoutResult(NetworkAreaDiagram nad) {
        return layoutResult(nad, Collections.emptyMap(), Collections.emptySet(), Collections.emptyMap());
    }

    private Map<String, Point> layoutResult(NetworkAreaDiagram nad, Map<String, Point> initialNodePositions, Set<String> nodesWithFixedPositions) {
        return layoutResult(nad, initialNodePositions, nodesWithFixedPositions, Collections.emptyMap());
    }

    private Map<String, Point> layoutResult(NetworkAreaDiagram nad, Map<String, Point> fixedNodePositions) {
        return layoutResult(nad, Collections.emptyMap(), Collections.emptySet(), fixedNodePositions);
    }

    private Map<String, Point> layoutResult(NetworkAreaDiagram nad,
                                            Map<String, Point> initialNodePositions,
                                            Set<String> nodesWithFixedPositions,
                                            Map<String, Point> fixedNodePositions
    ) {
        LayoutFactory delegateLayoutFactory = new BasicForceLayoutFactory();
        PositionsLayoutFactory positionsLayoutFactory = new PositionsLayoutFactory(
                delegateLayoutFactory,
                initialNodePositions,
                nodesWithFixedPositions,
                fixedNodePositions);
        StringWriter writer = new StringWriter();
        nad.draw(writer,
                getSvgParameters(),
                getLayoutParameters(),
                getStyleProvider(nad.getNetwork()),
                getLabelProvider(nad.getNetwork()),
                positionsLayoutFactory);
        return positionsLayoutFactory.getLayoutResult().positions;
    }

    static class PositionsLayoutFactory implements LayoutFactory {
        static class LayoutResult {
            Map<String, Point> positions;
        }

        private final LayoutFactory delegateLayoutFactory;
        private final LayoutResult layoutResult = new LayoutResult();
        private final Map<String, Point> initialNodePositions;
        private final Set<String> nodesWithFixedPositions;
        private final Map<String, Point> fixedNodePositions;

        PositionsLayoutFactory(LayoutFactory delegateLayoutFactory,
                               Map<String, Point> initialNodePositions,
                               Set<String> nodesWithFixedPositions,
                               Map<String, Point> fixedNodePositions
                               ) {
            this.delegateLayoutFactory = delegateLayoutFactory;
            this.initialNodePositions = initialNodePositions;
            this.nodesWithFixedPositions = nodesWithFixedPositions;
            this.fixedNodePositions = fixedNodePositions;
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
                    if (!initialNodePositions.isEmpty()) {
                        delegateLayout.setInitialNodePositions(initialNodePositions);
                    }
                    if (!nodesWithFixedPositions.isEmpty()) {
                        delegateLayout.setNodesWithFixedPosition(nodesWithFixedPositions);
                    }
                    // only if not empty,
                    // setting nodes with fixed node positions will invalidate previous nodes with fixed positions
                    if (!fixedNodePositions.isEmpty() && delegateLayout instanceof AbstractLayout) {
                        ((AbstractLayout) delegateLayout).setFixedNodePositions(fixedNodePositions);
                    }
                    delegateLayout.run(graph, layoutParameters);
                    layoutResult.positions = graph.getNodePositions();
                }

                @Override
                public void setInitialNodePositions(Map<String, Point> initialNodePositions) {
                    throw new PowsyblException("not implemented");
                }

                @Override
                public void setNodesWithFixedPosition(Set<String> nodesWithFixedPosition) {
                    throw new PowsyblException("not implemented");
                }

                @Override
                public Map<String, Point> getInitialNodePositions() {
                    throw new PowsyblException("not implemented");
                }

                @Override
                public Set<String> getNodesWithFixedPosition() {
                    throw new PowsyblException("not implemented");
                }
            };
        }
    }
}
